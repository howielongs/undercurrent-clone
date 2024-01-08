package com.undercurrent.start

import com.undercurrent.legacy.commands.executables.abstractcmds.SystemCommandStarter
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.repository_service.MessagesToBot
import com.undercurrent.legacy.routing.RunConfig.INBOUND_JOB_LOOP_DELAY
import com.undercurrent.shared.repository.database.ProductionDatabase
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.*
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.repository.entities.messages.InboundMessage
import com.undercurrent.system.repository.services.UserCreator
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class LemurBot(
    private val dbusProps: DbusProps,
    private val disableBtcWalletUsage: Boolean = false,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val delaySec: Int = when (dbusProps.role) {
        ShopRole.VENDOR -> 5
        ShopRole.CUSTOMER -> 7
        else -> 0
    }

    private val systemStartEpoch: EpochNano = EpochNano()

    /**
     * Use an enum of the different Apps that can be run
     * Direct to a different starting node depending on which input arg
     */
    private suspend fun handleNewCommand(thisUser: User, msg: InboundMessage) {
        try {
            SystemCommandStarter(
                context = SessionContext(
                    user = thisUser,
                    routingProps = dbusProps,
                )
            ).startNewCommand(tx { msg.body })
        } catch (e: Exception) {
            Log.fatal("handleMessageBody failure", e)
        }
    }

    fun run() = runBlocking {
        supervisorScope {
            withContext(Dispatchers.IO) {

                delay(delaySec * 1000L)
                ProductionDatabase(dbusProps.environment, shouldRunMigrations = dbusProps.role == ShopRole.ADMIN).db

                // Launch startup tasks concurrently
                StartupTasks(
                    dbusProps = dbusProps,
                    disableBtcWalletUsage = disableBtcWalletUsage,
                    delaySec = delaySec
                ).launchStartupTasks()

                // Message handling
                handleMessageJobs()
            }
        }
    }

    private var usersMap: MutableMap<SignalSms, User> = mutableMapOf()
    private var jobsMutex: MutableMap<SignalSms, Job> = mutableMapOf()
    private var latestReadEpoch: MutableMap<SignalSms, EpochNano> = mutableMapOf()

    // move from using SMS to uuid (or GroupID)
    private suspend fun handleMessageJobs() = supervisorScope {
        val ioScope = newScope("Dispatch notifications")

        while (true) {
            //perhaps use coroutineScope block here to prevent further execution/double-dispatch
            ioScope.launch {
                val now = EpochNano()
                //todo SMELLY

                //todo perhaps this should be concurrent with the message fetch, or in series?
                NotificationDispatcher(
                    dbusProps = dbusProps,
                    numbersToExclude = jobsMutex.keys.toSet()
                ).dispatch(now)
            }

            // further make this distinct by dbusPath too
            var msgsBySender: List<InboundMessage> = MessagesToBot.latestMessageForEachSender(
                smsReceiverIn = dbusProps.toBotSms(),
                systemStartEpoch = systemStartEpoch,
                latestReadEpoch = latestReadEpoch
            )

            //todo fetch all msg fields eagerly at the same time here

            msgsBySender.forEach { msg ->
                var senderNum = SignalSms(tx {
                    msg.senderSms
                })

                latestReadEpoch[senderNum] = EpochNano()

                // handle the user thing upon entry into command flow (can use other unique identifiers here)
                //also potentially just load this at startup?
                addSmsToUserMap(senderNum, dbusProps)

                if (!jobsMutex.containsKey(senderNum) && usersMap.containsKey(senderNum)) {
                    usersMap[senderNum]?.let { thisUser ->
                        async(start = CoroutineStart.LAZY) {
                            try {
                                var nowDate = Util.getCurrentUtcDateTime()
                                ctx {
                                    msg.readAtDate = nowDate
                                }

                                handleNewCommand(thisUser = thisUser, msg)
                            } catch (e: Exception) {
                                Log.fatal("handleMessageBody failure", e)
                            }
                            latestReadEpoch[senderNum] = EpochNano()
                            jobsMutex.remove(senderNum)
                        }?.let {
                            jobsMutex[senderNum] = it
                            it.start()
                        }
                    }
                }
            }
            delay(INBOUND_JOB_LOOP_DELAY)
        }
    }



    // clean this up to avoid db operations
    private suspend fun addSmsToUserMap(
        signalSms: SignalSms, dbusProps: DbusProps,
    ) {
        if (usersMap.containsKey(signalSms)) {
            "Found sms $signalSms on UsersMap ${dbusProps.role.name}".let {
                Log.debug(it)
            }
        } else {
            UserCreator.createOrFetchUser(sms = signalSms.value, dbusProps = dbusProps)?.let { user ->
                usersMap[signalSms] = user
                "Added sms $signalSms to UsersMap ${dbusProps.role.name}".let {
                    Log.debug(it)
                }
            } ?: run {
                "Unable to add sms $signalSms to UsersMap ${dbusProps.role.name}".let {
                    Log.error(it)
                }
            }
        }
    }
}

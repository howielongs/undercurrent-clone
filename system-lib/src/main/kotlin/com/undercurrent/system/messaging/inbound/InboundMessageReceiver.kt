package com.undercurrent.system.messaging.inbound


import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.repository.repository_service.AttachmentsManager
import com.undercurrent.legacy.routing.RunConfig.DEFAULT_MSG_EXPIRY_SEC
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.repository.database.ProductionDatabase
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.EpochNanoVal
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.data_classes.SignalEnvelopeProps
import com.undercurrent.system.repository.entities.messages.InboundMessage
import com.undercurrent.system.repository.entities.messages.InboundMessages
import com.undercurrent.system.service.dbus.ReadReceiptSender
import com.undercurrent.system.messaging.outbound.DbusMessageArraySender
import kotlinx.coroutines.*
import org.asamk.signal.manager.api.MessageEnvelope
import org.jetbrains.exposed.sql.or
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Responsibilities of this class:
 * - Save incoming messaging to database (raw and parsed)
 * - Send read receipts
 * - Send echo (optional)
 * - Send typing indicator
 * - Receive attachments
 * - (allow coroutines to run if not in LemurBot mode)
 * - Parse group messaging
 * - Parse incoming MOB transactions (payment receipts)
 * - Log incoming status/context attained
 * - Store state to RunConfig
 */

//@Suppress("UNCHECKED_CAST")
object InboundMessageReceiver {

    @JvmStatic
    fun saveFromSignalCliDbus(
        envelope: MessageEnvelope,
        botSessionNumber: String?,
    ) = runBlocking(Dispatchers.IO + CoroutineName("Daemon Inbound Message Receive")) {
        val logger: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

        //todo make use of logger and coroutines
        //todo also reduce fetches/upserts for users

        val fields = SignalEnvelopeProps.fromBotNumber(botSessionNumber) ?: throw Exception("Unable to parse envelope")
        val dbusProps = fields.dbusProps

        val databaseStarter = ProductionDatabase(
            dbusProps.environment,
            shouldRunMigrations = false
        )

        println("Database path: ${databaseStarter.fullPathToFile}")
        println("#######################################\n\n")

        val db = databaseStarter.db

        val envelopeProcessor = InboundEnvelopeProcessor(envelope)

        val parsedEnvelopeJob = async {
            envelopeProcessor.processEnvelope(dbusProps.toBotSms()) ?: throw Exception("Unable to parse envelope")
        }

        val parsedEnvelope = parsedEnvelopeJob.await()

        // Reusing parsedEnvelope to avoid multiple awaits on parsedEnvelopeJob
        val body = parsedEnvelope.msgBody
        val uuidVal = parsedEnvelope.senderUuid

        val senderSms = parsedEnvelope.humanSenderSms
        val thisTimestamp = parsedEnvelope.timestamp

        var thisUser: User? = null

        if (senderSms == null) {
            if (uuidVal != null) {
                Admins.notifyError("Logged a new user with UUID $uuidVal sending msg:\n\n$body")

                val thisMsgOut =
                    "Welcome to the Shopping Bot! \n\nPlease re-enter that message again to confirm you`re not a bot."

                //todo add to 'UnmatchedUUID' table

                val sender = DbusMessageArraySender.builder(
                    dest = uuidVal,
                    body = thisMsgOut,
                    dbusProps = dbusProps,
                )
                    .build()

                sender.sendMessage()
            } else {
                Admins.notifyError("User msg incoming without UUID or SMS:\n\n$body")
            }
        } else {
            thisUser = createUserIfNotExists(senderSms, dbusProps.role, uuidVal)
        }

        launch {
            envelopeProcessor.processMobData(parsedEnvelope.messageData, parsedEnvelope.botReceivingSms, senderSms)
        }

        val attachments: MutableList<String> = envelopeProcessor.processAttachments()

        if (senderSms != null && body != null) {
            val incomingHumanSms = UtilLegacy.stripOptional(senderSms)

            val parsedMsg = if (attachments.isNotEmpty()) {
                ""
            } else {
                UtilLegacy.stripOptional(body)
            }

            val bodyFinal = parsedMsg.replace("'", "`").trim()
            val timestampEpochNanoVal: EpochNanoVal =
                SystemEpochNanoProvider().forceEpochToNano(thisTimestamp)
                    ?: EpochNanoVal(SystemEpochNanoProvider.getEpochNano())
            val expiryEpochFinal = SystemEpochNanoProvider().epochNano(DEFAULT_MSG_EXPIRY_SEC)

            //todo just rely on unique index in db
            var msgExists = tx {
                InboundMessage.find {
                    InboundMessages.timestampNano eq timestampEpochNanoVal.value or (InboundMessages.timestampNano eq thisTimestamp)
                }.limit(1).firstOrNull()?.let {
                    Log.debug("Message already exists for ${timestampEpochNanoVal}, skipping...")
                    true
                } ?: false
            }

            if (msgExists) {
                throw Exception("Message already exists for ${timestampEpochNanoVal}, skipping...")
            }

            launch {
                println("Sending receipt and echo\n")
                buildAndSendReadReceipt(
                    incomingHumanSms = incomingHumanSms,
                    thisTimestamp = thisTimestamp,
                    dbusProps = dbusProps
                )
                println("Read receipt sent\n")
            }

            tx {
                try {
                    InboundMessage.new {
                        this.body = bodyFinal
                        this.senderSms = incomingHumanSms
                        this.receiverSms = dbusProps.toBotSms().value
                        this.timestamp = timestampEpochNanoVal.value
                        this.dbusPath = dbusProps.toFullPathStr()
                        this.uuid = uuidVal
                        this.expiryEpoch = expiryEpochFinal
                    }
                } catch (e: org.sqlite.SQLiteException) {
                    Log.error("Unable to save message (duplicate timestamp): ${e.message}")
                    null
                }
            }
            println("Saved raw message for: $bodyFinal\n")

            thisUser?.let { user ->
                handleAttachments(
                    attachments = attachments,
                    senderSms = senderSms,
                    body = body,
                    user = user,
                    dbusProps = dbusProps,
                    uploadEpoch = timestampEpochNanoVal.value,
                )
            }
        }
    }

    //todo need to also update User if UUID is present (actually it's backwards: should be starting with UUID)

    @Deprecated("Reuse other createUserIfNotExists methods")
    private fun createUserIfNotExists(senderSms: String, role: AppRole, uuid: String?): User {
        return tx {
            User.find { Users.smsNumber eq senderSms or (Users.uuid eq uuid) }.firstOrNull() ?: User.new {
                println(
                    "Saving new user: " +
                            "\n\tsmsNumber: $senderSms" +
                            "\n\trole: $role" +
                            "\n\tuuid: $uuid"
                )

                this.role = role as ShopRole
                this.smsNumber = senderSms
                this.uuid = uuid
            }
        }
    }

    // could wrap in async call to then be ready to send
    private fun buildAndSendReadReceipt(
        incomingHumanSms: String,
        thisTimestamp: Long,
        dbusProps: DbusProps
    ) {
        val receipt = ReadReceiptSender(
            recipientSms = incomingHumanSms,
            timestamp = thisTimestamp,
            dbusProps = dbusProps,
        )

        receipt.send()
    }

    private suspend fun handleAttachments(
        attachments: List<String>,
        senderSms: String,
        body: String,
        user: User,
        dbusProps: DbusProps,
        uploadEpoch: Long,
    ) {
        if (attachments.isNotEmpty()) {
            println("EXAMINING ATTACHMENTS FOR USER ${tx { user.id.value }}")
            var count = 0
            attachments.forEach {
                Log.debug("Handling incoming attachment from $senderSms")
                AttachmentsManager.parseAndSaveAttachment(
                    attachmentPath = it,
                    messageBody = body,
                    thisUser = user,
                    role = dbusProps.role,
                    dbusProps = dbusProps,
                    uploadEpoch = uploadEpoch
                )
                    .let { parseSuccess ->
                        if (parseSuccess) {
                            count++
                        }
                    }
            }

            if (count == 1) {
                Log.debug("Found and handled $count attachments for $senderSms")
                "Successfully uploaded $count attachment".let {
                    user.interrupt(it, dbusProps)
                }
            } else if (count > 1) {
                Log.debug("Found and handled $count attachments for $senderSms")
                "Successfully uploaded $count attachments".let {
                    user.interrupt(it, dbusProps)
                }
            } else {
                Log.debug("No attachments found for $senderSms")
            }

        }

    }

    fun ByteArray.toBase64Str(): String = String(Base64.getEncoder().encode(this))
    fun ByteArray.toBase64(): ByteArray? = Base64.getEncoder().encode(this)
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

}
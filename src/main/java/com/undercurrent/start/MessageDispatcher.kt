package com.undercurrent.start

import com.undercurrent.shared.messages.OutboundMessageEntity
import com.undercurrent.shared.messages.OutboundMessageTable
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.dbus.TypingIndicatorCancellor
import com.undercurrent.system.repository.entities.messages.NotificationMessage
import com.undercurrent.system.repository.entities.messages.NotificationMessages
import com.undercurrent.system.repository.entities.messages.OutboundMessage
import com.undercurrent.system.repository.entities.messages.OutboundMessages
import com.undercurrent.system.messaging.outbound.DbusMessageArraySender
import com.undercurrent.system.messaging.outbound.dispatchers.FetchAroundEntityQueryFunc
import com.undercurrent.system.messaging.outbound.dispatchers.OutboundMessageFetcher
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class OutboundMessageType(
    val table: OutboundMessageTable,
) {
    NOTIFICATION(
        table = NotificationMessages,
    ),
    OUTBOX(
        table = OutboundMessages,
    ),
}

interface CanSendMessageFromBot<E : OutboundMessageEntity> {
    suspend fun sendMessage(msg: E)
}


internal class NotificationDispatcher(
    dbusProps: DbusProps,
    numbersToExclude: Set<SignalSms> = setOf(),
) : MessageDispatcher<NotificationMessage, NotificationMessages>(
    dbusProps = dbusProps,
    smsNumbersToExclude = numbersToExclude.toList().map { it },
    msgLabel = OutboundMessageType.NOTIFICATION,
    fetchItems = {
        NotificationMessage.find {
            it()
        }.toList()
    }
)

internal class OutboxDispatcher(
    dbusProps: DbusProps,
    smsNumbersToExclude: List<SignalSms> = emptyList(),
) : MessageDispatcher<OutboundMessage, OutboundMessages>(
    dbusProps = dbusProps,
    smsNumbersToExclude = smsNumbersToExclude,
    msgLabel = OutboundMessageType.OUTBOX,
    fetchItems = {
        OutboundMessage.find {
            it()
        }.toList()
    }
)

internal abstract class MessageDispatcher<E : OutboundMessageEntity, T : OutboundMessageTable>(
    private val dbusProps: DbusProps,
    private val smsNumbersToExclude: List<SignalSms> = emptyList(),
    private val msgLabel: OutboundMessageType,
    private val fetchItems: FetchAroundEntityQueryFunc<E>,
) : CanSendMessageFromBot<E> {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val msgFetcherGenerator: () -> OutboundMessageFetcher<E, T> = {
        OutboundMessageFetcher(
            thisBotSenderSms = dbusProps.toBotSms(),
            excludedNumbers = smsNumbersToExclude,
            table = msgLabel.table as T,
            fetchItems = fetchItems
        )
    }

    override suspend fun sendMessage(msg: E) {
        MessageSender().sendMessage(msg)
    }

    //todo consider something other than coroutineScope here (maybe supervisorJob)
    //start new on each forEach iteration?
    suspend fun dispatch(now: EpochNano = EpochNano()) = coroutineScope {
        msgFetcherGenerator().fetchMessages(now).forEach {
            sendMessage(it)
        }
    }

    private inner class MessageSender : CanSendMessageFromBot<E> {
        //todo SMELLY

        //todo SUPER SMELLY
        override suspend fun sendMessage(msg: E) {
            var retries = 0
            val maxRetries = 5
            var isCommitted = false

            var now = SystemEpochNanoProvider.getEpochNano()

            //todo retry mechanism here
            tx {
                var humanReceiverSms = msg.receiverSms
                var body = msg.body
                var msgId = msg.uid
                var intendedSenderSms = msg.senderSms


                if (body != "") {
                    if (intendedSenderSms == "" || intendedSenderSms == dbusProps.toBotSms().value) {
                        msg.expiryEpoch = now

                        if (logger.isInfoEnabled) {
                            "Attempting to expire ${msgLabel.name.uppercase()} Message #$msgId to $now".let {
                                logger.info(it)
                            }
                        }

                        while (!isCommitted && retries < maxRetries) {
                            try {
                                commit()
                                isCommitted = true
                            } catch (e: Exception) {
                                isCommitted = false
                                retries += 1
                                if (retries >= maxRetries) {
                                    //throw exception here
                                    if (logger.isErrorEnabled) {
                                        "RETRY ERRORS EXHAUSTED (${msgLabel.name.uppercase()} Message #$msgId [$now]): ${e.message}\n".let {
                                            logger.error(it)
                                        }
                                    }
                                    throw e
                                } else {
                                    if (logger.isDebugEnabled) {
                                        "Attempt #$retries --> Problem committing ${msgLabel.name.uppercase()} Message #$msgId ($now): ${e.message}\n".let {
                                            logger.debug(it)
                                        }
                                    }
                                }
                            }
                        }


                        if (isCommitted) {
                            if (logger.isInfoEnabled) {
                                "Attempt #$retries --> SUCCESS committing ${msgLabel.name.uppercase()} Message #$msgId ($now)\n".let {
                                    logger.info(it)
                                }
                            }

                            // Send typing indicator
                            //todo this seems a bit impractical here
                            //can do outside tx block?
                            TypingIndicatorCancellor(
                                recipientSms = SignalSms(humanReceiverSms),
                                dbusProps = dbusProps
                            ).send()

                            val sender = DbusMessageArraySender
                                .builder(dest = humanReceiverSms, body = body, dbusProps = dbusProps)
                                .build()
                            var success = sender.sendMessage()

                            var outStr = if (success) {
                                msg.sentAtDate = Util.getCurrentUtcDateTime()
                                msg.timestamp = SystemEpochNanoProvider.getEpochNano()
                                "Sent ${msgLabel.name.uppercase()} Message #$msgId to user"
                            } else {
                                "Failed to send ${msgLabel.name.uppercase()} Message #$msgId to user"
                            }

                            if (logger.isInfoEnabled) {
                                logger.info(outStr)
                            }
                        }
                    }
                }
            }
        }
    }
}


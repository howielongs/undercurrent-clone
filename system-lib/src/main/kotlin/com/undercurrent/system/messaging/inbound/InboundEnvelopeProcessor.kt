package com.undercurrent.system.messaging.inbound

import com.undercurrent.legacy.repository.entities.payments.MobReceivedEvents
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.ReceiverReceipt
import com.undercurrent.legacy.types.protos.MobileCoinAPI
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.messaging.inbound.InboundMessageReceiver.toBase64Str
import org.asamk.signal.manager.api.MessageEnvelope
import org.asamk.signal.manager.api.RecipientAddress
import org.bouncycastle.util.encoders.Hex

class InboundEnvelopeProcessor(private val envelope: MessageEnvelope) {
    lateinit var botReceivingSms: String
    var msgBody: String = ""
    lateinit var messageData: MessageEnvelope.Data
    var timestamp: Long = 0L
    private var groupIdB64: String? = null
    var humanSenderSms: String? = null
    lateinit var senderUuid: String
    var attachments: MutableList<String> = mutableListOf()

    fun processAttachments(): MutableList<String> {
        if (messageData.attachments.isNotEmpty()) {
            try {
                Log.debug("Attachments found: ${messageData.attachments.joinToString(", ")}")

                messageData.attachments.forEach {
                    if (it.file.isPresent) {
                        attachments.add(it.file.toString())
                        Log.debug("Added ${it.file} to list of attachments")
                    } else {
                        Log.debug("No file found for attachment")
                    }
                }

            } catch (e: NoSuchElementException) {
                Log.error("Unable to parse attachments")
            }
        }

        return attachments
    }

    fun processMobData(messageData: MessageEnvelope.Data, botReceivingSms: String, humanSenderSms: String?) {
        if (messageData.payment.isPresent) {
            var payment: MessageEnvelope.Data.Payment?

            payment = messageData.payment.get()

            var paymentReceiptBytes = payment.receipt
            var receiptB64 = payment.receipt.copyOfRange(0, paymentReceiptBytes.size).toBase64Str()

            var receipt = MobileCoinAPI.Receipt.parseFrom(paymentReceiptBytes)

            var pubKey = Hex.toHexString(receipt.publicKey.toByteArray())
            var confirmation = Hex.toHexString(receipt.confirmation.toByteArray())


            val receiverRequestJson = ReceiverReceipt(pubKey, confirmation, receipt).build()

            try {
                botReceivingSms?.let {
                    Log.debug("Creating MobReceiveEvent for incoming payment...")

                    //todo update to handle UUID
                    humanSenderSms?.let { it1 ->
                        MobReceivedEvents.save(
                            senderSmsIn = it1,
                            recipientSmsIn = botReceivingSms,
                            receiptBytesIn = receiptB64,
                            receiptNoteIn = payment.note.toString(),
                            receiverRequestJsonIn = receiverRequestJson.toString(),
                        )?.let {
                            Log.debug("MobReceiveEvent created successfully")
                            println("MobReceiveEvent created successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                Admins.notifyError("Error while trying to save MobReceiveEvent", exception = e)
            }
        }
    }

    fun processEnvelope(
        receivingSmsIn: BotSms?
    ): InboundEnvelopeProcessor? {
        try {
            receivingSmsIn?.let {
                try {
                    botReceivingSms = SmsText(it.value).validate()
                } catch (e: Exception) {
                    Log.error("Error validating receivingSms")
                    return null
                }
            }

            try {
                messageData = envelope.data.get()
                messageData?.let {

                    println("Got message data: $it")

                    timestamp = it.timestamp
                    groupIdB64 = parseGroupData(it)

                    if (it.body.isPresent) {
                        msgBody = it.body.get()
                    }
                }
            } catch (e: Exception) {
                Log.error("Body came back null")
                return null
            }

            val srcAddr: RecipientAddress = envelope.sourceAddress.get()

            senderUuid = try {
                srcAddr.uuid.get().toString()
            } catch (e: Exception) {
                Log.warn("UUID not found for incoming message")
                return null
            }

            humanSenderSms = try {
                val sms = srcAddr.number.get()
                SmsText(sms).validate()
            } catch (e: Exception) {
                Log.warn("Sms number not found for incoming message")
                null
            }

        } catch (e: Exception) {
            Log.error("Error processing incoming message")
            return null
        }
        return this
    }

    private fun parseGroupData(msgData: MessageEnvelope.Data): String? {
        if (msgData.groupContext.isPresent) {
            return msgData.groupContext.get().groupId.toBase64()
        }
        return null
    }

}
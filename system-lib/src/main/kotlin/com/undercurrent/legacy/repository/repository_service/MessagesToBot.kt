package com.undercurrent.legacy.repository.repository_service

import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.messages.InboundMessage
import com.undercurrent.system.repository.entities.messages.InboundMessages
import org.jetbrains.exposed.sql.and


object MessagesToBot {

    fun latestMessageForEachSender(
        smsReceiverIn: BotSms,
        systemStartEpoch: EpochNano,
        latestReadEpoch: Map<SignalSms, EpochNano>,
    ): List<InboundMessage> {
        return tx {
            InboundMessage.find {
                InboundMessages.receiverSms eq smsReceiverIn.value and (
                        InboundMessages.readAtDate eq null) and
                        (InboundMessages.timestampNano greaterEq systemStartEpoch.value) and
                        (unexpiredExpr(InboundMessages, systemStartEpoch.value))
            }.filter {
                val senderSignalSms = SignalSms(it.senderSms)
                if (latestReadEpoch.containsKey(senderSignalSms)) {
                    it.timestamp > (latestReadEpoch[senderSignalSms]?.value ?: systemStartEpoch.value)
                } else {
                    true
                }
            }.sortedBy { it.timestamp }
                .distinctBy { it.senderSms }.toList()
        }
    }

}
package com.undercurrent.system.messaging.outbound

import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.routing.RunConfig.DEFAULT_MSG_EXPIRY_SEC
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.messages.OutboundMessage
import java.math.BigDecimal

class AttachmentMessageCreator {
    //pull out RunConfig into starting args
    // perhaps try to make use of coroutines in here (or at least suspend something)
    fun saveIfAttachment(
        destAddr: String,
        body: String,
        attachment: Attachments.Entity,
        dbusProps: RoutingProps
    ): String? {
        val newMsg = saveAttachmentMsg(
            bodyIn = body,
            receiverSmsIn = destAddr,
            dbusProps = dbusProps,
        )

        if (newMsg == null) {
            "Unable to save message in order to send attachment image".let {
                Admins.notifyError(it)
                Log.error(it)
            }
            return null
        }
        //todo SMELLY

        return "${tx { attachment.path }}"
    }

    // perhaps try to make use of coroutines in here (or at least suspend something)
    private fun saveAttachmentMsg(
        bodyIn: String,
        receiverSmsIn: String,

        //should probably make use of these params
        userIn: User? = null,
        dbusProps: RoutingProps,
    ): OutboundMessage? {
        //todo SMELLY

        //todo costly use of tx
        val smsUser: User = userIn ?: receiverSmsIn?.let { Users.fetchBySms(it) } ?: return null
        val offset = BigDecimal(-1).multiply(BigDecimal(DEFAULT_MSG_EXPIRY_SEC)).toLong()

        return OutboundMessage.save(
            bodyIn = bodyIn,
            botSmsIn = dbusProps.toBotSms(),
            roleIn = dbusProps.role,
            userIn = smsUser,
            expiryEpochIn = EpochNano(offset),
        )
    }

}
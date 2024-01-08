package com.undercurrent.system.repository.entities.messages

import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.entities.BotSms

import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.cleanOutboundMsg
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx

 open class OutboundMessageCompanion : RootEntityCompanion0<OutboundMessage>(OutboundMessages) {

    //todo perhaps encapsulate this (as well as entity) to help abstract functionality
    fun save(
        bodyIn: String,
        userIn: User,
        botSmsIn: BotSms,
        roleIn: AppRole,
        timestampIn: EpochNano = EpochNano(),
        expiryEpochIn: EpochNano? = null
    ): OutboundMessage? {
        return tx {
            try {
                "Saving interrupt message for User #${userIn.uid}".let {
                    Log.trace(it)
                }

                //todo SMELLY
                new {
                    body = bodyIn.cleanOutboundMsg()
                    senderSms = botSmsIn.value
                    receiverSms = userIn.smsNumber
                    role = roleIn.name
                    user = userIn
                    uuid = userIn.uuid
                    timestamp = timestampIn.value
                    expiryEpoch = expiryEpochIn?.value
                }
            } catch (e: Exception) {
                Admins.notifyError("Message could not be sent for ${userIn?.let { UserToIdString.toIdStr(it) }} $roleIn")
                null
            }
        }
    }
}
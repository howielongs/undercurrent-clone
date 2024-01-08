package com.undercurrent.system.service.dbus

import com.undercurrent.prompting.messaging.DbusMethod
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.entities.SignalSms

open class TypingIndicatorSender(
    private val humanRecipientAddr: SignalSms,
    private val shouldCancel: Boolean,
    dbusProps: RoutingProps,
) : DbusMethodSender(DbusMethod.TYPING_INDICATOR, dbusProps) {

    override fun buildArgArray(): Array<String> {
        DbusSendArrayBuilder().apply {
            newRoot(fullDbusPath = dbusProps.toPath().value, dbusMethod = dbusMethod, signalSms = humanRecipientAddr)
            val outArr = add(shouldCancel)
            outArr.let {
                if (logger.isInfoEnabled) {
                    it.joinToString(" ").let { argsStr ->
                        val message = "TYPING INDICATOR args: $argsStr\n"
                        logger.info(message)
                        // Consider removing println if it's not necessary, or ensure it's async or buffered.
//                        println(message)
                    }
                }
            }
            return outArr
        }
    }
}
package com.undercurrent.system.service.dbus

import com.undercurrent.prompting.messaging.DbusMethod
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.system.context.DbusProps

open class ExpirationTimerSetter(
    private val recipientSms: SignalSms,
    private val timeSeconds: Int = 604800,
    dbusProps: DbusProps,
) : DbusMethodSender(DbusMethod.SET_EXPIRATION_TIMER, dbusProps) {

    override fun buildArgArray(): Array<String> {
        DbusSendArrayBuilder().apply {
            newRoot(dbusProps.toPath().value, dbusMethod, recipientSms)
            return add(timeSeconds)
        }
    }
}
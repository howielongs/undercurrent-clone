package com.undercurrent.system.service.dbus

import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.system.context.DbusProps

class ExpirationTimerCancellor(
    recipientSms: SignalSms,
    dbusProps: DbusProps,
) : ExpirationTimerSetter(recipientSms, 0, dbusProps)
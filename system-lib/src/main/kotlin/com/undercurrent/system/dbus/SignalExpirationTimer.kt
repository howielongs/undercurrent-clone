package com.undercurrent.system.dbus

import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.SystemRole
import com.undercurrent.shared.view.components.EXPIRATION_TIMER_SECONDS
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.service.dbus.ExpirationTimerCancellor
import com.undercurrent.system.service.dbus.ExpirationTimerSetter


class SignalExpirationTimer(
    private val sessionContext: SystemContext,
    private val environment: Environment = sessionContext.environment,

    //figure out something better with timer (perhaps just in context?)
//    private val isTimerRunning: () -> Boolean = { RunConfig.expirationTimerOn },
//    private val setTimerIsRunning: (Boolean) -> Unit = { RunConfig.expirationTimerOn = it },
) : ExpirationTimer {

    val dbusProps by lazy {
        DbusProps(sessionContext.role, environment)
    }

    private val role: AppRole by lazy {
        dbusProps.role
    }

    private val thisSms by lazy {
        sessionContext.userSms
    }

//    private val thisDbusFullPath by lazy {
//        //todo pull RunConfig stuff out of here
//        dbusProps.toFullPathStr()
//    }

    private val timerCancellor by lazy {
        ExpirationTimerCancellor(recipientSms = thisSms, dbusProps = dbusProps)
    }

    private val timerSetter by lazy {
        ExpirationTimerSetter(
            recipientSms = thisSms,
            dbusProps = dbusProps,
        )
    }

    override fun startTimer() {
        startTimer(EXPIRATION_TIMER_SECONDS)
    }

    override fun startTimer(timeSeconds: Int) {
        if (sessionContext.isTestMode()) {
            return
        }

//        if (!isTimerRunning()) {
        timerSetter.send()
//            setTimerIsRunning(true)
//        }
    }

    override fun stopTimer() {
        if (sessionContext.isTestMode()) {
            return
        }

        //unsure about the role check and if this func will work like this
//        if (isTimerRunning()) {
        if (role.getSystemRole() != SystemRole.SUPERUSER) {
            timerCancellor.send()
//                setTimerIsRunning(false)
        }
//        }
    }
}
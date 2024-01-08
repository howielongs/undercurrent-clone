package com.undercurrent.system.repository.entities.messages

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.routing.SignalSmsConfigurationLoader
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.cleanOutboundMsg
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.dbus.DbusPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//todo lock this away inside encapsulation in a module
open class NotificationMessageCompanion : RootEntityCompanion0<NotificationMessage>(NotificationMessages) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun addToQueue(
        msgIn: String,
        userIn: User,
        dbusProps: DbusProps,
        roleOverride: AppRole = dbusProps.role,
        dbusPathIn: DbusPath = DbusProps(roleIn = roleOverride, envIn = dbusProps.environment).toPath(),
    ): NotificationMessage? {

        val thisBotSms  = BotSms(SignalSmsConfigurationLoader.findBotSms(
            environment = dbusProps.environment,
            role = roleOverride
        ))

        logSaveMessage(
            userIn = userIn,
            sendingBotSms = thisBotSms,
            dbusPathFull = dbusPathIn,
            roleIn = roleOverride,
            environmentIn = dbusProps.environment,
        )

        return tx {
            new {
                body = msgIn.cleanOutboundMsg()
                user = userIn
                senderSms = thisBotSms.value
                receiverSms = userIn.smsNumber
                dbusPath = dbusPathIn.value
                environment = dbusProps.environment
                uuid = userIn.uuid
                role = roleOverride
                expiryEpoch = SystemEpochNanoProvider.getEpochNano(RunConfig.MSG_EXPIRY_NANO_SEC)
            }
        }
    }

    private fun logSaveMessage(
        userIn: User,
        sendingBotSms: BotSms,
        dbusPathFull: DbusPath,
        roleIn: AppRole,
        environmentIn: Environment,
    ) {
        if (logger.isInfoEnabled) {
            ("Saving notification message for " +
                    "User #${userIn} " +
                    "($environmentIn role: ${roleIn}) to $sendingBotSms -> ${dbusPathFull.value}").let { outString ->
                logger.info(outString)
                println(outString)
            }
        }
    }
}
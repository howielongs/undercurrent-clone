package com.undercurrent.legacy.system_start

import com.undercurrent.legacy.repository.entities.system.IntroEvents
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.dbus.SignalExpirationTimer

//todo pull out of this
object SystemCommands {

    private fun fetchTimer(sessionContext: SystemContext): SignalExpirationTimer {
        return SignalExpirationTimer(sessionContext)
    }

    private fun stopTimer(sessionContext: SystemContext) {
        fetchTimer(sessionContext).stopTimer()
    }


    suspend fun homeCmd(sessionContext: SystemContext) {
        stopTimer(sessionContext)
        sessionContext.interrupt("You have returned to HOME. ${PressAgent.showHelp()}")
    }

    suspend fun cancelCmd(sessionContext: SystemContext) {
        stopTimer(sessionContext)
        sessionContext.interrupt("Operation canceled. ${PressAgent.showHelp()}")
    }

    suspend fun finishCmd(sessionContext: SystemContext) {
        stopTimer(sessionContext)
        sessionContext.interrupt("Operation completed. ${PressAgent.showHelp()}")
    }

    suspend fun welcomeCmd(sessionContext: SystemContext) {
        val user = sessionContext.user
        val userId = tx { user.uid }
        val role = sessionContext.role

        IntroEvents.Table.displayWelcomeMsg(
            userIn = user,
            userIdIn = userId,
            roleIn = role as ShopRole,
            sessionContext = sessionContext
        )
    }

    //    suspend fun htmlCmd(sessionPair: SessionPair) {
//        sessionPair.interrupt("Generating HTML image...")
//        ShopHtmlGenerator.saveToImage()
//    }

    //todo probably should ask user if they want to do this
//    suspend fun offerCrossoverSupport(
//        sessionContext: NodalContext,
//        command: BaseCommand,
//    ) {
//        sessionContext.interrupt("You don`t have permission for this operation (your current role: ${sessionContext.role})")
//        UserRoleFetcher().fetchRoles(sessionContext.user).forEach { role ->
//            if (command.roles.contains(role)) {
//                val channelUrl = PressAgent.Routing.fetchRoutingUrl(role = role)
//                val channelNum = PressAgent.Routing.getRoutingSms(role = role)
//
//                sessionContext.interrupt(
//                    "Please refer to your " +
//                            "$role channel for this command:\n$channelNum\n\n$channelUrl"
//                )
//
////                val receivingNum = PressAgent.Routing.getRoutingSms(role = role)
//
//                //todo inserts into db for other role on user
////                Messages.save(
////                    bodyIn = command.withSlash(),
////                    senderSmsIn = sessionContext.user.smsNumber,
////                    receiverSmsIn = receivingNum
////                )
//
//                return
//            }
//        }
//    }

}
package com.undercurrent.system.command_execution

import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.shared.view.components.CanStopExpirationTimer
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.dbus.SignalExpirationTimer
import com.undercurrent.system.messaging.outbound.Interrupter


abstract class CommandHandlerBase<T : BaseCommand>(
    val context: SystemContext,
    val cmd: T,
    outputProvider: UserOutputProvider<InterrupterMessageEntity> = Interrupter(context.user, context.routingProps),
    private val expirationTimer: ExpirationTimer = SignalExpirationTimer(context),
) : OutputNode(outputProvider), CanStopExpirationTimer {
    abstract suspend fun handleCmd(): TreeNode?

//    private suspend fun offerCrossoverSupport(role: Role): Boolean {
//        if (role !in cmd.roles) {
//            SystemCommands.offerCrossoverSupport(context, cmd)
//            return true
//        }
//        return false
//    }

    override suspend fun next(): TreeNode? {
        stopTimer()

        try {
//            if (offerCrossoverSupport(context.role)) return null

            return handleCmd()

        } catch (e: IllegalArgumentException) {
            Log.trace("Command is not a TopLevelCommand")
        }
        return null
    }

    override fun stopTimer() {
        expirationTimer.stopTimer()
    }

}
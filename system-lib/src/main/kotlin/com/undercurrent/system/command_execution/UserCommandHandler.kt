package com.undercurrent.system.command_execution

import com.undercurrent.legacy.commands.executables.AbstractException
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.UserCommand
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.service.PermissionsValidator
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.view.components.CanStopExpirationTimer
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.dbus.SignalExpirationTimer
import org.jetbrains.exposed.sql.transactions.transaction


class UserCommandHandler(
    context: SystemContext,
    cmd: UserCommand,
    private val expirationTimer: ExpirationTimer = SignalExpirationTimer(context),
) : CommandHandlerBase<UserCommand>(context, cmd), CanStopExpirationTimer {

    //todo needs major cleanup

    override fun stopTimer() {
        expirationTimer.stopTimer()
    }

    override suspend fun handleCmd(): TreeNode? {
        cmd.runnerFunc?.let {
            try {

                //todo is this redundant? Nodes sort of seem to cover this permissions check
                if (PermissionsValidator.hasValidPermissionsForOperation(
                        context,
                        cmd,
                        notifyUser = true
                    )
                ) {
                    it(context)
                }
            } catch (e: AbstractException) {
                transaction { e.action() }
                Log.warn(logException(exceptionLabel = "Abstract ", e))
            } catch (e: Exception) {
                Admins.notifyError(logException(exceptionLabel = "", e))
                return null
            } finally {
                stopTimer()
            }
            return null
        }

        cmd.handlerClass?.let {
            (it.constructors.first().newInstance(context) as Executable).apply {
                try {
                    if (PermissionsValidator.hasValidPermissionsForOperation(
                            context,
                            cmd,
                            notifyUser = true
                        )
                    ) {
                        execute()
                    }
                } catch (e: AbstractException) {
                    transaction { e.action() }
                    Log.warn(logException(exceptionLabel = "Abstract ", e))
                } catch (e: Exception) {
                    "${UtilLegacy.DIVIDER_STRING}\nException caught: \n${e.stackTraceToString()}".let { exceptionText ->
                        Admins.notifyError(exceptionText)
                    }
                    return null
                } finally {
                    stopTimer()
                }
            }
            return null
        }

        cmd.callback?.let {
            it(context)
            return null
        } ?: run {
            when (cmd) {
                else -> {
                    context.interrupt("Operation under construction")
                    return null
                }
            }
        }
    }

    private fun logException(exceptionLabel: String = "", exception: Exception? = null): String {
        return "${UtilLegacy.DIVIDER_STRING}\n${exceptionLabel}exception caught: \n${exception?.stackTraceToString()}"
    }

}


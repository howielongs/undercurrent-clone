package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SystemContext
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.shared.types.validators.YesNoValidator
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.view.components.CanStopExpirationTimer
import com.undercurrent.shared.view.components.ExpirationTimer
import com.undercurrent.system.dbus.SignalExpirationTimer

@Deprecated("Should be short-lived")
class ShouldRunNewCommandDecider(
    val sessionContext: SystemContext,
    private val expirationTimer: ExpirationTimer = SignalExpirationTimer(sessionContext),
) : CanStopExpirationTimer {

    //todo this could probably be cleaned up a fair amount
    suspend fun shouldRunNewCommand(
        userInput: String,
        shouldPromptForInterruptCommands: Boolean = true,
    ): Boolean {
        if (userInput.lowercase() == CmdRegistry.HOME.name.lowercase()) {
            sessionContext.interrupt("You have returned to Home. Previous operation canceled successfully.")
            return true
        }

        if (!shouldPromptForInterruptCommands) {

            return true
        }
        UserInput.promptUser(
            "You entered `$userInput`\n\n" +
                    "This will cancel your current operation " +
                    "and any unsaved changes will be lost." +
                    "\n\nCancel?\n  Y. Yes\n  N. No",
            sessionContext,
            validationType = ResponseType.YESNO,
        )?.let {
            Log.debug("Got: $it, will run if user confirms: `$userInput`")
            if (YesNoValidator.isValidYes(it)) {
                stopTimer()
                return true
            }
            sessionContext.interrupt("Continuing with current operation")
        }
        return false
    }

    override fun stopTimer() {
        expirationTimer.stopTimer()
    }

}
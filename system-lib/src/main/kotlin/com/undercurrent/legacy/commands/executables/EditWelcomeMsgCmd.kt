package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.utils.tx


//todo create generic 'edit field' commands class (sealed, perhaps)
class EditWelcomeMsgCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.EDIT_WELCOME_MSG, sessionContext) {
    override suspend fun execute() {
        //todo handle no storefront found

        val currentMsg = thisStorefront.let {
            tx { it.welcomeMsg }
        }

        sessionContext.interrupt("Current welcome message for your storefront:\n\n${currentMsg}")

        UserInput.promptYesNo(
            confirmText = "Would you like to update your welcome message to customers?${PressAgent.yesNoOptions()}",
            sessionContext = sessionContext,
        ).let {
            if (it) {
                UserInput.promptAndConfirm(
                    promptString = "Enter new welcome message:",
                    sessionContext = sessionContext,
                    confirmTextVerb = "Save",
                )?.let {
                    tx {
                        thisStorefront.welcomeMsg = it
                    }?.let {
                        sessionContext.interrupt("Updated welcome message")
                        return
                    }
                }
            }
        }
        sessionContext.interrupt("Operation complete. Nothing changed.")
    }
}
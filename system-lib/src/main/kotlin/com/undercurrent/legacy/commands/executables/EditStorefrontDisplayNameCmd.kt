package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.utils.tx


//todo create generic 'edit field' commands class (sealed, perhaps)
class EditStorefrontDisplayNameCmd(sessionContext: SessionContext) : Executable(CmdRegistry.EDIT_DISPLAY_NAME, sessionContext) {
    override suspend fun execute() {
        //todo handle no storefront found

        val currentName = thisStorefront.let {
            tx { it.displayName }
        }

        sessionContext.interrupt("Current display name for your storefront:\n\n${currentName}")

        UserInput.promptYesNo(
            confirmText = "Would you like to update your display name to customers?${PressAgent.yesNoOptions()}",
            sessionContext = sessionContext,
        ).let {
            if (it) {
                UserInput.promptAndConfirm(
                    promptString = "Enter new display name:",
                    sessionContext = sessionContext,
                    confirmTextVerb = "Save",
                )?.let { newDisplayName ->
                    UserInput.promptYesNo(
                        "Would you like to update your welcome message to:\n\n${
                            PressAgent.CustomerStrings.welcomeToStorefrontMsg(
                                newDisplayName
                            )
                        }", sessionContext
                    ).let {
                        val newMsg = if (it) {
                            PressAgent.CustomerStrings.welcomeToStorefrontMsg(
                                newDisplayName
                            )
                        } else {
                            null
                        }
                        tx {
                            thisStorefront.displayName = newDisplayName
                            newMsg?.let { msgToSet ->
                                thisStorefront.welcomeMsg = msgToSet
                            }
                        }?.let {
                            sessionContext.interrupt("Updated display name")
                            newMsg?.let { sessionContext.interrupt("Updated welcome message") }
                            return
                        }
                    }
                }
            }
        }

        sessionContext.interrupt("Operation complete. Nothing changed.")
    }
}
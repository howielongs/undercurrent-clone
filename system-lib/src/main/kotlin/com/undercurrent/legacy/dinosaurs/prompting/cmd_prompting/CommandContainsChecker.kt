package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCommand
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableOptionImpl

@Deprecated("This is a temporary class to help with the migration to the new prompting system")
class CommandContainsChecker {
    fun optionsContainsCancel(options: List<SelectableOptionImpl>): Boolean {
        val command = CmdRegistry.CANCEL
        options.forEach {
            if (it is SelectableCommand) {
                if (it.command.commandRef == command) {
                    return true
                }
            }
        }
        return false
    }

}
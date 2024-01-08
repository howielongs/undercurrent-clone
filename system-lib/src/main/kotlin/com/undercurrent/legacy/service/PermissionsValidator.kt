package com.undercurrent.legacy.service

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.system.context.SystemContext

object PermissionsValidator {


    fun insufficientPermissionsString(
        sessionContext: SystemContext,
        cmd: BaseCommand,
        asAdmin: Boolean = false,
    ): String {
        val personStr = if (!asAdmin) {
            "You do"
        } else {
            "-> ${sessionContext.role}\n${UserToIdString.toIdStr(sessionContext.user)} does"
        }
        return "$personStr not have permission for this operation\n\n" +
                " • Role: ${sessionContext.role}\n" +
                " • Operation: ${cmd.lower()}\n" +
                " • Role(s) required: ${cmd.permissions.joinToString(", ")}"
    }

    fun hasValidPermissionsForOperation(
        sessionContext: SystemContext,
        cmd: BaseCommand,
        notifyUser: Boolean = true,
    ): Boolean {
        //unclear if instanceRole might conflict with sessionPair
        if (!cmd.permissions.contains(sessionContext.role)
            || !cmd.permissions.contains(sessionContext.role)
        ) {
            if (notifyUser || (sessionContext.isTestMode())) {
                ExecutableExceptions.PermissionMismatchForCommand(sessionContext, cmd).action()
            }

            return false
        }
        return true
    }
}
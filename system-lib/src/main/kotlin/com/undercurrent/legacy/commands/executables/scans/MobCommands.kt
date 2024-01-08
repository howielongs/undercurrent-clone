package com.undercurrent.legacy.commands.executables.scans

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry.MOB_IMPORT_DEFAULT
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.repository_service.payments.crypto.DefaultMobAccount

abstract sealed class MobCommands(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
) : Executable(thisCommand, sessionContext)

data class EnsureDefaultMobAccountImported(
    override val sessionContext: SessionContext,
) : MobCommands(MOB_IMPORT_DEFAULT, sessionContext) {

    override suspend fun execute() {
        DefaultMobAccount().load()
    }
}




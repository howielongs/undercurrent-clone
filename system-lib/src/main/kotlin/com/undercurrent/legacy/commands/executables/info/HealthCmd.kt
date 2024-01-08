package com.undercurrent.legacy.commands.executables.info

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.entities.system.ScanEvents

class HealthCmd(sessionContext: SessionContext) : Executable(CmdRegistry.HEALTH, sessionContext) {

    override suspend fun execute() {
        //todo definitely wrap this in async for fetches
        sessionContext.interrupt(ScanEvents.Table.fetchLatestOfEach())

    }
}
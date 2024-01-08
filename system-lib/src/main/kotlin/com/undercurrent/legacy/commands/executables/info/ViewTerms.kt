package com.undercurrent.legacy.commands.executables.info

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.types.string.PressAgent

class ViewTerms(sessionContext: SessionContext) : Executable(CmdRegistry.TERMS, sessionContext) {

    override suspend fun execute() {
        sessionContext.interrupt(PressAgent.termsOfService())
    }
}
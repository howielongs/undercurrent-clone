package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.repository.entities.ZipCodeLookup


class ImportZipcodesCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.IMPORT_ZIPS, sessionContext) {
    override suspend fun execute() {
        ZipCodeLookup.fetch("90265")?.let {
            //may need to specify db to return context to
            sessionContext.interrupt("Zipcodes already imported, skipping...")
            return
        }
        ZipCodeLookup.importFromCsvFile()
        //may need to specify db to return context to

        sessionContext.interrupt("Zipcodes imported")
    }

}
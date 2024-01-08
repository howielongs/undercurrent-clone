package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacy.service.csvutils.csv_handlers.JoinCodesCsvHandler
import com.undercurrent.system.context.SessionContext

/**
 * View join codes for a storefront
 * 1. List existing join codes
 * 2. If large number, output CSV file
 *
 */
class ViewCodesCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.MYCODES, sessionContext) {

    override suspend fun execute() {
        JoinCode.listCodesForStorefront(thisStorefront)?.let { codes ->
            if (codes.isNotEmpty()) {
                JoinCode.codesForStorefrontString(thisStorefront, codes)?.let {
                    sessionContext.interrupt("Your current join codes:\n${it}")
                }
                sessionContext.interrupt("Generating CSV spreadsheet file of your join codes")
                JoinCodesCsvHandler(joinCodes = codes, sessionContext = sessionContext).send(sessionContext.user, sessionContext.routingProps)
            } else {
                sessionContext.interrupt("No join codes to write to CSV")
            }
        }
    }
}
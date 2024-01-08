package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacyshops.service.VendorFetcher
import com.undercurrent.legacy.dinosaurs.prompting.selectables.OptionSelector
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.types.enums.ListIndexTypeOld.*

// may need to further hone the sorting if using UID list

class ListVendors(override val sessionContext: SessionContext) : Executable(
    CmdRegistry.LISTVENDORS, sessionContext
) {

    override suspend fun execute() {

        val result = with(VendorFetcher.fetchAllVendors().map { SelectableEntity(it) }) {
            if (this.isEmpty()) {
                null
            } else {
                OptionSelector(
                    this,
                    headerText = "Active vendors:",
                    indexType = INTEGER,
                    isSelectable = false,
                    footerText = "",
                    headlineText = null,
                ).let {
                    it.promptString
                }
            }
        }

        result?.let {
            sessionContext.interrupt(it)
        } ?: run {
            sessionContext.interrupt("No vendors found")
        }
    }
}
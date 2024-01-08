package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.StorefrontPrefs
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.StorefrontPrefType
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx


class SetStorefrontFeesCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.EDITFEES, sessionContext) {

    /**
     * Select vendor/storefront(s) from list
     * Set percent amount (on Storefront prefs)
     * Confirm
     *
     * How does this handle previous orders? Probably excludes those.
     */
    override suspend fun execute() {
        val storefronts = tx { Storefront.find { unexpiredExpr(Storefronts) }.toList() }

        UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = storefronts.map { SelectableEntity(it, it.toFeePercentStr()) },
            headerText = "Select storefront to change transaction fee percent:",
            headlineText = thisCommand.lower(),
        )?.let { selectedEntity ->
            if (selectedEntity is SelectedEntity) {
                if (selectedEntity.entity is Storefront) {
                    val selectedStorefront = selectedEntity.entity

                    val currentFeePct = tx { selectedStorefront.feePercentFull() }

                    UserInput.promptAndConfirm(
                        sessionContext = sessionContext,
                        promptString = "Current fee percent for selection: ${currentFeePct}%\n\nEnter new value:",
                        validationType = ResponseType.FEE_PERCENT,
                    )?.let { newFeePercent ->
                        StorefrontPrefs.replaceValue(selectedStorefront, StorefrontPrefType.FEE_PERCENT, newFeePercent)
                        sessionContext.interrupt("Fee percent updated from ${currentFeePct}% to ${newFeePercent}%")
                        selectedStorefront.notify("Admin updated transaction fees from your storefront from ${currentFeePct}% to ${newFeePercent}%")
                        return
                    }
                }
            }
        }
        sessionContext.interrupt("Nothing changed. Operation complete.")
    }
}
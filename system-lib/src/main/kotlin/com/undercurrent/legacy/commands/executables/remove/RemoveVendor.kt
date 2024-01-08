package com.undercurrent.legacy.commands.executables.remove


import com.undercurrent.shared.utils.Log
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.TextBox.removalVerifyString
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedCommand
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.legacy.service.PermissionsValidator.hasValidPermissionsForOperation
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts.VerifyInputsPrompt
import com.undercurrent.legacy.dinosaurs.prompting.selectables.OptionSelector
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx

class RemoveVendor(sessionContext: SessionContext) : Executable(
    CmdRegistry.RMVENDOR, sessionContext
) {

    private fun fetchUnexpiredShopVendors(): List<ShopVendor> {
        return tx {
            ShopVendor.find { unexpiredExpr(ShopVendors) }.toList()
        }
    }

    //todo this all needs major cleanup
    private var singularItem: String = "Vendor"

    /**
     * Select Vendor from list to remove
     * Simply expire vendor
     *
     * Need to see about reinstating vendor...
     */
    override suspend fun execute() {

        //with generic: pass in callback function for actions to take
        //also: pass in special RemoveVendor sealed class with all the prompting and strings
        removeCmd(
            sessionContext = sessionContext,
            items = fetchUnexpiredShopVendors()
        )

    }


    // consider just passing function for listing -> could then have list by default
    // also consider followOn commands list (vararg)
    // probably include general role check
    @Deprecated("Clean this up massively")
    private suspend fun removeCmd(
        sessionContext: SessionContext,
        items: List<ExposedEntityWithStatus2>,
        unchangedMsg: String = "Nothing has changed with $singularItem. Operation complete.",
    ) {
        if (!preRemove(sessionContext, thisCommand)) {
            return
        }
        UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = items.map { SelectableEntity(it) },
            headerText = "Select $singularItem to remove:",
            headlineText = thisCommand.lower(),
        )?.let {
            when (it) {
                is SelectedCommand -> {
                    it.command.commandRef.callback?.invoke(sessionContext)
                    //todo unsure if should always return right after the invoke?
                    //todo CANCEL should do something about shutting down transactions and coroutines in progress
                    return
                }

                is SelectedEntity -> {
                    with(it.entity) {
                        Log.debug(
                            "User selected " +
                                    "$singularItem #${this.uid} to remove"
                        )
                        VerifyInputsPrompt(
                            sessionContext = sessionContext,
                            prompt = removalVerifyString(it),
                            noText = unchangedMsg,
                            yesFunc = {
                                (it.entity as ShopVendor).let { vendorToRemove ->
                                    if (vendorToRemove.expire()) {
                                        sessionContext.interrupt("Vendor removed")
                                    }

                                    //todo should also do something about existing storefronts
                                }
                                true
                            }
                        ).promptUser()?.let {
                            postRemove(sessionContext)
                            return
                        }

                    }
                }

                else -> {}
            }
        }
        sessionContext.interrupt(unchangedMsg)
        postRemove(sessionContext)

        // maybe add follow-up command
    }

    private fun emptyText(): String {
        return "No vendors found"
    }

    //todo might include "confirm proceed" at start?
//todo might be able to export some of this to external util class?
    private fun preRemove(
        sessionContext: SessionContext,
        cmd: BaseCommand,
    ): Boolean {
        with(fetchUnexpiredShopVendors()) {
            this?.let {
                if (it.isEmpty()) {
                    sessionContext.interrupt(emptyText())
                    return false
                }
                return hasValidPermissionsForOperation(sessionContext, cmd)
            }
            return false
        }
    }


    // will this be different from list() operation? May depend on role
// should output be INTEGER or BULLET?
    private fun postRemove(
        sessionContext: SessionContext,
    ) {
        fetchUnexpiredShopVendors()?.let { items ->
            val result = with(items.map { SelectableEntity(it) }) {
                if (this.isEmpty()) {
                    null
                } else {
                    OptionSelector(
                        this,
                        headerText = listHeader(),
                        indexType = ListIndexTypeOld.INTEGER,
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
                sessionContext.interrupt(emptyText())
            }

        }
        //todo display additional commands to choose from to run
    }

    //todo combine with ListVendors cmd class?
    private fun listHeader(): String {
        return "Active vendors:"
    }


    //todo unsure if this should be implemented
    companion object Prompts
}
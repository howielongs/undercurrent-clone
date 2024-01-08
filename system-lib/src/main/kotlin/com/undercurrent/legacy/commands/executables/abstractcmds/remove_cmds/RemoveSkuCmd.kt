package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.legacy.commands.executables.list.ListProducts
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItems
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.utils.Log
import org.jetbrains.exposed.sql.transactions.transaction

 class RemoveSkuCmd(
     sessionContext: SessionContext
) : RemoveCmds(CmdRegistry.REMOVESKU, sessionContext, "SKU") {

    override suspend fun execute() {
        /**
         * Check:
         *      - products exist
         *      - saleitems exist
         */
        //can probably do a better job caching this (don't optimize now)

        thisStorefront?.let {
            val selectedSku = SaleItems.selectSkuFromNestedList(
                sessionContext,
                promptText = "Select value to remove:",
                emptyText = "No SKUs available to remove. \n${PressAgent.showHelp()}",
                displayAttachments = false,
                storefront = it,
                command = CmdRegistry.REMOVESKU,
            ) ?: run {
                return
            }

            val confirmString = """
            |You selected to remove:
            |
            | Selection: ${selectedSku.name} ${selectedSku.unitSize}
            | Price: ${'$'}${selectedSku.price}
            | Details: ${selectedSku.details}
            |
            |${PressAgent.removeYesNoQuestion()}
        """.trimMargin()


            UserInput.promptYesNo(
                confirmString,
                sessionContext
            )?.let { confirmResponse ->
                if (confirmResponse) {
                    selectedSku.expire()
                    sessionContext.interrupt("Successfully removed SKU")

                    //todo display "Your inventory" as the header

                    ListProducts(sessionContext).execute()
                    return
                } else {
                    val denyMsg = "SKU not removed. Your inventory is unchanged."
                    Log.debug("$sessionContext: $denyMsg")
                    sessionContext.interrupt(denyMsg)
                    ListProducts(sessionContext).execute()
                    return
                }
            }
        }
    }

    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return transaction { sessionContext.user.shopVendor?.currentStorefront?.saleItems ?: listOf() }
    }

    override fun entityTable(): ExposedTableWithStatus2 {
        return SaleItems
    }

}
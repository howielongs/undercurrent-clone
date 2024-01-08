package com.undercurrent.legacy.commands.executables.remove





import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.shared.utils.Log
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import org.jetbrains.exposed.sql.transactions.transaction

class RemoveProduct(sessionContext: SessionContext) : Executable(
    CmdRegistry.RMPRODUCT, sessionContext
), CanCheckShouldShow {

    override fun shouldShow(): Boolean {
        return transaction { thisShopVendor.products.isNotEmpty() }
    }

    /**
     * As vendor: remove a product from my inventory
     * On remove, should expire underlying SKUs as well
     */
    override suspend fun execute() {


        //todo fix this up
        val vendor = sessionContext.user.shopVendor ?: run {
            sessionContext.interrupt("You are not a vendor")
            return
        }

        with(vendor.products) {
            if (this.isEmpty()) {
                sessionContext.interrupt(PressAgent.noVendorProductsHint())
                return
            }

            UserInput.selectAnOption(
                sessionContext = sessionContext,
                options = this.map { SelectableEntity(it) },
                headerText = "Select a product to remove:",
                headlineText = "/${CmdRegistry.RMPRODUCT.lower()}"
            )?.let {
                if (it is SelectedEntity && it.entity is ShopProduct) {
                    with(it.entity) {
                        Log.debug("User selected Product #${this.uid} to remove")

                        val confirmString = """
                             |Selected product to remove:
                             |$this
                             |
                             |${PressAgent.removeYesNoQuestion()} """.trimMargin()

                        UserInput.promptYesNo(
                            confirmString,
                            sessionContext
                        )?.let { confirmResponse ->
                            if (confirmResponse) {
                                //todo will want to check/notify customers if product/its items are in carts or orders
                                expire()
                                expireSaleItems()

                                sessionContext.interrupt("Successfully removed product and associated SKUs")
                                return
                            }
                        }
                    }
                }
            }
            sessionContext.interrupt("No products removed. Nothing has changed.")
            //todo should print list after?
        }
    }


    //todo unsure if this should be implemented
    companion object Prompts
}
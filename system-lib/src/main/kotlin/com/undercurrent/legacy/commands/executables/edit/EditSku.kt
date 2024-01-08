package com.undercurrent.legacy.commands.executables.edit

import com.undercurrent.shared.utils.tx
import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.list.ListProducts
import com.undercurrent.legacy.commands.registry.CmdRegistry.EDITPRICE
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItems
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacy.types.enums.AncestorMemoTypes
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.expireAncestor
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput

class EditSku(sessionContext: SessionContext) : Executable(EDITPRICE, sessionContext) {


    //todo consider command abstraction layer by role for lazy loading
    override val thisStorefront: Storefront by lazy {
        thisShopVendor.currentStorefront ?: throw ExecutableExceptions.StorefrontNotFoundException(sessionContext)
    }


    //todo this can be majorly refactored
    override suspend fun execute() {
        val storefront = thisStorefront

        SaleItems.selectSkuFromNestedList(
            sessionContext,
            promptText = "Select SKU to edit:",
            command = thisCommand,
            displayAttachments = false,
            storefront = storefront
        )?.let { selectedSku ->
            UserInput.promptUser(
                promptString = "Current price for this item: $${selectedSku.price}.\n\n" +
                        "Enter new price: ",
                sessionContext = sessionContext,
                validationType = ResponseType.CURRENCY,
            )?.let { newPrice ->

                val confirmString = """
            |You entered:
            |
            | Selection: ${selectedSku.name} ${selectedSku.unitSize}
            | 
            | Old price: ${'$'}${selectedSku.price}
            | New price: $${newPrice}
            |
            |${PressAgent.correctYesNoQuestion()}
        """.trimMargin()

                if (UserInput.promptYesNo(
                        confirmString,
                        sessionContext,
                        noText = "Price unchanged. Operation complete."
                    )
                ) {
                    val newSku = tx {
                        SaleItem.new {
                            product = selectedSku.product
                            unitSize = selectedSku.unitSize
                            price = newPrice
                        }

                    }

                    tx {
                        expireAncestor(selectedSku, sessionContext, newSku.uid, AncestorMemoTypes.PRICE_CHANGE.name)
                    }

                    ("Updated SKU price successfully. " +
                            "Existing customer orders will still show the old value.").let { it1 ->
                        sessionContext.interrupt(
                            it1
                        )
                    }

                    //todo is this the best way to start next processes?
                    ListProducts(sessionContext).execute()
                    return
                }
            }
            "Unable to update SKU price.".let { sessionContext.interrupt(it) }
        }
    }

}
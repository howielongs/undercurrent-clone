package com.undercurrent.legacyshops.nodes.vendor_nodes

import com.undercurrent.legacyshops.nodes.shared_nodes.CancelNode
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.prompting.nodes.interactive_nodes.CurrencyInputNode
import com.undercurrent.prompting.nodes.interactive_nodes.TextInputNode
import com.undercurrent.prompting.nodes.interactive_nodes.YesNoInputNode
import com.undercurrent.prompting.views.menubuilding.SelectAbcPrompt
import com.undercurrent.prompting.views.menubuilding.formatting.MenuPrefixFormatter
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext

class AddSkuCmdNodes(
    context: SystemContext
) : AbstractShopVendorNode(context) {

    override suspend fun next(): TreeNode? {
        return SelectProductToAddSku()
    }

    private fun fetchVendorProducts(): List<ShopProduct> {

        return tx {
            val storefrontIn = ShopVendors.fetchBySms(context.userSms.value)?.allStorefronts?.firstOrNull()
            storefrontIn?.let {
                storefrontIn.products
            }
        } ?: return emptyList()
    }

    @Deprecated("Should convert this underlying node to 'MenuSelectNode'")
    inner class SelectProductToAddSku : TextInputNode(inputProvider, nodeInterrupter) {

        override suspend fun next(): TreeNode? {

            val products = fetchVendorProducts()
            val prompt = SelectProductAbcPrompt(options = products)


            fetchInput(prompt.toString())?.let {
                val chosen = prompt.selectOption(it)?.item
                if (chosen != null) {
                    return AddProductCmdNodes(context)
                        .DecideToAddSkuToProduct(
                            chosen
                        )
                } else {
                    (prompt.selectFooterIfHandleCorresponds(it))?.let { footer ->
                        if (footer == "Cancel")
                            return CancelNode(context)
                    } ?: sendOutput("Invalid choice. Please try again.")
                }
            }
            return null
        }
    }


    inner class EnterPriceForSku(
        val product: ShopProduct,
    ) : CurrencyInputNode(interactors) {
        override suspend fun next(): TreeNode? {
            fetchInput("Enter unit price for SKU (e.g. \$29.99)")?.let {
                return EnterUnitSizeForSku(product = product, price = it)
            }
            logger.error("EnterPriceForSku should never come here")
            return null // Should never come here
        }
    }

    inner class EnterUnitSizeForSku(
        val product: ShopProduct, val price: String
    ) : TextInputNode(interactors) {
        override suspend fun next(): TreeNode? {
            return fetchInput("Enter unit size for SKU (e.g. 24-pack, bottle, etc.):")?.let {
                ConfirmAddSku(product, price, it)
            }
        }
    }

    inner class ConfirmAddSku(
        val product: ShopProduct,
        val price: String,
        val unitSize: String,
    ) : YesNoInputNode(interactors) {

        override suspend fun next(): TreeNode? {
            val prompt = tx {
                "[VENDOR:${thisStorefront?.displayName}]" + "\n\nNew SKU:" + "\n • Product: ${product.name}" + "\n • Details: ${product.details}" + "\n • Unit size: $unitSize" + "\n • Price per unit: $price" + "\n\nSave SKU to your inventory?"
            }
            return fetchInput(prompt)?.let {
                if (it) {
                    SaveSku(product, price, unitSize)
                } else {
                    //Same as in DecideToAddSkuToProduct
                    sendOutput("New SKU was not saved.")
                    AddProductCmdNodes(context).DecideToNotifyCustomers()
                }
            }
        }
    }

    inner class SaveSku(
        private val productIn: ShopProduct,
        private val priceIn: String,
        private val unitSizeIn: String,
    ) : OutputNode(interactors) {
        override suspend fun next(): TreeNode? {
            tx {
                SaleItem.new {
                    unitSize = unitSizeIn
                    price = priceIn
                    product = productIn
                }
            }?.let {
                sendOutput("SKU created and added to product")
                return DisplayInventory(productIn)
            }
            return null
        }
    }

    inner class DisplayInventory(
        private val productIn: ShopProduct,
    ) : OutputNode(interactors) {
        override suspend fun next(): TreeNode? {
            //todo output contents of inventory
            return AddProductCmdNodes(context).DecideToAddSkuToProduct(
                product = productIn,
                articleStr = "another"
            )
        }
    }


    /**
     * Creates a String based on Product data that will look similar to this
     *
     * Choose product to create SKU from:
     * A. CHELSEA'S TEST PRODUCT --> Chelsea's product description test test test.
     * B. TURTLE --> This is a turtle. He is a hypothetical product for sale.
     * C. Cancel
     */

    class SelectProductAbcPrompt(
        header: String = "Choose product to create SKU from:",
        footer: String? = "Cancel",
        options: List<ShopProduct> = emptyList(),
    ) : SelectAbcPrompt<String, ShopProduct>(
        header,
        footer,
        options,
        MenuPrefixFormatter.SimplePeriod(),
    )

}
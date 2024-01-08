package com.undercurrent.legacy.dinosaurs.prompting.selectables



import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.shared.utils.tx

/**
 * Nested SKU selection for decimal selection
 * SKU is nested under Product
 * Useful for /menu and /editsku commands
 */
class NestedSkuSelectionMap(
    val products: List<ShopProduct>,
    var headerText: String = "",
    private var displayItemlessProducts: Boolean = false,
) {

    //todo will need to come back and figure out when is best to send attachments
    var indexedSelectablesMap = HashMap<Int, SelectableElement>()
    var promptString = ""

    fun loadMenu(): NestedSkuSelectionMap {
        if (products.isEmpty()) {
            promptString = "No products found"
        } else {
            promptString = "$headerText"
            if (headerText != "") {
                promptString += "\n"
            }
            products.forEachIndexed { index, product ->
                //todo can probably be cleaned with more idiomatic Kotlin functional stuff
                val saleItems = tx { product.saleItems }
                if (saleItems.isNotEmpty() || displayItemlessProducts) {
                    val saleItemsPair = tx {
                        product.generateSelectableSaleItems(
                            index + 1, saleItems,
                        )
                    }
                    promptString += saleItemsPair.first
                    saleItemsPair.second.keys.forEach { decIndex ->
                        val sku = saleItemsPair.second[decIndex]
                        sku?.let {
                            indexedSelectablesMap[it.uid] = SelectableElement(
                                dereferencedUid = it.uid,
                                displayedIndex = decIndex,
                                indexType = ListIndexTypeOld.DECIMAL,
                            )
                        }
                    }
                }
            }
        }
        return this
    }

}
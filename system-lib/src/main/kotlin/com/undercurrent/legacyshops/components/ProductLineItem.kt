package com.undercurrent.legacyshops.components

import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.prompting.views.menubuilding.formatting.BaseLineItemBuilder
import com.undercurrent.shared.formatters.formatPretty
import com.undercurrent.shared.utils.ToStringFunc

sealed class ShopEntityLineItemBuilder<T>(
    private val formatLineItem: (T) -> String
) : BaseLineItemBuilder<T>({ formatLineItem(it) })

// • LETTUCE ->  Green and watery
class ProductLineItem : ShopEntityLineItemBuilder<ShopProduct>(
    { "${it.name} -> ${it.details}" }
)

// 	$5.00	 / single
class SaleItemLineItem : ShopEntityLineItemBuilder<SaleItem>(
    {
        "$${formatPretty(it.price)}   / ${it.unitSize}"
    })


val productToLineStr: ToStringFunc<ShopProduct> = {
    "${it.name} -> ${it.details}"
}
val saleItemToLineStr: ToStringFunc<SaleItem> = {
    "$${formatPretty(it.price)}   / ${it.unitSize}"
}

//object InventoryLineItemTransformers {
//    class ProductLineItem : AbstractStrTransformer<ShopProduct>(
//        { "${it.name} -> ${it.details}" }
//    )
//
//    class SaleItemLine(
//        private val currencySymbol: String = "$"
//    ) : AbstractStrTransformer<SaleItem>(
//        {
//            "$currencySymbol${formatPretty(it.price)}   / ${it.unitSize}"
//        })
//}



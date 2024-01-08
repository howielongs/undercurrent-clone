package com.undercurrent.legacyshops.service

import com.undercurrent.legacyshops.components.productToLineStr
import com.undercurrent.legacyshops.components.saleItemToLineStr
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_items.ShopProduct
import com.undercurrent.prompting.views.menubuilding.formatting.LineItemStringBuilder
import com.undercurrent.shared.utils.IntToAbcTransformer
import com.undercurrent.shared.utils.ToStringFunc

/**
 * This may be merged with MenuBuilder perhaps
 * Similar implementation of lineBuilders and index/handle formatting
 * Want to avoid duplicating efforts
 */

interface ListStringBuilder {
    fun buildString(): String
}

/**
 * todo need to take custom indices into account
 *
 * Consider separating out some of these deps into line builder
 */
class ProductInventoryStringBuilder(
    private val product: ShopProduct,
    private val saleItems: List<SaleItem>,

    //todo should this be more of a 'header' builder?
    private val productToStringTransformer: ToStringFunc<ShopProduct> = productToLineStr,
    private val productLineFormatter: (String) -> String = { "• $it" },

    //todo may want to pass some of these dependencies down into LineBuilder
    private val saleItemToStringTransformer: ToStringFunc<SaleItem> = saleItemToLineStr,

    //todo need to take custom indices into account
    //should have a map between 1.1 to 1, 2.1 to 3, etc.

     val indexTransformer: ToStringFunc<Int> = { IntToAbcTransformer().transform(it) },
    private val linePrefixFormatter: (String) -> String = { "[${it}]" },


    private val leadingSpace: String = " ",
    private val prefixBodySpacing: String = "\t",

) : ListStringBuilder {

    override fun buildString(): String {
        val sb = StringBuilder()

        val headerBody = productLineFormatter(productToStringTransformer(product))
        sb.append("$leadingSpace$headerBody\n")

        saleItems.forEachIndexed { index, item ->
            //todo perhaps map these to handle using data class?
            //could also include fuzzy handles and original index
            val handle = indexTransformer(index + 1)
            val lineBody = saleItemToStringTransformer(item)
            val prefix = linePrefixFormatter(handle)
            val lineWithPrefix = "$prefix$prefixBodySpacing$lineBody"

            sb.append("$leadingSpace$lineWithPrefix\n")
        }
        return sb.toString()
    }


}


/**
 * This needs to be able to build up the inventory and have
 * a map of the selectable indices, as well as the generated
 * strings for each item.
 *
 * @param T - the type of the item
 * @property selectablesMap - the map of indices to the generated strings
 * @property fullString - the full string of the inventory
 */
interface InventoryListBuilder<T> {
    val selectablesMap: Map<String, String>
    val fullString: String
    val indexFormatter: (Int) -> String

    fun buildInventoryString(items: List<T>): String
}

abstract class BaseInventoryListBuilder<T>(
    private val lineItemBuilder: LineItemStringBuilder<T>,
) : InventoryListBuilder<T> {

    override val selectablesMap: Map<String, String> = linkedMapOf()
    override val fullString: String = ""

    private fun buildLineItem(index: String, item: T): String {
        return lineItemBuilder.buildLineItem(item)
    }

    override fun buildInventoryString(items: List<T>): String {
        val sb = StringBuilder()
        items.forEachIndexed { index, item ->
//            val indexString = (index + 1).toString()
//            selectablesMap[indexString] = buildLineItem(indexString, item)
//            sb.append(selectablesMap[indexString] + "\n")
        }
        return sb.toString()
    }
}


// • LETTUCE ->  Green and watery
//  [1.1]	$5.00	 / single
//  [1.2]	$25	 / half a head
//  [1.3]	$28.30	 / smallest
//  [1.4]	$36.69	 / One head
//  [1.5]	$100.45	 / large pack
/**
 * This should on one hand have all the selectable indices for the products/items,
 * but also have the individual component strings and the overall prompt string.
 */
//class ProductInventoryListing(
//    override val selectablesMap: Map<String, String>,
//    override val fullString: String,
//    override val indexFormatter: (Int) -> String
//) : InventoryListBuilder<ShopProduct> {
//    private val productLineItemBuilder: LineItemStringBuilder<ShopProduct> = ProductLineItem()
//    private val saleItemLineItemBuilder: LineItemStringBuilder<SaleItem> = SaleItemLineItem()
//
//    private fun buildProductHeaderString(product: ShopProduct): String {
//        return productLineItemBuilder.buildLineItem(product)
//    }
//
//    private fun buildSaleItemsString(product: ShopProduct): String {
//        var outString = ""
//        product.saleItems.forEach {
//            //todo increment index accordingly
//            outString += saleItemLineItemBuilder.buildLineItem(it)
//        }
//        return outString
//    }
//
//    override fun buildInventoryString(items: List<ShopProduct>): String {
//        items.forEach {
//            //todo increment index accordingly
//            productLineItemBuilder.buildLineItem(it)
//            //todo then add saleItems
//        }
//        return ""
//    }
//}
//
////  [1.1]	$5.00	 / single
////  [1.2]	$25	 / half a head
////  [1.3]	$28.30	 / smallest
////  [1.4]	$36.69	 / One head
////  [1.5]	$100.45	 / large pack
//class SaleItemListBuilder(
//    override val selectablesMap: Map<String, String>,
//    override val fullString: String,
//    override val indexFormatter: (Int) -> String
//) : InventoryListBuilder<SaleItem> {
//    private val saleItemLineItemBuilder: LineItemStringBuilder<SaleItem> = SaleItemLineItem()
//    override fun buildInventoryString(items: List<SaleItem>): String {
//        var outString = ""
//
//        items.forEach {
//            //todo increment index accordingly
//            outString += saleItemLineItemBuilder.buildLineItem(it)
//        }
//
//
//        return outString
//    }
//}

// • LETTUCE ->  Green and watery
//  [1.1]	$5.00	 / single
//  [1.2]	$25	 / half a head
//  [1.3]	$28.30	 / smallest
//  [1.4]	$36.69	 / One head
//  [1.5]	$100.45	 / large pack
// ___________________________
//
//• TOGA ->  Classical Greek attire
//  [2.1]	$1.01	 / small
// ___________________________
//
//• ATLAS ->  Book of lovely maps 🗺️
//  [3.1]	$30.01	 / regular
// ___________________________
/**
 * This one is a collection of ProductItemListing
 */
//class FullShopInventoryListing


// IMG:
//*************************
//*************************
//*************************
//*************************
//*************************
//*************************
//*************************
// CAPTION > Description
class AttachmentSender











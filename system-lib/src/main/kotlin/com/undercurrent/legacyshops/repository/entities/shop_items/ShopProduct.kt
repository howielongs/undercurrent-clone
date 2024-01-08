package com.undercurrent.legacyshops.repository.entities.shop_items

import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import com.undercurrent.legacy.repository.entities.system.attachments.AttachmentLinks.Table as AttachLinks

object ShopProducts : ExposedTableWithStatus2("shop_products") {
    val storefront = reference("storefront_id", Storefronts)
    val name = varchar("name", VARCHAR_SIZE)
    val details = varchar("details", VARCHAR_SIZE)
}

class ShopProduct(id: EntityID<Int>) : ExposedEntityWithStatus2(id, ShopProducts) {
    companion object : RootEntityCompanion0<ShopProduct>(ShopProducts)

    var storefront by Storefront referencedOn ShopProducts.storefront
    var name by ShopProducts.name
    var details by ShopProducts.details


    val allSaleItems by SaleItem referrersOn SaleItems.product

    var saleItems: List<SaleItem> = listOf()
        get() {
            return transaction { allSaleItems.filter { it.isNotExpired() }.sortedBy { BigDecimal(it.price) } }
        }

    var attachmentLinks: List<AttachmentLinks.Entity> = listOf()
        get() {
            return transaction {
                AttachmentLinks.Entity.find {
                    AttachLinks.parentEntityId eq this@ShopProduct.uid and
                            (AttachLinks.parentEntityClassName eq this@ShopProduct::class.java.simpleName
                                    and (AttachLinks.attachmentType eq AttachmentType.PRODUCT_IMAGE.name)
                                    )
                }.filter { it.isNotExpired() }.toList()
            }
        }


    //todo convert to backReference
    var linkedAttachments: List<Attachments.Entity> = listOf()
        get() {
            return transaction { attachmentLinks }.map { transaction { it.parentAttachment } }
                .filter { it.isNotExpired() }.toList()
        }

    //todo replace with newer method for displayList and selectOption
    @Deprecated("Replace with newer messaging in UserInput:: displayList and selectOption")
    fun getSaleItemsListString(
        productIndex: Int,
        saleItemsList: List<SaleItem> = listOf(),
        indexType: ListIndexTypeOld = ListIndexTypeOld.BULLET,
    ): String {
        var emptyString = "No SKUs for this product"

        var listIndex = when (indexType) {
            ListIndexTypeOld.ABC -> UtilLegacy.getCharForNumber(productIndex) + "."
            ListIndexTypeOld.INTEGER -> "$productIndex."
            ListIndexTypeOld.BULLET -> "•"
            ListIndexTypeOld.NONE -> " "
            else -> {
                " "
            }
        }
        val productName = transaction { name }
        val productDetails = transaction { details }
        val productSaleItems = transaction { saleItems }


        var outText = "$listIndex ${productName?.uppercase()} ->  $productDetails\n"

        //prevent multiple queries if possible
        val theseSaleItems = saleItemsList.ifEmpty {
            productSaleItems
        }

        with(theseSaleItems) {
            if (this == null || this.isEmpty()) {
                return "$outText\t($emptyString)\n\n"
            }

            this.forEachIndexed { index, saleItem ->
                val salePrice = transaction { saleItem.price }
                val itemSize = transaction { saleItem.unitSize }
                outText += "  [$productIndex.${index + 1}]\t\$${salePrice}\t / ${itemSize}\n"
            }
        }
        return "$outText ___________________________\n\n"
    }


    fun generateSelectableSaleItems(
        productIndex: Int,
        saleItemsList: List<SaleItem>,
    ): Pair<String, MutableMap<String, SaleItem>> {
        return transaction {
            //todo can probably be cleaned with more idiomatic Kotlin functional stuff

            var selectableMap = HashMap<String, SaleItem>().toMutableMap()
            var outText = "• ${name?.uppercase()} ->  $details\n"

            if (saleItemsList.isEmpty()) {
                outText = "$outText\t(No SKUs for this product)\n\n"
                return@transaction Pair(outText, selectableMap)
            }

            var thisIndex: String

            saleItemsList.forEachIndexed { index, saleItem ->
                thisIndex = "$productIndex.${index + 1}"
                outText += "  [$thisIndex]\t\$${saleItem.price}\t / ${saleItem.unitSize}\n"
                selectableMap[thisIndex] = saleItem
            }
            outText += "\n___________________________\n\n"
            return@transaction Pair(outText, selectableMap)
        }
    }

    fun expireSaleItems(): Int {
        return SaleItems.expireByProductId(this.id.value)
    }

    override fun toString(): String {
        return transaction {
            var outText = "${name?.uppercase()} -->"
            outText += " $details"
            outText
        }
    }
}


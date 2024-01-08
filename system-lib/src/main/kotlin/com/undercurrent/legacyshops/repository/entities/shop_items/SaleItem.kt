package com.undercurrent.legacyshops.repository.entities.shop_items

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class SaleItem(id: EntityID<Int>) : ExposedEntityWithStatus2(id, SaleItems) {
    companion object : RootEntityCompanion0<SaleItem>(SaleItems)

    var product by ShopProduct referencedOn SaleItems.product

    var price by SaleItems.price
    var unitSize by SaleItems.unitSize

    var name: String? = null
        get() = transaction { product.name }

    var details: String? = null
        get() = transaction { product.details }


    //todo investigate if this is this is used
    override fun toString(): String {
        return transaction { "${product?.name}: \n\t\$$price/$unitSize\n\t${product?.details}\n" }
    }


}
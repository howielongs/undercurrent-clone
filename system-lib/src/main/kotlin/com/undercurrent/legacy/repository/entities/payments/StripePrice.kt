package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItems
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID

object StripePrice {

    /**
     * product_sid (Stripe ID)
     * name
     * description
     *
     * ref: SaleItem
     */
    object Table : ExposedTableWithStatus2("stripe_prices") {
        val saleItem = reference("sale_item_id", SaleItems)
        val priceStripeId = varchar("price_sid", VARCHAR_SIZE)
        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var saleItem by SaleItem referencedOn Table.saleItem
        var priceStripeId by Table.priceStripeId
    }


}

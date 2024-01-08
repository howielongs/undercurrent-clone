package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID

object StripePaymentLinks {
    object Table : ExposedTableWithStatus2("stripe_payment_links") {
        val deliveryOrder = reference("order_id", DeliveryOrders)
        val paymentLink = varchar("payment_link_id", VARCHAR_SIZE)
        val paymentUrl = varchar("payment_url", VARCHAR_SIZE)
        val checkoutSession = varchar("checkout_session_sid", VARCHAR_SIZE).nullable()
        val paymentIntent = varchar("payment_intent_sid", VARCHAR_SIZE).nullable()

        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var deliveryOrder by DeliveryOrder referencedOn Table.deliveryOrder
        var paymentLink by Table.paymentLink
        var paymentUrl by Table.paymentUrl
        var checkoutSession by Table.checkoutSession
        var paymentIntent by Table.paymentIntent
    }


}

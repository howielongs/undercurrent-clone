package com.undercurrent.legacyshops.repository.entities.shop_orders

import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.time.EpochNano
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.datetime

interface ShopOrderLookup {
    var shopOrder: DeliveryOrder
}

interface ShopOrderConfirmDeclineEvent : ShopOrderLookup {
    var notes: String
}

enum class ShopOrderEventType {
    CREATED,
    CONFIRMED,
    DECLINED,
    CANCELLED,
    PAID,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    UNKNOWN,
}

open class ShopOrderEventCompanion : RootEntityCompanion0<ShopOrderEvent>(ShopOrderEvents) {

//    fun fetchConfirmableOrdersForStorefront(storefront: Storefront): List<DeliveryOrder> {
//        return tx {
//            DeliveryOrder.all().toList().filter { it.isNotExpired() }
//        }
//    }
}

object ShopOrderEvents : RootTable0("shop_order_events") {
    val shopOrder = reference("shop_order_id", DeliveryOrders)
    val eventType = varchar("event_type", VARCHAR_SIZE)

    val eventDate = datetime("event_date").clientDefault { Util.getCurrentUtcDateTime() }
    val eventEpochNano = long("event_epoch_nano").clientDefault { EpochNano().value }

    val notes = varchar("notes", VARCHAR_SIZE)
    val memo = varchar("memo", VARCHAR_SIZE)
}

class ShopOrderEvent(id: EntityID<Int>) : RootEntity0(id, ShopOrderEvents), ShopOrderConfirmDeclineEvent {
    companion object : ShopOrderEventCompanion()

    override var shopOrder by DeliveryOrder referencedOn ShopOrderEvents.shopOrder
    var eventType by ShopOrderEvents.eventType.transform(
        toColumn = { it.name },
        toReal = { ShopOrderEventType.valueOf(it) })

    var eventDate by ShopOrderEvents.eventDate
    var eventEpochNano by ShopOrderEvents.eventEpochNano

    override var notes by ShopOrderEvents.notes
    var memo by ShopOrderEvents.memo
}
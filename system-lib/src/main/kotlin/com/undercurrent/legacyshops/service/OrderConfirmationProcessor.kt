package com.undercurrent.legacyshops.service

import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.ShopOrderEvent
import com.undercurrent.legacyshops.repository.entities.shop_orders.ShopOrderEventType
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx

interface OrderProcessor {
    fun process(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent?
}

interface CanConfirmShopOrder {
    fun confirmOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent?
}

interface CanDeclineShopOrder {
    fun declineOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent?
}

abstract class OrderApprovalProcessor(
    private val eventTypeIn: ShopOrderEventType,

    @Deprecated("use eventTypeIn instead")
    private val incomingStatus: OrderStatus,
) : OrderProcessor {

    override fun process(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent? {
        val datetime = Util.getCurrentUtcDateTime()
        val now = EpochNano()

        val newEvent = tx {
            //this first section should be phased out
            order.status = incomingStatus.name
            order.notesToCustomer = notesToCustomer

            if (incomingStatus == OrderStatus.CONFIRMED) {
                order.confirmedDate = datetime.toString()
                order.declinedDate = null

            } else if (incomingStatus == OrderStatus.DECLINED) {
                order.declinedDate = datetime.toString()
                order.confirmedDate = null
            }

            //rely more on this second section for event history
            ShopOrderEvent.new {
                shopOrder = order
                eventType = eventTypeIn
                notes = notesToCustomer
                eventDate = datetime
                eventEpochNano = now.value
            }
        }
        return newEvent
    }
}

class OrderConfirmationProcessor(
) : OrderApprovalProcessor(
    eventTypeIn = ShopOrderEventType.CONFIRMED,
    incomingStatus = OrderStatus.CONFIRMED
), CanConfirmShopOrder {
    override fun confirmOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent? {
        return process(order, notesToCustomer)
    }
}

class OrderDeclinationProcessor(
) : OrderApprovalProcessor(
    eventTypeIn = ShopOrderEventType.DECLINED,
    incomingStatus = OrderStatus.DECLINED
), CanDeclineShopOrder {
    override fun declineOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent? {
        return process(order, notesToCustomer)
    }
}


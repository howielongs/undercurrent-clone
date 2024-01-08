package com.undercurrent.legacyshops.nodes.vendor_nodes

import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.legacyshops.repository.entities.shop_orders.ShopOrderEvent
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.service.CanConfirmShopOrder
import com.undercurrent.legacyshops.service.CanDeclineShopOrder
import com.undercurrent.legacyshops.service.OrderConfirmationProcessor
import com.undercurrent.legacyshops.service.OrderDeclinationProcessor
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import org.jetbrains.exposed.sql.and


class ConfirmOrderNodes(
    context: SystemContext,
) : AbstractShopVendorNode(context), CanConfirmShopOrder, CanDeclineShopOrder {
    private val orderToStringFunc: (DeliveryOrder) -> String = {
        DeliveryOrder.displayLongString(it, ShopRole.VENDOR)
    }

    private val fetchConfirmableOrdersFunc: (ShopVendor, Int) -> List<DeliveryOrder> = { vendor, limit ->
        val now = EpochNano()
        DeliveryOrder.find {
            DeliveryOrders.vendor eq vendor.id and unexpiredExpr(DeliveryOrders, now) and (
                    DeliveryOrders.status eq OrderStatus.SUBMITTED.name)
        }.limit(limit).sortedBy { it.id }.toList()
    }

    override fun confirmOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent? {
        return OrderConfirmationProcessor().confirmOrder(order, notesToCustomer)
    }

    override fun declineOrder(order: DeliveryOrder, notesToCustomer: String): ShopOrderEvent? {
        return OrderDeclinationProcessor().declineOrder(order, notesToCustomer)
    }

    private val notifyShopOrderAudience: (DeliveryOrder, String) -> Unit = { order, outString ->

        //todo impl this
    }

    fun checkForReceivableWallet(): TreeNode? {
        //todo figure out where this should be inserted
        return null
    }

    override suspend fun next(): TreeNode? {
        return fetchConfirmableOrdersNode()
    }

    suspend fun fetchConfirmableOrdersNode(): TreeNode? {
        val results = tx {
            fetchConfirmableOrdersFunc(thisVendor, 5)
        }

        return if (results.isEmpty()) {
            notifyNoOrdersToConfirm()
        } else {
            val orderStr = if (results.size == 1) "order" else "orders"
            sendOutput("You have ${results.size} $orderStr to confirm.")

            //todo instead just page through them with options for a) confirm, b) decline, c) skip, d) go back, e) exit
            decideActionOnDisplayedOrderNode(results)
        }
    }

    fun notifyNoOrdersToConfirm(): TreeNode? {
        sendOutput("No orders to confirm.")
        // perhaps loop back to start? or just go back a level?
        return null
    }


    suspend fun decideActionOnDisplayedOrderNode(orders: List<DeliveryOrder>, startIndex: Int = 0): TreeNode? {
        if (orders.isEmpty()) {
            return notifyNoOrdersToConfirm()
        }

        var selectedOrder = if (startIndex < orders.size) {
            orders[startIndex]
        } else {
            sendOutput("All orders exhausted. Starting at the beginning...")

            sendTypingIndicatorWithDelay()
            return decideActionOnDisplayedOrderNode(orders, startIndex = 0)
        }

        val promptStr = orderToStringFunc(selectedOrder)
        sendOutput(promptStr)

        sendTypingIndicatorWithDelay()
        return menuSelectNode(
            options = listOf("Confirm order...", "Decline order...", "Skip for now", "Cancel"),
            headerText = "Select an option:",
            ifSuccess = {  index, _ ->
                when (index) {
                    0 -> promptConfirmReasonNode(selectedOrder)
                    1 -> promptDeclineReasonNode(selectedOrder)
                    2 -> decideActionOnDisplayedOrderNode(orders, startIndex = startIndex + 1)
                    else -> {
                        sendOutput("Operation cancelled.")
                        null
                    }
                }
            },
        )
    }

    fun promptConfirmReasonNode(
        order: DeliveryOrder,
    ): TreeNode? {
        return textInputNode(
            "Please enter any notes to customer (e.g. eta):",
            ifSuccess = {
                confirmOrderNode(order, it)
            },
        )
    }

    fun promptDeclineReasonNode(
        order: DeliveryOrder,
    ): TreeNode? {
        return textInputNode(
            "Please enter a reason for declining this order (e.g. out of stock, etc.):",
            ifSuccess = {
                declineOrderNode(order, it)
            },
        )
    }

    suspend fun confirmOrderNode(
        order: DeliveryOrder,
        notesToCustomer: String,
    ): TreeNode? {
        logger.debug("Performing confirm order for order ${order.id} with notes $notesToCustomer")
        val result = confirmOrder(order, notesToCustomer)
        return notifyConfirmedOrderNode(orderEvent = result, order = order, notesToCustomer = notesToCustomer)
    }

    suspend fun notifyConfirmedOrderNode(
        orderEvent: ShopOrderEvent?,
        order: DeliveryOrder,
        notesToCustomer: String,
    ): TreeNode? {
        notifyShopOrderAudience(order, notesToCustomer)

        //todo impl this
        //notify customer
        //notify admins
        return operationCompleteNode(orderEvent)
    }


    suspend fun notifyDeclinedOrderNode(
        orderEvent: ShopOrderEvent?,
        order: DeliveryOrder,
        reason: String,
    ): TreeNode? {

        notifyShopOrderAudience(order, reason)
        //todo impl this
        //notify customer
        //notify admins
        return operationCompleteNode(orderEvent)
    }

    suspend fun declineOrderNode(
        order: DeliveryOrder, reason: String
    ): TreeNode? {
        logger.debug("Performing decline order for order ${order.id} with reason $reason")
        val result = declineOrder(order, reason)
        return notifyDeclinedOrderNode(orderEvent = result, order = order, reason = reason)
    }

    suspend fun operationCompleteNode(orderEvent: ShopOrderEvent?): TreeNode? {
        if (orderEvent != null) {
            val outString: String =
                tx { "Order ${orderEvent.shopOrder.orderCode} ${orderEvent.eventType.name.lowercase()} with notes ${orderEvent.notes}" }

            // add symbols and headers to these sorts of messages
            sendOutput(outString)
        } else {
            sendOutput("Order not confirmed nor declined.")
        }
        return fetchConfirmableOrdersNode()
    }


}
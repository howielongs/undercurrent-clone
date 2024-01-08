package com.undercurrent.legacyshops.nodes.shared_nodes

import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext

class ShopOrderNodes(
    context: SystemContext,
    private val fetchConfirmableOrdersFunc: () -> List<DeliveryOrder> = { listOf() },
) : AbstractShopSharedRoleNode(
    context
) {
    override suspend fun next(): TreeNode? {
        return fetchConfirmableOrdersNode()
    }

    fun fetchConfirmableOrdersNode(): TreeNode? {
        val result = fetchConfirmableOrdersFunc()

        return if (result.isEmpty()) {
            notifyNoOrdersToConfirm()
        } else {
            selectOrderToConfirm(result)
        }
    }

    fun notifyNoOrdersToConfirm(): TreeNode? {
        sendOutput("No orders to confirm.")

        //todo perhaps loop back to start? or just go back a level?
        return null
    }


    fun selectOrderToConfirm(orders: List<DeliveryOrder>): TreeNode? {
        //todo impl this
        return null
    }

    fun confirmOrderNode(
        order: DeliveryOrder,
        reason: String
    ): TreeNode? {
        return null
    }

    fun declineOrderNode(
        order: DeliveryOrder,
        reason: String
    ): TreeNode? {
        return null
    }

    fun checkForReceivableWallet(): TreeNode? {
        return null
    }

}
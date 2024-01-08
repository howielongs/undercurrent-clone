package com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.PromptableParam
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.messaging.outbound.notifyAdmins
import org.jetbrains.exposed.sql.transactions.transaction

class MarkShippedCmd(context: SessionContext) :
    SelectAndUpdateCmd(CmdRegistry.MARKSHIPPED, context), CanSendToUserByRole {
    override val operationInfinitiveVerb: String
        get() = "to mark shipped"

    private val interrupter by lazy {
        context.interrupter
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        interrupter.sendOutputByRole(msgBody, role)
    }

    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return shippableOrders
    }

    private val shippableOrders: List<DeliveryOrder> by lazy {
        transaction {
            thisShopVendor.shippableOrders
        }.ifEmpty {
            throw ExecutableExceptions.GenericException(
                context,
                sessionMsg = "No orders ready to ship. Ensure all payments have been received.",
                errorLogMsg = "No orders ready to ship. Ensure all payments have been received."
            )
        }
    }

    private fun notifySuccess(
        order: DeliveryOrder, sessionContext: SessionContext
    ) {

        val thisOrder = transaction { order }
        val orderId = transaction { thisOrder.uid }
        val thisCustomer = transaction { order.customer }

        PressAgent.VendorStrings.orderShipped(
            orderId,
            DeliveryOrder.displayLongString(thisOrder, ShopRole.VENDOR)
        ).let { sendOutputByRole(it, ShopRole.VENDOR) }

        thisCustomer.notify(
            customerMsg = PressAgent.CustomerStrings.orderShipped(
                DeliveryOrder.displayLongString(thisOrder, ShopRole.CUSTOMER)
            )
        )
        notifyAdmins(
            msg = PressAgent.VendorStrings.orderShipped(
                orderId,
                receipt = DeliveryOrder.displayLongString(thisOrder, ShopRole.ADMIN)
            ),
            routingProps = sessionContext.routingProps,
        )
    }

    suspend fun markShipped(
        order: DeliveryOrder,
        trackingInfo: TrackingInfoPrompt = TrackingInfoPrompt(),
        sessionContext: SessionContext
    ) {
        val thisTrackingInfo = trackingInfo.acquireValue(sessionContext)

        transaction {
            order.trackingNumber = thisTrackingInfo
            order.status = OrderStatus.SHIPPED.name
        }
        notifySuccess(order, sessionContext)
    }


    class TrackingInfoPrompt(
        value: String? = null,
    ) : PromptableParam(
        value,
        field = DeliveryOrder::trackingNumber.name,
        validationType = ResponseType.STRING,
        prompt = "Enter shipping info for customer to track",
        displayName = "Tracking",
    )
    // probably another round of comments?

}
package com.undercurrent.legacy.commands.executables.shopping.cancelations




import com.undercurrent.legacy.commands.executables.abstractcmds.SystemCommandStarter
import com.undercurrent.system.context.SystemContext
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCallback
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCommand
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.repository_service.payments.accounting.LedgerEntryFetcher
import com.undercurrent.legacy.types.enums.status.InvoiceStatus
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.shared.utils.tx

class OrderCancellationHandler(
    val sessionContext: SessionContext
) {
    private fun existingOrdersStr(orders: List<DeliveryOrder>): String {
        val isPlural = orders.count() != 1

        var outStr = """
            |You have ${orders.count()} unpaid order${if (isPlural) "s" else ""}. 
        """.trimMargin()

        return outStr
    }

    suspend fun handle() {
        promptUserIfUnpaidOrders()
    }

    suspend fun promptUserIfUnpaidOrders(): Boolean {
        with(ordersAwaitingPayment(sessionContext.user)) {
            if (isNotEmpty()) {
                sessionContext.interrupt(existingOrdersStr(this))
                val options = mutableListOf(
                    SelectableCallback(
                        "View payment instructions for open order " +
                                "so I can clear my balance.",
                        Companion::viewPaymentInstructions
                    ),
                    SelectableCommand(
                        CmdRegistry.OPENORDERS,
                        "View open order details",
                    ),
                    SelectableCallback(
                        "Cancel my open order and continue checking out",
                        Companion::cancelOpenOrdersAndRunCheckout
                    ),
                )

                UserInput.selectAndRunCallback(
                    sessionContext,
                    options,
                    headerText = "What would you like to do?",
                    appendCancel = false,
                )
                return true
            }
        }
        return false
    }


    companion object {

        private fun cancelOrder(order: DeliveryOrder) {
            val ledgerEntries = LedgerEntryFetcher().fetchByOrder(order)

            tx {
                order.status = OrderStatus.CANCELED.name
                order.invoice.status = InvoiceStatus.CANCELED.name
                ledgerEntries.forEach { it.expire() }
            }
        }

        private suspend fun cancelOpenOrdersAndRunCheckout(sessionContext: SystemContext) {
            val orders = ordersAwaitingPayment(sessionContext.user)
            orders.forEach { cancelOrder(it) }
            SystemCommandStarter(sessionContext).startNewCommand(CmdRegistry.CHECKOUT)
        }

        private suspend fun viewPaymentInstructions(sessionContext: SystemContext) {
            BtcPaymentInstructionsBuilder(sessionContext).build()?.let {
                sessionContext.interrupt(it)
            }
            // unsure how to finish this (if transitions smoothly back into lifecycle)
        }

        /**
         *                             it.statusDoesNotMatchAny(
         *                                 CANCELED,
         *                                 DECLINED,
         *                                 SHIPPED,
         *                                 NEW,
         *                             )
         *
         */
        fun ordersAwaitingPayment(user: User): List<DeliveryOrder> {
            val statusSet = setOf(
                OrderStatus.AWAITING_PAYMENT.name,
                OrderStatus.SUBMITTED.name,
                OrderStatus.CONFIRMED.name,
            )
            return tx {
                user.pendingOrders.filter {
                    statusSet.contains(it.status)
                }
            }
        }

    }
}
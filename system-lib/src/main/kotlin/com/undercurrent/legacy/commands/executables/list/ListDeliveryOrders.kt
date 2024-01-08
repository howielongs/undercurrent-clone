package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.StatusMatchChecker
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

class ListDeliveryOrders(sessionContext: SessionContext) : Executable(
    CmdRegistry.OPENORDERS, sessionContext
) {

    override suspend fun execute() {
        //todo fix this up and add different views by role/context
        sessionContext.interrupt(
            getListOfActiveOrdersString(sessionContext)
        )
    }

    //todo fix issue with java class ref showing
    private fun pendingInvoicesToString(
        pendingInvoices: List<Invoice>
    ): String {
        var outString = "Pending orders:\n"
        pendingInvoices
            .ifEmpty { return "No pending orders" }
            .forEach { outString += "$it\n" }
        return outString
    }

    private fun fetchAllOrders(): List<DeliveryOrder> {
        return transaction { DeliveryOrder.all() }.toList().filter { it.isNotExpired() }
    }

    private fun fetchPendingOrders(): List<DeliveryOrder> {
        val statusMatcher = StatusMatchChecker()

        return transaction {
            return@transaction fetchAllOrders().filter {
                it.isNotExpired() &&
                        statusMatcher.statusDoesNotMatchAny(
                            it,
                            OrderStatus.CANCELED,
                            OrderStatus.DECLINED,
                            OrderStatus.NEW,
                        )
            }
        }
    }

    private fun getListOfActiveOrdersString(sessionContext: SessionContext): String {
        val sessionUser = sessionContext.user
        val role = sessionContext.role
        return transaction {
            when (role) {
                ShopRole.VENDOR -> {
                    sessionUser.shopVendor?.let {
                        return@transaction pendingInvoicesToString(it.pendingInvoices)
                    } ?: return@transaction ""
                }

                ShopRole.ADMIN -> {
                    var outString = "Pending orders:\n"
                    fetchPendingOrders().ifEmpty { return@transaction "No pending orders" }
                        .forEach {
                            outString += "${it.invoice?.displayInvoice(role)}\n"
                        }
                    return@transaction outString
                }

                ShopRole.CUSTOMER -> {
                    return@transaction pendingInvoicesToString(sessionUser.pendingInvoices)
                }

            }

            return@transaction ""
        }
    }

}
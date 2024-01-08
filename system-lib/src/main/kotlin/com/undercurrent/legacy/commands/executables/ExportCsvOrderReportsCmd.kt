package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.service.csvutils.csv_handlers.DeliveryOrdersCsvHandler
import com.undercurrent.legacy.types.enums.status.OrderStatus
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext


class ExportCsvOrderReportsCmd(sessionContext: SessionContext) :
    Executable(CmdRegistry.CSV_REPORT, sessionContext) {
    override suspend fun execute() {
        val allOrdersForVendor = tx { thisShopVendor.deliveryOrders.toList() }

        val invalidStatuses =
            setOf(
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED,
            )

        val openOrders = allOrdersForVendor
                .filter { order ->
                    order.isNotExpired() &&
                            tx {
                                !invalidStatuses.map { it.name }.contains(order.status)
                            }
                }

        if (openOrders.isNotEmpty()) {
            sessionContext.interrupt("Generating CSV spreadsheet file of your orders history")
            val count = openOrders.count()
            var pluralStr = if (count == 1) {
                ""
            } else {
                "s"
            }

            sessionContext.interrupt("Writing $count order${pluralStr} to new CSV file")
            DeliveryOrdersCsvHandler(orders = openOrders, sessionContext = sessionContext).send(sessionContext.user, sessionContext.routingProps)
        } else {
            sessionContext.interrupt("No orders to write to CSV")
        }

        sessionContext.interrupt("Operation complete")

    }
}
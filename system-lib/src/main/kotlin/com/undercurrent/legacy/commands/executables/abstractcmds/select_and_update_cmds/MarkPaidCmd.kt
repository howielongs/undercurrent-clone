package com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.service.VendorFetcher
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

class MarkPaidCmd(sessionContext: SessionContext) :
    SelectAndUpdateCmd(CmdRegistry.MARKPAID, sessionContext) {


    override val operationInfinitiveVerb: String
        get() = "to mark paid"

    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return VendorFetcher.fetchAllVendors().flatMap { transaction { it.payableOrders } }
            .sortedByDescending { transaction { it.createdDate } }
    }
//
//    suspend fun markPaid(
//        order: DeliveryOrder,
//    ) {
//
//        //todo manually update ledger?
//        //todo prompt for the exact amount the customer paid?
//
//        //may be some odd issues if customer has multiple orders in progress...
//        val currencyType = transaction { order.cryptoType }
//
////        if (currencyType != CryptoType.STRIPE) {
////            sessionPair.interrupt("This command is not valid for this currency type.")
////            Admins.notifyError("$sessionPair attempted to use ${CmdRegistry.MARKPAID.commandTag()} with $currencyType")
////            return
////        }
//
//        val thisInvoice = transaction { order.invoice }
//        val thisRoundedFudge = transaction { thisInvoice.roundedTotalFudgeCrypto }
//        val thisUser = transaction { order.customerProfile.user }
//        val thisCurrencyType = transaction { thisInvoice.cryptoType } ?: run {
//            "Cannot determine currency type for MARKPAID".let { sessionContext.interrupt(it) }
//            return
//        }
//
//        thisRoundedFudge?.let {
//            UserCreditLedger.save(
//                userIn = thisUser,
//                amountIn = it,
//                roleIn = Rloe.CUSTOMER,
//                memoIn = TransactionMemo.MARKED_PAID.name,
//                invoiceIn = thisInvoice,
//                currencyInterfaceIn = thisCurrencyType,
//                statusIn = LedgerEntryStatus.RECEIVED,
//            )
//        }
//
////        transaction {
////            order.status = OrderStatus.AWAITING_SHIPMENT.name
////            order.invoice.status = InvoiceStatus.RECEIVED_FULL_FROM_CUSTOMER.name
////        }
//
//        //todo update Ledger accordingly
//        //may want to have the exact amount the customer paid...
//
//        notifySuccess(sessionContext, order)
//    }
//
//    companion object {
//        private fun notifySuccess(
//            sessionContext: SessionContext,
//            order: DeliveryOrder
//        ) {
//
//            val thisOrder = transaction { order }
////            val orderId = transaction { thisOrder.uid }
//            val orderCode = transaction { thisOrder.orderCode }
//            val thisCustomer = transaction { order.customerProfile }
//
//            PressAgent.VendorStrings.orderPaid(
//                orderCode,
//            ).let { sessionContext.interruptByRole(it, Rloe.VENDOR) }
//
//            thisCustomer.notify(
//                customerMsg = PressAgent.VendorStrings.orderPaid(orderCode)
//            )
//            notifyAdmins(PressAgent.VendorStrings.orderPaid(orderCode))
//        }
//    }

}
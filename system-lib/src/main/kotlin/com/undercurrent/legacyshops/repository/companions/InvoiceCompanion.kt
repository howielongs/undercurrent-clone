package com.undercurrent.legacyshops.repository.companions

import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.legacy.data_transfer_objects.currency.ReceiptValuesFiat
import com.undercurrent.legacy.repository.entities.payments.LegacyExchangeRates
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.status.InvoiceStatus
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoices
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.tx
import java.math.BigDecimal
import java.math.RoundingMode

open class InvoiceCompanion : RootEntityCompanion0<Invoice>(Invoices) {

    fun fetchAwaitingPayments(
        status: InvoiceStatus = InvoiceStatus.AWAITING_CUSTOMER_PAYMENT
    ): List<Invoice> {
        return fetchInvoicesByStatus(status)
    }

    fun save(
        cryptoTypeIn: CryptoType,
        receiptValues: ReceiptValuesFiat,
        receiptText: String? = null,
        storefrontIn: Storefront,
    ): Invoice? {
        val subtotalIn = receiptValues.subtotal
        val feesIn = receiptValues.fees
        val totalIn = receiptValues.total

        val thisExchangeRate = LegacyExchangeRates.Table.save(cryptoTypeIn) ?: return null
        val savedFees = thisExchangeRate.newCrypto(feesIn) ?: return null

        return tx {
            val splitFees = feesIn.amount
                .multiply(Invoices.ADMIN_PAYOUT_FUDGE_FACTOR)
                .divide(BigDecimal(2), 2, RoundingMode.HALF_UP)

            val savedSplitFees = thisExchangeRate.newCrypto(FiatAmount(splitFees)) ?: return@tx null

            val thisSubtotal = thisExchangeRate.newCrypto(subtotalIn) ?: return@tx null
            val thisTotal = thisExchangeRate.newCrypto(totalIn) ?: return@tx null

            new {
                exchangeRate = thisExchangeRate
                subtotalCrypto = thisSubtotal
                feesCrypto = savedFees
                totalCrypto = thisTotal
                splitFeesPerAdminAmount = savedSplitFees
                raw = thisExchangeRate.cryptoType
                receipt = receiptText ?: ""
                feePercentDecimal = storefrontIn.feePercentDecimal().toString()
            }
        }

    }


    private fun fetchInvoicesByStatus(status: InvoiceStatus): List<Invoice> {
        return tx {
            find {
                Invoices.status eq status.name
            }.toList()
        }.filter { it.isNotExpired() }
    }


}
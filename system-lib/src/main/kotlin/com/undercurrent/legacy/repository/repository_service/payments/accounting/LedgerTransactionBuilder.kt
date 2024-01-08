package com.undercurrent.legacy.repository.repository_service.payments.accounting


import com.undercurrent.legacy.repository.entities.payments.LegacyExchangeRates
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import java.math.BigDecimal

class LedgerTransactionBuilder(
    val currencyInterface: CurrencyLegacyInterface,
    val invoice: Invoice,
    private val feeSplitAmt: BigDecimal,
    private val totalFudgeAmt: BigDecimal,
    private val subtotalAmt: BigDecimal,
    val thisVendor: ShopVendor,
    private val thisCustomer: ShopCustomer,
    private val exchangeRateFromUsd: LegacyExchangeRates.Entity? = null,
    private val startStatus: LedgerEntryStatus = LedgerEntryStatus.PENDING,
) {


    private fun saveNewEntry(
        userIn: User,
        roleIn: AppRole,
        amountIn: BigDecimal,
        memoIn: TransactionMemo,
        statusIn: LedgerEntryStatus = startStatus,
    ): LedgerTransactionBuilder {
        tx {
            UserCreditLedger.save(
                userIn = userIn,
                roleIn = roleIn,
                invoiceIn = invoice,
                amountIn = amountIn,
                currencyInterfaceIn = currencyInterface,
                memoIn = memoIn.name,
                statusIn = statusIn,
                exchangeRateFromUsdIn = exchangeRateFromUsd
            )
        }
        return this
    }

    suspend fun addAdmins(
        adminUser1: User,
        adminUser2: User
    ): LedgerTransactionBuilder {
        addAdmin(adminUser1, TransactionMemo.TO_ADMIN1)
        return addAdmin(adminUser2, TransactionMemo.TO_ADMIN2)
    }


    suspend fun addVendor(): LedgerTransactionBuilder {
        Log.debug("Adding vendor to Ledger for $subtotalAmt")
        return saveNewEntry(
            userIn = tx { thisVendor.user },
            roleIn = ShopRole.VENDOR,
            amountIn = subtotalAmt,
            memoIn = TransactionMemo.TO_VENDOR,
        )

    }

    suspend fun addCustomer(): LedgerTransactionBuilder {
        Log.debug("Adding customer to Ledger for $subtotalAmt")
        return saveNewEntry(
            userIn = tx { thisCustomer.user },
            roleIn = ShopRole.CUSTOMER,
            amountIn = totalFudgeAmt.multiply(BigDecimal(-1)),
            memoIn = TransactionMemo.FROM_CUSTOMER,
            statusIn = LedgerEntryStatus.AWAITING,
        )
    }

    private suspend fun addAdmin(
        userIn: User, memo: TransactionMemo
    ): LedgerTransactionBuilder {
        return saveNewEntry(
            userIn = userIn,
            roleIn = ShopRole.ADMIN,
            amountIn = feeSplitAmt,
            memoIn = memo,
        )
    }
}
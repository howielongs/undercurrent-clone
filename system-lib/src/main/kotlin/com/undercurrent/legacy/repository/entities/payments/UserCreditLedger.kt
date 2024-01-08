package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.legacy.repository.abstractions.BaseEventsEntity
import com.undercurrent.legacy.repository.abstractions.BaseEventsTable
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoice
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoices
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.legacy.types.enums.CreditOrDebit
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.TransactionMemo
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.EntityHasStatusField
import com.undercurrent.shared.repository.dinosaurs.entityHasStatus
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.RoleTransformers.fromAbbrev
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

object UserCreditLedger {
    object Table : BaseEventsTable("accounting_user_credit_ledger") {
        val user = reference("user_id", Users)

        val exchangeRateToUsd = optReference("exchange_rate_id", LegacyExchangeRates.Table)
        val invoice = optReference("invoice_id", Invoices)

        val role = varchar("role", VARCHAR_SIZE).nullable()

        val amount = varchar("amount", VARCHAR_SIZE)
        val currencyType = varchar("currency_type", VARCHAR_SIZE)

        var verifiedTime = datetime("verified_date").nullable()

    }

    class Entity(id: EntityID<Int>) : BaseEventsEntity(id, Table), EntityHasStatusField {
        companion object : RootEntityCompanion0<Entity>(Table)

        var user by User referencedOn Table.user
        var exchangeRateFromUsd by LegacyExchangeRates.Entity optionalReferencedOn Table.exchangeRateToUsd
        var invoice by Invoice optionalReferencedOn Table.invoice

        var role by Table.role
        var amount by Table.amount
        var currencyType by Table.currencyType

        var verifiedTime by Table.verifiedTime.transform(
            { LocalDateTime.parse(it) },
            { it?.let { it1 -> UtilLegacy.formatDbDatetime(it1) } })

        var roleEnum: AppRole = ShopRole.CUSTOMER
            get() {
                //todo this could be a candidate for in/out transform on db
                return transaction { fromAbbrev(role.toString()) } ?: ShopRole.CUSTOMER
            }

        override fun hasStatus(status: String): Boolean {
            return entityHasStatus(status)
        }

        fun send(type: String, address: String, dbusProps: RoutingProps): Boolean {
            return when (type) {
                CryptoType.BTC.name -> sendBtc(address, dbusProps)
//                CryptoType.MOB.name -> sendMob(address)
                else -> false
            }
        }

        private fun sendBtc(
            address: String,
            dbusProps: RoutingProps
        ): Boolean {
            return BitcoinWalletServices.tryToSend(
                destAddressString = address,
                amountBtc = transaction { amount.replace("-", "") },
                user = transaction { user },
                ledgerEntry = this,
                memo = transaction { memo },
                dbusProps)
        }
    }

    fun saveReceivedAmount(
            recAddr: DepositCryptoAddress,
            amountIn: String,
    ): Entity? {
        return transaction { recAddr.user }?.let { userIn ->
            save(
                userIn = userIn,
                roleIn = ShopRole.CUSTOMER,
                invoiceIn = null,
                amountIn = BigDecimal(amountIn),
                currencyInterfaceIn = transaction { recAddr.cryptoType } ?: CryptoType.BTC,
                statusIn = LedgerEntryStatus.RECEIVED,
                memoIn = TransactionMemo.FROM_CUSTOMER.name,
            )
        } ?: null
    }

    fun save(
        userIn: User,
        roleIn: AppRole,
        invoiceIn: Invoice? = null,
        amountIn: BigDecimal,
        currencyInterfaceIn: CurrencyLegacyInterface,
        statusIn: LedgerEntryStatus = LedgerEntryStatus.AWAITING,
        memoIn: String = "",
        exchangeRateFromUsdIn: LegacyExchangeRates.Entity? = null
    ): Entity? {
        val creditOrDebit = if (amountIn > BigDecimal(0)) {
            CreditOrDebit.CREDIT
        } else {
            CreditOrDebit.DEBIT
        }

        // create exchange rate here -> instantaneous
        val rateToSave =
            exchangeRateFromUsdIn ?: transaction {
                invoiceIn?.exchangeRate
            } ?: if (currencyInterfaceIn is CryptoType) {
                LegacyExchangeRates.Table.save(
                    cryptoTypeIn = currencyInterfaceIn,
                )
            } else {
                null
            }


        val newEntity = transaction {
            Entity.new {
                user = userIn
                role = roleIn.name
                invoice = invoiceIn
                amount = amountIn.toString()
                currencyType = currencyInterfaceIn.abbrev()
                status = statusIn.name
                type = creditOrDebit.name
                memo = memoIn
                exchangeRateFromUsd = rateToSave
            }
        }

        return newEntity
    }
}

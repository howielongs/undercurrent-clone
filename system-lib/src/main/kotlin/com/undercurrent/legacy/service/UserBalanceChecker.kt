package com.undercurrent.legacy.service

import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.formatters.UserToIdString

import com.undercurrent.shared.utils.Log
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
interface CheckableBalances {
    fun balances(): java.util.HashMap<CurrencyLegacyInterface, BigDecimal>
    fun checkBalance(currencyInterface: CurrencyLegacyInterface = CryptoType.BTC): BigDecimal
}
class UserBalanceChecker(val user: User) : CheckableBalances {
    override fun balances(): HashMap<CurrencyLegacyInterface, BigDecimal> {
        val balancesMap = HashMap<CurrencyLegacyInterface, BigDecimal>()
        CryptoType.values().forEach {
            balancesMap[it] = checkBalance(it)
        }
        FiatType.values().forEach {
            balancesMap[it] = checkBalance(it)
        }
        return balancesMap
    }

    override fun checkBalance(currencyInterface: CurrencyLegacyInterface): BigDecimal {
        val entries = transaction {
            UserCreditLedger.Entity.find { UserCreditLedger.Table.user eq user.id }
                .filter { it.isNotExpired() && it.status != LedgerEntryStatus.PENDING.name } //todo figure out better filtering by status
        }

        val sum = entries.filter {
            it.currencyType == currencyInterface.abbrev()
        }.sumOf { BigDecimal(it.amount) }

        "${UserToIdString.toIdStr(user)} balance (${currencyInterface.abbrev()}): $sum".let { Log.debug(it) }

        return UtilLegacy.round(sum, currencyInterface.roundingScale)

    }
}
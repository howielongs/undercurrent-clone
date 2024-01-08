package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.utils.CryptoConverter
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

@Deprecated("Switch to version in swap-service")
object CryptoAmountsLegacy : ExposedTableWithStatus2("crypto_amounts") {

    val exchangeRate = reference("exchange_rate_id", LegacyExchangeRates.Table)
    val cryptoAtomicAmount = varchar("crypto_atomic_amount", VARCHAR_SIZE).default("0")
    val fiatAmount = varchar("fiat_amount", VARCHAR_SIZE).default("0")

    

    //todo clean up currency type wrappers and add easier exchange
//    fun save(
//        cryptoAmountIn: BigDecimal,
//        type: CryptoType = CryptoType.BTC,
//    ) : CryptoAmount? {
//
//    }


    fun save(
        amountFiatIn: FiatAmount,
        fiatToCryptoExchangeRateIn: BigDecimal,
        cryptoTypeIn: CryptoType

    ): CryptoAmountLegacy? {
        LegacyExchangeRates.Table.save(
                cryptoTypeIn, fiatToCryptoXchangeRateIn = fiatToCryptoExchangeRateIn
        )?.let { newRate ->
            return newRate.newCrypto(amountFiatIn)
        }
        return null
    }

    fun save(
        exchangeRateIn: LegacyExchangeRates.Entity,
        fiatAmountIn: FiatAmount
    ): CryptoAmountLegacy? {
        return transaction {
            exchangeRateIn?.let {
                it.cryptoTypeEnum?.let { cryptoType ->
                    val converter =
                            CryptoConverter(BigDecimal(it.fiatToCryptoAtomicExchangeRate), cryptoType = cryptoType)

                    CryptoAmountLegacy.new {
                        cryptoAtomicAmount = converter.toAtomic(fiatAmountIn.amount.toString()).toString()
                        fiatAmount = fiatAmountIn.formatted()
                        exchangeRate = it
                    }
                }
            }
        }
    }

}
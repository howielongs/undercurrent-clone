package com.undercurrent.legacy.utils

import com.undercurrent.shared.formatters.formatPretty
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.FiatType
import java.math.BigDecimal
import java.math.RoundingMode

class CryptoConverter(
    private val fiatToCryptoExchangeRate: BigDecimal = BigDecimal(1),
    val cryptoType: CryptoType,
    private val fiatType: FiatType = FiatType.USD,
) {
    fun toAtomic(amountFiat: String): BigDecimal {
        return macroToAtomic(fiatToCrypto(amountFiat, fiatToCryptoExchangeRate))
    }

    // move these away from being static methods?
    private fun fiatToCrypto(
        fiatAmount: String,
        exchangeRate: BigDecimal,
        cryptoType: CryptoType = CryptoType.BTC,
    ): BigDecimal {
        return fiatToCrypto(BigDecimal(fiatAmount), exchangeRate, cryptoType)
    }

    private fun fiatToCrypto(
        fiatAmount: BigDecimal,
        exchangeRate: BigDecimal,
        cryptoType: CryptoType = CryptoType.BTC,
    ): BigDecimal {
        // unsure if the 9 for scale works for other payments types
        //why is btc set to 9 and not 8? Does MOB then need 13 rather than 12?
        return fiatAmount.divide(exchangeRate, cryptoType.atomicOffset + 1, RoundingMode.HALF_UP)
    }


    private fun toAtomicStringWithLabel(amountFiat: String): String {
        return "${UtilLegacy.toAtomicFormat(toAtomic(amountFiat))} ${cryptoType.atomicUnitAbbrev}"
    }

    @Deprecated("Pull into newer formatter class (esp with currency labels/symbols)")
    private fun toAtomicAndFiatString(amountFiat: String): String {
        return "${toAtomicStringWithLabel(amountFiat)} ($${formatPretty(amountFiat)} ${fiatType.name})"
    }


    private fun macroToAtomic(
        amountMacro: BigDecimal,
        cryptoType: CryptoType = CryptoType.BTC
    ): BigDecimal {
        return cryptoType.toAtomic(amountMacro)
    }


    fun toString(amountFiat: String): String {
        return toAtomicAndFiatString(amountFiat)
    }
}
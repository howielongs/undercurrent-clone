package com.undercurrent.legacyswaps.types

import com.undercurrent.shared.formatters.TextFormatter
import com.undercurrent.shared.formatters.formatPretty
import com.undercurrent.shared.abstractions.swaps.SwappableCurrencyEnum
import java.math.BigDecimal

sealed interface SwapValueFormatter<T : SwappableCurrencyEnum> : TextFormatter<BigDecimal> {

    abstract class BaseFormatter<T : SwappableCurrencyEnum>(val currencyType: T) : SwapValueFormatter<T>

    class Default<T : SwappableCurrencyEnum>(currencyType: T) : BaseFormatter<T>(currencyType) {
        override fun format(data: BigDecimal): String {
            return "${formatPretty(data)} ${currencyType.abbrev}"
        }
    }

    class FiatPrependSymbol<T : SwappableFiat>(currencyType: T) : BaseFormatter<T>(currencyType) {
        override fun format(data: BigDecimal): String {
            return "${currencyType.symbol} $data"
        }
    }
}

fun <T : SwappableCurrencyEnum> defaultSwapCurrencyFormat(text: BigDecimal, currencyType: T): String {
    return SwapValueFormatter.Default(currencyType).format(text)
}
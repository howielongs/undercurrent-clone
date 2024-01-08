package com.undercurrent.legacyswaps.types

import com.undercurrent.shared.types.CurrencyAmount
import com.undercurrent.shared.abstractions.swaps.SwappableCurrencyEnum
import java.math.BigDecimal


data class SwapAmount<T : SwappableCurrencyEnum>(
    override val amount: BigDecimal,
    override val currency: T,
    override val formatter: (BigDecimal, T) -> String = ::defaultSwapCurrencyFormat
): CurrencyAmount<T> {
    override fun toPretty(): String {
        return formatter(amount, currency)
    }

    override fun toString(): String {
        return toPretty()
    }
}
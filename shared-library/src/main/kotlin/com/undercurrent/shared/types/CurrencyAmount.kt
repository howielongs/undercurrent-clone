package com.undercurrent.shared.types

import java.math.BigDecimal

interface CurrencyAmount<T : CurrencyEnum>: CanDisplayPrettyCurrency {
    val amount: BigDecimal
    val currency: T
    val formatter: (BigDecimal, T) -> String

    override fun toPretty(): String {
        return formatter(amount, currency)
    }
}
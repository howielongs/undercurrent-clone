package com.undercurrent.shared.abstractions.swaps

import com.undercurrent.shared.types.CurrencyEnum


interface SupportedSwapCurrencyEnum : CurrencyEnum
interface SwappableCurrencyEnum : SupportedSwapCurrencyEnum, CurrencyEnum


//data class SwapAmount<T : SupportedSwapCurrency>(
//    val amount: BigDecimal,
//    val currency: T,
//    val formatter: (BigDecimal, T) -> String = ::defaultSwapCurrencyFormat
//) {
//    override fun toString(): String {
//        return formatter(amount, currency)
//    }
//}
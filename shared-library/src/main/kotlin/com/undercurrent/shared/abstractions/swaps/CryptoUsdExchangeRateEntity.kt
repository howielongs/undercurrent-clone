package com.undercurrent.shared.abstractions.swaps

import java.math.BigDecimal

interface CryptoUsdExchangeRateEntity {
    var rate: BigDecimal
//    var currencyType: T
}

interface CurrencySwapEntity {
    var amount: String
    var currency: String
}

interface CryptoAmountEntity {
    var amount: BigDecimal
//    var exchangeRate: E
}


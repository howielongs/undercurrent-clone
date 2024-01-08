package com.undercurrent.system.payments.types

import java.math.BigDecimal

interface UsableCurrency {

}

interface ExchangeableCurrency {
    fun fetchExchangeRate(url: String): BigDecimal
}


interface CryptoCurrency : ExchangeableCurrency {
    val fullName: String?
    val symbol: String
}


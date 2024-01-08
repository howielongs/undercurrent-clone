package com.undercurrent.legacy.data_transfer_objects.currency

import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.shared.formatters.formatPretty
import java.math.BigDecimal

//todo include things like symbols and exchange rates
class FiatAmount(
    val amount: BigDecimal,
    val type: FiatType = FiatType.USD,
) {
    constructor(amountStr: String) : this(BigDecimal(amountStr))
    constructor(amountInt: Int) : this(BigDecimal(amountInt))

//    fun saveCryptoAmount(): CryptoAmount? {
//
//
//    }

    fun formatted(): String {
        return formatPretty(amount, hasCommas = false)
    }

    fun pretty(withSymbol: Boolean = false, fiatType: String = FiatType.USD.toString()): String {
        return prettyFiat(withSymbol, FiatType.USD.abbrevToType(fiatType) ?: FiatType.USD)
    }


    fun prettyFiat(withSymbol: Boolean = false, fiatType: FiatType = FiatType.USD): String {
        return "${if (withSymbol) "${fiatType.symbol}" else ""}" + formatPretty(amount)
    }

    override fun toString(): String {
        return formatted()
    }

    //todo add some tests around this (thx!)
    fun addSafely(n: FiatAmount?): FiatAmount? {
        if (n == null) {
            return null
        }
        var newFiat = FiatAmount(amount)
        return newFiat.add(n)
    }

    fun add(n: FiatAmount): FiatAmount {
        return FiatAmount(amount.add(n.amount))
    }

    fun add(n: String): FiatAmount {
        return FiatAmount(amount.add(BigDecimal(n)))
    }

    fun add(n: BigDecimal): FiatAmount {
        return FiatAmount(amount.add(n))
    }

    fun multiply(n: String): FiatAmount {
        return FiatAmount(amount.multiply(BigDecimal(n)))
    }

    fun multiply(n: BigDecimal): FiatAmount {
        return FiatAmount(amount.multiply(n))
    }

    fun multiply(n: FiatAmount): FiatAmount {
        return FiatAmount(amount.multiply(n.amount))
    }
}
package com.undercurrent.shared.formatters

import java.math.BigDecimal
import java.text.DecimalFormat

sealed class CurrencyFormatter<T : Any> : TextFormatter<T> {
    open class Base(private val pattern: String) : CurrencyFormatter<BigDecimal>() {
        override fun format(data: BigDecimal): String {
            return DecimalFormat(pattern).format(data)
        }
    }
}


// perhaps put this in own class, or under CurrencyFormatter
fun formatPretty(data: String): String {
    return formatPretty(BigDecimal(data), 2, true)
}

fun formatPretty(data: BigDecimal): String {
    return formatPretty(data, 2, true)
}

fun formatPretty(data: BigDecimal, numDecimals: Int = 2, hasCommas: Boolean = true): String {
    val decimalPart = if (numDecimals > 0) "." + "0".repeat(numDecimals) else ""
    val pattern = if (hasCommas) {
        "#,##0$decimalPart"
    } else {
        "####0$decimalPart"
    }
    return DecimalFormat(pattern).format(data)
}


//todo make use of this for rounding and crypto atomic values
//todo add rounding and precision formatting here as well
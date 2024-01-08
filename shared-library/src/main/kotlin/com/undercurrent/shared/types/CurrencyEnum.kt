package com.undercurrent.shared.types

interface CurrencyEnum: CanDisplayPrettyCurrency {
    val label: String
    val abbrev: String
    val symbol: String?

    override fun toPretty(): String
}
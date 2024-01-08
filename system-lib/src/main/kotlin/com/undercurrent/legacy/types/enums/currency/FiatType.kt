package com.undercurrent.legacy.types.enums.currency

import com.undercurrent.legacy.types.enums.ResponseType

enum class FiatType(
    override val fullName: String? = null,
    override val symbol: String = "$",
    override val validationType: ResponseType = ResponseType.CURRENCY,
    override val priority: Int? = null,
    override val roundingScale: Int = 2,
    override val canCashOut: Boolean = false,
    override val isSwappable: Boolean = false,

    ) : CurrencyLegacyInterface {
    USD(
        symbol = "$",
    ),
    //todo rework this with PaymentService vs PaymentMethod vs CurrencyType
    EUR(
        symbol = "€"
    ),
    GBP(
        symbol = "£"
    ),
    JPY(
        symbol = "¥"
    ),

    ;

    override fun abbrev(): String {
        return this.name.uppercase()
    }

    override fun selectableLineString(): String {
        return "${this.fullName} (${abbrev()})"
    }

    fun abbrevToType(abbrev: String): FiatType? {
        FiatType.values().forEach {
            if (abbrev.uppercase() == it.name.uppercase()) {
                return it
            }
        }
        return null
    }
}
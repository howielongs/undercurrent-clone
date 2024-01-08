package com.undercurrent.legacy.types.enums.currency

import com.undercurrent.legacy.types.enums.ResponseType


@Deprecated("Create newer version of this interface")
interface CurrencyLegacyInterface {
    val fullName: String?
    val symbol: String
    val validationType: ResponseType
    val priority: Int?
    val roundingScale: Int
    val canCashOut: Boolean
    val isSwappable: Boolean
    //todo impl compare to USD exchange rate for all currencies

    fun abbrev(): String
    fun selectableLineString(): String

}
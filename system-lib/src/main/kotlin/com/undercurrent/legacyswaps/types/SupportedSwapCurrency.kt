package com.undercurrent.legacyswaps.types

import com.undercurrent.shared.abstractions.swaps.SupportedSwapCurrencyEnum
import com.undercurrent.shared.abstractions.swaps.SwappableCurrencyEnum
import com.undercurrent.shared.formatters.formatPretty
import java.math.BigDecimal

/**
 *     enum class ExchangeRateSource {
 *         BINANCE, COINBASE, GATEAPI
 *     }
 *
 *     val exchangeSource = ExchangeRateSource.GATEAPI
 *
 *     val mobUrl = "https://api.binance.com/api/v3/avgPrice?symbol=MOBUSDT"
 *
 *     val btcBinanceUrl = "https://api.binance.com/api/v3/avgPrice?symbol=BTCUSDT"
 *     val btcCoinbaseUrl = "https://api.coinbase.com/v2/exchange-rates?currency=BTC"
 *
 *     val mobGateApiUrl = "https://data.gateapi.io/api2/1/ticker/mob_usdt"
 *     val btcGateApiUrl = "https://data.gateapi.io/api2/1/ticker/btc_usdt"
 *
 */

sealed interface SupportedSwapCurrency : SupportedSwapCurrencyEnum {
    override val label: String
    override val abbrev: String
}


enum class SwappableCrypto(
    override val label: String,
    override val abbrev: String,
    override val symbol: String? = null,
    val exchangeRateUrl: String? = null,
) : SwappableCurrencyEnum {
    BTC("Bitcoin", "BTC"),
    MOB("MobileCoin", "MOB")

    ;

    override fun toPretty(): String {
        return "$label ($abbrev)"
    }

    override fun toString(): String {
        return toPretty()
    }
}

enum class SwappableFiat(
    override val label: String,
    override val abbrev: String,
    override val symbol: String,
    val formatter: (BigDecimal) -> String = ::formatPretty
) : SwappableCurrencyEnum, SupportedSwapCurrency {
    USD(
        label = "United States Dollar",
        abbrev = USD.name,
        symbol = "$"
    )
    ;

    override fun toPretty(): String {
        return "$label ($abbrev)"
    }

    override fun toString(): String {
        return toPretty()
    }

}
package com.undercurrent.legacy.types.enums

import com.undercurrent.system.payments.types.ExchangeRateSource
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.enums.currency.PaymentMethod
import com.undercurrent.shared.utils.Log
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL

enum class CryptoType(
    override val fullName: String? = null,
    val atomicUnitAbbrev: String = "",
    val atomicUnitName: String = "",
    val atomicOffset: Int = 0,
    override val validationType: ResponseType,
    val exchangeRateUrl: String? = null,
    val tickerSymbol: String? = null,
    val testExchangeRate: BigDecimal,
    override val priority: Int? = null,
    override val symbol: String = "",
    override val roundingScale: Int = 6,
    override val canCashOut: Boolean = true,
    override val isSwappable: Boolean = true,
) : CurrencyLegacyInterface, PaymentMethod {
    BTC(
        "Bitcoin",
        "sat",
        "satoshi",
        atomicOffset = 8,
        roundingScale = 6,
        validationType = ResponseType.BTC_ADDRESS,
        exchangeRateUrl = "https://data.gateapi.io/api2/1/ticker/btc_usdt",
        tickerSymbol = "BTCUSDT",
        testExchangeRate = BigDecimal("42210.71106131"),
        priority = 1,
        symbol = "BTC",
    ),
    MOB(
        "MobileCoin",
        atomicUnitAbbrev = "pMOB",
        atomicUnitName = "pico MOB",
        atomicOffset = 12,
        validationType = ResponseType.MOB_ADDRESS,
        exchangeRateUrl = "https://data.gateapi.io/api2/1/ticker/mob_usdt",
        tickerSymbol = "MOBUSDT",
        testExchangeRate = BigDecimal("0.85346684"),
        priority = 2,
        symbol = "MOB",
    ),
    STRIPE(
        "Stripe",
        atomicUnitAbbrev = "USD",
        atomicOffset = 2,
        validationType = ResponseType.STRIPE_SECRET_TEST_KEY,
        testExchangeRate = BigDecimal("1"),
        priority = 3,
        symbol = "USD",
        roundingScale = 2,
        canCashOut = false,
        isSwappable = false,
    ), ;

    fun getFiatToCryptoExchangeRate(
        bypassTestMode: Boolean = false,
        exchangeSource: ExchangeRateSource = ExchangeRateSource.GATEAPI
    ): BigDecimal? {
        if (RunConfig.isTestMode && !bypassTestMode) {
            Log.debug("Fetching test exchange rate for $name")
            return testExchangeRate
        }

        return try {
            Log.debug("Fetching current $name price ($tickerSymbol) from $exchangeRateUrl")

            if (this@CryptoType == STRIPE) {
                return BigDecimal(1)
            }

            return when (exchangeSource) {
                ExchangeRateSource.BINANCE -> {

                    URL(exchangeRateUrl).readText().let { response ->
                        JSONObject(response).get("price").toString().let { priceValue ->
                            BigDecimal(priceValue).let {
                                Log.debug("Fetched current $name price: $it")
                                return it
                            }
                        }
                    }
                }

                ExchangeRateSource.COINBASE -> {
                    val btcCoinbaseUrl = "https://api.coinbase.com/v2/exchange-rates?currency=BTC"

                    try {
                        Log.debug("Fetching current btc price...")
                        val response = URL(btcCoinbaseUrl).readText()
                        val priceValue =
                            ((JSONObject(response).get("data") as JSONObject).get("rates") as JSONObject).get("USD")
                                .toString()
                        var cryptoExchangeRate = BigDecimal(priceValue)
                        Log.debug("Fetched current btc price: $cryptoExchangeRate")
                        cryptoExchangeRate
                    } catch (e: Exception) {
                        Log.error("Error retrieving current BTC pricing", e)
                        null
                    }
                }

                ExchangeRateSource.GATEAPI -> {
                    URL(exchangeRateUrl).readText().let { response ->
                        JSONObject(response).get("last").toString().let { priceValue ->
                            BigDecimal(priceValue).let {
                                Log.debug("Fetched current $name price: $it")
                                return it
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.error("Error retrieving current $name pricing from $exchangeRateUrl", e)
            null
        }
    }

//    fun toUsd(): BigDecimal {
//
//    }

    fun toMacro(atomicAmount: String, rounded: Boolean = false): BigDecimal {
        return toMacro(BigDecimal(atomicAmount), rounded)
    }

    //todo unsure how this rounding should look across currencies
    fun toMacro(atomicAmount: BigDecimal, rounded: Boolean = false): BigDecimal {
        var roundMode = RoundingMode.HALF_UP
        val scale = if (rounded) {
            roundMode = RoundingMode.UP
            6
        } else {
            atomicOffset + 1
        }

        //todo check that this rounding operation does what you expect
        return atomicAmount.scaleByPowerOfTen(atomicOffset * -1).divide(BigDecimal(1), scale, roundMode)
    }

    //todo unsure if this rounds values
    fun toAtomic(cryptoAmount: BigDecimal): BigDecimal {
        return cryptoAmount.scaleByPowerOfTen(atomicOffset)
    }

    override fun abbrev(): String {
        return this.name.uppercase()
    }

    fun lineString(fullNameInParens: Boolean = true): String {
        return if (fullNameInParens) {
            addCryptoLineString()
        } else {
            selectableLineString()
        }
    }

    //todo have version with USD/fiat displayed on line
    override fun selectableLineString(): String {
        return "${this.fullName} (${abbrev()})"
    }

    private fun addCryptoLineString(): String {
        return "${abbrev()} (${fullName})"
    }


    fun abbrevToType(abbrev: String): CryptoType? {
        for (type in CryptoType.values()) {
            if (abbrev.uppercase() == type.abbrev() ||
                abbrev.uppercase() == type.atomicUnitAbbrev.uppercase()
            ) {
                return type
            }
        }
        return null
    }

}
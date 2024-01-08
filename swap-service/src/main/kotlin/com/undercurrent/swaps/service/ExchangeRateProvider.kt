package com.undercurrent.swaps.service

import com.undercurrent.shared.formatters.formatPretty
import com.undercurrent.legacyswaps.types.SupportedSwapCurrency
import com.undercurrent.legacyswaps.types.SwappableFiat
import java.math.BigDecimal

interface ExchangeRateProvider {

    //todo will have to implement relative to USD (so multiple pulls, and then division)
    fun fetchExchangeRate(
        source: SupportedSwapCurrency,
        target: SupportedSwapCurrency,
    ): BigDecimal

    fun displayExchangeRate(
        source: SupportedSwapCurrency,
        target: SupportedSwapCurrency,
        reference: SwappableFiat,
    ): String
}

/**
 * e.g: "1 BTC = 48,465 MOB ($26,298.20 USD)"
 * when:
 *  source = BTC
 *  target = MOB
 *  reference = USD
 */
abstract class BaseExchangeRateProvider(
    val source: SupportedSwapCurrency,
    val target: SupportedSwapCurrency,
    val reference: SwappableFiat = SwappableFiat.USD,
    val formatter: (BigDecimal) -> String = ::formatPretty,
) : ExchangeRateProvider {

    abstract override fun fetchExchangeRate(
        source: SupportedSwapCurrency,
        reference: SupportedSwapCurrency,
    ): BigDecimal

    override fun displayExchangeRate(
        source: SupportedSwapCurrency,
        target: SupportedSwapCurrency,
        reference: SwappableFiat
    ): String {
        val rate1 = formatter(fetchExchangeRate(source, target))
        val rate2 = formatter(fetchExchangeRate(source, reference))

        //todo will have to apply currencyFormatter here
        return "1 ${source.abbrev} = $rate1 ${target.abbrev} " +
                "(${reference.symbol}$rate2 ${reference.abbrev})"
    }
}

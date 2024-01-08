package com.undercurrent.legacy.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.format.DateTimeFormatter

class TimeAndDateProvider {

    companion object {
        fun getInstantNow(): String? {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        }

        private fun valueGreaterThan(value: BigDecimal, value2: BigDecimal): Boolean {
            return value.subtract(value2) > BigDecimal("0")
        }

        fun valueLessThan(value: BigDecimal, value2: BigDecimal): Boolean {
            return value.subtract(value2) < BigDecimal("0")
        }

        private fun valueGreaterThan(value: BigDecimal, comparison: Int): Boolean {
            return valueGreaterThan(value, BigDecimal(comparison))
        }

        private fun valueLessThan(value: BigDecimal, comparison: Int): Boolean {
            return valueLessThan(value, BigDecimal(comparison))
        }

        private fun valueInRange(value: BigDecimal, low: Int, high: Int): Boolean {
            return valueGreaterThan(value, low) && valueLessThan(value, high)
        }

        fun getTimeAgoString(
            lastActiveNanoSec: Long?,
            prependValue: String = "Last active: ",
        ): String {
            lastActiveNanoSec ?: run {
                return ""
            }

            BigDecimal(lastActiveNanoSec.toString())
                .divide(BigDecimal(1000000000), 2, RoundingMode.HALF_UP).let {
                    return if (valueLessThan(it, 1)) {
                        "${prependValue}Just now"
                    } else if (valueLessThan(it, 60)) {
                        "$prependValue$it seconds ago"
                    } else if (valueInRange(it, 60, 3600)) {
                        "$prependValue${it.divide(BigDecimal(60), 2, RoundingMode.HALF_UP)} " +
                                "minutes ago"
                    } else if (valueInRange(it, 3600, 86400)) {
                        "$prependValue${it.divide(BigDecimal(3600), 2, RoundingMode.HALF_UP)} " +
                                "hours ago"
                    } else {
                        "$prependValue${it.divide(BigDecimal(86400), 2, RoundingMode.HALF_UP)} " +
                                "days ago"
                    }
                }
        }

    }

}
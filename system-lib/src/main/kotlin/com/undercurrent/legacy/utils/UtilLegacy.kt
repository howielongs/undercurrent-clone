package com.undercurrent.legacy.utils

import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.utils.joincodes.RandomAbcStringGenerator
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.time.EpochNano
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.math.RoundingMode.HALF_UP
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

//todo break out into payments utils and other converters
@Deprecated("Use shared Util class")
object UtilLegacy {
    const val DIVIDER_STRING = "\n_______________________\n"

    fun roundBigDecimal(value: BigDecimal, currencyInterface: CurrencyLegacyInterface): BigDecimal {
        return value.divide(BigDecimal("1"), currencyInterface.roundingScale, RoundingMode.UP)
    }


//    fun roundBigDecimal(value: String, currencyInterface: CurrencyLegacyInterface): BigDecimal {
//        return roundBigDecimal(BigDecimal(value), currencyInterface)
//    }

//    fun feesToSplitFees(feesIn: FiatAmount): CryptoAmount {
//        transaction {
//            feesIn.amount
//                .multiply(RunConfig.ADMIN_PAYOUT_FUDGE_FACTOR)
//                .divide(BigDecimal(2), 2, RoundingMode.HALF_UP)
//        }?.let { splitFees ->
//
//            val splitFeesFiatAmount = FiatAmount(splitFees)
//            this.newCrypto(splitFeesFiatAmount)
//        }
//
//    }

//    fun getDaysAgo(localDateTime: LocalDateTime): Long {
//        return getDaysAgo(getEpoch() - localDateTime.nano)
//    }
//
//    fun getTimeDiffString(timestampNano: Long): String {
//        return getTimeAgoString(
//            getEpoch() - timestampNano,
//            prependValue = ""
//        )
//    }
//
//    fun getTimeDiffString(localDateTime: LocalDateTime): String {
//        return getTimeAgoString(
//            getEpoch() - localDateTime.nano,
//            prependValue = ""
//        )
//    }

//    fun noneAreNull(vararg items: Any?): Boolean {
//        items.forEach {
//            if (it == null) {
//                return false
//            }
//        }
//        return true
//    }


    fun getRoleEnum(roleString: String?): AppRole? {
        return try {
            roleString?.let { return ShopRole.valueOf(it) } ?: null
        } catch (e: Exception) {
            null
        }

    }

    @Deprecated("Make use of newer RandomGenerator class")
    fun generateUniqueOrderCode(): String {
        var code = RandomAbcStringGenerator(6).generate()

        while (transaction {
                DeliveryOrder.find {
                    DeliveryOrders.orderCode eq code
                }.limit(1).toList()
            }.isNotEmpty()) {
            code = RandomAbcStringGenerator(6).generate()
        }
        return code
    }


//    fun generateUniqueCode(
//        numDigits: Int,
//    ): String {
//        var numericCode = generateRandomCode(numDigits, prependDigit = 1)
//
//        while (JoinCodes.fetchByCode(numericCode) != null) {
//            Log.debug("Found another vendor storefront with code $numericCode, retrying...")
//            numericCode = generateRandomCode(numDigits, prependDigit = 1)
//        }
//        return numericCode
//    }

//    private fun generateRandomCode(numDigits: Int = 6, prependDigit: Int? = null): String {
//        var code = (prependDigit ?: "").toString()
//        for (i in 0 until numDigits) {
//            var value = Random.nextInt(until = 10)
//            code += value.toString()
//        }
//
//        return code
//    }

//    fun fudgeFactor(amount: String): String {
//        return FiatAmount(BigDecimal(amount)).multiply(BigDecimal("1.0001")).formatted()
//    }
//
//    fun pctToPretty(pct: BigDecimal): String {
//        return pct.multiply(BigDecimal(100)).divide(
//            BigDecimal(1), 2, HALF_UP
//        ).toString()
//    }

    @Deprecated("Move this to IntToChar place")
    fun getCharForNumber(i: Int): String {
        return when {
            i in 1..26 -> (i + 64).toChar().toString()
            i in 27..52 -> {
                var newChar = (i - 26 + 64).toChar().toString()
                "$newChar$newChar"
            }
            //todo You are fully capable of generalizing this further
            i > 52 -> {
                var newChar = (i - 52 + 64).toChar().toString()
                "$newChar$newChar$newChar"
            }

            else -> ""
        }
    }

    fun atomicToFiat(atomicAmount: BigDecimal, exchangeRate: BigDecimal): BigDecimal {
        return cryptoToFiat(atomicToMacro(atomicAmount), exchangeRate)
    }

    private fun cryptoToFiat(cryptoAmount: BigDecimal, exchangeRate: BigDecimal): BigDecimal {
        return cryptoAmount.multiply(exchangeRate).divide(BigDecimal(1), 2, HALF_UP)
    }

    fun fiatToCrypto(fiatAmount: String, exchangeRate: BigDecimal): BigDecimal {
        return fiatToCrypto(BigDecimal(fiatAmount), exchangeRate)
    }

    private fun fiatToCrypto(
        fiatAmount: BigDecimal,
        exchangeRate: BigDecimal
    ): BigDecimal {
        //todo unsure if the 9 for scale works for other payments types
        //why is btc set to 9 and not 8? Does MOB then need 13 rather than 12?
        return fiatAmount.divide(exchangeRate, 9, HALF_UP)
    }

    fun round(value: BigDecimal, currencyInterface: CurrencyLegacyInterface): BigDecimal {
        return value.divide(BigDecimal(1), currencyInterface.roundingScale, HALF_UP)
    }

    fun round(value: BigDecimal, scale: Int = 2): BigDecimal {
        return value.divide(BigDecimal(1), scale, HALF_UP)
    }

//    fun atomicToMacro(amountSat: String, rounded: Boolean = false): BigDecimal {
//        var roundMode = HALF_UP
//        val scale = if (rounded) {
//            roundMode = UP
//            6
//        } else {
//            9
//        }
//        return BigDecimal(amountSat).divide(BigDecimal(100000000), scale, roundMode)
//    }


    private fun atomicToMacro(amountAtomic: BigDecimal, cryptoType: CryptoType = CryptoType.BTC): BigDecimal {
        return cryptoType.toMacro(amountAtomic)
    }


    //todo Should move to validator classes
//    fun isValidYes(data: String): Boolean {
//        return validYes.contains(data.lowercase())
//    }

    @Deprecated("Add to shared-library CurrencyFormatters")
    fun toAtomicFormat(n: BigDecimal): String? {
        val df = DecimalFormat("#,###")
        return df.format(n)
    }


    @Deprecated("Use EpochNano()")
    fun getEpoch(): Long {
        return EpochNano().value
//        with(Instant.now()) {
//            return this.epochSecond * (1000000000) + this.nano
//        }
    }

    fun valueLessThan(value: BigDecimal, value2: BigDecimal): Boolean {
        return value.subtract(value2) < BigDecimal("0")
    }

    fun isoToEpochNano(dateString: String, attemptNum: Int = 0): Long {
        val retryLimit = 3
        try {
            var instant = Instant.parse(dateString)
            return instant.toEpochMilli() * 1000000 + instant.nano.toLong()
        } catch (e: DateTimeParseException) {
            Log.error("Unable to parse $dateString. Retrying...", exception = e)
            if (attemptNum >= retryLimit) {
                Admins.notifyError("Could not parse $dateString after $attemptNum retries", exception = e)
                return 0L
            }
        }
        if (attemptNum < retryLimit) {
            val retryString = if (!dateString.contains("Z")) {
                dateString + "Z"
            } else {
                dateString
            }
            return isoToEpochNano(retryString, attemptNum + 1)
        }
        //todo could potentially add more rules here
        Admins.notifyError("Could not parse $dateString after $attemptNum retries")
        return 0L
    }

//    fun toLocalDateTime(dateString: String?): LocalDateTime? {
//        dateString?.let {
//            var instant = Instant.parse(it)
//            var utcZoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC)
//            return LocalDateTime.ofInstant(instant, utcZoneId)
//        } ?: return null
//    }


//    private fun forceEpochToNano(epochString: String?): Long? {
//        epochString?.let {
//            var outEpochStr = it
//            val diff = 19 - epochString.length
//            if (diff > 15) {
//                return null
//            }
//            if (diff > 0) {
//                for (i in 0 until diff) {
//                    outEpochStr += "0"
//                }
//            }
//            return try {
//                val longEpoch = outEpochStr.toLong()
//                if (longEpoch > 0L) {
//                    return longEpoch
//                } else {
//                    return null
//                }
//            } catch (e: Exception) {
//                Log.trace("Hit weird long conversion error for $epochString")
//                null
//            }
//        } ?: return null
//    }

//    fun forceEpochToNano(epoch: Long): Long? {
//        return forceEpochToNano(epoch.toString())
//    }

    // Input: "2022-12-07 12:32:08.074"
    // Output: "Thu 22 Dec 09:02:09 PT"
    fun formatDbDatetime(dateString: String): String {
        return formatDbDatetime(LocalDateTime.parse(dateString))
    }

    // Input: "2022-12-07 12:32:08.074"
    // Output: "Thu 22 Dec 09:02:09 PT"
    fun formatDbDatetime(dateTime: LocalDateTime): String {
        var pstZoneId = ZoneId.of("America/Los_Angeles")
        var zdt = dateTime.atZone(pstZoneId)

        val formatter = DateTimeFormatter.ofPattern("EE dd MMM HH:mm:ss")
        val formatted = zdt.format(formatter)

        return "$formatted PT"
    }

//    fun formatDbDate(dateString: String): String {
//        return formatDbDate(LocalDateTime.parse(dateString))
//
//    }

    fun formatDbDate(dateTime: LocalDateTime): String {
        var pstZoneId = ZoneId.of("America/Los_Angeles")
        var zdt = dateTime.atZone(pstZoneId)

        val formatter = DateTimeFormatter.ofPattern("EE dd MMM uuuu")
        val formatted = zdt.format(formatter)

        return formatted
    }

//    fun localDateTimeToEpoch(dateTime: LocalDateTime): Int {
//        return dateTime.nano
//    }
//

    fun isoToDatetime(dateString: String, showTimezone: Boolean = true): String {
        var instant = Instant.parse(dateString)
        var pstZoneId = ZoneId.of("America/Los_Angeles")
        var zdt = instant.atZone(pstZoneId)

        val formatter = DateTimeFormatter.ofPattern("EE dd MMM HH:mm:ss")
        val formatted = zdt.format(formatter)

        return if (showTimezone) {
            "$formatted PT"
        } else {
            formatted
        }
    }

//    fun parseTimestampDiff(date1: String, date2: String): Long {
//        val stamp1 = Instant.parse(date1)
//        val stamp2 = Instant.parse(date2)
//
//        val diff = stamp1.until(stamp2, ChronoUnit.SECONDS)
//        return diff
//    }


    //todo can probably replace these calls with "apply" or "also"
    fun stripOptional(data: String): String {
        var returnString = data
        if (returnString.contains("Optional.of(")) {
            returnString = returnString.replace("Optional.of(", "")
            returnString = returnString.replace("'", "`")
            returnString = returnString.substring(0, returnString.length - 1)
            return returnString
        } else if (returnString.contains("Optional[")) {
            returnString = returnString.replace("Optional[", "")
            returnString = returnString.replace("'", "`")
            returnString = returnString.substring(0, returnString.length - 1)
            return returnString
        }
        return returnString
    }
}
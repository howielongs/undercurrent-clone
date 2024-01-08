package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.utils.UtilLegacy
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.RoundingMode


@Deprecated("Switch to version in swap-service")
class CryptoAmountLegacy(id: EntityID<Int>) : ExposedEntityWithStatus2(id, CryptoAmountsLegacy) {
    companion object : RootEntityCompanion0<CryptoAmountLegacy>(CryptoAmountsLegacy) {
        //this ensures that customers always send slightly more BTC than needed (to avoid round-off weirdness)
        const val CRYPTO_CUST_FUDGE_FACTOR = 1.001

    }

    var exchangeRate by LegacyExchangeRates.Entity referencedOn CryptoAmountsLegacy.exchangeRate

    var cryptoAtomicAmount by CryptoAmountsLegacy.cryptoAtomicAmount
    var fiatAmount by CryptoAmountsLegacy.fiatAmount

    var amount: BigDecimal = BigDecimal(0)
        get() {
            return BigDecimal(cryptoAtomicAmount)
        }


    fun toCustomerFudgeCrypto(isRounded: Boolean = false): BigDecimal {
        val startVal = if (isRounded) {
            toRoundedMacro()
        } else {
            toMacro()
        }


        return startVal.multiply(BigDecimal(CRYPTO_CUST_FUDGE_FACTOR))
    }

    fun roundedCustomerFudge(): BigDecimal {
        return toRoundedMacro().multiply(BigDecimal(CRYPTO_CUST_FUDGE_FACTOR))
            .divide(BigDecimal("1"), CryptoType.BTC.roundingScale, RoundingMode.UP)
    }


    var cryptoType: CryptoType? = null
        get() {
            return transaction { CryptoType.BTC.abbrevToType(exchangeRate.cryptoType) }
        }

    fun toRoundedMacro(
        fudgeFactor: Double = 1.0,
        scale: Int = 6,
    ): BigDecimal {
        return toMacro()
            .multiply(BigDecimal(fudgeFactor))
            .divide(BigDecimal("1"), scale, RoundingMode.UP)
    }


    // does the scale need to be adjusted depending on the type?
    fun toRoundedMacroString(
            showLabel: Boolean = false,
            fudgeFactor: Double = 1.0,
            scale: Int = 6,
            cryptoType: CryptoType? = null,
    ): String {
        var thisCryptoType = cryptoType ?: transaction { this@CryptoAmountLegacy.cryptoType }
        val labelString = if (showLabel) {
            " ${thisCryptoType?.abbrev()}"
        } else {
            ""
        }
        // will need to update scale to reflect cryptotype and context
        return "${
            toMacro()
                .multiply(BigDecimal(fudgeFactor))
                .divide(BigDecimal("1"), scale, RoundingMode.UP)
        }$labelString"
    }

    fun prettyFiat(withSymbol: Boolean = false): String {
        return transaction { FiatAmount(fiatAmount).pretty(withSymbol, exchangeRate.fiatType) }
    }

    fun toMacro(): BigDecimal {
        return transaction {
            UtilLegacy.fiatToCrypto(fiatAmount, BigDecimal(exchangeRate.fiatToCryptoAtomicExchangeRate))
        }

    }

}
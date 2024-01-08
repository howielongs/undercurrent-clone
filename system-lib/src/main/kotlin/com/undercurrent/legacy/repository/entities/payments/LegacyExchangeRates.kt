package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.legacy.data_transfer_objects.currency.FiatAmount
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal


@Deprecated("Will switch to version in swap-service")
class LegacyExchangeRates {

    object Table : ExposedTableWithStatus2("crypto_fiat_exchange_rates") {
        val cryptoType = varchar("crypto_type", VARCHAR_SIZE)
        val fiatType = varchar("fiat_type", VARCHAR_SIZE)
        val fiatToCryptoAtomicExchangeRate = varchar("fiat_to_crypto_atomic_exchange_rate", VARCHAR_SIZE)
        

        fun save(
            cryptoTypeIn: CryptoType = CryptoType.BTC,
            fiatTypeIn: FiatType = FiatType.USD,
            fiatToCryptoXchangeRateIn: BigDecimal? = null,
        ): Entity? {

            val exchangeRateIn = (fiatToCryptoXchangeRateIn ?: cryptoTypeIn.getFiatToCryptoExchangeRate()
            ?: BigDecimal(0)).toString()

            return transaction {
                Entity.new {
                    cryptoType = cryptoTypeIn.abbrev()
                    fiatType = fiatTypeIn.name
                    fiatToCryptoAtomicExchangeRate = exchangeRateIn
                }
            }
        }
    }


    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var fiatToCryptoAtomicExchangeRate by Table.fiatToCryptoAtomicExchangeRate

        var cryptoType by Table.cryptoType
        var fiatType by Table.fiatType

        var cryptoTypeEnum: CryptoType? = null
            get() {
                return CryptoType.BTC.abbrevToType(cryptoType)
            }

        fun newCrypto(amount: FiatAmount): CryptoAmountLegacy? {
            return transaction {
                CryptoAmountsLegacy.save(this@Entity, amount)
            }
        }
    }
}
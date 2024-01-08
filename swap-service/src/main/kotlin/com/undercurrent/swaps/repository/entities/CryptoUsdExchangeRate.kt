package com.undercurrent.swaps.repository.entities

import com.undercurrent.legacyswaps.types.SwappableCrypto
import com.undercurrent.shared.abstractions.swaps.CryptoUsdExchangeRateEntity
import com.undercurrent.swaps.repository.companions.CryptoUsdExchangeRateCompanion
import org.jetbrains.exposed.dao.id.EntityID

//usd_to_crypto_atomic_rate
object CryptoUsdExchangeRates : SwapBotTable("crypto_exchange_rates") {
    val rate = varchar("rate", 100)
    val currencyType = varchar("currency", 100)
}

class CryptoUsdExchangeRate(id: EntityID<Int>) : SwapBotEntity(id, CryptoUsdExchangeRates), CryptoUsdExchangeRateEntity {
     override var rate by CryptoUsdExchangeRates.rate.transform(
        toColumn = { it.toString() },
        toReal = { it.toBigDecimal() }
    )
      var currencyType: SwappableCrypto by CryptoUsdExchangeRates.currencyType.transform(
        toColumn = { it.name },
        toReal = { SwappableCrypto.valueOf(it) }
    )

    companion object : CryptoUsdExchangeRateCompanion()
}
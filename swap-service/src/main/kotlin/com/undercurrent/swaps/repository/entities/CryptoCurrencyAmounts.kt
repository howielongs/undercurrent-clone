package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.abstractions.swaps.CryptoAmountEntity
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.CryptoCurrencyAmountCompanion
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal


//will need to revise the name of the old CryptoAmounts, as the db table is too similar
object CryptoCurrencyAmounts : SwapBotTable("amounts_crypto") {
    val amount = varchar("amount", VARCHAR_SIZE)
    val exchangeRate = reference("exchange_rate_id", CryptoUsdExchangeRates)
}

class CryptoCurrencyAmount(id: EntityID<Int>) : SwapBotEntity(id, CryptoCurrencyAmounts), CryptoAmountEntity {
    override var amount: BigDecimal by CryptoCurrencyAmounts.amount.transform(
        toColumn = { it.toString() },
        toReal = { it.toBigDecimal() }
    )
     var exchangeRate by CryptoUsdExchangeRate referencedOn CryptoCurrencyAmounts.exchangeRate

    companion object : CryptoCurrencyAmountCompanion()
}
package com.undercurrent.legacy.repository.entities.swap





import com.undercurrent.legacy.repository.entities.payments.*
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.currency.FiatType
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID

object SwapTransactions {

    object Table : ExposedTableWithStatus2("swap_transactions") {

        val user = reference("user_id", Users)
        val role = varchar("role", VARCHAR_SIZE)

        val fromExchangeRate = reference("from_exchange_rate_id", LegacyExchangeRates.Table)
        val toExchangeRate = reference("to_exchange_rate_id", LegacyExchangeRates.Table)

        val fromAmount = reference("from_amount_id", CryptoAmountsLegacy)
        val toAmount = reference("to_amount_id", CryptoAmountsLegacy)

        val targetFiatAmount = varchar("target_fiat_amount", VARCHAR_SIZE).nullable()
        val fiatType = varchar("fiat_type", VARCHAR_SIZE).clientDefault { FiatType.USD.name }

        val fromAddress = reference("from_address_id", DepositCryptoAddresses)
        val toAddress = reference("to_address_id", DepositCryptoAddresses)


        
    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var user by User referencedOn Table.user
        var role by Table.role

        var fromExchangeRate by LegacyExchangeRates.Entity referencedOn Table.fromExchangeRate
        var toExchangeRate by LegacyExchangeRates.Entity referencedOn Table.toExchangeRate

        var fromAmount by CryptoAmountLegacy referencedOn Table.fromAmount
        var toAmount by CryptoAmountLegacy referencedOn Table.toAmount

        var targetFiatAmount by Table.targetFiatAmount
        var fiatType by Table.fiatType

        var fromAddress by DepositCryptoAddress referencedOn Table.fromAddress
        var toAddress by DepositCryptoAddress referencedOn Table.toAddress


    }
}

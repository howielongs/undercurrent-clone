package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.sql.transactions.transaction

// determine if specific to shop
object DepositCryptoAddresses :
    ExposedTableWithStatus2("crypto_deposit_addresses") {

    val user = reference("user_id", Users).nullable()
    val address = varchar("address", VARCHAR_SIZE)
    val type = varchar("crypto_type", VARCHAR_SIZE).default(CryptoType.BTC.name)

    

    override fun singularItem(): String {
        return "payments address"
    }

    override fun pluralItems(): String {
        return "payments addresses"
    }

    fun save(
        userIn: User,
        typeIn: CurrencyLegacyInterface,
        addressKey: String,
    ): DepositCryptoAddress? {
        return transaction {
            DepositCryptoAddress.new {
                user = userIn
                type = typeIn.abbrev()
                address = addressKey
            }
        }
    }

}
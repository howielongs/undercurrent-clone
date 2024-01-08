package com.undercurrent.legacy.repository.entities.payments


import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object CryptoAddresses : ExposedTableWithStatus2("crypto_addresses") {

    val user = reference("user_id", Users).nullable()
    val address = varchar("address", VARCHAR_SIZE)
    val type = varchar("crypto_type", VARCHAR_SIZE).default(CryptoType.BTC.name)




    override fun singularItem(): String {
        return "payments address"
    }

    override fun pluralItems(): String {
        return "payments addresses"
    }


    fun byCurrencyTypeAndUser(user: User, currencyInterface: CurrencyLegacyInterface): CryptoAddress? {
        return transaction {
            CryptoAddress.find {
                CryptoAddresses.user eq user.uid and
                        (type eq currencyInterface.abbrev())
            }.lastOrNull { it.isNotExpired() }
        }

    }

    fun fetchUnexpiredByCurrency(userIn: User, cryptoType: CryptoType): List<CryptoAddress> {
        return transaction {
            CryptoAddress.find {
                user eq userIn.id and (type eq cryptoType.name)
            }.filter { it.isNotExpired() }
        }
    }
}
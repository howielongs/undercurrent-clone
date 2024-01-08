package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class DepositCryptoAddress(id: EntityID<Int>) :
    ExposedEntityWithStatus2(id, DepositCryptoAddresses) {

    var user by User optionalReferencedOn DepositCryptoAddresses.user
    var address by DepositCryptoAddresses.address
    var type by DepositCryptoAddresses.type

    var cryptoType: CryptoType? = null
        get() = transaction { CryptoType.BTC.abbrevToType(type) }


    override fun toString(): String {
        return transaction { "$type: $address" }
    }


    companion object : RootEntityCompanion0<DepositCryptoAddress>(DepositCryptoAddresses)


    // make backReference (optional) to Invoice?

//    //most likely to run into num format issues
//    fun sumReceivedForBtcAddress(): BigDecimal? {
//        var thisAddress = transaction { address }
//
//        return try {
//            Log.debug("Gathering BTC received sum for $thisAddress")
//            val sum = transaction {
//                BtcReceivedEvents.Entity.find {
//                    BtcReceivedEvents.Table.receivingAddressStr eq thisAddress
//                }
//                    .toList()
//                    .sumOf { BigDecimal(it.amount) }
//            }
//
//            Log.debug("BTC received for $thisAddress: $sum BTC")
//            return sum
//        } catch (e: Exception) {
//            Admins.notifyError(
//                "Error summing received BTC for " +
//                        "${thisAddress}\n${e.stackTraceToString()}"
//            )
//            null
//        }
//    }
}
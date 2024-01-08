package com.undercurrent.legacy.repository.entities.payments

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.shared.abstractions.CryptoAddressEntity
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CryptoAddress(id: EntityID<Int>) : ExposedEntityWithStatus2(id, CryptoAddresses), CryptoAddressEntity {
    companion object : RootEntityCompanion0<CryptoAddress>(CryptoAddresses)

    var user by User optionalReferencedOn CryptoAddresses.user
    var address by CryptoAddresses.address
    var type: String by CryptoAddresses.type

    var cryptoType: CryptoType? = null
        get() = transaction { CryptoType.BTC.abbrevToType(type) }


    override var typeLabel: String? = null
        get() = cryptoType?.name

    override fun toString(): String {
        return transaction { "$type: $address" }
    }

}
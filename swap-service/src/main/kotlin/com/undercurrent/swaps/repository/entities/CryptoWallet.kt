package com.undercurrent.swaps.repository.entities

import com.undercurrent.legacyswaps.types.SwappableCrypto
import com.undercurrent.shared.abstractions.CryptoAddressEntity
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.CryptoWalletCompanion
import org.jetbrains.exposed.dao.id.EntityID


object CryptoWallets : SwapBotTable("crypto_wallets") {
    val swapUser = reference("swap_user_id", SwapUsers)
    val currencyType = varchar("currency_type", 10)
    val label = varchar("label", VARCHAR_SIZE)
}

class CryptoWallet(id: EntityID<Int>) : SwapBotEntity(id, CryptoWallets), CryptoAddressEntity {
    var swapUser by SwapUser referencedOn CryptoWallets.swapUser

    var currencyType: SwappableCrypto by CryptoWallets.currencyType.transform(
        toColumn = { it.name },
        toReal = { SwappableCrypto.valueOf(it) }
    )
    var label by CryptoWallets.label

    override var typeLabel: String? = null
        get() = currencyType?.name

    companion object : CryptoWalletCompanion()
}
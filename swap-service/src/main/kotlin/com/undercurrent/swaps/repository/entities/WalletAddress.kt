package com.undercurrent.swaps.repository.entities

import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.swaps.repository.companions.WalletAddressCompanion
import org.jetbrains.exposed.dao.id.EntityID


object WalletAddresses : SwapBotTable("crypto_wallet_addresses") {
    val wallet = reference("wallet_id", CryptoWallets)
    val address = varchar("address", VARCHAR_SIZE)
    val label = varchar("label", VARCHAR_SIZE)
}

class WalletAddress(id: EntityID<Int>) : SwapBotEntity(id, WalletAddresses) {
    var wallet by CryptoWallet referencedOn WalletAddresses.wallet
    var address by WalletAddresses.address
    var label by WalletAddresses.label

    companion object : WalletAddressCompanion()
}
package com.undercurrent.legacyshops.nodes.shared_nodes

import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.User

interface AddCryptoWalletNode {
    suspend fun next(): TreeNode?
    fun displayExistingAddresses(addresses: List<CryptoAddress>): TreeNode?
    fun overwriteExistingAddress(type: CryptoType): TreeNode?
    fun confirmOverwriteAddress(type: CryptoType): TreeNode?
    fun performOverwriteAddress(newAddr: CryptoAddress, type: CryptoType): TreeNode?
}

class AddCryptoWalletNodes(
    context: SystemContext,
    private val fetchCryptoAddressesFunc: (User) -> List<CryptoAddress> = {
        //todo impl this
        listOf()
    },
    private val cryptoAddrToString: (CryptoAddress) -> String = { "${it.type}: ${it.address}" },
    private val defaultCryptoType: CryptoType = CryptoType.BTC,
    private val defaultStr: String = "    <-- default",
    private val cryptoTypeToSelectableStr: (CryptoType, Boolean) -> String = { type, isDefault ->
        "${type.abbrev().uppercase()} (${type.fullName})${if (isDefault) defaultStr else ""}"
    },

    ) : AbstractShopSharedRoleNode(context), AddCryptoWalletNode {

    override suspend fun next(): TreeNode? {
        val addresses: List<CryptoAddress> = fetchCryptoAddressesFunc(context.user)
        if (addresses.isEmpty()) {
            return notifyNoAddresses()
        }
        return displayExistingAddresses(addresses)
    }

    fun notifyNoAddresses(): TreeNode? {
        sendOutput("No addresses found.")
        return decideToAddNewAddress(listOf())
    }

    override fun displayExistingAddresses(
        addresses: List<CryptoAddress>,

        ): TreeNode? {
        val outString =
            "Your current wallet addresses and keys:\n${addresses.joinToString { "• ${cryptoAddrToString(it)}\n" }}"
        sendOutput(outString)

        return decideToAddNewAddress(addresses)
    }


    fun decideToAddNewAddress(addresses: List<CryptoAddress>): TreeNode? {
        val options = if (addresses.isEmpty()) {
            listOf("Add new crypto address")
        } else {
            listOf("Add new crypto address", "Overwrite existing address", "Remove existing address")
        }


        return menuSelectNode(
            options = options,
            "Select an option:",
            ifSuccess = { index, _ ->
                when (index) {
                    0 -> promptForNewAddress()
                    1 -> overwriteExistingAddress()
                    else -> {
                        sendOutput("Invalid selection. Please try again.")
                        decideToAddNewAddress(addresses)
                    }
                }
            },
        )

    }

    fun decideTypeOfAddress(): TreeNode? {
        return menuSelectNode(
            options = CryptoType.values().mapIndexed { index, type ->
                cryptoTypeToSelectableStr(type, type == defaultCryptoType)
            },
            "Select a crypto type:",
            ifSuccess = { index, _ ->
                val type = CryptoType.values()[index]
                promptForNewAddress()
            },
        )
    }
//        val options = if (addresses.isEmpty()) {
//            listOf("Add new crypto address")
//        } else {
//            listOf("Add new crypto address", "Overwrite existing address", "Remove existing address")
//        }
//
//
//        return menuSelectNode(
//            options = options,
//            "Select an option:",
//            ifSuccess = { index ->
//                when (index) {
//                    0 -> promptForNewAddress()
//                    1 -> overwriteExistingAddress()
//                    else -> {
//                        sendOutput("Invalid selection. Please try again.")
//                        decideToAddNewAddress(addresses)
//                    }
//                }
//            },
//        )

//        return null
//    }

//    fun decideTypeOfAddress(): TreeNode? {
//        return menuSelectNode(
//            options = CryptoType.values().mapIndexed { index, type ->
//                cryptoTypeToSelectableStr(type, type == defaultCryptoType)
//            },
//            "Select a crypto type:",
//            ifSuccess = { index ->
//                val type = CryptoType.values()[index]
//                promptForNewAddress()
//            },
//        )
//    }

    override fun overwriteExistingAddress(type: CryptoType): TreeNode? {
        TODO("Not yet implemented")
    }

    fun promptForNewAddress(): TreeNode? {
        TODO("Not yet implemented")
    }

    fun overwriteExistingAddress(): TreeNode? {
        TODO("Not yet implemented")
    }

    override fun confirmOverwriteAddress(type: CryptoType): TreeNode? {
        TODO("Not yet implemented")
    }

    override fun performOverwriteAddress(newAddr: CryptoAddress, type: CryptoType): TreeNode? {
        TODO("Not yet implemented")
    }
}
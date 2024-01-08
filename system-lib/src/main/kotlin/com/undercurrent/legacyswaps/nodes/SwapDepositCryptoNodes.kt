package com.undercurrent.legacyswaps.nodes

import com.undercurrent.legacyshops.nodes.shared_nodes.CancelNode
import com.undercurrent.legacyswaps.types.SwappableCrypto
import com.undercurrent.shared.abstractions.CryptoAddressEntity
import com.undercurrent.shared.abstractions.ListFetcher
import com.undercurrent.shared.view.treenodes.tnode
import com.undercurrent.system.context.SystemContext

interface SwapDepositCryptoNodeClass {
    val cryptoAddressFetcher: ListFetcher<CryptoAddressEntity>
    val cryptoAddressListFormatter: (List<CryptoAddressEntity>) -> String

    fun currentCryptoBalance(): tnode
    fun selectCryptoForDepositNode(addresses: List<CryptoAddressEntity>): tnode
    fun inputCryptoAmountForDepositNode(selectedValue: String): tnode
    fun depositConfirmNode(): tnode
    fun successfulDepositNode(): tnode
    fun failedDepositNode(): tnode
}

class SwapDepositCryptoNodes(
    context: SystemContext,
    override val cryptoAddressFetcher: ListFetcher<CryptoAddressEntity>,
    override val cryptoAddressListFormatter: (List<CryptoAddressEntity>) -> String,
) : SwapOperationNode(
    context
), SwapDepositCryptoNodeClass {

    override suspend fun next(): tnode {
        return null
    }

    /**
     * --------------------
     * Your current wallet addresses and keys
     *
     * Bitcoin (BTC): tb1q5926e4w04...........45t6q
     * -------------------
     *
     */
    override fun currentCryptoBalance(): tnode {
        val addresses: List<CryptoAddressEntity> = cryptoAddressFetcher.fetchList()
        val cryptoAddrToString = cryptoAddressListFormatter(addresses)

        val output = """
            |Your current wallet addresses and keys:
            |
            |$cryptoAddrToString
        """.trimMargin()

        sendOutput(output)
        return selectCryptoForDepositNode(addresses)
    }

    override fun selectCryptoForDepositNode(addresses: List<CryptoAddressEntity>): tnode {
        val cryptoNames = addresses.mapNotNull { it.typeLabel }
        val cryptoNamesWithCancel = cryptoNames + "Cancel"

        return menuSelectNode(
            options = cryptoNamesWithCancel,
            headerText = "Which crypto would you like to deposit?",
            ifSuccess = { i, thisList ->
                when (val selectedValue = thisList[i]) {
                    SwappableCrypto.BTC.name, SwappableCrypto.MOB.name -> {
                        inputCryptoAmountForDepositNode(selectedValue)
                    }
                    "Cancel" -> {
                        CancelNode(context).next()
                    }
                    else -> {
                        null
                    }
                }
            }
        )
    }

    //todo can perhaps reuse this with the withdraw node
    override fun inputCryptoAmountForDepositNode(selectedValue: String): tnode {
        TODO("Not yet implemented")
    }

    override fun depositConfirmNode(): tnode {
        TODO("Not yet implemented")
    }

    override fun successfulDepositNode(): tnode {
        TODO("Not yet implemented")
    }

    override fun failedDepositNode(): tnode {
        TODO("Not yet implemented")
    }

}
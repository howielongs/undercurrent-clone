package com.undercurrent.swaps.views.banker_views

import com.undercurrent.shared.experimental.command_handling.MenuCallback
import com.undercurrent.prompting.views.menubuilding.SelectAbcPrompt
import com.undercurrent.legacyswaps.types.WalletAddress


/**
 * Your current wallet addresses and keys:
 *
 * Bitcoin (BTC): 17777777777777777777777SLmv7DivfNa
 *
 * Select a crypto to withdraw from:
 * – [A] Bitcoin (BTC) [default]
 * – [B] MobileCoin (MOB)
 * - [C] Cancel
 */
class CurrentWalletAndKeysView(
    val addresses: List<WalletAddress>,
    val defaultCurrency: WalletAddress,
) : SelectAbcPrompt<String, MenuCallback<String>>(
    header = """
            |Your current wallet addresses and keys:
            |
        """.trimMargin(),
    options = listOf()
)

//todo impl this


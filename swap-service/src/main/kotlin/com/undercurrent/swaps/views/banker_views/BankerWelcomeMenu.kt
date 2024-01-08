package com.undercurrent.swaps.views.banker_views

import com.undercurrent.prompting.views.menubuilding.formatting.MenuPrefixFormatter
import com.undercurrent.shared.experimental.command_handling.MenuCallback
import com.undercurrent.prompting.views.menubuilding.SelectAbcPrompt

abstract class ExecutiveSwapStartView(
    greetingAddressee: String,
    header: String = "Welcome, $greetingAddressee! What would you like to do?",
    options: List<MenuCallback<String>> = listOf(
        SwapCallbacks.ViewActivityStats(),
        SwapCallbacks.WithdrawLiquidity(),
        SwapCallbacks.DepositLiquidity(),
        SwapCallbacks.AddCryptoWallet(),
        SwapCallbacks.RemoveCryptoWallet(),
    ),
    prefixFormatter: MenuPrefixFormatter<String>? = null
) : SelectAbcPrompt<String, MenuCallback<String>>(
    header = header,
    options = options,
    prefixFormatter = prefixFormatter ?: MenuPrefixFormatter.WrappedWithBrackets()
)

class BankerSelectStartAction(
    prefixFormatter: MenuPrefixFormatter<String>? = null
) : ExecutiveSwapStartView(
    greetingAddressee = "Banker",
    prefixFormatter = prefixFormatter
)

class AdminSelectStartAction(
    prefixFormatter: MenuPrefixFormatter<String>? = null
) : ExecutiveSwapStartView(
    greetingAddressee = "Admin",
    prefixFormatter = prefixFormatter
)
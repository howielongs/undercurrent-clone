package com.undercurrent.swaps.views.banker_views

import com.undercurrent.prompting.views.menubuilding.SelectAbcPrompt
import com.undercurrent.legacyswaps.types.SwappableCrypto

class SelectCryptoPrompt(
    header: String = "Select a crypto currency:",
) : SelectAbcPrompt<String, SwappableCrypto>(
    header,
    options = SwappableCrypto.values().toList(),
)

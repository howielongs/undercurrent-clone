package com.undercurrent.legacyswaps.types

data class WalletAddress(
    val address: String,
    val currency: SupportedSwapCurrency,
)
package com.undercurrent.legacy.service.crypto.mobilecoin.requests


import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.legacy.routing.RunConfig

object MobileCoinDefaultValues {
    fun name(environmentIn: Environment = RunConfig.environment): MobAcctNameText {
        return MobAcctNameText("default_${environmentIn.name.lowercase()}")
    }

    fun mnemonic(environment: Environment = RunConfig.environment): MobMnemonicText {
        val mobLiveMnemonic = System.getProperty("mob_live_mnemonic") ?: ""
        val mobQaMnemonic = System.getProperty("mob_qa_mnemonic") ?: ""
        val mobDevMnemonic = System.getProperty("mob_dev_mnemonic") ?: ""

        val mnemonic = when(environment) {
            Environment.LIVE -> mobLiveMnemonic
            Environment.QA -> mobQaMnemonic
            Environment.DEV -> mobDevMnemonic
            else -> ""
        }

        return MobMnemonicText(mnemonic)
    }
}
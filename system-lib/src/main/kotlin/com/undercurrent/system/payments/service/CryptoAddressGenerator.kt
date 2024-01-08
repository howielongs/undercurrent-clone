package com.undercurrent.system.payments.service

import com.undercurrent.shared.utils.Log
import org.bitcoinj.kits.WalletAppKit

sealed interface CryptoAddressGenerator {
    fun generateFreshAddress(): String?
}

class BtcAddressGenerator(val kit: WalletAppKit) : CryptoAddressGenerator {
    override fun generateFreshAddress(): String? {
        return if (kit.isRunning) {
            val freshAddress = kit.wallet().freshReceiveAddress().toString()
            Log.debug("fresh address generated: $freshAddress")
            freshAddress
        } else {
            Log.error("generateFreshAddrBTC: kit is not running")
            null
        }
    }
}

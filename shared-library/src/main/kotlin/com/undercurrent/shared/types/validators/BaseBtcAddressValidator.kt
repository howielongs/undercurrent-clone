package com.undercurrent.shared.types.validators

import com.undercurrent.shared.types.BtcAddress
import com.undercurrent.shared.types.BtcMainnetAddress
import com.undercurrent.shared.types.BtcTestnetAddress
import com.undercurrent.shared.utils.Log
import org.bitcoinj.base.Address
import org.bitcoinj.params.BitcoinNetworkParams
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

class BtcAddressValidator : BaseBtcAddressValidator<BtcMainnetAddress>(
    wrapperFunc = { BtcMainnetAddress(it) },
    isTestnet = false
)

class BtcTestAddressValidator : BaseBtcAddressValidator<BtcTestnetAddress>(
    wrapperFunc = { BtcTestnetAddress(it) },
    isTestnet = true
)

sealed class BaseBtcAddressValidator<T : BtcAddress>(
    val wrapperFunc: (Address) -> T?,
    val isTestnet: Boolean,
) : DataValidator<String, T> {


    override fun validate(data: String): T? {
        return try {
            var address: Address = Address.fromString(fetchParams(isTestnet), data)
            wrapperFunc(address)
        } catch (e: Exception) {
            Log.error(invalidCryptoAddress() + "\n\n\t$data", e, this::class.java.simpleName)
            null
        }
    }

    private fun fetchParams(isTestnet: Boolean): BitcoinNetworkParams? {
        if (isTestnet) {
            return TestNet3Params.get()
        } else {
            return MainNetParams.get()
        }
    }

    private fun invalidCryptoAddress(): String {
        return "Invalid crypto address."
    }
}
package com.undercurrent.shared.types

import org.bitcoinj.base.Address

interface BtcAddress {
    val addressText: String

    fun isTestnetType(): Boolean
}

inline class BtcMainnetAddress(override val addressText: String) : BtcAddress {
    constructor(address: Address) : this(address.toString())

    override fun isTestnetType(): Boolean {
        return false
    }
}

inline class BtcTestnetAddress(override val addressText: String) : BtcAddress {
    constructor(address: Address) : this(address.toString())

    override fun isTestnetType(): Boolean {
        return true
    }
}

package com.undercurrent.system.types

import org.bitcoinj.wallet.Wallet


enum class WalletMemo {
    RECEIVE,
    SEND
}

interface WalletEventEntity {
    fun save(wallet: Wallet?, rawIn: String?, memoIn: WalletMemo?)

}
package com.undercurrent.system.payments

interface BtcBlockchainScanner {
    fun scanByAddress(address: String)
}

class BtcTransactionScanner {
}
package com.undercurrent.swaps.repository

import com.undercurrent.swaps.repository.entities.*
import org.jetbrains.exposed.sql.Table

object SwapTablesList {
    fun allTables(): List<Table> {
        return listOf(
            BankerDeposits,
            BankerWithdrawals,
            CryptoCurrencyAmounts,
            CryptoUsdExchangeRates,
            CryptoWallets,
            CustomerDeposits,
            CustomerWithdrawals,
            FeesPerAdmin,
            LiquidityPoolStates,
            LiquidityPools,
            PoolBalanceSnapshots,
            PoolCryptoAddresses,
            PoolThresholds,
            SwapAdmins,
            SwapBankers,
            SwapTransactions,
            SwapTransactionStates,
            SwapUsers,
            WalletAddresses
        )
    }
}
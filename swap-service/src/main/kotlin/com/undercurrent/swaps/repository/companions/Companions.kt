package com.undercurrent.swaps.repository.companions

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.swaps.repository.entities.*

open class SwapUserCompanion : RootEntityCompanion0<SwapUser>(SwapUsers)
open class CustomerDepositCompanion : RootEntityCompanion0<CustomerDeposit>(CustomerDeposits)
open class CustomerWithdrawalCompanion : RootEntityCompanion0<CustomerWithdrawal>(CustomerWithdrawals)

open class CryptoWalletCompanion : RootEntityCompanion0<CryptoWallet>(CryptoWallets)
open class WalletAddressCompanion : RootEntityCompanion0<WalletAddress>(WalletAddresses)

open class SwapCompanion : RootEntityCompanion0<SwapTransaction>(SwapTransactions)
open class SwapTransactionStateCompanion : RootEntityCompanion0<SwapTransactionState>(SwapTransactionStates)

open class CryptoUsdExchangeRateCompanion : RootEntityCompanion0<CryptoUsdExchangeRate>(CryptoUsdExchangeRates)
open class CryptoCurrencyAmountCompanion : RootEntityCompanion0<CryptoCurrencyAmount>(CryptoCurrencyAmounts)

open class PoolAddressCompanion : RootEntityCompanion0<PoolCryptoAddress>(PoolCryptoAddresses)
open class LiquidityPoolCompanion : RootEntityCompanion0<LiquidityPool>(LiquidityPools)
open class LiquidityPoolStateCompanion : RootEntityCompanion0<LiquidityPoolState>(LiquidityPoolStates)
open class PoolBalanceSnapshotCompanion : RootEntityCompanion0<PoolBalanceSnapshot>(PoolBalanceSnapshots)
open class PoolThresholdsCompanion : RootEntityCompanion0<PoolThreshold>(PoolThresholds)


open class SwapBankerCompanion : RootEntityCompanion0<SwapBanker>(SwapBankers)
open class BankerDepositCompanion : RootEntityCompanion0<BankerDeposit>(BankerDeposits)
open class BankerWithdrawalCompanion : RootEntityCompanion0<BankerWithdrawal>(BankerWithdrawals)


open class SwapAdminCompanion : RootEntityCompanion0<SwapAdmin>(SwapAdmins)
open class FeePerAdminCompanion : RootEntityCompanion0<FeePerAdmin>(FeesPerAdmin)




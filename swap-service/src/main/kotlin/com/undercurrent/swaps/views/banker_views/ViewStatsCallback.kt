package com.undercurrent.swaps.views.banker_views

import com.undercurrent.shared.experimental.command_handling.MenuCallback
import com.undercurrent.shared.types.ActivityPeriod

// move this into system or shared-library (most likely)
class CancelCallback<T> : MenuCallback<T>("Cancel") {
    override fun runCallback() {}
}

object SwapCallbacks {

    class ViewActivityStats<T> : BankerMenuCallback<T>("View statistics") {
        override fun runCallback() {}
    }

    abstract class BankerMenuCallback<T>(prompt: String) : MenuCallback<T>(prompt)
    class WithdrawLiquidity<T> : BankerMenuCallback<T>("Withdraw liquidity") {
        override fun runCallback() {}
    }

    class DepositLiquidity<T> : BankerMenuCallback<T>("Deposit liquidity") {
        override fun runCallback() {}
    }

    class DownloadCsv<T>() : BankerMenuCallback<T>("Download CSV") {
        override fun runCallback() {}
    }

    class ViewStatsFromTimePeriodOption<T>(
        timePeriod: ActivityPeriod,
        promptPrefix: String = "View activity from "
    ) :
        BankerMenuCallback<T>(promptPrefix + timePeriod.label) {
        override fun runCallback() {}

    }

    class RemoveCryptoWallet<T> : BankerMenuCallback<T>("Remove crypto wallet") {
        override fun runCallback() {}
    }

    class AddCryptoWallet<T> : BankerMenuCallback<T>("Add crypto wallet") {
        override fun runCallback() {}
    }
}


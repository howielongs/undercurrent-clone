package com.undercurrent.legacyswaps.services

import com.undercurrent.shared.abstractions.ListFetcher
import com.undercurrent.shared.abstractions.swaps.SwapTransactionEntity
import com.undercurrent.shared.abstractions.swaps.SwappableCurrencyEnum
import com.undercurrent.shared.repository.entities.UserEntity
import com.undercurrent.shared.service.StringProvider
import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.shared.types.CurrencyAmount

class SwapStatsFetcher {


}


//todo should have separate for admins vs bankers: different visibility
class SwapStatsStringProvider(
    val uniqueUsersFetcher: ListFetcher<UserEntity>,
    val completedSwapsFetcher: ListFetcher<SwapTransactionEntity>,
    val calcUsdTotalForSwaps: (List<SwapTransactionEntity>) -> CurrencyAmount<SwappableCurrencyEnum>
) : StringProvider {

    val completedSwaps: List<SwapTransactionEntity> by lazy {
        completedSwapsFetcher.fetchList()
    }

    val numCompletedSwaps: Int by lazy {
        completedSwaps.count()
    }

    val totalSwapUsdValue: CurrencyAmount<SwappableCurrencyEnum> by lazy {
        calcUsdTotalForSwaps(completedSwaps)
    }

    val uniqueUsers: List<UserEntity> by lazy {
        uniqueUsersFetcher.fetchList()
    }

    val uniqueUsersCount: Int by lazy {
        uniqueUsers.count()
    }


    val currentTimePeriod: ActivityPeriod by lazy {
        ActivityPeriod.MONTH
    }


    override fun buildString(): String {
        return """
                |Here’s the activity from ${currentTimePeriod.label}.
                |
                |Swaps completed: $numCompletedSwaps
                |Total value of swaps: $totalSwapUsdValue
                |Number of unique users: $uniqueUsersCount
                |
            """.trimMargin()

    }

}
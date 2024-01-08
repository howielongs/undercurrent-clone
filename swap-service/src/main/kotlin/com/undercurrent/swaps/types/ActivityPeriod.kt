package com.undercurrent.swaps.types

import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.swaps.views.banker_views.SwapCallbacks

object ActivityPeriodText {
    fun <T> genCallbacksForOtherTimePeriod(
        currentTimePeriod: ActivityPeriod,
        prefix: String = "View activity from "
    ): List<SwapCallbacks.ViewStatsFromTimePeriodOption<T>> {
        return ActivityPeriod.values().filter { it != currentTimePeriod }
            .map { SwapCallbacks.ViewStatsFromTimePeriodOption(it, prefix) }
    }
}


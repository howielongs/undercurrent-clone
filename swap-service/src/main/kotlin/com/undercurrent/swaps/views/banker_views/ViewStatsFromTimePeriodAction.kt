package com.undercurrent.swaps.views.banker_views

import com.undercurrent.prompting.views.menubuilding.*
import com.undercurrent.shared.experimental.command_handling.MenuCallback
import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.swaps.types.ActivityPeriodText.genCallbacksForOtherTimePeriod
import com.undercurrent.legacyswaps.types.SwapAmount
import com.undercurrent.legacyswaps.types.SwappableFiat

/**
 * Here’s the activity from last week.
 *
 * Swaps completed: 52
 * Total value of swaps: 1,500 USD
 * Number of unique users: 18
 *
 * – [A] View activity from last month
 * – [B] Cancel
 */
class ViewStatsFromTimePeriodAction(
    val numCompletedSwaps: Int,
    val swapValue: SwapAmount<SwappableFiat>,
    val uniqueUsersCount: Int,
    val currentTimePeriod: ActivityPeriod,
) : SelectAbcPrompt<String, MenuCallback<String>>(
    header = """
                |Here’s the activity from ${currentTimePeriod.label}.
                |
                |Swaps completed: $numCompletedSwaps
                |Total value of swaps: $swapValue
                |Number of unique users: $uniqueUsersCount
                |
            """.trimMargin(),
    options = genCallbacksForOtherTimePeriod<String>(
        currentTimePeriod = currentTimePeriod,
        prefix = "View activity from "
    ) + listOf(
        SwapCallbacks.DownloadCsv(),
        CancelCallback()
    )
)

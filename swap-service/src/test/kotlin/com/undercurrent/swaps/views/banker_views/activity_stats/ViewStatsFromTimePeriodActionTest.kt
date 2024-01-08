package com.undercurrent.swaps.views.banker_views.activity_stats

import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.legacyswaps.types.SwapAmount
import com.undercurrent.legacyswaps.types.SwappableFiat
import com.undercurrent.swaps.views.banker_views.SwapCallbacks
import com.undercurrent.swaps.views.banker_views.ViewStatsFromTimePeriodAction
import com.undercurrent.testutils.TestAssertUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ViewStatsFromTimePeriodActionTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `test view last week then view last month`() {
        val result = ViewStatsFromTimePeriodAction(
            numCompletedSwaps = 52,
            swapValue = SwapAmount(1500.0.toBigDecimal(), SwappableFiat.USD),
            uniqueUsersCount = 18,
            currentTimePeriod = ActivityPeriod.WEEK,
        ).toString()

        val option = SwapCallbacks.ViewStatsFromTimePeriodOption<String>(
            timePeriod = ActivityPeriod.WEEK,
        ).toString()

        TestAssertUtils().   assertContains(
            result, listOf(
                "Here’s the activity from last week.",
                "Swaps completed: 52",
                "Total value of swaps: 1,500.00 USD",
                "Number of unique users: 18",
                "-[A] View activity from last month",
                "-[C] Download CSV",
                "-[D] Cancel",
            )
        )

    }

    @Test
    fun getSwapValue() {
    }
}
package com.undercurrent.swaps

import com.undercurrent.prompting.views.menubuilding.formatting.MenuPrefixFormatter
import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.legacyswaps.types.SwapAmount
import com.undercurrent.legacyswaps.types.SwappableFiat
import com.undercurrent.swaps.views.banker_views.BankerSelectStartAction
import com.undercurrent.swaps.views.banker_views.ViewStatsFromTimePeriodAction
import com.undercurrent.testutils.TestAssertUtils
import org.junit.jupiter.api.Test


class BankerStartMenuTest {
    @Test
    fun `test banker welcome simple formatter`() {
        val result = BankerSelectStartAction(
            prefixFormatter = MenuPrefixFormatter.SimplePeriod()
        ).toString()
        TestAssertUtils().assertContains(
            result, listOf(
                "Welcome, Banker!",
                "A. View statistics",
                "B. Withdraw liquidity",
            )
        )

        TestAssertUtils().  assertDoesntContain(
            result, listOf(
                "?:", "com.", "undercurrent."
            )
        )

    }

    @Test
    fun `test banker welcome wrapped index formatter`() {
        val formatter = { MenuPrefixFormatter.WrappedWithBrackets<String>() }
        val results = BankerSelectStartAction(prefixFormatter = formatter()).toString()

        TestAssertUtils(). assertContains(
            results, containsList = listOf(
                "Welcome, Banker!",
                "-[A] View statistics",
                "-[B] Withdraw liquidity",
            )
        )
    }

    private fun assertBankerViewStats(
        swapsCount: Int,
        usdAmount: Double,
        userCount: Int,
        timePeriod: ActivityPeriod,
        usdString: String,
    ) {
        val result = ViewStatsFromTimePeriodAction(
            numCompletedSwaps = swapsCount,
            swapValue = SwapAmount(usdAmount.toBigDecimal(), SwappableFiat.USD),
            uniqueUsersCount = userCount,
            currentTimePeriod = timePeriod,
        ).toString()

        var expected: MutableList<String> = mutableListOf()

        val moreExpectedStrings = if (TestToggles.FETCH_SWAPS_STATS) {
            mutableListOf(
                """Here’s the activity from ${timePeriod.label.lowercase()}. 
        |
        |Swaps completed: $swapsCount
        |Total value of swaps: $usdString USD
        |Number of unique users: $userCount
        |
        |–[A] View activity from last 
        |–[B] View activity from last 
        |–[C] Download CSV 
        |–[D] Cancel
        """.trimMargin()
            )
        } else {
            mutableListOf(
                "Swaps completed",
                "Total value of swaps",
                "Number of unique users",
                "Here’s the activity from ${timePeriod.label.lowercase()}",
                "-[A] View activity from last ",
                "-[B] View activity from last ",
                "–[C] Download CSV",
                "-[D] Cancel",
            )
        }

        expected.apply {
            addAll(moreExpectedStrings)
            add(usdString)
            add("$usdString USD")
        }

        TestAssertUtils().  assertContains(
            resultStr = result, containsList = expected
        )

        TestAssertUtils(). assertDoesntContain(
            result, listOf(
                "] View activity from ${timePeriod.label.lowercase()}",
                "] View activity from ${timePeriod.label.lowercase()}}",
            )
        )
    }

    @Test
    fun `test banker view monthly stats`() {
        assertBankerViewStats(
            swapsCount = 120,
            usdAmount = 3100.0,
            usdString = "3,100.00",
            userCount = 25,
            timePeriod = ActivityPeriod.MONTH,
        )
    }


    @Test
    fun `test banker view weekly stats`() {
        assertBankerViewStats(
            swapsCount = 52,
            usdAmount = 1500.0,
            usdString = "1,500.00",
            userCount = 18,
            timePeriod = ActivityPeriod.WEEK,
        )
    }


}


package com.undercurrent.legacyswaps.nodes

import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.shared.types.CurrencyAmount
import com.undercurrent.shared.types.CurrencyEnum
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext


class SwapAdminNodes(
    context: SystemContext,
) : SwapOperationNode(context) {

    override suspend fun next(): TreeNode? {
        //Do permissions check (on all startNodes)
        //check if already got welcome message
        return adminWelcomeNode()
    }

    fun adminWelcomeNode(): TreeNode? {
        val options = listOf(
            "View statistics",
            "Withdraw liquidity",
            "Deposit liquidity",
            "Add crypto wallet",
            "Remove crypto wallet",
            "Cancel"
        )

        return menuSelectNode(
            options = options,
            headerText = "Welcome, Admin! What would you like to do?",
            ifSuccess = { i, thisList ->
                val selectedValue = thisList[i]

                when (selectedValue) {
                    "View statistics" -> {
                        //todo impl this
//                        viewStatsNode()

                        null
                    }

                    "Withdraw liquidity" -> {
                        withdrawLiquidityNode()
                    }

                    "Deposit liquidity" -> {
                        depositLiquidityNode()
                    }

                    "Add crypto wallet" -> {
                        addCryptoWalletNode()
                    }

                    "Remove crypto wallet" -> {
                        removeCryptoWalletNode()
                    }

                    "Cancel" -> {
                        null
                    }

                    else -> {
                        sendOutput("Invalid choice. Please try again later.")
                        null
                    }
                }
            }
        )


        //todo change return here
        return null
    }

    /**
     * Here’s the activity from last week.
     *
     * Swaps completed: 52
     * Total value of swaps: 1,500 USD
     * Number of unique users: 18
     * Total value of liquidity pool: 0.00345 BTC, 0.44370 MOB, 20,000 USD
     *
     * – [A] View activity from last month
     * – [B] Download CSV
     * – [C] Cancel
     */
    fun viewStatsNode(
        numCompletedSwaps: Int,
        swapValue: CurrencyAmount<CurrencyEnum>,
        uniqueUsersCount: Int,
        currentTimePeriod: ActivityPeriod,
    ): TreeNode? {
        val header = """
                |Here’s the activity from ${currentTimePeriod.label}.
                |
                |Swaps completed: $numCompletedSwaps
                |Total value of swaps: $swapValue
                |Number of unique users: $uniqueUsersCount
                |
            """.trimMargin()



        return menuSelectNode(
            options = listOf(
                "View activity from last month",
                "Download CSV",
                "Cancel"
            ),
            headerText = header,
            ifSuccess = { it, options ->
                val selectedValue = options[it]

                when (selectedValue) {
                    "View activity from last month" -> {
                        viewStatsNode(
                            numCompletedSwaps = numCompletedSwaps,
                            swapValue = swapValue,
                            uniqueUsersCount = uniqueUsersCount,
                            currentTimePeriod = ActivityPeriod.MONTH
                        )
                    }

                    "Download CSV" -> {
                        downloadCsvNode()
                    }

                    "Cancel" -> {
                        null
                    }

                    else -> {
                        sendOutput("Invalid choice. Please try again later.")
                        null
                    }
                }
            }
        )
    }


    private fun downloadCsvNode(): TreeNode? {
        TODO("Not yet implemented")
    }


    fun depositLiquidityNode(): TreeNode? {
        TODO("Not yet implemented")
    }

    fun addCryptoWalletNode(): TreeNode? {
        TODO("Not yet implemented")
    }

    fun removeCryptoWalletNode(): TreeNode? {
        TODO("Not yet implemented")
    }

    fun withdrawLiquidityNode(): TreeNode? {
        TODO("Not yet implemented")
    }


}
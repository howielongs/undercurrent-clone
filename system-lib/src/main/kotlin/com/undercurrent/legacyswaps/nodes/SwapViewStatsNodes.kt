package com.undercurrent.legacyswaps.nodes

import com.undercurrent.legacyswaps.SwapRole
import com.undercurrent.shared.types.ActivityPeriod
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.shared.view.treenodes.tnode
import com.undercurrent.system.context.SystemContext

interface SwapNodeClassCoverage

interface SwapViewStatsNodeCoverage : SwapNodeClassCoverage, SwapBotNodeClass {
    fun displayForBanker(): TreeNode?
    fun displayForAdmin(): TreeNode?

    fun decideStatsView(
        swapRole: SwapRole,
        timePeriod: ActivityPeriod
    ): TreeNode?
}

class SwapViewStatsNodes(
    context: SystemContext,
) : SwapOperationNode(
    context
), SwapViewStatsNodeCoverage {

    override suspend fun next(): tnode {
        return null
    }

    override fun displayForBanker(): TreeNode? {
        TODO("Not yet implemented")
    }

    override fun displayForAdmin(): TreeNode? {
        TODO("Not yet implemented")
    }

    override fun decideStatsView(swapRole: SwapRole, timePeriod: ActivityPeriod): TreeNode? {
        TODO("Not yet implemented")
    }


}
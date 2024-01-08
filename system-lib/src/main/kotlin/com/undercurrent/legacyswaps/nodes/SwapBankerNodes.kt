package com.undercurrent.legacyswaps.nodes

import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext


class SwapBankerNodes(
    context: SystemContext,
) : SwapOperationNode(context) {

    override suspend fun next(): TreeNode? {
        //todo change return here
        return null
    }


}
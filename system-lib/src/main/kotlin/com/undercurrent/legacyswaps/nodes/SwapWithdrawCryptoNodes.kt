package com.undercurrent.legacyswaps.nodes

import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.shared.view.treenodes.tnode
import com.undercurrent.system.context.SystemContext

interface SwapBotNodeClass

abstract class SwapOperationNode(context: SystemContext) : OperationNode<SystemContext>(context), SwapBotNodeClass

class SwapWithdrawCryptoNodes(
    context: SystemContext,
) : SwapOperationNode(
    context
) {

    override suspend fun next(): tnode {
        return null
    }


}
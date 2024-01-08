package com.undercurrent.legacyswaps.nodes

import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.shared.view.treenodes.tnode
import com.undercurrent.system.context.SystemContext


class SwapperNodes(
    context: SystemContext,
) : SwapOperationNode(context) {

    //todo perhaps check for current user permissions
    override suspend fun next(): tnode {
        return displayCustomerWelcome()
//        return textInputNode("Enter phone number for vendor:",
//            ifSuccess = {
//                null
//            })
    }

    fun displayCustomerWelcome(): TreeNode? {
        val welcomeMsg = """
        |Welcome to the crypto swap tool in Signal.
        |
        |Let’s get started.
    """.trimMargin()

        sendOutput(welcomeMsg)

        //todo change return here
        return null
    }


}
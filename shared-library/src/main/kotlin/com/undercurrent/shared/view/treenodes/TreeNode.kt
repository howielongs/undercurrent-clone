package com.undercurrent.shared.view.treenodes

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface CanGetNextNode {
    suspend fun next(): TreeNode?
}

interface CanExecuteAllNodes {
    suspend fun execute(steps: Int): TreeNode?
    suspend fun execute()
}

typealias NodeBranchFunc = suspend () -> TreeNode?
typealias NodeResultOutFunc<T> = suspend (T) -> TreeNode?
typealias MenuSelectNodeResultOutFunc<T, L> = suspend (T, List<L>) -> TreeNode?

typealias NextNodeFunc<T> = suspend T.() -> TreeNode?

interface HasNextNodeFunc<T> {
    val nextNodeFunc: NextNodeFunc<T>
}

typealias tnode = TreeNode?

abstract class TreeNode : CanGetNextNode, CanExecuteAllNodes {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun execute(steps: Int): TreeNode? {
        var count = 0
        var node: TreeNode? = this
        while (node != null && count < steps) {
            logger.debug("Executing node: ${node::class.java.simpleName}")
            count++
            if (count == steps) {
                return node
            }
            node = node.next()
        }
        return null
    }

    override suspend fun execute() {
        var node: TreeNode? = this
        while (node != null) {
            logger.debug("Executing node: ${node::class.java.simpleName}")
            node = node.next()
        }
    }
}





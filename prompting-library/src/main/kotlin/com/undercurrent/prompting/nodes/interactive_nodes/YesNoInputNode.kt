package com.undercurrent.prompting.nodes.interactive_nodes

import com.undercurrent.prompting.nodes.YesNoInputUtil
import com.undercurrent.shared.messages.*
import com.undercurrent.shared.types.validators.No
import com.undercurrent.shared.types.validators.Yes
import com.undercurrent.shared.view.treenodes.InteractiveNode
import com.undercurrent.shared.view.treenodes.TreeNode

// perhaps extend to have a ConfirmNode -> would have a builder for incoming fields
open class YesNoInputNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val nextNodeFunc: suspend YesNoInputNode.() -> TreeNode? = { null },
    private val maxAttempts: Int? = null,
) : InteractiveNode(inputProvider, outputProvider), CanFetchInput<Boolean> {

    constructor(
        interactorsStruct: InteractorStructSet,
        nextNodeFuncIn: suspend YesNoInputNode.() -> TreeNode? = { null },
        maxAttemptsIn: Int? = null,
    ) : this(
        interactorsStruct.inputter,
        interactorsStruct.interrupter,
        nextNodeFunc = nextNodeFuncIn,
        maxAttempts = maxAttemptsIn
    )

    override suspend fun fetchInput(vararg prompts: String): Boolean? {
        YesNoInputUtil(
            inputProvider = inputProvider,
            outputProvider = outputProvider,
            maxAttempts,
        ).fetchInput(*prompts).let {
            return when (it) {
                is Yes -> true
                is No -> false
                else -> null
            }
        }
    }

    override suspend fun next(): TreeNode? {
        return nextNodeFunc()
    }
}


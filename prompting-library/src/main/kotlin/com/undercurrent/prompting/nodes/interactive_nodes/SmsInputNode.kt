package com.undercurrent.prompting.nodes.interactive_nodes

import com.undercurrent.prompting.nodes.SmsInputUtil
import com.undercurrent.shared.messages.InteractorStructSet
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.view.treenodes.HasNextNodeFunc
import com.undercurrent.shared.view.treenodes.NextNodeFunc
import com.undercurrent.shared.view.treenodes.TreeNode

open class SmsInputNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    override val nextNodeFunc: NextNodeFunc<SmsInputNode> = { null },
) : ValidatableInputNode<Sms>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    inputUtilProvider = SmsInputUtil(inputProvider, outputProvider)
), HasNextNodeFunc<SmsInputNode> {

    constructor(
        interactorsStruct: InteractorStructSet,
        nextNodeFuncIn: NextNodeFunc<SmsInputNode> = { null },
    ) : this(
        inputProvider = interactorsStruct.inputter,
        outputProvider = interactorsStruct.interrupter,
        nextNodeFunc = nextNodeFuncIn
    )

    override suspend fun next(): TreeNode? {
        return nextNodeFunc()
    }
}
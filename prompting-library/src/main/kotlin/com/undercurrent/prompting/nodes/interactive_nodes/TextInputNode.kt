package com.undercurrent.prompting.nodes.interactive_nodes

import com.undercurrent.prompting.nodes.BaseInputUtilProvider
import com.undercurrent.prompting.nodes.TextInputUtil
import com.undercurrent.shared.OperationContext
import com.undercurrent.shared.messages.*
import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.view.treenodes.*

abstract class OperationNode<T : OperationContext>(
    val context: T,
    val inputProvider: UserInputProvider = context.inputter,
    val nodeInterrupter: UserOutputProvider<InterrupterMessageEntity> = context.interrupter,
) : TreeNode(), CanGetNextNode, CanSendToUser<InterrupterMessageEntity> {
    override fun sendOutput(msgBody: String): InterrupterMessageEntity? {
        return nodeInterrupter.sendOutput(msgBody)
    }

    protected val interactors: InteractorStructSet = InteractorStruct(
        inputter = inputProvider,
        interrupter = nodeInterrupter,
    )

    private fun runSmsInputNode(func: suspend SmsInputNode.() -> TreeNode?): TreeNode? {
        return SmsInputNode(interactorsStruct = interactors, nextNodeFuncIn = func)
    }

    fun smsInputNode(
        vararg prompts: String,
        ifSuccess: NodeResultOutFunc<Sms>,
        ifFail: NodeBranchFunc = { null },
    ): TreeNode? {
        return runSmsInputNode {
            fetchInput(*prompts)?.let {
                ifSuccess(it)
            } ?: ifFail()
        }
    }


    private fun intNode(func: suspend TextInputNode.() -> TreeNode?): TreeNode? {
        return TextInputNode(interactorsStruct = interactors, nextNodeFuncIn = func)
    }

    fun inttextInputNode(
        vararg prompts: String,
        ifSuccess: NodeResultOutFunc<Int>,
        ifFail: NodeBranchFunc = { null },
    ): TreeNode? {
        return intNode {
            fetchInput(*prompts)?.let {
                try {
                    ifSuccess(it.toInt())
                } catch (e: NumberFormatException) {
                    ifFail()
                }
            } ?: ifFail()
        }
    }

    private fun runTextInputNode(func: suspend TextInputNode.() -> TreeNode?): TreeNode? {
        return TextInputNode(interactorsStruct = interactors, nextNodeFuncIn = func)
    }

    fun textInputNode(
        vararg prompts: String,
        ifSuccess: NodeResultOutFunc<String>,
        ifFail: NodeBranchFunc = { null },
    ): TreeNode? {
        return runTextInputNode {
            fetchInput(*prompts)?.let {
                ifSuccess(it)
            } ?: ifFail()
        }
    }


    private fun runYesNoNode(
        retries: Int? = null,
        func: suspend YesNoInputNode.() -> TreeNode?
    ): TreeNode? {
        return YesNoInputNode(
            interactorsStruct = interactors,
            nextNodeFuncIn = func,
            maxAttemptsIn = retries
        )
    }

    fun yesNoNode(
        vararg prompts: String,
        ifYes: NodeBranchFunc,
        ifNo: NodeBranchFunc,
        retries: Int? = null,
    ): TreeNode? {
        return runYesNoNode(retries) {
            if (fetchInput(*prompts) == true
            ) {
                ifYes()
            } else {
                ifNo()
            }
        }
    }


    private fun runMenuSelect(
        retries: Int? = null,
        func: suspend MenuSelectInputNode.() -> TreeNode?
    ): TreeNode? {
        return MenuSelectInputNode(
            interactorsStruct = interactors,
            nextNodeFuncIn = func,
            maxAttemptsIn = retries
        )
    }

    /**
     * Additional flexibility may be needed for:
     *  - displaying int vs abc
     *  - map input vs list
     *
     *  - handling more types than just string
     *  - callbacks
     *  - customizing the menu display
     *  - customizing smaller components
     */
    fun menuSelectNode(
        options: List<String>,
        headerText: String,
        ifSuccess: MenuSelectNodeResultOutFunc<Int, String>,
        ifFail: NodeBranchFunc = { null },
        retries: Int? = null,
    ): TreeNode? {
        return runMenuSelect(retries) {
            selectOptionIndex(
                options = options,
                headerText = headerText
            )?.let {
                val optionsListString = options.mapIndexed { index, s -> "\n${index}. $s" }
                val selectedInt = it

                if (logger.isDebugEnabled) {
                    "User selected ${options[it]} (index $selectedInt) from $optionsListString".let { logMsg ->
                        logger.debug(logMsg)
                    }
                }

                ifSuccess(it, options)
            } ?: ifFail()
        }
    }


}

open class TextInputNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    override val nextNodeFunc: NextNodeFunc<TextInputNode> = { null },
) : ValidatableInputNode<String>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    inputUtilProvider = TextInputUtil(inputProvider, outputProvider),
), HasNextNodeFunc<TextInputNode> {

    constructor(
        interactorsStruct: InteractorStructSet,
        nextNodeFuncIn: NextNodeFunc<TextInputNode> = { null },
    ) : this(
        interactorsStruct.inputter, interactorsStruct.interrupter,
        nextNodeFunc = nextNodeFuncIn
    )

    override suspend fun next(): TreeNode? {
        return nextNodeFunc()
    }
}

abstract class ValidatableInputNode<R>(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val inputUtilProvider: BaseInputUtilProvider<R>,
) : InteractiveNode(inputProvider, outputProvider), CanFetchInput<R> {

    override suspend fun fetchInput(vararg prompts: String): R? {
        return inputUtilProvider.fetchInput(*prompts)
    }
}
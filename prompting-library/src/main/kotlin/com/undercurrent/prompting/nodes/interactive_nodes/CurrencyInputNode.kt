package com.undercurrent.prompting.nodes.interactive_nodes

import com.undercurrent.prompting.nodes.CurrencyInputUtil
import com.undercurrent.shared.messages.*
import com.undercurrent.shared.view.treenodes.InteractiveNode


abstract class CurrencyInputNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : InteractiveNode(inputProvider, outputProvider), CanFetchInput<String> {
    constructor(
        interactorsStruct: InteractorStructSet,
    ) : this(interactorsStruct.inputter, interactorsStruct.interrupter)

    override suspend fun fetchInput(vararg prompts: String): String? {
        return CurrencyInputUtil(inputProvider, outputProvider).fetchInput(*prompts)
    }
}
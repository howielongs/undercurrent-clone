package com.undercurrent.prompting.nodes.interactive_nodes

import com.undercurrent.prompting.nodes.MenuSelectInputUtil
import com.undercurrent.prompting.nodes.menubuilding.MenuBuilder
import com.undercurrent.shared.messages.*
import com.undercurrent.shared.utils.ordinalOfKey
import com.undercurrent.shared.view.treenodes.InteractiveNode
import com.undercurrent.shared.view.treenodes.TreeNode

open class MenuSelectInputNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    val nextNodeFunc: suspend MenuSelectInputNode.() -> TreeNode? = { null },
    private val menuBuilder: MenuBuilder<String> = MenuBuilder(),
    private val maxAttempts: Int? = null,
) : InteractiveNode(inputProvider, outputProvider),
    CanSelectFromMenu<String> {

    constructor(
        interactorsStruct: InteractorStructSet,
        nextNodeFuncIn: suspend MenuSelectInputNode.() -> TreeNode? = { null },
        maxAttemptsIn: Int? = null,
    ) : this(
        inputProvider = interactorsStruct.inputter,
        outputProvider = interactorsStruct.interrupter,
        nextNodeFunc = nextNodeFuncIn,
        maxAttempts = maxAttemptsIn
    )

    private var menuString: String = ""
    private var validHandlesToLineBodyMap: LinkedHashMap<String, String> = linkedMapOf()

    override suspend fun selectOptionIndex(
        options: List<String>,
        headerText: String
    ): Int? {
        menuString = menuBuilder.buildMenu(options)
        validHandlesToLineBodyMap = menuBuilder.handleToOptionBodyMap

        MenuSelectInputUtil(
            inputProvider = inputProvider,
            outputProvider = outputProvider,
            validOptions = validHandlesToLineBodyMap.keys,
            maxAttempts = maxAttempts,
        ).fetchInput(headerText + "\n" + menuString)?.let {
            return validHandlesToLineBodyMap.ordinalOfKey(it)
        }
        return null
    }


    override suspend fun next(): TreeNode? {
        return nextNodeFunc()
    }
}
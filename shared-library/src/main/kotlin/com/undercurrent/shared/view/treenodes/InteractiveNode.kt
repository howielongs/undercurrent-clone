package com.undercurrent.shared.view.treenodes

import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.InteractorStructSet
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserInteractable
import com.undercurrent.shared.messages.UserOutputProvider


abstract class InteractiveNode(
    override val inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : OutputNode(outputProvider), UserInteractable<InterrupterMessageEntity> {
    constructor(
        interactorsStruct: InteractorStructSet,
    ) : this(interactorsStruct.inputter, interactorsStruct.interrupter)
}

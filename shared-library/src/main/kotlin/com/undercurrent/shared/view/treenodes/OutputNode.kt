package com.undercurrent.shared.view.treenodes

import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.InteractorStructSet
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.messages.UserOutputtable
import com.undercurrent.shared.utils.time.EpochNano

abstract class OutputNode(
    override val outputProvider: UserOutputProvider<InterrupterMessageEntity>
) : TreeNode(), UserOutputtable<InterrupterMessageEntity> {

    constructor(
        interactorsStruct: InteractorStructSet,
    ) : this(interactorsStruct.interrupter)

    val epochUpdaterFunc: () -> EpochNano = { EpochNano() }

    private var lastSentEpoch: EpochNano = EpochNano(0L)

    override fun sendOutput(message: String): InterrupterMessageEntity? {
        lastSentEpoch = epochUpdaterFunc()
        return outputProvider.sendOutput(message)
    }
}

//todo impl special properties for this
abstract class ListOutputNode(
    override val outputProvider: UserOutputProvider<InterrupterMessageEntity>
) : OutputNode(outputProvider)
package com.undercurrent.legacyshops.nodes.shared_nodes

import com.undercurrent.prompting.nodes.interactive_nodes.YesNoInputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext

class CancelNode(
    context: SystemContext
) : YesNoInputNode(
    inputProvider = context.inputter,
    outputProvider = context.interrupter,
) {

    override suspend fun next(): TreeNode? {
        val prompt = "You entered 'cancel'\n" +
                "\n" +
                "This will cancel your current operation and any unsaved changes will be lost.\n" +
                "\n" +
                "Cancel?\n"

        fetchInput(prompt)?.let {
            if (it) {
                sendOutput("Operation cancelled.")
                return null
            }
        }
        sendOutput("Resuming...")
        //todo figure out how to link back in...
        return null
    }
}
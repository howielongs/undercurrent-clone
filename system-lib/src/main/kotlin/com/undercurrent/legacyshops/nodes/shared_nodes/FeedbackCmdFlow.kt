package com.undercurrent.legacyshops.nodes.shared_nodes

import com.undercurrent.legacy.types.string.FeedbackStrings.confirmStr
import com.undercurrent.legacy.types.string.FeedbackStrings.noMsg
import com.undercurrent.legacy.types.string.FeedbackStrings.yesMsg
import com.undercurrent.legacy.types.string.FeedbackStrings.yourMessageStr
import com.undercurrent.prompting.nodes.interactive_nodes.OperationNode
import com.undercurrent.prompting.nodes.interactive_nodes.TextInputNode
import com.undercurrent.prompting.nodes.interactive_nodes.YesNoInputNode
import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.view.treenodes.OutputNode
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.notifyAdmins

/**
 * Feedback Stages:
 * -> User enters 'feedback' command OR answers Y when encountering issue
 *
 * START
 * - User is prompted to send feedback
 * - User enters feedback
 * - User is prompted to confirm feedback
 * - User confirms or denies feedback sending
 * - User is notified that feedback was sent/not sent
 * - Admins receive feedback (if user sent feedback)
 */
class FeedbackCmdNodes(
    context: SystemContext,
    inputProvider: UserInputProvider? = null,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>? = null,
) : OperationNode<SystemContext>(
    //todo fix up test cases to allow passing in input/output providers
    context = context,
    inputProvider = inputProvider ?: context.inputter,
    nodeInterrupter = outputProvider ?: context.interrupter,
) {
    override suspend fun next(): TreeNode? {
        return FeedbackEntryNode()
    }

    inner class FeedbackEntryNode(
        private val fromString: String = "User",
    ) : TextInputNode(inputProvider, nodeInterrupter) {

        override suspend fun next(): TreeNode? {
            val feedbackPrompt1 = "Message to send to admins"

            //todo add additional case if feedback comes back null
            return fetchInput(feedbackPrompt1)?.let {
                FeedbackConfirmSendToAdminsNode(
                    it, fromString
                )
            }
        }
    }

    inner class FeedbackConfirmSendToAdminsNode(
        private val feedback: String,
        private val fromString: String = "User",
    ) : YesNoInputNode(inputProvider, nodeInterrupter) {

        override suspend fun next(): TreeNode? {
            fetchInput("$yourMessageStr $feedback", confirmStr)?.let {
                if (it) {
                    sendOutput(yesMsg)
                    return AdminReceiveFeedbackNode(feedback, fromString)
                } else {
                    sendOutput(noMsg)
                }
            }
            return null
        }
    }

    inner class AdminReceiveFeedbackNode(
        private val feedback: String,
        private val fromString: String = "User",
        val feedbackFormatter: (String, String) -> String = { fromStr, feedbackStr ->
            """
                |FEEDBACK MESSAGE
                |
                |MSG:
                |``$feedbackStr``
                |
                |FROM:
                |$fromStr
                |
            """.trimMargin()
        },
    ) : OutputNode(nodeInterrupter) {

        override suspend fun next(): TreeNode? {
            // Simulate the process of admins receiving the feedback
            // In a real-world scenario, this might involve sending the feedback to a database,
            // notifying the admins through an email or a dashboard, etc.

            val adminMsg = feedbackFormatter(fromString, feedback)

            notifyAdmins(adminMsg)

            sendOutput("Admins have received the following feedback: $feedback")
            Log.debug("Feedback received: $feedback")

            return null
        }
    }
}
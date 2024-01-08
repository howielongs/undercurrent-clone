package com.undercurrent.legacy.commands.registry.lifecycle.nodes

import com.undercurrent.legacyshops.nodes.shared_nodes.FeedbackCmdNodes
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.PROMPT_RETRIES
import com.undercurrent.shared.view.treenodes.TreeNode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FeedbackCmdFlowTest : BaseTestClass() {
    private val confirmStr = "Are you sure you want to send this to the admins?\n"

    private val yesMsg =
        "Great! Your message has been sent. Thank you for your feedback.\n" + "\n" + "Type 'feedback' at any time to send us your comments."

    private val noMsg =
        "No worries! We won't send that comment.\n" + "\n" + "Type 'feedback' at any time to send us your comments."

    @BeforeEach
    fun setup() {
        setUpWithRetries()
        PROMPT_RETRIES = 5
    }

    private fun setUpWithRetries(attempts: Int = 0) {
        val maxRetries = 5
        try {
            TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
            setupMocks()
            setUpShopContext()
        } catch (e: Exception) {
            e.printStackTrace()
            if (attempts < maxRetries) {
                setUpWithRetries(attempts + 1)
            } else {
                throw e
            }
        }
    }

    @Test
    fun `test FeedbackEntryNode`() = runBlocking {
        mockVendorInputQueue.setInput("Sample feedback message")
        val node =
            FeedbackCmdNodes(
                context = vendorContext,
                inputProvider = mockVendorInputQueue,
                outputProvider = vendorOutputs
            ).FeedbackEntryNode()
        node.next()
        asserts.assertContains(vendorOutputs.getOutput(), "Message to send to admins")
    }

    @Test
    fun `test FeedbackConfirmNode - Yes`() = runBlocking {
        mockVendorInputQueue.setInput("y")
        val node =
            FeedbackCmdNodes(
                context = vendorContext,
                inputProvider = mockVendorInputQueue,
                outputProvider = vendorOutputs
            ).FeedbackConfirmSendToAdminsNode("Sample feedback message")
        node.next()
        asserts.assertContains(vendorOutputs.getOutput(), confirmStr)
        asserts.assertContains(vendorOutputs.getOutput(), yesMsg)
    }

    @Test
    fun `test FeedbackConfirmNode - No`() = runBlocking {
        mockVendorInputQueue.setInput("n")
        val node =
            FeedbackCmdNodes(
                context = vendorContext,
                inputProvider = mockVendorInputQueue,
                outputProvider = vendorOutputs
            ).FeedbackConfirmSendToAdminsNode("Sample feedback message")
        node.next()
        asserts.assertContains(vendorOutputs.getOutput(), confirmStr)
        asserts.assertContains(vendorOutputs.getOutput(), noMsg)
    }

    @Test
    fun `test FeedbackCmdFlow from start to end with max validation violations`() = runBlocking {

        // Entry Node
        val startNode =
            FeedbackCmdNodes(
                vendorContext,
                mockVendorInputQueue,
                vendorOutputs,
            ).FeedbackEntryNode()

        mockVendorInputQueue.apply {
            add("Sample feedback message")
            add("yo whattup")
            add("yo whattup")
            add("yo whattup")
            add("yo whattup")
            add("yo whattup")
            add("y")
        }

        var nextNode: TreeNode? = startNode.next()

        nextNode = nextNode?.next()

        // Check final output
        asserts.assertContains(
            vendorOutputs.getOutput(),
            "Maximum attempts reached. Please try again later.",
            "Invalid input. Please enter",
            "Attempts remaining: 4",
            "Attempts remaining: 3",
            "Attempts remaining: 0",

            )

        asserts.assertDoesntContain(
            vendorOutputs.getOutput(),
            "We won't send that comment"
        )
    }

    @Test
    fun `test FeedbackCmdFlow from start to end with 2 yes-no validation violations`() = runBlocking {

        // Entry Node
        val startNode = FeedbackCmdNodes(
            vendorContext,
            mockVendorInputQueue,
            vendorOutputs,
        ).FeedbackEntryNode()

        mockVendorInputQueue.apply {
            add("Sample feedback message")
            add("yo whattup")
            add("yo whattup")
            add("y")
        }

        var nextNode: TreeNode? = startNode.next()

        nextNode = nextNode?.next()

        // Check final output
        asserts.assertContains(
            vendorOutputs.getOutput(),
            yesMsg,
            "Invalid input. Please enter",
            "Attempts remaining: 4",
            "Attempts remaining: 3"
        )
    }

    @Test
    fun `test queued inputs not enough yes no for FeedbackCmdFlow from start to end`() = runBlocking {
        val feedbackPrompt1 = "Message to send to admins"

        // Entry Node
        val startNode = FeedbackCmdNodes(
            vendorContext,
            mockVendorInputQueue,
            vendorOutputs,
        ).FeedbackEntryNode()
        mockVendorInputQueue.add("Sample feedback message")

        var nextNode: TreeNode? = startNode.next()

        // Confirm Node
        nextNode = nextNode?.next()

        //todo assert along the way here
        val invalidYesNoMsg = "Invalid input. Please enter 'yes' or 'no'."

        // Check final output
        val outputs = vendorOutputs.getOutput()
        asserts.assertContains(outputs, invalidYesNoMsg, feedbackPrompt1, "Are you sure")
    }

    @Test
    fun `test queued inputs for FeedbackCmdFlow from start to end`() = runBlocking {

        // Entry Node
        val startNode = FeedbackCmdNodes(
            vendorContext,
            mockVendorInputQueue,
            vendorOutputs,
        ).FeedbackEntryNode()
        mockVendorInputQueue.add("Sample feedback message")
        mockVendorInputQueue.add("yes")

        var nextNode: TreeNode? = startNode.next()

        // Confirm Node
        nextNode = nextNode?.next()

        //todo assert along the way here
        val invalidYesNoMsg = "Invalid input. Please enter 'yes' or 'no'."

        // Check final output
        asserts.assertContains(vendorOutputs.getOutput(), yesMsg)
    }

    @Test
    fun `test FeedbackCmdFlow from start to end`() = runBlocking {

        // Entry Node
        val startNode = FeedbackCmdNodes(
            vendorContext,
            mockVendorInputQueue,
            vendorOutputs,
        ).FeedbackEntryNode()
        mockVendorInputQueue.setInput("Sample feedback message")
        var nextNode: TreeNode? = startNode.next()

        // Confirm Node
        mockVendorInputQueue.setInput("y")
        nextNode = nextNode?.next()

        //todo assert along the way here

        // Check final output
        asserts.assertContains(vendorOutputs.getOutput(), yesMsg)
    }
}

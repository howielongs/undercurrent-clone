package com.undercurrent.legacy.runners.command_execution.nodes

import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.command_execution.CoreNodes
import com.undercurrent.system.repository.entities.messages.NotificationMessage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BotEntryNodeTest : BaseTestClass() {

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
        setupMocks()
    }

    @Test
    fun `test valid join code`() = runBlocking {
        val joinCodeToCreate = "123456"

        setUpShopContext(joinCodeToCreate)

        runJoinCmd("123456")
    }

    @Test
    fun `test START command`() = runBlocking {
        //todo still need to ensure TopCommands are using newer inputProvider
    }

    @Test
    fun `test feedback command`() = runBlocking {
        setUpShopContext()

        val msgIn = "feedback"

        val startNode = CoreNodes(
            body = msgIn,
            context = customerContext1,
            inputProvider = mockCustomerInputQueue1
        )
            .InitialPermissionsCheckNode()

        startNode.execute()

        val results = customerOutputs1.getOutput()

        asserts.assertContains(results, "Message to send to admins")

    }


    private suspend fun runJoinCmd(joinCodeToCreate: String, inputJoinCodeStr: String = joinCodeToCreate) {

        CoreNodes(
            body = inputJoinCodeStr,
            context = customerContext1,
            inputProvider = mockCustomerInputQueue1
        ).InitialPermissionsCheckNode().execute()

        val results = customerOutputs1.getOutput()
        asserts.assertContains(
            results,
            "You are now a customer of Shop $joinCodeToCreate",
            "Vendor shops you're linked to"
        )
        asserts.assertDoesntContain(results, "I don`t understand")

        assertDbusPathFormat()

    }

    /**
     * Bad: asamk/_Signal17857777777
     * Good: /org/asamk/Signal/_17857777777
     */
    private fun assertDbusPathFormat() {
        tx {
            NotificationMessage.all().forEach {
                asserts.assertContains(it.dbusPath, "/org/asamk/Signal/")
                asserts.assertDoesntContain(
                    it.dbusPath,
                    "asamk/_Signal17857777777",
                    "asamk/_Signal18158888888",
                    "asamk/_Signal19109999999",
                )
            }
        }

    }


    @Test
    fun `test invalid join code`() = runBlocking {
        val newJoinCode = "123456"
        val invalidJoinCode = "111111"
        setUpShopContext(newJoinCode)


        CoreNodes(
            body = invalidJoinCode,
            context = customerContext1,
            inputProvider = mockCustomerInputQueue1
        ).InitialPermissionsCheckNode().execute()

        val results = customerOutputs1.getOutput()
        asserts.assertDoesntContain(
            results,
            "You are now a customer of Shop $newJoinCode",
            "Vendor shops you're linked to"
        )
        asserts.assertContains(results, "I don`t understand")
    }

    @Test
    fun `test bot entry shop join code`() {
    }

    @Test
    fun `test first time show entry welcome message`() {
    }

    @Test
    fun `test does not show welcome message after first time`() {
    }
}
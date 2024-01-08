package com.undercurrent.legacy.runners.command_execution.nodes

import com.undercurrent.system.repository.entities.messages.NotificationMessage
import com.undercurrent.system.command_execution.CoreNodes
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.utils.tx
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SessionContextAtStartNodeTest : BaseTestClass() {

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
    fun `test ADDVENDOR command`() = runBlocking {
        //todo still need to ensure TopCommands are using newer inputProvider
    }



    private suspend fun runJoinCmd(joinCodeToCreate: String, inputJoinCodeStr: String = joinCodeToCreate) {

        CoreNodes(
            body = inputJoinCodeStr,
            context = customerContext1,
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


}
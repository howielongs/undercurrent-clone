package com.undercurrent.testutils

import com.undercurrent.shared.experimental.command_handling.CommandParser
import com.undercurrent.shared.experimental.command_handling.GlobalCommand
import com.undercurrent.shared.experimental.command_handling.RootCommand
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CommandParserTest : BaseAssertableTestClass() {

    @Test
    fun testValidCmdParsing() {
        assertCmd("start this thing", null)
        assertCmd("start", GlobalCommand.START)
        assertCmd("/start", GlobalCommand.START)
        assertCmd("/st art", GlobalCommand.START)
        assertCmd("st ar  t", GlobalCommand.START)
        assertCmd("/st a/rt", GlobalCommand.START)
        assertCmd("/st a/rt t", null)
        assertCmd("//  cancEl", GlobalCommand.CANCEL)
        assertCmd("yowhatup", null)

    }

    private fun assertCmd(input: String, expected: RootCommand?) {
        val nullStrMsg = "Result ought to be null for valid command parsed"
        val actual: RootCommand? = CommandParser.toValidCommand(input)

        expected?.let {
            assertEquals(
                expected = expected.handle().uppercase(),
                actual = actual!!.handle().uppercase(),
                message = "Expecting valid command"
            )
        } ?: assertNull(actual, message = nullStrMsg)
    }
}
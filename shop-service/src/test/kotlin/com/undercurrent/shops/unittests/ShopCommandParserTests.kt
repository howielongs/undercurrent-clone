package com.undercurrent.shops.unittests

import com.undercurrent.shops.asserts.BasicDBIT


class ShopCommandParserTests : BasicDBIT() {

    //todo Impl version of this for ShopCommands
//    @Test
//    fun testValidCmdParsing() {
//        assertCmd("start this thing", null)
//        assertCmd("start", UserCommand.START)
//        assertCmd("/start", UserCommand.START)
//        assertCmd("/st art", UserCommand.START)
//        assertCmd("st ar  t", UserCommand.START)
//        assertCmd("/st a/rt", UserCommand.START)
//        assertCmd("/st a/rt t", null)
//        assertCmd("//  cancEl", UserCommand.CANCEL)
//        assertCmd("yowhatup", null)
//
//    }
//
//    private fun assertCmd(input: String, expected: UserCommand?) {
//        val nullStrMsg = "Result ought to be null for valid command parsed"
//        val actual: UserCommand? = CommandParser.toValidCommand(input)
//
//        expected?.let {
//            assertEquals(
//                expected = expected.name.uppercase(),
//                actual = actual!!.name.uppercase(),
//                message = "Expecting valid command"
//            )
//        } ?: assertNull(actual, message = nullStrMsg)
//    }
}
package com.undercurrent.testutils

import com.undercurrent.shared.types.strings.CleanString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CleanStringTest : BaseAssertableTestClass() {

    @Test
    fun testClean() {
        assertClean("start", "START")
        assertClean("sta/rt", "START")
        assertClean("/start", "START")
        assertClean("/st art", "START")
        assertClean("/  st art", "START")
    }


    private fun assertClean(input: String, expected: String) {
        val result = CleanString(input).clean()
        assertEquals(expected, result, message = "Expecting equal strings after clean")
    }

}
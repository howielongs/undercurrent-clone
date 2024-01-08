package com.undercurrent.testutils

import com.undercurrent.shared.types.strings.CoreText.Questions.YesNoOptions
import com.undercurrent.shared.types.strings.CoreText.Questions.YesNoQuestion
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TextResourceLookupTest {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun assertYesNo(result: String) {
        assertContains(result, "Y. Yes")
        assertContains(result, "N. No")
    }

    @Test
    fun `invoke yesNoOptions and validate string out`() {
        YesNoOptions().let {
            val result = it()

            logger.info("Got: $result")
            assertYesNo(result)
        }
    }


    @Test
    fun `invoke correctYesNoQuestion and validate string out`() {
        YesNoQuestion.Correct().let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "Save?")
            assertYesNo(result)
        }
    }

    @Test
    fun `invoke overwriteYesNoQuestion and validate string out`() {
        YesNoQuestion.Overwrite().let {
            val result = it()

            logger.info("Got: $result")
            assertContains(result, "This operation cannot be undone.\nProceed with update?")
            assertYesNo(result)
        }
    }


}
package com.undercurrent.shared.types.validators

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Stream
import kotlin.test.assertFailsWith

class CurrencyValidatorTest {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val currencyValidator = CurrencyValidator(shouldThrowException = true)


    @ParameterizedTest(name = "given \"{0}\", when validating the price value, then it should return {1}")
    @MethodSource("validPriceArguments")
    fun `when valid price inserted should return refined value`(given: String, expected: String) {
        currencyValidator.validate(given).let {
            logger.info("Got: $it")
            assertEquals(expected, it)
        }
    }

    private companion object {
        @JvmStatic
        fun validPriceArguments() = Stream.of(
            Arguments.of("$1.00", "1.00"),
            Arguments.of("123", "123"),
            Arguments.of("$$ 100 $$ ", "100"),
        )
    }


    @ParameterizedTest(name = "validate should fail for {0}")
    @ValueSource(strings = ["one dollar", "123d", "100.999", ""])
    fun `when invalid  price inserted should fail`(failString: String) {
        assertFailsWith<CurrencyValidator.CurrencyValidatorException>(
            message = "Invalid format of currency for $failString.",
            block = {
                currencyValidator.validate(failString)
            }
        )
    }

}
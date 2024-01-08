package com.undercurrent.prompting.interaction_providers

import com.undercurrent.prompting.nodes.SmsInputUtil
import com.undercurrent.prompting.nodes.TextInputUtil
import com.undercurrent.prompting.nodes.YesNoInputUtil
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.repository.entities.Sms
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidationProviderTest : BaseTestClass() {


    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
    }

    @Test
    fun `test MenuSelectInputUtil valid ABC index selected`() = runBlocking {
        val validIndices = setOf("A", "B", "C")

        val inputSelection: String = "a"
        val outputSelection: String = "A"

//        MenuOptionValidation(validIndices,
//            { it.trim() }).validate("A").let {
//            assert(it == "A")
//        }
//
//        val input1 = "2625637291   "
//        val expectedValidatedOutput = "+12625637291"
//
//        mockInputProvider.setInput(input1)
//        val inputUtil = SmsInputUtil(mockInputProvider, mockOutputProvider)
//
//        val result1: Sms? = inputUtil.getInput("Send a phone number")
//        val resultStr = result1?.value
//
//        println("result1: $resultStr")
//
//        assert(resultStr == expectedValidatedOutput)
//        assert(resultStr != input1)
    }


    @Test
    fun `test SmsInputUtil valid sms without code - add country code`() = runBlocking {
        setupMocks()

        val input1 = "2625637291   "
        val expectedValidatedOutput = "+12625637291"

        mockVendorInputQueue.setInput(input1)
        val inputUtil = SmsInputUtil(mockVendorInputQueue, vendorOutputs)

        val result1: Sms? = inputUtil.fetchInput("Send a phone number")
        val resultStr = result1?.value

        println("result1: $resultStr")

        assert(resultStr == expectedValidatedOutput)
        assert(resultStr != input1)
    }

    @Test
    fun `test SmsInputUtil invalid sms`() = runBlocking {
        setupMocks()

        val input1 = "2625691   "
        val unexpectedValidatedOutput = "+12625637291"

        mockVendorInputQueue.setInput(input1)
        val inputUtil = SmsInputUtil(mockVendorInputQueue, vendorOutputs)

        val result1: Sms? = inputUtil.fetchInput("Send a phone number")
        val resultStr = result1?.value

        println("result1: $resultStr")

        assert(resultStr != unexpectedValidatedOutput)
        asserts.assertContains(vendorOutputs.getOutput(), "Invalid SMS number")
    }

    private suspend fun assertResponse(
        input: String,
        expectedOut: String,
        isSuccessful: Boolean = true
    ) {
        mockVendorInputQueue.setInput(input)
        val inputUtil = YesNoInputUtil(mockVendorInputQueue, vendorOutputs)

        val result1 = inputUtil.fetchInput("Send us a ${expectedOut.uppercase()}")
        val resultStr = result1?.text

        println("result: $resultStr")

        assert((resultStr == expectedOut) == isSuccessful) {
            "Input: ``$input``, Expected: ``$expectedOut``, Actual: ``$resultStr``"
        }

    }

    private suspend fun assertSuccessfulNo(input: String) {
        return assertResponse(input, "no")
    }

    private suspend fun assertSuccessfulYes(input: String) {
        return assertResponse(input, "yes")
    }

    private suspend fun assertUnsuccessful(input: String, expectedOut: String) {
        return assertResponse(input, expectedOut, isSuccessful = false)
    }

    @Test
    fun `test YesNoInputUtil`() = runBlocking {
        setupMocks()
        val successfulYesList = listOf("y ", "YES ", "yes", "yEs", "Y   ", "   Y")
        val successfulNoList = listOf("n ", "NO ", "no", "nO", "N   ", "   N")
        val unsuccessfulList = listOf("a ", "4 YES ", "ayes", "yEss", "Y2   ", "   Y Y")

        successfulYesList.forEach {
            assertSuccessfulYes(it)
        }

        successfulNoList.forEach {
            assertSuccessfulNo(it)
        }

        unsuccessfulList.forEach {
            assertUnsuccessful(it, "yes")
        }

        unsuccessfulList.forEach {
            assertUnsuccessful(it, "no")
        }
    }


    @Test
    fun `test TextInputUtil`() = runBlocking {
        setupMocks()
        val input1 = "SAMPLE TEXT   "
        val expectedValidatedOutput = "SAMPLE TEXT"

        mockVendorInputQueue.setInput(input1)
        val inputUtil = TextInputUtil(mockVendorInputQueue, vendorOutputs)

        val result1 = inputUtil.fetchInput("Send us some text")

        println("result1: $result1")

        assert(result1 == expectedValidatedOutput)
        assert(result1 != input1)
    }
}
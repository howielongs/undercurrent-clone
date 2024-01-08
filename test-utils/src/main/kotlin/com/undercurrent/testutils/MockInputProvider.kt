package com.undercurrent.testutils

import com.undercurrent.shared.messages.InterrupterMessageEntity
import com.undercurrent.shared.messages.UserInputProvider
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.time.EpochNano

// Mock classes for testing
open class MockInputProvider(private val formatter: TestIOFormatterProvider) : UserInputProvider {
    private var inputQueue = mutableListOf<String>()

    fun setInput(vararg inputs: String) {
        inputQueue.clear()
        add(*inputs)
    }

    fun add(vararg inputs: String) {
        inputQueue.addAll(inputs)
    }

    //see about how this might be reduced from being so exposed everywhere
    override suspend fun getRawInput(afterEpochNano: EpochNano): String? {
        return inputQueue.firstOrNull()?.let {
            formatter.formatAndPrintInput(this::class.simpleName, it)
            inputQueue.removeAt(0)
            it
        }
    }
}

open class MockOutputProvider(
    private val formatter: TestIOFormatterProvider
) : UserOutputProvider<InterrupterMessageEntity> {

    private val outputs = mutableListOf<String>()

    // Map to hold outputs by Role
    private val roleOutputs = mutableMapOf<AppRole, MutableList<String>>()
    override fun sendOutput(output: String): InterrupterMessageEntity? {
        outputs.add(output)
        formatter.formatAndPrintOutput(this::class.simpleName ?: "", output)
        return null
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        // Get or create the list of outputs for the given role
        val roleOutputList = roleOutputs.getOrPut(role) { mutableListOf() }
        // Add the output to the list for the given role
        roleOutputList.add(msgBody)
        // Optionally, format and print the output
        formatter.formatAndPrintOutput(role.toString(), msgBody)
    }

    fun getOutput(): String {
        return outputs.joinToString("\n")
    }

    // Function to get outputs by role
    fun getOutputByRole(role: AppRole): String {
        return roleOutputs[role]?.joinToString("\n") ?: ""
    }
}

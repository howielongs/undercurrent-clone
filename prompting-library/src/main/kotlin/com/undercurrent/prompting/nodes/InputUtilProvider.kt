package com.undercurrent.prompting.nodes

import com.undercurrent.shared.messages.*
import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.types.BtcMainnetAddress
import com.undercurrent.shared.types.BtcTestnetAddress
import com.undercurrent.shared.types.validators.*
import com.undercurrent.shared.utils.PROMPT_RETRIES
import com.undercurrent.shared.utils.asAbcHandle
import com.undercurrent.shared.utils.time.EpochNano

/**
 * Invalid index.
 *
 * Valid options: A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T
 *
 * Please try again by typing a letter, such as A, or use /cancel

 */
class InvalidIndexSelectedStringBuilder(
    private val validOptions: Set<String>
) {
    fun buildErrorMessage(): String {
        val validOptionsStr = validOptions.joinToString { "${it.asAbcHandle().uppercase()} " }

        return """
            |Invalid selection.
            |
            |Valid options: $validOptionsStr
            |
            |Please try again by typing a letter, such as A, or use /cancel
        """.trimMargin()
    }
}

//todo this can likely be cleaned up much more: after more robust testing in place

class MenuSelectInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    validOptions: Set<String>,
    maxAttempts: Int? = null,
    ) : BaseInputUtilProvider<String>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    validationProvider = ValidationProvider(
        validateFunc = { str ->
            validOptions.firstOrNull { it.asAbcHandle() == str.asAbcHandle() }
        },
        errorMessage = InvalidIndexSelectedStringBuilder(validOptions).buildErrorMessage()
    ),
    maxAttempts = maxAttempts ?: PROMPT_RETRIES
)

class TextInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : BaseInputUtilProvider<String>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    validationProvider = ValidationProvider(
        validateFunc = { it.trim() },
        errorMessage = "Invalid input."
    )
)

class CurrencyInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : BaseInputUtilProvider<String>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    validationProvider = ValidationProvider(
        dataValidator = CurrencyValidator(),
        errorMessage = "Invalid price value input."
    )
)

class SmsInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : BaseInputUtilProvider<Sms>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    validationProvider = ValidationProvider(
        dataValidator = SmsValidator(),
        errorMessage = "Invalid SMS number."
    )
)

class YesNoInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    maxAttempts: Int? = null,
) : BaseInputUtilProvider<YesNoWrapper>(
    inputProvider = inputProvider,
    outputProvider = outputProvider,
    validationProvider = ValidationProvider(
        validateFunc = { YesNoChar.validate(it.trim()) },
        errorMessage = "Invalid input. Please enter 'yes' or 'no'."
    ),
    footerBuilder = { YesNoFooterStringBuilder().buildFooterString() },
    maxAttempts = maxAttempts ?: PROMPT_RETRIES
)

class BtcAddressInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : BaseInputUtilProvider<BtcMainnetAddress>(
    inputProvider, outputProvider, ValidationProvider(
        dataValidator = BtcAddressValidator(),
        errorMessage = "Invalid address."
    )
)

class BtcTestAddressInputUtil(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
) : BaseInputUtilProvider<BtcTestnetAddress>(
    inputProvider, outputProvider, ValidationProvider(
        dataValidator = BtcTestAddressValidator(), errorMessage = "Invalid address."
    )
)


abstract class BaseInputUtilProvider<V>(
    inputProvider: UserInputProvider,
    outputProvider: UserOutputProvider<InterrupterMessageEntity>,
    validationProvider: ValidationProvider<V>,
    private val promptTransformer: (() -> String, Array<String>) -> Array<String> = { footerBuilder, prompts ->
        val lastPrompt = prompts.last() + footerBuilder()
        prompts.dropLast(1).toTypedArray() + lastPrompt
    },
    private val footerBuilder: () -> String = { "" },
    private val maxAttempts: Int = PROMPT_RETRIES
) : CanFetchInput<V> {

    private val inputFetcher: ValidatedInputFetcher<V> = ValidatedInputFetcher(
        userInputReader = inputProvider,
        outputProvider = outputProvider,
        validationProvider = validationProvider
    )

    override suspend fun fetchInput(vararg prompts: String): V? {
        return inputFetcher.fetchInput(*promptTransformer(footerBuilder, arrayOf(*prompts)))
    }

    //perhaps designate this more specifically as the "validate & retry" mechanism
    private inner class ValidatedInputFetcher<R>(
        private val userInputReader: UserInputProvider,
        private val outputProvider: UserOutputProvider<InterrupterMessageEntity>,
        private val validationProvider: ValidationProvider<R>,
    ) : CanFetchInput<R>, Validatable<R>, CanSendToUser<InterrupterMessageEntity>, CanReadUserInput {

        override suspend fun fetchInput(vararg prompts: String): R? {
            var attempts = 0
            while (attempts < maxAttempts) {
                attempts++

                val lastSentEpoch = EpochNano()

                prompts.forEach { sendOutput(it) }

                getRawInput(afterEpochNano = lastSentEpoch)?.let {
                    validate(it)
                }?.let {
                    return it
                }

                sendOutput(validationProvider.errorMessage + "\n\nAttempts remaining: ${maxAttempts - attempts}")
            }
            sendOutput("Maximum attempts reached. Please try again later.")
            return null
        }


        override fun validate(data: String): R? {
            return validationProvider.validate(data)
        }

        override fun sendOutput(message: String): InterrupterMessageEntity? {
            return outputProvider.sendOutput(message)
        }

        override suspend fun getRawInput(afterEpochNano: EpochNano): String? {
            return userInputReader.getRawInput(afterEpochNano)
        }
    }
}




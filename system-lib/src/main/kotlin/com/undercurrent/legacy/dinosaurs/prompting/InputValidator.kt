package com.undercurrent.legacy.dinosaurs.prompting


import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableElement
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.service.crypto.BitcoinWalletServices
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.legacy.types.enums.ResponseType
import com.undercurrent.legacy.types.string.PressAgent.Crypto.invalidCryptoAddress
import com.undercurrent.legacy.types.string.PressAgent.Validation.invalidSms
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.validators.YesNoValidator.validNo
import com.undercurrent.shared.types.validators.YesNoValidator.validYes
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.sendInterrupt
import org.bitcoinj.base.Address
import org.bitcoinj.core.NetworkParameters
import java.math.BigDecimal


object InputValidator {

    @Deprecated("Soon can get rid of this")
    fun validateInput(
        sessionContext: SystemContext,
        inputData: String,
        validationType: ResponseType,
        validIndices: List<String> = emptyList(),
        selectionIndexType: ListIndexTypeOld? = null,
        minNum: Int? = null,
        maxNum: Int? = null,
    ): String? {
        return if (validationType == ResponseType.INDEX) {
            if (validIndices.isNotEmpty()) {
                if (validIndices.contains(inputData.uppercase())) {
                    inputData
                } else {
                    sessionContext.interrupt(
                        "Invalid index.\n\nValid options: ${validIndices.joinToString(", ")}\n" +
                                "\nPlease try again${selectionIndexType?.hintText ?: ""}, or use /cancel"
                    )
                    //todo following this, should restart the job?
                    null
                }
            } else {
                sessionContext.interrupt(
                    "Unexpected error. " +
                            "Please try again${selectionIndexType?.hintText ?: ""}, or use /cancel"
                )

                null
            }
        } else {
            validationType.validateFunction(inputData, sessionContext)?.let {
                when (validationType) {
                    ResponseType.INT, ResponseType.POSITIVE_INT -> {
                        val thisValue = it.toInt()
                        if (minNum != null && thisValue < minNum) {
                            sessionContext.interrupt(
                                "Number cannot be below $minNum.\n" +
                                        "Please try again, or use /cancel to cancel operation."
                            )
                            null
                        } else if (maxNum != null && thisValue > maxNum) {
                            sessionContext.interrupt(
                                "Number cannot be above $maxNum.\n" +
                                        "Please try again, or use /cancel to cancel operation."
                            )
                            null
                        } else {
                            it
                        }
                        it
                    }

                    else -> {
                        it
                    }
                }
            }
        }
    }

    @Deprecated("Get rid of this overly rigid structure")
    fun validateString(data: String, context: SystemContext?): String? {
        return data.trim()
    }

    fun validateMobileCoinAddress(data: String, sessionContext: SystemContext? = null): String? {
        val ownerUser = sessionContext?.user ?: run {
            Log.error("Could not find user for validateMobileCoinAddress")
            return null
        }
        return sessionContext?.let {
            validateMobileCoinAddress(
                data = data.trim().replace(" ", ""),
                ownerUser = ownerUser,
                role = it.role,
                environment = it.environment
            )
        }
    }

    private fun validateStripeKey(prefixStr: String, data: String, sessionContext: SystemContext? = null): String? {
        if (data.contains(prefixStr)) {
            return data
        }

        sendError(
            sessionContext,
            "Invalid Stripe key (this key should start with `$prefixStr`).\n\nPlease try again, or use /cancel to cancel operation."
        )
        return null
    }

    fun validateStripeSecretLiveKey(
        data: String, sessionContext: SystemContext? = null
    ): String? {
        return validateStripeKey(prefixStr = "sk_live", data, sessionContext)
    }

    fun validateStripeSecretTestKey(
        data: String, sessionContext: SystemContext? = null
    ): String? {
        return validateStripeKey(prefixStr = "sk_test", data, sessionContext)
    }


    private fun validateMobileCoinAddress(
        data: String,
        ownerUser: User,
        role: AppRole,
        environment: Environment,
    ): String? {
        return try {
            return data
        } catch (e: Exception) {
            Log.error(invalidCryptoAddress() + "\n\n\t$data", e, this::class.java.simpleName)
            sendInterrupt(
                user = ownerUser,
                role = role,
                environment = environment,
                msg = invalidCryptoAddress()
            )
            null
        }
    }


    fun validateBitcoinAddress(data: String, sessionContext: SystemContext? = null): String? {
        val ownerUser = sessionContext?.user ?: run {
            Log.error("Could not find user for validateBitcoinAddress")
            return null
        }
        val role = sessionContext.role
        return validateBitcoinAddress(
            data = data.trim().replace(" ", ""),
            ownerUser = ownerUser,
            walletParams = BitcoinWalletServices.params(sessionContext.environment),
            role = role,
            environment = sessionContext.environment
        )
    }

    fun validateBitcoinAddress(
        data: String,
        ownerUser: User,
        environment: Environment,
        walletParams: NetworkParameters = BitcoinWalletServices.params(environment),
        role: AppRole,
    ): String? {
        return try {
            var sendAddress: Address = Address.fromString(walletParams, data)
            sendAddress.toString()
        } catch (e: Exception) {
            Log.error(invalidCryptoAddress() + "\n\n\t$data", e, this::class.java.simpleName)
            sendInterrupt(
                user = ownerUser,
                role = role,
                environment = environment,
                msg = invalidCryptoAddress()
            )
            null
        }
    }

    fun validateYesNo(data: String, sessionContext: SystemContext? = null): String? {
        if (validYes.contains(data.lowercase())) {
            return "Yes"
        } else if (validNo.contains(data.lowercase())) {
            return "No"
        }
        sendError(
            sessionContext,
            "Invalid Y/N (expecting 'Yes' or 'No').\n\nPlease try again, or use /cancel to cancel operation."
        )
        return null
    }

    //todo May need to use sessionPair? to satisfy tests and other functionality
    fun validateCurrency(data: String, sessionContext: SystemContext? = null): String? {
        return try {
            var cleanedData = data.replace("$", "")
            var returnValue = cleanedData.toBigDecimal()
            Log.debug("Cleaned value $data to $returnValue")
            returnValue.toString()
        } catch (e: NumberFormatException) {
            Log.error("Invalid format of currency for $data")
            sessionContext?.let {
                sendError(
                    it,
                    "Invalid number format.\nEnter the number like this:\n\t45.23\n\nPlease try again, or use /cancel to cancel operation."
                )
            }
            null
        }
    }

    //todo use this kind of thing in Entity classes for more organized lifecycle of information
    private fun sendError(sessionContext: SystemContext? = null, userMsg: String, logMsg: String? = null) {
        Log.error(logMsg ?: userMsg)

        sessionContext?.interrupt(userMsg)
    }

    fun validateFeePercent(data: String, sessionContext: SystemContext? = null): String? {
        try {
            val thisValue = BigDecimal(data)
            with(thisValue.toDouble()) {
                if (this in 0.0..100.0) {
                    return thisValue.toString()
                } else {
                    sendError(
                        sessionContext,
                        "Number cannot be below 0% or above 100%.\n" +
                                "Please try again, or use /cancel to cancel operation."
                    )
                    return null
                }
            }
        } catch (e: Exception) {
            // Move error messaging to string resources (inner class for namespacing?)
            sendError(
                sessionContext,
                "Invalid number format (expecting decimal number, e.g. '14.9' or '12').\n\n" +
                        "Please try again, or use /cancel to cancel operation."
            )
            return null
        }
        return data

    }


    fun validatePositiveInt(data: String, sessionContext: SystemContext? = null): String? {
        try {
            if (data.toInt() < 0) {
                sendError(
                    sessionContext,
                    "Number cannot be negative." +
                            "Please try again, or use /cancel to cancel operation."
                )
                return null
            }
        } catch (e: NumberFormatException) {
            //todo Move error messaging to string resources (inner class for namespacing?)
            sendError(
                sessionContext,
                "Invalid number format (expecting integer, e.g. '3').\n\n" +
                        "Please try again, or use /cancel to cancel operation."
            )
            return null
        }
        return data
    }


    fun validateInt(data: String, sessionContext: SystemContext? = null): String? {
        try {
            data.toInt()
        } catch (e: NumberFormatException) {
            //todo Move error messaging to string resources (inner class for namespacing?)
            sendError(
                sessionContext,
                "Invalid number format (expecting integer, e.g. '3').\n\n" +
                        "Please try again, or use /cancel to cancel operation."
            )
            return null
        }
        return data
    }

    /**
     * Takes userInput and checks:
     *  1. Correct type given by user
     *  2. Index value exists on map
     *
     *  Returns dereferenced value
     */
    fun dereferenceSelectedItem(
        userInput: String,
        selectionMap: HashMap<Int, SelectableElement>,
    ): Int? {
        val displayIndices = selectionMap.values.map { it.displayedIndex.lowercase() }
        if (displayIndices.contains(userInput.lowercase())) {
            return try {
                val element =
                    selectionMap.values.firstOrNull {
                        it.displayedIndex.lowercase() == userInput.lowercase()
                    }
                element?.dereferencedUid
            } catch (e: Exception) {
                Log.error("Unable to parse $userInput for list")
                null
            }
        }
        return null
    }


    //todo make use of this
    fun validateZipCode(data: String, sessionContext: SystemContext? = null): String? {
        Log.info("Validating zipcode $data")
        var isValid = true
        var code = data.trim()
        isValid = isValid && code.length == 5

        for (char in code) {
            isValid = isValid && char.isDigit()
        }

        //todo Extract to string resource
        return if (!isValid) {
            sendError(
                sessionContext,
                "Invalid zip code (expecting format '51354').\n\nPlease try again, or use /cancel to cancel operation."
            )
            null
        } else {
            data
        }
    }

    //todo Need some sort of user feedback if invalid b/c vendor already exists
    //todo Consider having other parts of this class in Framework, and extending for custom types
    fun validatePhoneNumber(data: String, sessionContext: SystemContext? = null): String? {
        Log.trace("Validating phone number: $data")
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val parsedNum = phoneUtil.parse(data, "US")
            if (!phoneUtil.isValidNumber(parsedNum)) {
                sessionContext?.let {
                    sendError(
                        sessionContext = it, userMsg = invalidSms(), logMsg = "Invalid phone number: $data"
                    )
                }
                return null
            }
            if (sessionContext?.isTestMode() ?: RunConfig.isTestMode) {
                Log.trace("Parsed num from $data: \n$parsedNum\n\n")
                Log.trace(displayPhoneFormat(phoneUtil, parsedNum, PhoneNumberFormat.INTERNATIONAL));
                Log.trace(displayPhoneFormat(phoneUtil, parsedNum, PhoneNumberFormat.NATIONAL));
                Log.trace(displayPhoneFormat(phoneUtil, parsedNum, PhoneNumberFormat.RFC3966));
                Log.trace(displayPhoneFormat(phoneUtil, parsedNum, PhoneNumberFormat.E164));
            }
            val transformedData = phoneUtil.format(parsedNum, PhoneNumberFormat.E164)
            Log.trace("Parsed num: $transformedData")
            return transformedData
        } catch (e: NumberParseException) {
            Log.error("NumberParseException was thrown for $data: $e", exception = e)

            //todo inject this as dependency (after pulling this to its own class)
            sessionContext?.let { sendError(it, invalidSms()) }
            return null
        }
    }

    private fun displayPhoneFormat(
        phoneUtil: PhoneNumberUtil,
        data: PhoneNumber,
        numberFormat: PhoneNumberFormat
    ): String {
        return "${numberFormat.name}:\t\t\t ${phoneUtil.format(data, numberFormat)}"
    }

}
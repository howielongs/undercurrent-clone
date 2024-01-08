package com.undercurrent.shared.types.strings

import com.undercurrent.shared.types.validators.SmsTextValidator
import com.undercurrent.shared.types.errors.NullValueValidationException
import com.undercurrent.shared.utils.Log

/**
 * TextType that can be encountered when gathering inputs from Users
 */
interface TextWrapper {
    val value: String?
}

interface ValidatableString {
    fun validate(): String?
}

abstract class ValidatableText(override val value: String?) : TextWrapper, ValidatableString {
    private var isValid: Boolean? = null

    override fun validate(): String? {
        return value?.let {
            isValid = true
            it
        } ?: run {
            isValid = false
            throw NullValueValidationException()
        }
    }
}

class SmsText(
    value: String?
) : ValidatableText(value) {
    private var validated: String? = null

    fun sms(): String {
        validated?.let {
            return it
        }
        validated = validate()
        return validated!! // Can't be null because of validate() is never null
    }

    override fun validate(): String {
        with(validated) {
            if (this != null) {
                return this
            }

            try {
                SmsTextValidator(value).validate()?.let {
                    validated = it
                    return it
                }
            } catch (e: Exception) {
                Log.error("Error validating sms: $value", e, this@SmsText::class.java.canonicalName)
                //todo figure out something better than this...
                throw e
            }

        }
    }
}

package com.undercurrent.shared.types.validators

import com.undercurrent.shared.types.errors.UuidValidationException


class SignalUuidValidator(
    private val text: String?,
) : TextValidator {
    override fun validate(): String {
        try {
            SmsTextValidator(text).validate()?.let {
//                return it
            }
        } catch (e: Exception) {
            throw UuidValidationException("Invalid UUID: $text", e)
        }

        with(text) {
            if (this == null) {
                throw UuidValidationException("UUID should not be null")
            }
            if (this.contains("+")) {
                throw UuidValidationException("Invalid UUID (contains +): $text")
            }
            if (!this.contains("-")) {
                throw UuidValidationException("Invalid UUID (no - char found): $text")
            }
        }
        return text!!
    }
}
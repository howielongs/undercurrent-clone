package com.undercurrent.shared.types.validators

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.undercurrent.shared.types.errors.SmsValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//todo base on Sms validation (reuse, and don't redeclare)
class SmsTextValidator(
    private val text: String?, private val region: String = "US"
) : TextValidator {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun validate(): String {
        with(PhoneNumberUtil.getInstance()) {
            try {
                val parsedNum = parse(text, region)
                if (!isValidNumber(parsedNum)) {
                    logger.warn("Invalid phone number: $text")
                    throw SmsValidationException("Invalid phone number: $text")
                }
                return format(parsedNum, PhoneNumberUtil.PhoneNumberFormat.E164)
            } catch (e: NumberParseException) {
                logger.warn("NumberParseException was thrown: $e", e)
                throw SmsValidationException("Phone parsing exception: $text", e)
            }
        }
    }
}
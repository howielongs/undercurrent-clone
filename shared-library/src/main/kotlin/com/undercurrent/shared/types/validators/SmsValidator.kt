package com.undercurrent.shared.types.validators

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.utils.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.SystemColor.text

class SmsValidator(
    private val region: String = "US",
    shouldThrowException: Boolean = false
) : BaseValidator<String, Sms>(shouldThrowException = shouldThrowException) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun validate(data: String): Sms? {
        with(PhoneNumberUtil.getInstance()) {
            try {
                val parsedNum = parse(data, region)
                if (!isValidNumber(parsedNum)) {
                    logger.error("Invalid phone number: $text")
                    return null
                }
                return Sms(format(parsedNum, PhoneNumberUtil.PhoneNumberFormat.E164))
            } catch (e: NumberParseException) {
                Log.error("NumberParseException was thrown: $e", exception = e)

                if (shouldThrowException) {
                    throw e
                }

                return null
            }
        }
    }

}


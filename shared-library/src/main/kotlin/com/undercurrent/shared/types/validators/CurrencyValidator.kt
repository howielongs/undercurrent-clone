package com.undercurrent.shared.types.validators

import com.undercurrent.shared.utils.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CurrencyValidator(shouldThrowException: Boolean = false) :
    BaseValidator<String, String>(shouldThrowException = shouldThrowException) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun validate(data: String): String? {
        try {
            val cleanedData = data.replace("$", "").replace(" ", "")
            Log.info("Cleaned value $data")

            // Price RegExp
            if (cleanedData.matches("\\d+(\\.\\d{1,2}\$)?".toRegex())) {
                return cleanedData
            } else {
                Log.warn("Invalid format of currency for $data")
                throw CurrencyValidatorException(data)
            }
        } catch (e: Exception) {
            Log.error("Error validating currency: $data", exception = e)

            if (shouldThrowException) {
                throw CurrencyValidatorException(data)
            }

            return null
        }
    }

    class CurrencyValidatorException(private val insertedCurrencyValue: String? = null) : Exception() {
        override val message: String?
            get() = insertedCurrencyValue ?: "Invalid price format ${insertedCurrencyValue}}"
    }

}

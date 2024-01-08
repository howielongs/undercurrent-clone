package com.undercurrent.shared.types.validators

import com.undercurrent.shared.types.validators.YesNoValidator.isValidNo
import com.undercurrent.shared.types.validators.YesNoValidator.isValidYes
import com.undercurrent.shared.utils.Log


//todo refactor this to resolve to types generically

abstract class YesNoWrapper(open val text: String)

class Yes(text: String = "yes") : YesNoWrapper(text) {
    override fun toString(): String {
        return "YES"
    }
}

class No(text: String = "no") : YesNoWrapper(text) {
    override fun toString(): String {
        return "NO"
    }
}

class YesNoChar(open val text: String? = null) : Validatable<YesNoWrapper> {

    override fun validate(data: String): YesNoWrapper? {
        return Companion.validate(data)
    }

    companion object : Validatable<YesNoWrapper> {
        private fun isYesValue(str: String?): Boolean {
            return isWrapperValue(str, ::isValidYes)
        }

        private fun isNoValue(str: String?): Boolean {
            return isWrapperValue(str, ::isValidNo)
        }

        private fun isWrapperValue(str: String?, func: (String) -> Boolean): Boolean {
            return str?.let { func(it.replace(" ", "")) } ?: false
        }

        //todo pull out with interface and dep-injection
        override fun validate(data: String): YesNoWrapper? {
            return when {
                isYesValue(data) -> Yes("yes")
                isNoValue(data) -> No("no")
                else -> {
                    Log.error("Invalid YesNoChar value: $data")
                    null
                }
            }
        }
    }
}

//todo pull into separate class with interface
object YesNoValidator {
    val validYes = listOf("yes", "y")
    val validNo = listOf("no", "n")

    fun isValidYes(data: String): Boolean {
        return validYes.contains(data.lowercase())
    }

    fun isValidNo(data: String): Boolean {
        return validNo.contains(data.lowercase())
    }
}

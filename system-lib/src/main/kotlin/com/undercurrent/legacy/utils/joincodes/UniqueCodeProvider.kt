package com.undercurrent.legacy.utils.joincodes

import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacy.utils.joincodes.AbcStringIndices.INT_MAX_CHAR_INDEX
import com.undercurrent.legacy.utils.joincodes.AbcStringIndices.STRING_MAX_CHAR_INDEX
import com.undercurrent.shared.types.errors.CoreException
import kotlin.random.Random

interface CanBuildRandomString {
    fun buildRandomStr(): String
}

object AbcStringIndices {
    const val STRING_MAX_CHAR_INDEX = 36
    const val INT_MAX_CHAR_INDEX = 10
}

enum class RandomStringType {
    MIXED,
    INT,
}

class RandomStringBuilder(
    val length: Int,
    private val prefix: String,
    type: RandomStringType = RandomStringType.MIXED,
) : CanBuildRandomString {

    private val maxCharIndex = when (type) {
        RandomStringType.MIXED -> STRING_MAX_CHAR_INDEX
        RandomStringType.INT -> INT_MAX_CHAR_INDEX
    }

    override fun buildRandomStr(): String {
        var code = prefix
        for (i in 0 until length) {
            code += nextChar(maxCharIndex)
        }
        return code
    }

    private fun nextChar(maxIndex: Int = maxCharIndex): String {
        Random.nextInt(until = maxIndex).let {
            return if (it > INT_MAX_CHAR_INDEX) {
                UtilLegacy.getCharForNumber(it - INT_MAX_CHAR_INDEX).uppercase()
            } else {
                it.toString()
            }
        }
    }
}


object JoinCodeUtils {
    const val JOIN_CODE_LENGTH: Int = 10
    const val JOIN_CODE_PREFIX: String = ""
}

interface RandomStringProvider {
    fun generate(): String
}

interface CanCheckForExisting<T> {
    fun alreadyExists(value: T): Boolean
}

interface UniqueCodeProvider : RandomStringProvider, CanCheckForExisting<String> {
    override fun alreadyExists(value: String): Boolean
}


abstract class UniqueRandomIntStringGenerator(
    length: Int,
    type: RandomStringType,
    prefix: String,
    private val maxAttempts: Int = 20,
    private val randStrBuilder: CanBuildRandomString = RandomStringBuilder(length, prefix, type),
) : CanBuildRandomString, UniqueCodeProvider {

    abstract override fun alreadyExists(value: String): Boolean

    override fun buildRandomStr(): String {
        return randStrBuilder.buildRandomStr()
    }

    override fun generate(): String {
        var numericCode = buildRandomStr()
        var attempts = 1

        while (alreadyExists(numericCode)) {
            if (attempts >= maxAttempts) {
                throw CoreException("Failed to generate unique code after $maxAttempts attempts")
            } else {
                numericCode = buildRandomStr()
                attempts++
            }
        }
        return numericCode
    }

}


class UniqueJoinCodeGenerator(
    type: RandomStringType = RandomStringType.MIXED,
    length: Int = JoinCodeUtils.JOIN_CODE_LENGTH,
    prefix: String = JoinCodeUtils.JOIN_CODE_PREFIX,
    private val existenceChecker: (String) -> Boolean = { code ->
        JoinCode.fetchByCode(code) != null
    },
) : UniqueRandomIntStringGenerator(
    type = type,
    length = length,
    prefix = prefix,
) {

    override fun alreadyExists(value: String): Boolean {
        return existenceChecker(value)
    }
}

package com.undercurrent.legacy.utils.joincodes

import com.undercurrent.legacy.utils.UtilLegacy
import kotlin.random.Random

interface StringGenerator {
    fun generate(): String
}


@Deprecated("Make use of UniqueJoinCodeGenerator instead")
class RandomAbcStringGenerator(
    private val numDigits: Int = 10,
    private val prependStr: String = ""
) : StringGenerator {

    override fun generate(): String {
        var code = prependStr
        for (i in 0 until numDigits) {
            Random.nextInt(until = 36).let {
                code += if (it > 10) {
                    UtilLegacy.getCharForNumber(it - 10).uppercase()
                } else {
                    it.toString()
                }
            }
        }
        return code
    }
}
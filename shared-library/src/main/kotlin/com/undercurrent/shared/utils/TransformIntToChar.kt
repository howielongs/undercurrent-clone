package com.undercurrent.shared.utils

import java.math.BigDecimal


interface Transformable<F, T> {
    fun transform(i: F): T
}

interface TransformableWithReverse<F, T> : Transformable<F, T> {
    fun reverseTransform(i: T): F
}

// OrdinalTransformer extends Transformable but doesn't introduce new methods.
interface OrdinalTransformer<T> : TransformableWithReverse<Int, T> {
}

typealias ToStringFunc<T> = (T) -> String

interface TransformableToString<T> : Transformable<T, String> {
    override fun transform(item: T): String
}

abstract class AbstractStrTransformer<T>(
    private val transformFunc: (T) -> String = { it.toString() }
) : TransformableToString<T> {

    override fun transform(item: T): String {
        return transformFunc(item)
    }
}


class IntToAbcTransformer : TransformableWithReverse<Int, String> {
    override fun transform(i: Int): String = when (i) {
        in 1..26 -> (i + 64).toChar().toString()
        in 27..52 -> "${(i - 26 + 64).toChar()}${(i - 26 + 64).toChar()}"
        else -> "${(i - 52 + 64).toChar()}${(i - 52 + 64).toChar()}${(i - 52 + 64).toChar()}"
        // TODO throw exception if i > 52 * 3
    }

    // TODO Reuse this instead of SelectPrompt<T, R> optionsMap: MutableMap<T, SelectableOption<T, R>>?
    override fun reverseTransform(letter: String): Int {
        val intChar = letter[0].uppercaseChar().toInt() - 64
        when (letter.length) {
            1 -> return intChar
            2 -> return intChar + 26
            3 -> return intChar + 52
            else -> throw IntToAbcFormatException()
        }
    }
}
class IntToAbcFormatException : Exception("AbcToIntTransformer only accepts values between A and ZZZ")


class IntToYesNoTransformer : TransformableWithReverse<Int, String> {
    override fun transform(i: Int): String = when (i) {
        1 -> "Y"
        2 -> "N"
        else -> "N"
    }

    override fun reverseTransform(i: String): Int {
            TODO("Not yet implemented") // And most likely never will be needed
    }
}

open class AbcMenuOrdinalTransformer(
    private val transformer: TransformableWithReverse<Int, String>
) : OrdinalTransformer<String> {

    override fun transform(i: Int): String {
        return transformer.transform(i)
    }

    override fun reverseTransform(i: String): Int {
        return transformer.reverseTransform(i)
    }
}

class OrdinalToAbcTransformer : AbcMenuOrdinalTransformer(IntToAbcTransformer())
class OrdinalToYesNoTransformer : AbcMenuOrdinalTransformer(IntToYesNoTransformer())


class DecimalListTransformer(val leadingValue: Int) : TransformableToString<BigDecimal> {
    override fun transform(item: BigDecimal): String {
        return "${leadingValue + 1}.${item.toPlainString()}"
    }
}

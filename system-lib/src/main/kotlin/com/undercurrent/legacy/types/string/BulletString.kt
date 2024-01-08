package com.undercurrent.legacy.types.string

import com.undercurrent.legacy.repository.entities.payments.CryptoAmountLegacy
import com.undercurrent.legacy.utils.UtilLegacy
import org.jetbrains.exposed.sql.transactions.transaction

class BulletString(var outString: String = "") {
    class Builder {
        var startString: String = ""
    }

    companion object {
        operator fun invoke(body: Builder.() -> Unit = {}): BulletString {
            val builder = Builder().apply(body)
            return BulletString(
                outString = builder.startString
            )
        }
    }

    private fun lineBullet(): String {
        return "• "
    }

    private fun lineItem(str: String?): String {
        return str?.let {
            "$str\n"
        } ?: ""
    }

    private fun lineItem(field: String, str: String?): String {
        return str?.let {
            "${lineBullet()}$field: $str\n"
        } ?: ""
    }

    fun close(): BulletString {
        add("=======================")
        return this
    }

    fun divide(): BulletString {
        add(UtilLegacy.DIVIDER_STRING.trim() + "\n")
        return this
    }

    fun section(str: String? = null, displayLine: Boolean = false, trimHeader: Boolean = false): BulletString {
        val headerText = str?.let { str } ?: ""
        if (displayLine) {
            add("${UtilLegacy.DIVIDER_STRING}$headerText")
        } else {
            if (trimHeader) {
                add(headerText)
            } else {
                add("\n" + headerText)
            }
        }
        return this
    }

    fun add(str: String?): BulletString {
        outString += lineItem(str)
        return this
    }

    fun add(field: String, str: String?): BulletString {
        outString += lineItem(field, str)
        return this
    }

    //todo should also be able to convert to/from fiat easily
    fun add(label: String, amountCrypto: CryptoAmountLegacy): BulletString {
        transaction { amountCrypto.cryptoType }?.let {
            add(label, amountCrypto.toRoundedMacroString(showLabel = true))
        }
        return this
    }

    fun line(): BulletString {
        outString += "\n"
        return this
    }
}
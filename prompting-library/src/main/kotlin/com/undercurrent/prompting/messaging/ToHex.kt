package com.undercurrent.prompting.messaging

fun ByteArray.toHex(): String =
    joinToString(separator = ",") {
        val hexValue = "%02x".format(it)
        "0x$hexValue"
    }.replace(" ", "")
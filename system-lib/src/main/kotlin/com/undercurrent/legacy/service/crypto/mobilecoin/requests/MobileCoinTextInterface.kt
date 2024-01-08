package com.undercurrent.legacy.service.crypto.mobilecoin.requests

import com.google.gson.JsonElement


interface MobileCoinTextInterface {
    var value: String
}

open class MobileCoinText(override var value: String) : MobileCoinTextInterface {
    constructor(valueIn: JsonElement?) : this(value = valueIn.toString())

    var cleanedValue: String = ""
        get() = value.replace("\"", "")
}

class MobAcctNameText(valueIn: String) : MobileCoinText(valueIn) {
    constructor(valueIn: JsonElement?) : this(valueIn = valueIn.toString())
}

class MobMnemonicText(valueIn: String) : MobileCoinText(valueIn) {
    fun toArray(): List<String> {
        return value.split(" ")
    }
}


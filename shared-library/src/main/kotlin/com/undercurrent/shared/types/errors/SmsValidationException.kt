package com.undercurrent.shared.types.errors

class SmsValidationException (private val s: String? = null, private val e :Exception) : Exception() {

    constructor(s: String? = null) : this(s, Exception())

    override val message: String?
        get() = s ?: "Validation fail for sms verification. ${e.stackTraceToString()}"
}
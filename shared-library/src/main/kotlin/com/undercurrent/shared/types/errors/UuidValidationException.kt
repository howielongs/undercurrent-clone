package com.undercurrent.shared.types.errors

class UuidValidationException (private val s: String? = null, private val e :Exception) : Exception() {

    constructor(s: String? = null) : this(s, Exception())

    override val message: String?
        get() = s ?: "Validation fail for uuid verification. ${e.stackTraceToString()}"
}
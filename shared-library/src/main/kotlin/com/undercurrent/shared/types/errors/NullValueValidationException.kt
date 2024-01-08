package com.undercurrent.shared.types.errors

class NullValueValidationException (private val s: String? = null) : Exception() {

    override val message: String?
        get() = s ?: "Null value validation failed"
}
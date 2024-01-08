package com.undercurrent.shared.types.validators

interface Validatable<T> {
    fun validate(data: String): T?
}



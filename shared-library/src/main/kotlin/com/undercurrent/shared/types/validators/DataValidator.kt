package com.undercurrent.shared.types.validators

interface DataValidator<T, R> {
    fun validate(data: T): R?
}

abstract class BaseValidator<T, R>(
    protected val shouldThrowException: Boolean = false,
) : DataValidator<T, R>
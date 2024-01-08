package com.undercurrent.prompting.nodes

import com.undercurrent.shared.types.validators.DataValidator
import com.undercurrent.shared.types.validators.Validatable

open class ValidationProvider<T>(
    private val validateFunc: (String) -> T?,
    val errorMessage: String,
) : Validatable<T> {

    constructor(
        dataValidator: DataValidator<String, T>,
        errorMessage: String
    ) : this(
        validateFunc = { dataValidator.validate(it) },
        errorMessage = errorMessage
    )

    override fun validate(data: String): T? {
        return validateFunc(data)
    }
}


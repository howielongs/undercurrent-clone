package com.undercurrent.shared.types.errors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias CoreException = Exception

open class BaseError(
    open val msg: String = "Error thrown",
    open val e: Exception? = null,
)  {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

}


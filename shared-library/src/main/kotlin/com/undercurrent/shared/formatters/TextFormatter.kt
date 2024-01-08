package com.undercurrent.shared.formatters

interface TextFormatter<T> {
    fun format(data: T): String
}
package com.undercurrent.legacy.service.csvutils.csv_linewriters

sealed class CsvLineWriter<T>(val item: T) {
    abstract suspend fun write(): String

    protected fun StringBuilder.appendValue(value: String): StringBuilder {
        return this.append("\"").append(value).append("\",")
    }
}
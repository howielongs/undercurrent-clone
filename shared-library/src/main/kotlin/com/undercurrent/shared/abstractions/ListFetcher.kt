package com.undercurrent.shared.abstractions

interface ListFetcher<T> {
    fun fetchList(): List<T>
}
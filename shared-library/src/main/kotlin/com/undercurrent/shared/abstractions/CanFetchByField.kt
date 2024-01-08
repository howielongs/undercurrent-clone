package com.undercurrent.shared.abstractions

interface CanFetchByField<F, R> {
    suspend fun fetch(field: F): R?
}
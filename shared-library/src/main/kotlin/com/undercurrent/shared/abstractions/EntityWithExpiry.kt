package com.undercurrent.shared.abstractions

import com.undercurrent.shared.repository.bases.EntityMappedToTable
import org.jetbrains.exposed.sql.Column

interface EntityWithExpiry : EntityMappedToTable, Expirable {
    var expiryEpoch: Long?
}

interface TableWithExpiry {
    val expiryEpoch: Column<Long?>
}

interface CanFetchByCode<T> {
    fun fetchByCode(code: String): T?
}
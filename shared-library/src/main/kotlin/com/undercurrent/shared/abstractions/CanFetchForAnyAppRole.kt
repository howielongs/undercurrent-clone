package com.undercurrent.shared.abstractions

import com.undercurrent.shared.types.enums.AppRole

interface CanFetchForAnyAppRole<T> {
    suspend fun fetchRoles(entity: T): Set<AppRole>
}

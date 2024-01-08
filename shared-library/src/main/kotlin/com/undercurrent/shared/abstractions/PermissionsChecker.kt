package com.undercurrent.shared.abstractions

interface PermissionsChecker {
    suspend fun hasValidPermissions(): Boolean
}
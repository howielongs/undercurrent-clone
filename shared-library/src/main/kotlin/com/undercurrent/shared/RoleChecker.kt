package com.undercurrent.shared

import com.undercurrent.shared.types.enums.AppRole

interface RoleChecker {
    suspend fun matchesAtLeastOneRole(vararg appRoles: AppRole): Boolean
}
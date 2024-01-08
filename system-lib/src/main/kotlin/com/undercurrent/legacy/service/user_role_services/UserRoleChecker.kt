package com.undercurrent.legacy.service.user_role_services

import com.undercurrent.shared.RoleChecker
import com.undercurrent.shared.abstractions.CanFetchForAnyAppRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.system.repository.entities.User

class UserRoleChecker(
    val user: User,
    private val roleChecker: CanFetchForAnyAppRole<User> = UserRoleFetcher()
) : RoleChecker, CanFetchForAnyAppRole<User> {

    override suspend fun fetchRoles(entity: User): Set<AppRole> {
        return roleChecker.fetchRoles(entity)
    }

    override suspend fun matchesAtLeastOneRole(vararg appRoles: AppRole): Boolean =
        fetchRoles(user).any { it in appRoles }

}
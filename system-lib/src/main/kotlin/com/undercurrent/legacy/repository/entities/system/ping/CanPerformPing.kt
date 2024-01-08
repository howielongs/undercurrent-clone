package com.undercurrent.legacy.repository.entities.system.ping

import com.undercurrent.system.repository.entities.User

interface CanPerformPing {
    suspend fun pingAllAdmins()
    suspend fun pingAllRolesForUser(user: User)
    suspend fun pingUsers(users: List<User>)
}
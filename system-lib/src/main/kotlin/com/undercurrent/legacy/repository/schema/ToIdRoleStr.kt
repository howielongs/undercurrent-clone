package com.undercurrent.legacy.repository.schema

import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.system.context.SystemContext

fun toIdRoleStr(sessionContext: SystemContext): String {
    return "${UserToIdString.toIdStr(sessionContext.user)} ${sessionContext.role})"
}

fun toIdRoleStr(user: User, role: AppRole): String {
    return "${UserToIdString.toIdStr(user)} (${role.name})"
}
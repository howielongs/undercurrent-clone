package com.undercurrent.shared.types.enums

object RoleTransformers {
    fun fromString(str: String?): AppRole? {
        return str?.let {
            ShopRole.values().find { it.name == str }
        }
    }

    fun fromRole(role: AppRole?): String? {
        return role?.name ?: null
    }

    fun fromAbbrev(abbrev: String): AppRole? {
        ShopRole.values().forEach {
            if (abbrev.uppercase() == it.name.uppercase()) {
                return it
            }
        }
        return null
    }

}
package com.undercurrent.legacyshops.repository.entities

import com.undercurrent.shared.repository.dinosaurs.EntityWithStatus
import com.undercurrent.shared.utils.tx

class StatusMatchChecker {

    fun statusDoesNotMatchAny(entityWithStatus: EntityWithStatus, vararg status: Enum<*>): Boolean {
        status.map { it.name.uppercase() }.toList().forEach {
            if (entityHasStatus(entityWithStatus, it)) {
                return false
            }
        }
        return true

    }

    fun entityHasStatus(entity: EntityWithStatus, status: String): Boolean {
        return tx { entity.isNotExpired() && entity.status.uppercase() == status.uppercase() }
    }

}
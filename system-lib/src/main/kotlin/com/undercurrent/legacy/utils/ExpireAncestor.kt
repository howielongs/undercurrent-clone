package com.undercurrent.legacy.utils

import com.undercurrent.system.context.SystemContext
import com.undercurrent.legacy.repository.entities.system.Ancestors
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.utils.tx

fun expireAncestor(
    entity: RootEntity0,
    sessionContext: SystemContext,
    newId: Int,
    newMemo: String,
) {
    //todo will need to reexamine transaction wrappings
    val newAncestor = tx {
        Ancestors.new {
            ownerUser = sessionContext.user
            ownerRole = sessionContext.role
            entityType = entity::class.simpleName.toString()
            oldEntity = entity.uid
            newEntity = newId
            memo = newMemo
        }
    }
    newAncestor.save(sessionContext.role)
    entity.expire()
}
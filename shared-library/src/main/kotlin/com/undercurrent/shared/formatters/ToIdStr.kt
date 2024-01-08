package com.undercurrent.shared.formatters

import com.undercurrent.shared.abstractions.CanOutputUid
import com.undercurrent.shared.utils.tx

object UserToIdString {
    /**
     * User -> User #1
     */
    fun toIdStr(entity: CanOutputUid, label: String? = null): String {
        val singularLabel = label ?: entity::class.java.simpleName
        return "${singularLabel.capitalize()} #${tx { entity.fetchId() }}"
    }

}



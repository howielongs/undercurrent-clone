package com.undercurrent.shared.repository.entities

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column

interface JoinCodeEntity<T> {
    var code: T
}

interface JoinCodeTable {
    val code: Column<String>
    val parent: Column<EntityID<Int>?>
}
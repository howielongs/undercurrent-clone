package com.undercurrent.shared.repository.bases.system

import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import org.jetbrains.exposed.dao.id.EntityID

abstract class SystemEntity(id: EntityID<Int>, table: SystemTable) : RootEntity0(id, table)

abstract class SystemTable(
    tableName: String,
) : RootTable0(tableName)

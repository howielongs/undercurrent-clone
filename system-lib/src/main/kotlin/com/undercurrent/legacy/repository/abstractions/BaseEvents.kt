package com.undercurrent.legacy.repository.abstractions

import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID

@Deprecated("Reduce usage of this sort of inheritance")
abstract class BaseEventsTable(
         thisTableName: String
) : ExposedTableWithStatus2(thisTableName) {

    val tag = varchar("tag", VARCHAR_SIZE).default("")
    val type = varchar("type", VARCHAR_SIZE).default("")
    val memo = varchar("memo", VARCHAR_SIZE).default("")

    val raw = varchar("raw", VARCHAR_SIZE).default("")
    val json = varchar("json", VARCHAR_SIZE).default("")

    

    override fun singularItem(): String {
        return "scan event"
    }
}

@Deprecated("Reduce usage of this sort of inheritance")
abstract class BaseEventsEntity(
        id: EntityID<Int>, table: BaseEventsTable
) : ExposedEntityWithStatus2(id, table) {

    var tag by table.tag
    var type by table.type
    var memo by table.memo

    var raw by table.raw
    var json by table.json
}


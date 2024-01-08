package com.undercurrent.shared.repository.dinosaurs

import com.undercurrent.shared.abstractions.EntityWithExpiry
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column


interface TableWithStatus {
    val status: Column<String>
}

interface EntityWithStatus : EntityWithExpiry {
    var status: String
}


abstract class ExposedTableWithStatus2(
    thisTableName: String
) : TableWithLabels1(thisTableName),
    TableWithStatus {

    @Deprecated("Remove status from most tables")
    override val status: Column<String> = varchar("status", VARCHAR_SIZE).default("")
}


abstract class ExposedEntityWithStatus2(
    id: EntityID<Int>,
    thisTable: ExposedTableWithStatus2
) : EntityWithLabels1(id, thisTable),
    EntityWithStatus {

    override var status by thisTable.status
}

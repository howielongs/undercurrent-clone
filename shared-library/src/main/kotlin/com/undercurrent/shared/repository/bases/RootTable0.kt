package com.undercurrent.shared.repository.bases

import com.undercurrent.shared.abstractions.TableWithExpiry
import com.undercurrent.shared.utils.Util
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime

//consider adding transformer to convert epoch to datetime or vice versa
abstract class RootTable0(
    tableName: String,
) : IntIdTable(name = tableName), TableWithExpiry {
    val createdDate = datetime("created_date").clientDefault { Util.getCurrentUtcDateTime() }
    val updatedDate = datetime("updated_date").clientDefault { Util.getCurrentUtcDateTime() }
    override val expiryEpoch: Column<Long?> = long("expiry_epoch").nullable()
}
package com.undercurrent.shared.repository.dinosaurs


import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.id.EntityID

interface HasSingularLabel {
    fun singularItem(): String
}


interface HasPluralLabel {
    fun pluralItems(): String
}

interface HasMultLabels : HasSingularLabel, HasPluralLabel


//todo unsure what to do with this
fun ExposedEntityWithStatus2.entityHasStatus(status: String): Boolean {
    return tx { this@entityHasStatus.isNotExpired() && this@entityHasStatus.status.uppercase() == status.uppercase() }
}


abstract class TableWithLabels1(tableName: String) : RootTable0(tableName),
    HasSingularLabel, HasPluralLabel {

    @Deprecated("Get rid of this blight")
    override fun singularItem(): String {
        return tableName
    }

    @Deprecated("Get rid of this blight")
    override fun pluralItems(): String {
        return singularItem() + "s"
    }
}

abstract class EntityWithLabels1(
    id: EntityID<Int>,
    private val thisTable: TableWithLabels1,
) : RootEntity0(id, thisTable),
    HasMultLabels {

    @Deprecated("Get rid of this blight")
    override fun singularItem(): String {
        return thisTable.singularItem()
    }

    @Deprecated("Get rid of this blight")
    override fun pluralItems(): String {
        return thisTable.pluralItems()
    }

}





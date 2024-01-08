package com.undercurrent.legacy.commands.executables.abstractcmds

import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.system.context.SessionContext
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.service.PermissionsValidator
import com.undercurrent.legacy.types.enums.ListIndexTypeOld

interface CanRunPreFunc {
    suspend fun preFunc(): Boolean
}

interface CanCheckShouldShow {
    fun shouldShow(): Boolean
}

interface HasUnchangedMsg {
    fun unchangedMsg(): String
}

interface HasEntityTable {
    fun entityTable(): ExposedTableWithStatus2?
}

interface HasSingularItem {
    fun singularItem(): String
}

interface HasPluralItems {
    fun pluralItems(): String
}

interface CanListHeaderText {
    fun listHeaderText(verbInfinitive: String): String
}

interface CanListIndexType {
    fun listIndexType(): ListIndexTypeOld
}

abstract class SelectEntityCmd(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
) : Executable(thisCommand, sessionContext), CanCheckShouldShow, CanListIndexType,
    CanListHeaderText, CanRunPreFunc, HasUnchangedMsg,
    HasEntityTable,
    HasSingularItem,
    HasPluralItems {

    abstract fun sourceList(): List<ExposedEntityWithStatus2>

    override fun entityTable(): ExposedTableWithStatus2? {
        return thisCommand.entityClass
    }

    override fun unchangedMsg(): String {
        return "Nothing has changed with ${pluralItems()}. Operation complete."
    }

    @Deprecated("Get rid of this sort of thing")
    override fun singularItem(): String {
        return entityTable()?.singularItem() ?: "item"
    }

    @Deprecated("Get rid of this sort of thing")
    override fun pluralItems(): String {
        return entityTable()?.pluralItems() ?: "items"
    }


    override suspend fun preFunc(): Boolean {
        if (PermissionsValidator.hasValidPermissionsForOperation(
                sessionContext,
                thisCommand,
                true
            ) && !sourceList().isNullOrEmpty()
        ) {
            return true
        }
        sessionContext.interrupt(emptyText()) //consider throwing exception here
        return false
    }

    override fun shouldShow(): Boolean {
        return sourceList().isNotEmpty()
    }

    override fun listHeaderText(verbInfinitive: String): String {

        val byListIndexText = listIndexType().displayName?.let {
            " by $it"
        } ?: ""

        return "Select ${singularItem().lowercase()}$verbInfinitive$byListIndexText:"
    }

    override fun listIndexType(): ListIndexTypeOld {
        return ListIndexTypeOld.ABC
    }

    open fun emptyText(): String {
        return "No ${pluralItems()} found"
    }

}


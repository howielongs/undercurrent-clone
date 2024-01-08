package com.undercurrent.legacy.dinosaurs.prompting.selectables

import com.undercurrent.legacy.data_transfer_objects.CommandWrapper
import com.undercurrent.legacy.types.enums.ListIndexTypeOld
import com.undercurrent.shared.repository.dinosaurs.EntityWithLabels1
import com.undercurrent.system.context.SystemContext
import kotlin.reflect.KSuspendFunction1

sealed class SelectedListOption(
    open var deref: String? = null,
    open var promptText: String? = null,
    var selectionHandle: String = "",
    var selectionHandleType: ListIndexTypeOld = ListIndexTypeOld.ABC,
    var fullLineText: String = "",
)

data class SelectedCommand(
    val command: CommandWrapper,
) : SelectedListOption() {
    init {
        deref = command?.handle
    }
}


data class SelectedEnum(
    val enum: Enum<*>
) : SelectedListOption() {
    init {
        deref = enum.name
    }
}

data class SelectedCallback(
    val callback: KSuspendFunction1<SystemContext, Unit>?,
) : SelectedListOption() {
    init {
        deref = promptText
    }
}

data class SelectedEntity(
    val entity: EntityWithLabels1,
) : SelectedListOption() {
    init {
        deref = entity.uid.toString()
    }
}

//todo add option for Yes/No

data class SelectedText(
        override var promptText: String? = null,
) : SelectedListOption()


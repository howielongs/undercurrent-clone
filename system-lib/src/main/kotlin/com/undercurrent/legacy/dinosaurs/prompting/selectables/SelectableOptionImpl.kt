package com.undercurrent.legacy.dinosaurs.prompting.selectables


import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.data_transfer_objects.CommandWrapper
import com.undercurrent.shared.repository.dinosaurs.EntityWithLabels1
import com.undercurrent.system.context.SystemContext
import kotlin.reflect.KSuspendFunction1

//todo need overlapping inputs even if they're null?
//this may not be a good usage of sealed class
//try interface if this doesn't work as expected

sealed class SelectableOptionImpl(
    open val promptText: String? = null,
)


data class SelectableText(
    override val promptText: String?
) : SelectableOptionImpl()


data class SelectableEnum(
    override val promptText: String? = null,
    val enumValue: Enum<*>
) : SelectableOptionImpl()

data class SelectableCallback(
    override val promptText: String? = null,
    val callback: KSuspendFunction1<SystemContext, Unit>?,
) : SelectableOptionImpl()


data class SelectableEntity(
    val entity: EntityWithLabels1,
    override val promptText: String? = null,
) : SelectableOptionImpl()


data class SelectableCommand(
    private val commandEnum: BaseCommand,
    override val promptText: String? = null,
) : SelectableOptionImpl() {
    val command = CommandWrapper
        .fromCommandEnum(commandEnum)
}


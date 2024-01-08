package com.undercurrent.legacy.commands.registry

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.shared.ValidCmdInput
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.system.context.SystemContext
import kotlin.reflect.KSuspendFunction1

typealias HandlerClassType = Class<out Executable>?
typealias CallbackType = KSuspendFunction1<SystemContext, Unit>?

//todo reduce number of fields on each
sealed interface BaseCommand : ValidCmdInput {
    val hint: String
    val permissions: Set<AppRole>
    val prompt: String?

    @Deprecated("Move to using RunnerFunc")
    val callback: CallbackType
    val priority: Int

    @Deprecated("Move to using RunnerFunc")
    val displayAs: String

    @Deprecated("Move to using RunnerFunc")
    val handlerClass: HandlerClassType

    @Deprecated("Move to using RunnerFunc")
    val entityClass: ExposedTableWithStatus2?

    val simpleHelp: String?
    val runnerFunc: RunnerFuncType

    fun selectable(): SelectableEnum
    fun upper(): String
    fun lower(): String
    fun withSlash(): String
}
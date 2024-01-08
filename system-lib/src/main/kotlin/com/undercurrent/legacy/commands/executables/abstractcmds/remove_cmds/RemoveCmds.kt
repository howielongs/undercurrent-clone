package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.legacy.commands.executables.abstractcmds.CanListIndexType
import com.undercurrent.legacy.commands.executables.abstractcmds.SelectEntityCmd
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.dinosaurs.prompting.TextBox.removalVerifyString
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.OptionSelector
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEntity
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectedEntity
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.repository.dinosaurs.EntityWithLabels1
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

interface HasHeadlineText {
    fun headlineText(): String
}

sealed class RemoveCmds(
    override val thisCommand: BaseCommand,
    sessionContext: SessionContext,
    private val singularItemLabel: String? = null,
) : SelectEntityCmd(
    thisCommand = thisCommand,
    sessionContext = sessionContext,
), HasHeadlineText, CanListIndexType {
    //verb "remove" may be abstracted out for future use
    open fun verifyFunc(selection: SelectedEntity): String {
        return """You selected to remove:
            |
            |${selection.fullLineText}
            |
            |${PressAgent.continueYesNoQuestion()}
        """.trimMargin()
    }


    private fun doFunc(
        entity: EntityWithLabels1,
    ): Boolean {
        return entity.expire()
    }

    private fun postFunc(
        sessionContext: SessionContext,
    ) {
        sourceList()?.let { items ->
            val result = with(items.map { SelectableEntity(it) }) {
                if (this.isEmpty()) {
                    null
                } else {
                    OptionSelector(
                        this,
                        headerText = listHeaderText("to remove"),
                        indexType = listIndexType(),
                        isSelectable = false,
                        footerText = "",
                        headlineText = null,
                    ).let {
                        it.promptString
                    }
                }
            }

            result?.let {
                sessionContext.interrupt(it)
            } ?: run {
                sessionContext.interrupt(emptyText())
            }
        }
        // display additional commands to choose from to run
    }

    override fun headlineText(): String {
        return thisCommand.lower()
    }


    override suspend fun execute() {
        removeEntityCmd(
            items = sourceList(),
            listHeaderText = listHeaderText("to remove"),
            headlineText = headlineText(),
            confirmText = null, //this may be redundant (look at confirmFunc)
            emptyText = emptyText(),
            successMsg = "${singularItem()} successfully removed",
            errorMsg = "An error occurred while attempting ${singularItem()} removal. " +
                    "Please try again.",
            unchangedMsg = unchangedMsg(),

            preFunc = this::preFunc,
            confirmFunc = this::verifyFunc,
            doFunc = this::doFunc,
            postFunc = this::postFunc,
            singularItemLabel = singularItemLabel,
        )
    }

    open suspend fun removeEntityCmd(
        items: List<EntityWithLabels1>,
        preFunc: KSuspendFunction0<Boolean>,
        confirmFunc: KFunction1<SelectedEntity, String>,
        doFunc: KFunction1<EntityWithLabels1, Boolean>,
        postFunc: KFunction1<SessionContext, Unit>,
        listHeaderText: String = "Select ${singularItem()} to remove:",
        headlineText: String? = null,
        confirmText: String? = null,
        emptyText: String = emptyText(),
        successMsg: String = "${singularItem()} successfully removed",
        errorMsg: String = "An error occurred while attempting ${singularItem()} removal. " +
                "Please try again.",
        unchangedMsg: String = "Nothing has changed with ${pluralItems()}. Operation complete.",
        singularItemLabel: String? = null,
    ) {
        if (!preFunc()) {
            return
        }

        UserInput.selectAnOption(
            sessionContext = sessionContext,
            options = items.map { SelectableEntity(it) },
            headerText = listHeaderText,
            headlineText = headlineText,
        )?.let {
            if (it is SelectedEntity) {
                with(it.entity) {
                    val singularItem = singularItemLabel ?: this@with::class.simpleName
                    Log.debug(
                        "User selected " +
                                "$singularItem #${this.uid} to remove"
                    )
                    UserInput.promptYesNo(
                        confirmText ?: removalVerifyString(it),
                        sessionContext
                    )?.let { confirmResponse ->
                        if (confirmResponse) {
                            if (doFunc(this@with)) {
                                sessionContext.interrupt(successMsg)
                                postFunc(sessionContext)
                                return
                            } else {
                                Log.error("$sessionContext: $errorMsg")
                                sessionContext.interrupt(errorMsg)
                                //todo perhaps offer hint commands here
                                return
                            }
                        }
                    }
                }
            }
        }
        sessionContext.interrupt(unchangedMsg)
        postFunc(sessionContext)
        //todo maybe add follow-up command
    }
}





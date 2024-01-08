package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting

import com.undercurrent.legacy.types.enums.ResponseType

@Deprecated("Move away from using this")
data class InputField(
    val name: String,
    val type: ResponseType,
    val prompt: String,
    val displayName: String? = null,
    val prepopulatedValue: String = "",
    val expectResponse: Boolean = false,
    val callbackFunction: () -> Unit = {}
)
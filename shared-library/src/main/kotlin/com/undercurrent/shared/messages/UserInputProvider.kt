package com.undercurrent.shared.messages

import com.undercurrent.shared.utils.time.EpochNano

interface UserInputProvider : CanReadUserInput

interface CanFetchInput<T> {
    suspend fun fetchInput(vararg prompts: String): T?
}

interface CanSelectFromMenu<T> {
    suspend fun selectOptionIndex(
        options: List<T>,
        headerText: String,
    ): Int?
}

interface CanReadUserInput {
    suspend fun getRawInput(afterEpochNano: EpochNano): String?
}

interface UserInteractable<T : OutboundMessageEntity> {
    val inputProvider: UserInputProvider
    val outputProvider: UserOutputProvider<T>
}

interface InteractorStructSet {
    val inputter: UserInputProvider
    val interrupter: UserOutputProvider<InterrupterMessageEntity>
//    val adminNotifier: (String, String, Environment) -> Unit
}

 class InteractorStruct(
     override val inputter: UserInputProvider,
     override val interrupter: UserOutputProvider<InterrupterMessageEntity>,
): InteractorStructSet

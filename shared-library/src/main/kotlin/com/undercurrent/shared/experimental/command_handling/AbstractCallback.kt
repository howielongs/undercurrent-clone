package com.undercurrent.shared.experimental.command_handling


interface RootCallback<T> {
    fun runCallback()
}

abstract class MenuCallback<T>(val prompt: String) : RootCallback<T> {
    var handle: T? = null
    var fullLine: String? = null

    abstract override fun runCallback()

    override fun toString(): String {
        return prompt
    }
}

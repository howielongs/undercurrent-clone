package com.undercurrent.prompting.views.menubuilding

data class SelectableOption<T, R>(
    val handle: T,
    val item: R,
    val outString: String,
)
package com.undercurrent.prompting.views.menubuilding.formatting

interface LineItemStringBuilder<T> {
    fun buildLineItem(item: T): String
}

abstract class BaseLineItemBuilder<T>(
    private val transformFunc: (T) -> String = { it.toString() }
) : LineItemStringBuilder<T> {
    override fun buildLineItem(item: T): String {
        return transformFunc(item)
    }
}
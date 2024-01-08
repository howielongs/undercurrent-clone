package com.undercurrent.shared.experimental

enum class TextStyle {
    BOLD, ITALIC, SPOILER, STRIKETHROUGH, MONOSPACE, NONE
}

//todo come up with better way of constructing this
class FormattedText(
    val originalStr: String,
    val stylesArr: List<String>,
)

//--text-style "0:2:BOLD" "2:3:STRIKETHROUGH"
class FormattedTextBuilder {
    var stylesArr: MutableList<String> = mutableListOf()

    fun addForLength(style: TextStyle, start: Int, length: Int) {
        stylesArr.add("$start:$length:${style.name.uppercase()}")
    }

    fun add(style: TextStyle, start: Int, end: Int) {
        addForLength(style, start, end - start)
    }
}
package com.undercurrent.legacy.types.enums

@Deprecated("Use MenuIndexType in shared-library instead")
enum class ListIndexTypeOld(
    val char: String? = null,
    val responseType: ResponseType = ResponseType.STRING,
    val hintText: String = "",
    val postfix: String = " ",
    val displayName: String? = null,
) {
    ABC(
        hintText = " by typing a letter, such as A",
        postfix = ". ",
        displayName = "letter",
    ),
    INTEGER(
        responseType = ResponseType.INT,
        hintText = " by typing a number, such as 4",
        postfix = ". ",
        displayName = "number",
    ),
    UID(
        responseType = ResponseType.INT,
        hintText = " by typing a number, such as 4",
        postfix = ". ",
        displayName = "ID",
    ),
    DECIMAL(
        displayName = "number"
    ),
    YESNO(postfix = ". "),
    BULLET("•"),
    CHEVRON(">"),
    STRING(responseType = ResponseType.STRING, displayName = "text"),
    NONE(""),
}
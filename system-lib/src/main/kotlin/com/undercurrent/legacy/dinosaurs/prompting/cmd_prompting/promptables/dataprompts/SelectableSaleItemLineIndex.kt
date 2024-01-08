package com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts

data class SelectableSaleItemLineIndex(
    val productName: String,
    val itemLabel: String,
    val unitPrice: String,
    val productDescription: String,
    val index: String,
) {
    override fun toString(): String {
        return buildString {
            append("[")
            append(index)
            append("] - $")
            append(unitPrice)
            append(" / each\n\n")
            append(productName)
            append(" - ")
            append(itemLabel)
            append("\n")
            append(productDescription)
        }
    }
}
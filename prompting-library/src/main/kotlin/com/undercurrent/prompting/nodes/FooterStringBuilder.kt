package com.undercurrent.prompting.nodes

interface FooterStringBuilder {
    fun buildFooterString(): String
}

class YesNoFooterStringBuilder : FooterStringBuilder {
    override fun buildFooterString(): String {
        return "\n" + " - [y] Yes\n" + " - [n] No"
    }
}









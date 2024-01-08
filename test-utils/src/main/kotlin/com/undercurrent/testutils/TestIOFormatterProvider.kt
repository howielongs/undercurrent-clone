package com.undercurrent.testutils

class TestIOFormatterProvider {

    fun formatAndPrintInput(nodeName: String?, input: String) {
        println("\n======================================================")
        println("INPUT FROM USER [$nodeName]")
        println("\n --> ``$input``")
        println("======================================================\n")
    }

    fun formatAndPrintOutput(nodeName: String?, output: String) {
        println("\n======================================================")
        println("OUTPUT TO USER [$nodeName]")
        println("\n$output")
        println("======================================================\n")
    }
}
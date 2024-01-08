package com.undercurrent.shared

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LibStart {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    fun main() {
        log.info("Hello and welcome from lib!")
    }

}
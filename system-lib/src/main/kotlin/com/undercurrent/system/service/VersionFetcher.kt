package com.undercurrent.system.service

import com.undercurrent.legacy.routing.RunConfig
import java.util.*

object VersionFetcher {
    var version: String? = null

    fun fetchVersion(): String? {
        if (version != null && version != "") {
            RunConfig.version = version ?: ""
            println("Version: $version")
            return version
        }

        if (RunConfig.version != null && RunConfig.version != "") {
            version = RunConfig.version
            println("Version: $version")
            return RunConfig.version
        }

        val properties = Properties().apply {
            // do this with coroutine?
            ClassLoader.getSystemClassLoader().getResourceAsStream("version.properties").use {
                load(it)
            }
        }
        with(properties.getProperty("version")) {
            if (this != null) {
                version = this
                println("Version: $version")
                RunConfig.version = this
            }
            return this
        }
    }

}
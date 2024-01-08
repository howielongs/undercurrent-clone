package com.undercurrent.shared.utils

object PathUtils {
    private const val HOME_DIR = "user.home"

    val homePath: String by lazy {
        System.getProperty(HOME_DIR)
    }

    private val sqlitePath: String by lazy {
        "$homePath/.sqlite"
    }

    fun generateFullDatabasePath(dbFileName: String): String {
        // Ensure dbFileName ends with ".db"
        val finalDbFileName = if (dbFileName.endsWith(".db")) dbFileName else "$dbFileName.db"

        return "$sqlitePath/$finalDbFileName"
    }

}
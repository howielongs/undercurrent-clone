package com.undercurrent.shared.repository.database

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.PathUtils
import java.io.File

interface DatabasePathsProvider {
    val url: String
    val fullPathToFile: String
}

fun ensureDirectoryExistsOnSystem(systemPath: String) {
    with(File(systemPath)) {
        if (!exists()) {
            mkdirs()
            println("Directory $systemPath created.")
        } else {
            println("Directory $systemPath already exists.")
        }
    }
}


abstract class SystemPathsProvider : DatabasePathsProvider {
    abstract override val url: String
    abstract override val fullPathToFile: String

    fun ensureSystemPathExists(systemPath: String) {
        ensureDirectoryExistsOnSystem(systemPath)
    }
}

//todo pull this out into more widely usable class
//especially get rid of needing environment for this
class SqliteJdbcPathsProvider(
    private val environment: Environment,
    private val name: String? = null,
    private val fileExtension: String = ".db",
    private val filenameProvider: (Environment) -> String = { env ->
        (name ?: DatabaseFilenameFetcher.fetchFilename(env)).let {
            return@let if (it.endsWith(fileExtension)) it else "$it$fileExtension"
        }
    },
    private val bottomDirectoryName: String = ".sqlite",
    private val urlPrefix: String = "jdbc:sqlite:",
) : SystemPathsProvider(), DatabasePathsProvider {

    private val databaseName: String by lazy {
        filenameProvider(environment)
    }

    private val databasePath: String by lazy {
        "${PathUtils.homePath}/$bottomDirectoryName"
    }

    override val fullPathToFile: String by lazy {
        "$databasePath/$databaseName"
    }

    override val url: String by lazy {
        ensureSystemPathExists(databasePath)
        "$urlPrefix$fullPathToFile"
    }

}

//Database.connect("jdbc:postgresql://localhost:5432/mydb", driver = "org.postgresql.Driver", user = "username", password = "password")


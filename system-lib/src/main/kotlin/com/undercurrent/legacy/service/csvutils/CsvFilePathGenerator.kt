package com.undercurrent.legacy.service.csvutils


import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.legacy.utils.joincodes.RandomAbcStringGenerator
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.system.context.SessionContext
import java.nio.file.Files
import java.nio.file.Paths

interface CsvPathGenerator {
    fun generate(): String
}

class RandomCsvFilePathGenerator(val sessionContext: SessionContext, numDigits: Int = 20) :
    CsvFilePathGenerator(
        fileName = RandomAbcStringGenerator(numDigits).generate(),
        environment = sessionContext.environment
    )

open class CsvFilePathGenerator(
    private val fileName: String,
    private val environment: Environment = RunConfig.environment,
    private val finalDir: String = "reports",
    private val pathFromHome: String = "/.local/share/signal-cli/attachments/",
) : CsvPathGenerator {

    override fun generate(): String {
        val userhome = "user.home"
        val path = System.getProperty(userhome)
        val envPath =
            "$path$pathFromHome${environment.name.lowercase()}/$finalDir"
        Files.createDirectories(Paths.get(envPath))
        return "$envPath/$fileName.csv"
    }
}
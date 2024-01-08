package com.undercurrent

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

//todo test this before using it
class AwsSqliteFileFetcher {
//    fun runFetcher(args: Array<String>) {
//        val env = parseEnv(args)
//        val timestamp = generateTimestamp()
//        println("Timestamp to write to filename: $timestamp")
//        val targetFilename = buildDbFileName(env)
//        val newLocalFilename = buildAwsDbFileName(env, timestamp)
//        fetchSqlFile(env, targetFilename, newLocalFilename)
//        println("Operation complete. File downloaded to ${Config.destPath}$newLocalFilename")
//    }

    object Config {
        const val defaultConfig = "config.ini"
        const val defaultConfigHeader = "EC2"
        var destPath = ""
    }

    private fun decideConfigFileName(envIn: String): Pair<String, String> {
        return when (envIn) {
            "prod" -> {
                println("Environment specified as '$envIn'. Using config_prod.ini")
                Pair("config_prod.ini", "PROD")
            }

            else -> {
                println("Environment specified as '$envIn'. Using config_qa.ini")
                Pair("config_qa.ini", "QA")
            }
        }
    }

    private fun parseEnv(argsArr: Array<String>): String {
        return when {
            argsArr.contains("prod") -> {
                println("Current environment: PROD")
                "prod"
            }

            argsArr.contains("qa") -> {
                println("Current environment: QA")
                "qa"
            }

            argsArr.contains("live") -> {
                println("Current environment: LIVE")
                "live"
            }

            argsArr.contains("dev") -> {
                println("Current environment: DEV")
                "dev"
            }

            else -> {
                println("No environment specified. Defaulting to 'dev'.")
                "dev"
            }
        }
    }

    private fun formatDateTime(dateTime: LocalDateTime, tz: String = "UTC"): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm'$tz'")
        return dateTime.format(formatter)
    }

    private fun buildAwsDbFileName(envIn: String, timestampIn: String): String {
        return "lemur_${envIn}_aws_${timestampIn}.db"
    }

    private fun buildDbFileName(envIn: String): String {
        return "lemur_${envIn}.db"
    }

    private fun readConfigFile(configFilenameIn: String, configHeaderIn: String): Properties {
        val properties = Properties()
        properties.load(File(configFilenameIn).inputStream())
        return properties
    }

    private fun readEnvConfigToProps(envIn: String): Pair<String, String> {
        val (configFilenameOut, configHeader) = decideConfigFileName(envIn)
        val envFieldsOut = readConfigFile(configFilenameOut, configHeader)
        val prefixOut = envFieldsOut.getProperty("prefix")
        val keyPathOut = File(envFieldsOut.getProperty("key_path")).absolutePath
        return Pair(prefixOut, keyPathOut)
    }

    private fun readDefaultConfigToProps(
        configFilenameIn: String = Config.defaultConfig,
        configHeaderIn: String = Config.defaultConfigHeader
    ): List<String> {
        val fieldsOut = readConfigFile(configFilenameIn, configHeaderIn)
        val awsUser = fieldsOut.getProperty("aws_user")
        val srcPathOut = fieldsOut.getProperty("src_path")
        Config.destPath = File(fieldsOut.getProperty("dest_path")).absolutePath
        val awsRegionOut = fieldsOut.getProperty("aws_region")
        val awsSuffixOut = fieldsOut.getProperty("aws_suffix")
        return listOf(awsUser, srcPathOut, awsRegionOut, awsSuffixOut)
    }

    private fun buildHostname(prefixIn: String, awsRegionIn: String, awsSuffixIn: String): String {
        return "$prefixIn.$awsRegionIn.$awsSuffixIn"
    }

    private fun buildSrcPath(awsUserIn: String, srcPathIn: String): String {
        return "/home/$awsUserIn/$srcPathIn"
    }

    private fun buildScpCommand(envIn: String, targetFilenameIn: String, newLocalFilenameIn: String): String {
        val (awsUser, srcPath, awsRegion, awsSuffix) = readDefaultConfigToProps()
        val (prefix, keyPath) = readEnvConfigToProps(envIn)
        val srcHostname = buildHostname(prefix, awsRegion, awsSuffix)
        return "scp -i \"$keyPath\" $awsUser@$srcHostname:${
            buildSrcPath(
                awsUser,
                srcPath
            )
        }$targetFilenameIn ${Config.destPath}$newLocalFilenameIn"
    }

    private fun fetchSqlFile(envIn: String, targetFilenameIn: String, newLocalFilenameIn: String) {
        val command = buildScpCommand(envIn, targetFilenameIn, newLocalFilenameIn)
        run(command)
    }

    fun run(commandStr: String): String {
        return ""
//        println("Running command:\n\t$commandStr\n")
//        val process = Runtime.getRuntime().exec(commandStr)
//        val output = process.inputStream.bufferedReader().readText()
//        println(output)
//        return output
    }

    private fun generateTimestamp(): String {
        val dateUtc = LocalDateTime.now()
        return formatDateTime(dateUtc)
    }



}
/*
  Copyright (C) 2015-2022 AsamK and contributors

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.asamk.signal


import com.undercurrent.ktorservice.startKtorService
import com.undercurrent.system.service.VersionFetcher
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.routing.SignalSmsConfigurationLoader
import com.undercurrent.shared.repository.database.ProductionDatabase
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.shared.utils.Log
import com.undercurrent.start.LemurBot
import com.undercurrent.start.StartupProps
import com.undercurrent.system.context.DbusProps
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.DefaultSettings
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.asamk.signal.commands.exceptions.*
import org.asamk.signal.logging.LogConfigurator
import org.asamk.signal.manager.ManagerLogger
import org.asamk.signal.util.SecurityProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.security.Security
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger: org.slf4j.Logger = LoggerFactory.getLogger(this::class.java)

        println("Starting Signal CLI...")
        println("Args: ${args.joinToString(", ")}")
        var nextArgs = args

        Security.setProperty("payments.policy", "unlimited")
        installSecurityProviderWorkaround()

//        val mainScope = CoroutineScope(Dispatchers.Default)

        if (nextArgs.contains("server")) {

            // may want to parse env here before calling service
            val databaseStarter = ProductionDatabase(
                Environment.DEV,
                shouldRunMigrations = true
            )
            databaseStarter.db


            //lsof -ti :8080 | xargs kill
//            mainScope.launch {
            startKtorService()
//            }

            println("Ending Ktor service")
            exitProcess(0)
        }


        // Configuring the logger needs to happen before any logger is initialized
        val nsLog = parseArgs(nextArgs)
        val verboseLevel = if (nsLog == null) 0 else nsLog.getInt("verbose")
        val logFile = nsLog?.get<File>("log-file")
        val scrubLog = nsLog != null && nsLog.getBoolean("scrub-log")
        configureLogging(verboseLevel, logFile, scrubLog)
        val parser = App.buildArgumentParser()
        val ns = parser.parseArgsOrFail(nextArgs)

        if (nextArgs.intersect(
                setOf(
                    "link",
                    "setPin",
                    "updateAccount",
                    "updateConfiguration",
                    "verify",
                    "register",
                    "unregister",
                    "unblock",
                    "block",
                    "listIdentities",
                    "quitGroup",
                    "listDevices",
                    "trust",
                    "getUserStatus",
                    "daemon",
                    "receive",
                    "updateProfile"
                )
            ).isNotEmpty()
        ) {
            runCommand(ns, verboseLevel)
            exitProcess(0)
        }


        val props = initializeSystem(ns, verboseLevel, nsLog)
        val dbusProps = DbusProps().fromSms(props.botSms)

        println("Parsed environment: ${dbusProps.environment.name}")
        println("Parsed role: ${dbusProps.role.name}")


        //todo pull into props
        LemurBot(
            dbusProps = dbusProps,
            disableBtcWalletUsage = false,
        ).run()

        exitProcess(0)
    }


    private fun extractRoleArg(ns: Namespace, verboseLevel: Int): AppRole {
        val roleArg = getRequiredArg(ns, "role", false) ?: run {
            runCommand(ns, verboseLevel)
            exitProcess(0)
        }

        return ShopRole.valueOf(roleArg.uppercase())
    }

    private fun extractEnvArg(ns: Namespace, verboseLevel: Int): Environment {
        val envArg = getRequiredArg(ns, "env", false) ?: run {
            runCommand(ns, verboseLevel)
            exitProcess(0)
        }

        return Environment.valueOf(envArg.uppercase())
    }

    private fun initializeSystem(ns: Namespace, verboseLevel: Int, nsLog: Namespace?): StartupProps {
        val propsBuilder = StartupProps.Builder()

        var thisRole: AppRole
        var thisEnv: Environment
        var thisSms: String
        var isTestMode: Boolean

        val sms: BotSms = try {
            val thisSms = nsLog!!.getString("username")
            BotSms(SmsText(thisSms!!).validate())
        } catch (e: Exception) {
            "Caught exception: $e on startup".let {
                println(it)
                Log.error(it, e, "Main")
            }

            thisRole = extractRoleArg(ns, verboseLevel).let {
                propsBuilder.role(it)
                println("Parsed role: ${it.name}")
                it
            }

            thisEnv = extractEnvArg(ns, verboseLevel).let {
                propsBuilder.environment(it)
                println("Parsed environment: ${it.name}")
                it
            }
            BotSms(SignalSmsConfigurationLoader.findBotSms(environment = thisEnv, role = thisRole))
        }

        propsBuilder.botSms(sms.value)
        println("Parsed username: ${sms.value}")

        val dbusProps = DbusProps().fromSms(sms)
        thisRole = dbusProps.role
        thisEnv = dbusProps.environment

        RunConfig.environment = thisEnv

        val routingProps = DbusProps(thisRole, thisEnv)

        println("Is test mode: ${RunConfig.isTestMode} ")
        println("Environment mode: $thisEnv")
        println("User role: $thisRole")
        println("Dbus path: ${routingProps.toFullPathStr()}")


        VersionFetcher.fetchVersion()?.let {
            RunConfig.version = it
            propsBuilder.version(it)
            println("Version loaded from Main: $it")
        }

        return propsBuilder.build()!!
    }


    fun runCommand(args: Array<String>): Int {
// enable unlimited strength payments via Policy, supported on relevant JREs
        Security.setProperty("payments.policy", "unlimited")
        installSecurityProviderWorkaround()

        // Configuring the logger needs to happen before any logger is initialized
        val nsLog = parseArgs(args)
        val verboseLevel = if (nsLog == null) 0 else nsLog.getInt("verbose")
        val logFile = nsLog?.get<File>("log-file")
        val scrubLog = nsLog != null && nsLog.getBoolean("scrub-log")
        configureLogging(verboseLevel, logFile, scrubLog)
        val parser = App.buildArgumentParser()
        val ns = parser.parseArgsOrFail(args)
        return runCommand(ns, verboseLevel)
    }


    fun runCommand(ns: Namespace, verboseLevel: Int): Int {
        var status = 0
        try {
            App(ns).init()
        } catch (e: CommandException) {
            System.err.println(e.message)
            if (verboseLevel > 0 && e.cause != null) {
                e.cause!!.printStackTrace()
            }
            status = getStatusForError(e)
        } catch (e: Throwable) {
            e.printStackTrace()
            status = 2
        }

        return status
    }


    private fun installSecurityProviderWorkaround() {
        // Register our own security provider
        Security.insertProviderAt(SecurityProvider(), 1)
        Security.addProvider(BouncyCastleProvider())
    }

    private fun getRequiredArg(
        ns: Namespace, arg: String, quitIfNull: Boolean = false
    ): String? {
        return ns.getString(arg) ?: run {
            if (quitIfNull) {
                println("`$arg` arg is required")
                exitProcess(0)
                null
            } else {
                null
            }
        }
    }

    private fun parseArgs(args: Array<String>): Namespace? {
        val parser = ArgumentParsers.newFor("signal-cli", DefaultSettings.VERSION_0_9_0_DEFAULT_SETTINGS)
            .includeArgumentNamesAsKeysInResult(true).build().defaultHelp(false)
        parser.addArgument("-v", "--verbose").action(Arguments.count())
        parser.addArgument("--log-file").type(File::class.java)
        parser.addArgument("--scrub-log").action(Arguments.storeTrue())
        return try {
            parser.parseKnownArgs(args, null)
        } catch (e: ArgumentParserException) {
            null
        }
    }

    private fun configureLogging(verboseLevel: Int, logFile: File?, scrubLog: Boolean) {
        LogConfigurator.setVerboseLevel(verboseLevel)
        LogConfigurator.setLogFile(logFile)
        LogConfigurator.setScrubSensitiveInformation(scrubLog)
        if (verboseLevel > 0) {
            Logger.getLogger("").level = if (verboseLevel > 2) Level.FINEST else Level.INFO
            ManagerLogger.initLogger()
        }
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }

    fun getStatusForError(e: CommandException): Int {
        return if (e is UserErrorException) {
            1
        } else if (e is UnexpectedErrorException) {
            2
        } else if (e is IOErrorException) {
            3
        } else if (e is UntrustedKeyErrorException) {
            4
        } else if (e is RateLimitErrorException) {
            5
        } else {
            2
        }
    }
}
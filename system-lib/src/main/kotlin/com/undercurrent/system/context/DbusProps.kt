package com.undercurrent.system.context

import com.undercurrent.legacy.routing.RoutingConfig
import com.undercurrent.legacy.routing.SignalSmsConfigurationLoader
import com.undercurrent.legacy.routing.SmsRoute
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.errors.CoreException
import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.service.dbus.DbusPath


/**
 * Look into creating DbusProviders
 * /org/asamk/Signal/_11119999111
 */
class DbusProps(
    fullPath: String? = null
) : RoutingProps {
    constructor(
        roleIn: AppRole,
        envIn: Environment
    ) : this() {
        //todo SMELLY

        val thisBotSms = BotSms(SignalSmsConfigurationLoader.findBotSms(envIn, roleIn))
        botSms = thisBotSms
        fromSms(thisBotSms)
    }

    private var coreNumber: String = ""
    private var pathRoot: String = "/org/asamk/Signal"
    var countryCode: String = "1"

    private val route: SmsRoute by lazy {
        routeFromDbusPath(toPath())
    }


    //todo make this less circular
    private fun routeFromDbusPath(dbusPathIn: DbusPath): SmsRoute {
        //todo SMELLY

        val botSms = DbusProps().fromFullPath(dbusPathIn).toBotSms()
        val botSmsIn = botSms.value

        val smsRoutes = RoutingConfig.getSmsRoutes().filter { "+1" + it.sms == botSmsIn || it.sms == botSmsIn }

        return if (smsRoutes == null || smsRoutes.isEmpty()) {
            throw CoreException("No sms route found for $botSmsIn (SignalSmsConfigurationLoader)")
        } else {
            smsRoutes[0]
        }
    }


    override val environment: Environment by lazy {
        route.environment
    }

    override val role: AppRole by lazy {
        route.role
    }

    private var botSms: BotSms? = null


    init {
        fullPath?.let {
            fromFullPath(DbusPath(it))
            environment
        }
    }


    fun toExtension(): String {
        return "_$countryCode$coreNumber"
    }

    override fun toBotSms(): BotSms {
        val useCountryCode = true
        with(botSms) {
            if (this != null) {
                return this
            }
        }
        val smsString = if (useCountryCode) {
            "+$countryCode$coreNumber"
        } else {
            coreNumber
        }
        //todo SMELLY

        //todo probably clean up this constantly running validation
        BotSms(SmsText(smsString).validate()).let {
            botSms = it
            return it
        }
    }

    fun fromDbusExtension(
        extension: String,
    ): DbusProps {
        pathRoot = "org/asamk/Signal"
        coreNumber = extension.substring(1)
        countryCode = extension.substring(0, 1)
        return this
    }

    fun fromSms(sms: BotSms): DbusProps {
        return fromSms(sms.value)
    }

    fun fromSms(
        sms: String,
    ): DbusProps {
        pathRoot = "/org/asamk/Signal"

        val cleanerSms = try {
            //todo SMELLY

            SmsText(sms).validate()
        } catch (e: Exception) {
            Log.error("Error cleaning sms: $sms")
            sms
        }

        with(cleanerSms.replace("+", "")) {
            coreNumber = sms.takeLast(10)

            if (this.length == 11) {
                countryCode = this.take(1)
            } else if (this.length == 12) {
                countryCode = this.take(2)
            } else {
                countryCode = "1"
            }
        }
        return this
    }

    fun fromFullPath(fullPath: DbusPath): DbusProps {
        // Define a more lenient regex pattern that extracts the relevant parts
        val regexPattern = "^/org/asamk/Signal/_(1\\d{10})(.*)$"

        // Match the input against the regex pattern
        val matchResult = regexPattern.toRegex().find(fullPath.value)

        if (matchResult != null) {
            // Extract country code, SMS number, and remaining path
            val thisCountryCode = matchResult.groupValues[1]
            val smsNumber = matchResult.groupValues[2]
            val rootPath = "/org/asamk/Signal/"

            pathRoot = rootPath
            coreNumber = smsNumber
            countryCode = thisCountryCode
            //todo SMELLY

            Log.debug("Incoming fullPath: ${fullPath.value}")
            Log.debug("Core number: $coreNumber")
            Log.debug("Country Code: $thisCountryCode")
            Log.debug("SMS Number: $smsNumber")
            Log.debug("Root Path: $rootPath")
        } else {
            Log.warn("Input does not match the regex pattern.")
        }

        return this
    }

    override fun toPath(): DbusPath {
        //todo SMELLY

        return DbusPath(toFullPathStr())
    }

    override fun isTestMode(): Boolean {
        return environment == Environment.TEST
    }

    fun toFullPathStr(): String {
        //todo SMELLY

        return toFullPathUrl("$pathRoot/${toExtension()}")
    }


    private fun toFullPathUrl(value: String): String {
        return ("/" + value).replace("//", "/").replace("__", "_")
    }
}


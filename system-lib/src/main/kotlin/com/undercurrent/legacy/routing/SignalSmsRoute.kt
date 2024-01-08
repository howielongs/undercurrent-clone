package com.undercurrent.legacy.routing

//interface SignalSmsRouteLoader {
////    var sms: String
////    var role: Role
////    var environment: EnvMode
////    var countryCodeStr: String // default to US (+1)
//
//    fun findRoutesForEnvironmentType(envMode: EnvMode): Map<Role, String>
//    fun findRole(sms: String): Role?
//    fun findEnvironmentType(sms: String): EnvMode?
//    fun findSms(envMode: EnvMode, role: Role): String
//    fun generateDbusExtension(sms: String): String
//    fun generateDbusExtension(envMode: EnvMode, role: Role): String
//    fun findRoute(sms: String): SignalSmsRoute?
//    fun findRoute(envMode: EnvMode, role: Role): SignalSmsRoute?
//}

//interface SignalSmsRouteLoader {
//    fun findRoutesForEnvironmentType(envMode: EnvMode): Map<Role, String>
//    fun generateDbusExtension(sms: String): String
//    fun findRole(sms: String): Role
//    fun findEnvironmentType(sms: String): EnvMode
//}
//
//data class SignalSmsRoute(
//    var sms: String? = null,
//    private var role: Role? = null,
//    var environment: EnvMode? = null,
//    var countryCodeStr: String = "+1"
//) {
//    private val impl: SignalSmsRouteLoader = SignalSmsRouteImpl()
//
//    val formattedSms: String
//        get() = sms ?: findSms(environment!!, role!!)
//
//    val detectedRole: Role
//        get() = role ?: impl.findRole(sms!!)
//
//    val detectedEnvironment: EnvMode
//        get() = environment ?: impl.findEnvironmentType(sms!!)
//
//    val dbusExtension: String
//        get() = impl.generateDbusExtension(formattedSms)
//
//    private fun findSms(envMode: EnvMode, role: Role): String {
//        return impl.findRoutesForEnvironmentType(envMode)[role]
//            ?: throw IllegalArgumentException("SMS not found for role: $role in environment: $envMode")
//    }
//}
//
//class SignalSmsRouteImpl : SignalSmsRouteLoader {
//
//    override fun findRoutesForEnvironmentType(envMode: EnvMode): Map<Role, String> {
//        val properties = Properties().apply {
//            val envSuffix = if (envMode == EnvMode.DEV && isMacOs()) "mac" else envMode.name.toLowerCase(Locale.ROOT)
//            val filePath = "resources/sms_routes/sms_routes_$envSuffix.properties"
//            load(FileInputStream(filePath))
//        }
//
//        return mapOf(
//            Rloe.ADMIN to properties.getProperty("ADMIN"),
//            Rloe.VENDOR to properties.getProperty("VENDOR"),
//            Rloe.CUSTOMER to properties.getProperty("CUSTOMER")
//        )
//    }
//
//    override fun findRole(sms: String): Role {
//        return EnvMode.values().asSequence()
//            .map { env -> findRoutesForEnvironmentType(env) }
//            .flatMap { it.entries.asSequence() }
//            .firstOrNull { (_, number) -> number == sms }
//            ?.key
//            ?: throw IllegalArgumentException("Role not found for SMS: $sms")
//    }
//
//    override fun findEnvironmentType(sms: String): EnvMode {
//        return EnvMode.values().firstOrNull { env ->
//            findRoutesForEnvironmentType(env).values.contains(sms)
//        } ?: throw IllegalArgumentException("Environment not found for SMS: $sms")
//    }
//
//    override fun generateDbusExtension(sms: String): String {
//        return if (sms.startsWith("+")) "_${sms.substring(1)}" else "_$sms"
//    }
//
//    private fun isMacOs(): Boolean {
//        return System.getProperty("os.name").contains("Mac OS X")
//    }
//}


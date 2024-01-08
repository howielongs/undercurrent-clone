package com.undercurrent.legacy.routing

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import java.util.*

interface SignalAdminsConfig {
    fun getAdminSmsNumbers(environment: Environment): HashMap<String, AppRole>
    fun fetchPaymentAdminsNumbers(): List<String>
}

class SignalAdminConfiguration : SignalAdminsConfig {

    override fun getAdminSmsNumbers(environment: Environment): HashMap<String, AppRole> {
        val numbersMap = HashMap<String, AppRole>()

        // Default TEST numbers (hardcoded)
        if (environment == Environment.TEST) {
            numbersMap["+1234567890"] = ShopRole.ADMIN
            numbersMap["+1234567891"] = ShopRole.ADMIN
            numbersMap["+1234567892"] = ShopRole.ADMIN
            return numbersMap
        }

        // Load the properties file for other environments
        val properties = Properties().apply {
            val filePath = "sms_routes/admin_numbers.properties"
            load(Thread.currentThread().contextClassLoader.getResourceAsStream(filePath))
        }

        // Filter and populate the numbersMap based on the envMode
        properties.forEach { key, value ->
            val parts = key.toString().split(".")
            val number = parts[0]
            val fileEnvironment = Environment.valueOf(parts[1])
            if (fileEnvironment == environment) {
                numbersMap[number] = ShopRole.valueOf(value.toString())
            }
        }

        return numbersMap
    }

    override fun fetchPaymentAdminsNumbers(): List<String> {
        // Assuming you want to fetch all admin numbers related to payments
        // This can be modified based on your exact requirements
        return getAdminSmsNumbers(Environment.PROD).filterValues { it == ShopRole.ADMIN }.keys.toList()
    }
}

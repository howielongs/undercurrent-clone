package com.undercurrent.system.service.data_classes

import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.system.context.DbusProps

//may want to introduce coroutines here for async processing
class SignalEnvelopeProps(
    val dbusProps: DbusProps,
) {

    companion object {
        fun fromBotNumber(botSessionNumber: String?): SignalEnvelopeProps? {

            println("\n\n#######################################")
            println("ENTERING saveFromSignalCliDbus")

            val parsedBotSms = try {
                SmsText(botSessionNumber).validate()
            } catch (e: Exception) {
                println("ERROR -> Unable to parse botSessionNumber: ${e.message}")
                return null
            }
            println("Parsed selfNumber to $parsedBotSms")

            val dbusProps = DbusProps().fromSms(parsedBotSms)

            with(SignalEnvelopeProps(dbusProps)) {
                println("Parsed dbusPath to ${dbusProps.toFullPathStr()}")
                return this
            }
        }
    }
}
package com.undercurrent.legacy.routing

import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.strings.SmsText

//todo SMELLY

class SignalSmsConfigurationLoader {

    companion object {
        //consider completely encapsulating in DbusProps
        fun findBotSms(environment: Environment, role: AppRole): String {
            val smsRoute = RoutingConfig.getSmsRoutes().single { it.environment == environment && it.role == role }
            return SignalSms(SmsText(smsRoute.sms).validate()).value
        }
    }
}

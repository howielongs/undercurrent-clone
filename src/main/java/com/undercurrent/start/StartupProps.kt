package com.undercurrent.start

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole

class StartupProps private constructor(
   val role: AppRole,
   val environment: Environment,
   val version: String,
   val botSms: String
) {
   class Builder {
      private var role: AppRole? = null
      private var environment: Environment? = null
      private var version: String = ""
      private var botSms: String = ""

      fun role(roleIn: AppRole) = apply { this.role = roleIn }
      fun environment(environmentIn: Environment) = apply { this.environment = environmentIn }
      fun version(versionIn: String) = apply { this.version = versionIn }
      fun botSms(botSms: String) = apply { this.botSms = botSms }

      fun build() = role?.let {
         environment?.let { it1 ->
            StartupProps(
               role = it,
               environment = it1,
               version = version,
               botSms = botSms
            )
         }
      }
   }
}

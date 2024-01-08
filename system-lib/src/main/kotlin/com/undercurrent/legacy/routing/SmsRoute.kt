package com.undercurrent.legacy.routing

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole

data class SmsRoute(
    val sms: String,
    val environment: Environment,
    val role: AppRole
)
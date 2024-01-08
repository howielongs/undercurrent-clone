package com.undercurrent.legacy.service.fetchers

import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodes
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.abstractions.CanFetchByField
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and


class JoinCodeFetch : CanFetchByField<String, JoinCode> {
    override suspend fun fetch(codeStr: String): JoinCode? {
        val cleanedCode = codeStr.trim().replace("/", "")
            .replace(" ", "").uppercase()

        return tx {
            JoinCode.find(
                JoinCodes.code eq cleanedCode
                        and unexpiredExpr(JoinCodes)
            ).firstOrNull()
        }
    }
}

sealed interface UserFetcher<T> : CanFetchByField<T, User>


object UserFetcherById : UserFetcher<Int> {
    override suspend fun fetch(field: Int): User? {
        return tx {
            User.findById(field)
        }
    }
}

object UserFetcherBySms : UserFetcher<SignalSms> {
    override suspend fun fetch(sms: SignalSms): User? {
        sms.value?.let {
            Users.fetchBySms(it)?.let { existingUser ->
                return existingUser
            }
        }
        return null
    }
}

class ShopVendorFetcher : CanFetchByField<User, ShopVendor> {
    override suspend fun fetch(user: User): ShopVendor? {
        return tx {
            user.shopVendor
        }
    }
}
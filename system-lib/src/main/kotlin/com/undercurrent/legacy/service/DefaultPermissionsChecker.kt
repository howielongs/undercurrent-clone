package com.undercurrent.legacy.service

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.service.user_role_services.UserRoleChecker
import com.undercurrent.shared.abstractions.PermissionsChecker
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import kotlin.reflect.KSuspendFunction1

interface EmptyMessageChecker {
    fun isEmptyMessage(msgBody: String): Boolean
}

class DefaultPermissionsChecker(
    val user: User,
    val thisRole: AppRole,
//    val interruptFunc: (String) -> Unit = { Log.error(it) },
    val notVendorInterruptFunc: (String) -> Unit = { Log.error(it) },
    private val roleChecker: KSuspendFunction1<Array<out AppRole>, Boolean> = UserRoleChecker(user)::matchesAtLeastOneRole,
) : PermissionsChecker {

    override suspend fun hasValidPermissions(): Boolean {
        with(user) {
//            val uid = tx { this@with.uid }
            val thisVendor = tx { this@with.shopVendor }
            val userHasRole = roleChecker(arrayOf(thisRole))

//            if (!userHasRole) {
//                Log.error(
//                    "Role $thisRole not granted to user $uid"
//                )
//
//                //todo pull out dependency on Pinger
//                Pinger(RunConfig.environment).pingAllRolesForUser(user)
//
//                interruptFunc(
//                    "Access denied. You are not a $thisRole"
//                )
//                return false
//            }

            if (userHasRole && thisRole == ShopRole.VENDOR && thisVendor == null) {
                //todo clean up interrupt/notify providers to allow sending to various roles
                notVendorInterruptFunc("You need to create a vendor account first")

                return false
            }

        }
        return true
    }
}
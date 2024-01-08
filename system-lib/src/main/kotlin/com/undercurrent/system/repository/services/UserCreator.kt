package com.undercurrent.system.repository.services

import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.repository.entities.system.ping.Pinger
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.messaging.outbound.sendInterrupt
import org.jetbrains.exposed.sql.transactions.transaction

object UserCreator {

    @Deprecated("Clean this up and use dep-injection for outbound notifs")
    suspend fun createOrFetchUser(
        sms: String,
        dbusProps: DbusProps,
    ): User? {
        val thisRole: AppRole = dbusProps.role

        val existingUser = tx {
            User.find { Users.smsNumber eq sms }.firstOrNull()
        }
        return existingUser ?: run {
            if (thisRole == ShopRole.ADMIN) {
                notifyAdmins("Admin user creation attempted by $sms", subject = "ADMIN_ONBOARD_ALERT")
                Log.fatal("Admin user creation attempted by $sms")
                    return null
                }

            if (thisRole == ShopRole.VENDOR) {
                notifyAdmins("New user attempting to join as vendor: $sms", subject = "VENDOR_ONBOARD")
                Log.debug("Vendor user creation attempted by $sms")
            }

            Log.info("User with sms $sms not found, creating as $thisRole role...")

            //check if sealed sender
            //if yes, then send message to user to send message again
            if (sms.contains("-")) {
                // shouldn't save UUID here at this stage...
                transaction {
                    User.new {
                        smsNumber = sms
                        role = thisRole as ShopRole
                        // add future expiry for user here
//                        expire()
                    }
                }.let {
                    // use something better to send the response
                    sendInterrupt(
                        user = it,
                        role = dbusProps.role,
                        environment = dbusProps.environment,
                        msg = "Welcome to the Shopping Bot! \n\n" +
                                "\n\nPlease re-enter that message again to confirm you`re not a bot."
                    )
                    Admins.notifyError("Logged a sealed sender for $sms")
                    return null
                }
            }

            transaction {
                    User.new {
                    smsNumber = sms
                        role = thisRole as ShopRole
                    }
            }.let {
                Pinger(dbusProps).pingAllRolesForUser(it)
                return it
            }
        }
    }
}
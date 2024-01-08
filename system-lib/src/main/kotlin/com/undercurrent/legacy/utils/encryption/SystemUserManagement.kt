package com.undercurrent.legacy.utils.encryption


import com.undercurrent.legacy.dinosaurs.prompting.InputValidator.validatePhoneNumber
import com.undercurrent.system.repository.entities.AdminProfile
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.transactions.transaction

class SystemUserManagement {

    fun addDefaultUser(
        smsToSet: String,
        roleToSet: AppRole,
    ) {
        validatePhoneNumber(smsToSet)?.let {
            val count = transaction {
                User.find { Users.smsNumber eq it }.count()
            }
            if (count.toInt() == 0) {
                (createNewUser(smsToSet, roleToSet))?.let {
                    if (roleToSet == ShopRole.ADMIN) {
                        saveAdmin(it)
                    }
                }
            }
        }
    }

    companion object {
        fun createNewUser(numberIn: String, roleIn: AppRole): User? {
            return tx {
                try {
                    User.new {
                        smsNumber = numberIn
                        role = roleIn as ShopRole
                    }
                } catch (e: Exception) {
                    Log.warn("User with $numberIn already exists. Fetching now...")
                    throw e
                }
            }
        }

        //todo do check for uniqueness?
        private fun saveAdmin(newUser: User): ExposedEntityWithStatus2? {
            return transaction {
                if (newUser.role == ShopRole.ADMIN) {
                    val newAdminProfile = AdminProfile.new {
                        user = newUser
                    }
                    //todo impl payments address creation for admins?
                    newAdminProfile
                }
                return@transaction null
            }

        }


    }


}
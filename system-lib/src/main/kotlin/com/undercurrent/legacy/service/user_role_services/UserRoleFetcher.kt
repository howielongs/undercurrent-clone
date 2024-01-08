package com.undercurrent.legacy.service.user_role_services

import com.undercurrent.shared.abstractions.CanFetchForAnyAppRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.filterOutExpired
import com.undercurrent.system.repository.entities.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * ctx { admins.filterUnexpired() }: This line uses the ctx function to run the
 * filterUnexpired() function within a database transaction. The result of the
 * transaction is awaited later in the coroutine.
 *
 * async { ... }: These blocks run concurrently to check the results of the database
 * transactions and modify the roles set accordingly. Note that concurrent modifications
 * to a collection need to be thread-safe, but in this case, it's safe because coroutines
 * use a single thread by default.
 *
 * return@coroutineScope roles: Returns the roles set to the caller of fetchAllRoles().
 */
class UserRoleFetcher : CanFetchForAnyAppRole<User> {
    override suspend fun fetchRoles(entity: User): Set<AppRole> = coroutineScope {
        val allAdmins = async { ctx { entity.admins.filterOutExpired() } }
        val allVendors = async { ctx { entity.vendors.filterOutExpired() } }
        val allCustomers = async { ctx { entity.customers.filterOutExpired() } }

        val roles = mutableSetOf<AppRole>()

        // List of async blocks that add roles based on conditions
        val roleAdditionTasks = listOf(
            async {
                if (allAdmins.await().isNotEmpty()) {
                    roles.add(ShopRole.ADMIN)
                }
            },
            async {
                if (allVendors.await().isNotEmpty()) {
                    roles.add(ShopRole.VENDOR)
                }
            },
            async {
                if (allCustomers.await().isNotEmpty()) {
                    roles.add(ShopRole.CUSTOMER)
                }
            }
        )

        // Await all the role addition tasks to complete
        roleAdditionTasks.awaitAll()

        // Additional logic for role cascading
        if (roles.contains(ShopRole.ADMIN)) {
            roles.add(ShopRole.VENDOR)
        }
        roles.add(ShopRole.CUSTOMER)

        return@coroutineScope roles
    }

}
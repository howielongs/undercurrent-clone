package com.undercurrent.legacyshops.service

import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.abstractions.ListFetcher
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.and

interface CanFetchCustomerList : ListFetcher<ShopCustomer>

class CustomerListFetcher(val storefront: Storefront) : CanFetchCustomerList {


    override fun fetchList(): List<ShopCustomer> {
        val now = EpochNano()

        // Perform a single query to get all active customers with their user expiration data
        return tx {
            val customers = ShopCustomer.find {
                ShopCustomers.storefront eq storefront.id and
                        unexpiredExpr(ShopCustomers, now.value)
            }.distinctBy { it.user }.toList() // Get the list once from the database

            // Filter the list in memory, assuming the list size is reasonable and won't cause memory issues
            customers.filter {
                // Now this checks the pre-fetched expiryEpoch without additional queries
                it.user.expiryEpoch?.let { expiry ->
                    expiry - now.value > 0
                } ?: true
            }
        }
    }

//    override fun fetchList(): List<CustomerProfile> {
//        val now = EpochNano()
//
//        val expiredUsers = fetchExpiredUsers()
//        val activeCustomers = fetchCustomers()
//
//
//        return tx {
//            CustomerProfile.find {
//                CustomerProfiles.storefront eq storefront.id and
//                        unexpiredExpr(CustomerProfiles, now.value)
//            }.filter {
//                it.user.isNotExpired(now.value)
//            }.toList()
//        }
//    }

    //    private fun fetchCustomers(now: EpochNano = EpochNano()): List<CustomerProfile> {
//        return tx {
//            CustomerProfile.find {
//                CustomerProfiles.storefront eq storefront.id and
//                        unexpiredExpr(CustomerProfiles, now.value)
//            }.distinctBy { it.user }.toList()
//        }
//    }

    //    private fun fetchExpiredUsers(now: EpochNano = EpochNano()): List<User> {
//        return tx {
//            User.find {
//                not(unexpiredExpr(Users, now.value))
//            }.toList()
//        }
//    }


}
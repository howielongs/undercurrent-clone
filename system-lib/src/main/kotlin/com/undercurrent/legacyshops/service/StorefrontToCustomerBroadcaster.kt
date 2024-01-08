package com.undercurrent.legacyshops.service

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.shared.messages.NotificationMessageEntity
import com.undercurrent.shared.messages.UserOutputProvider
import com.undercurrent.shared.utils.tx

interface Broadcaster {
    suspend fun broadcast(msg: String): Int
}

interface BroadcastFromStorefront : Broadcaster
interface BroadcastToCustomers : Broadcaster

//todo make use of this in more places, especially where Storefront.broadcast() is
class StorefrontToCustomerBroadcaster(
    val thisStorefront: Storefront,
    private val customerFetcher: CanFetchCustomerList = CustomerListFetcher(thisStorefront),
    private val customerNotifier: (User) -> UserOutputProvider<NotificationMessageEntity>,
) : BroadcastFromStorefront, BroadcastToCustomers, CanFetchCustomerList {

    override suspend fun broadcast(msg: String): Int {
        tx { activeCustomers.forEach { customer ->

                val contentStr = """
            |``$msg``
            |
            |Shop: ${thisStorefront.displayName}
            | • Date joined: ${UtilLegacy.formatDbDate(customer.createdDate)}
            | • Join code: ${thisStorefront.joinCode}
        """.trimMargin()

                val strToSend = PressAgent.wrappedHint(
                    "Incoming message from your vendor:",
                    contentStr,
                    CmdRegistry.MENU,
                    CmdRegistry.FEEDBACK,
                )

                customerNotifier(customer.user).let {
                    it.sendOutput(strToSend)
                    // consider sending actual START menu here
                }
            }
        }

        return activeCustomers.count()
    }

    override fun fetchList(): List<ShopCustomer> {
        return customerFetcher.fetchList()
    }

//    val displayName: String by lazy {
//        tx { thisStorefront.displayName }
//    }

//    val joinCode: String by lazy {
//        tx { thisStorefront.joinCode }
//    }

    private val activeCustomers: List<ShopCustomer> by lazy {
        fetchList()
    }


}
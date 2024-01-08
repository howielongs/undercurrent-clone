package com.undercurrent.legacyshops.nodes.vendor_nodes

import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.CanFetchCustomer
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.HasBackingStorefrontField
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.HasVendorField
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.HasVendorUserField
import com.undercurrent.legacyshops.nodes.ShopAppOperationNode
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.storefronts.*
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.components.CanSendTypingIndicator
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import org.jetbrains.exposed.sql.and

interface HasTypingIndicatorDelay : CanSendTypingIndicator {
    suspend fun sendTypingIndicatorWithDelay()
}

sealed class AbstractShopVendorNode(
    context: SystemContext,
) : ShopAppOperationNode(
    context
), CanSendTypingIndicator,
    HasBackingStorefrontField<SystemContext>,
    HasVendorField,
    CanFetchCustomer<DeliveryOrder>,
    HasVendorUserField {

    override fun fetchCustomer(entity: DeliveryOrder): ShopCustomer? {
        return tx {
            entity.customer
        }
    }

    override val thisVendorUser: User by lazy {
        tx {
            thisStorefront.user
        }
    }

    override val thisVendor: ShopVendor by lazy {
        tx {
            thisStorefront.vendor
        }
    }


    override val thisStorefront: Storefront by lazy {
        fetchStorefront() ?: throw IllegalStateException("Storefront not found")
    }



    override fun fetchStorefront(): Storefront? {
        return fetchStorefront(context)
    }

    override fun fetchStorefront(entity: SystemContext): Storefront? {
        val now = EpochNano()
        return tx {
            // Get the user SMS from the context, assuming context is accessible here.
            val userSms = entity.userSms.value

            // Directly perform the User fetch operation here.
            val user = User.find { Users.smsNumber eq userSms }.firstOrNull()

            // Perform the ShopVendor operation here.
            val shopVendor = user?.let {
                ShopVendor.find {
                    unexpiredExpr(ShopVendors, now) and (ShopVendors.user eq user.id)
                }.firstOrNull()
            }

            // Finally, fetch all Storefronts for the ShopVendor.
            shopVendor?.let {
                Storefront.find {
                    Storefronts.vendor eq shopVendor.id and unexpiredExpr(Storefronts, now)
                }.toList()
            }?.firstOrNull()
        }
    }


}
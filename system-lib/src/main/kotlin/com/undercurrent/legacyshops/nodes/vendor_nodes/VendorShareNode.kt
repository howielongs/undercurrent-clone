package com.undercurrent.legacyshops.nodes.vendor_nodes

import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendors
import com.undercurrent.shared.repository.entities.BotSms
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SystemContext

class VendorShareNode(
     context: SystemContext,
) : AbstractShopVendorNode(context) {

    override suspend fun next(): TreeNode? {
        val shareStringWithJoinCodeUrl = tx {
            ShopVendors.fetchBySms(context.user.smsNumber)?.let {
                vendorShareCode(
                    joinCode = it.joinCode.toString(),
                    botSmsForCustomer = DbusProps(roleIn = ShopRole.CUSTOMER, envIn = context.environment).toBotSms(),
                )
            } ?: "Vendor not found"
        }

        sendOutput(shareStringWithJoinCodeUrl)
        return null
    }

    //todo needs testing to ensure shows correct url
    private fun vendorShareCode(joinCode: String, botSmsForCustomer: BotSms): String {
        return "Join my shop! COPY & PASTE this entire message or send code \"$joinCode\" to " +
                "\"https://signal.me/#p/$botSmsForCustomer\" ($botSmsForCustomer) in the Signal Messenger app."
    }

}
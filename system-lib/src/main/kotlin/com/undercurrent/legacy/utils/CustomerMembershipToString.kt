package com.undercurrent.legacy.utils

import com.undercurrent.system.context.SystemContext
import com.undercurrent.shared.utils.tx

class CustomerMembershipToString(val context: SystemContext) {
    fun generateString(): String {
        return tx {
            val user = context.user
            val currentVendor = context.user.currentCustomerProfile?.shopVendor
            val userId = user.id.value
            val vendorId = currentVendor?.uid
            val vendorNickname = currentVendor?.nickname

            if (currentVendor == null) {
                """|• User #${userId} (${context.role})
            |• Not a member of any shop""".trimMargin()

            } else {
                """|• User #${userId} (${context.role})
            |• Member of: Vendor #${vendorId} - $vendorNickname""".trimMargin()
            }
        }
    }
}
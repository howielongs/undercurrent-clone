package com.undercurrent.swaps.repository.entities

import com.undercurrent.swaps.repository.companions.SwapAdminCompanion
import org.jetbrains.exposed.dao.id.EntityID

object SwapAdmins : SwapBotTable("swap_admins") {
    val swapUser = reference("swap_user", SwapUsers)
}

class SwapAdmin(id: EntityID<Int>) : SwapBotEntity(id, SwapUsers) {
    var swapUser by SwapUser referencedOn SwapAdmins.swapUser

    companion object : SwapAdminCompanion()
}
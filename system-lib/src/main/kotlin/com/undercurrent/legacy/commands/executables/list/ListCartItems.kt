package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer

class ListCartItems(sessionContext: SessionContext) : Executable(CmdRegistry.VIEWCART, sessionContext) {

    fun displayCartContents(): Boolean {
        with(sessionContext.user.currentCustomerProfile?.cart ?: ShopCustomer.Cart()) {
            sessionContext.interrupt(this.receiptText)
            return this.isNotEmpty()
        }
    }

    override suspend fun execute() {
        displayCartContents()
    }
}
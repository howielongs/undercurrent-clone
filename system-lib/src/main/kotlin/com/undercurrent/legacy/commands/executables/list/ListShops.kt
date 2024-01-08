package com.undercurrent.legacy.commands.executables.list

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.system.context.SessionContext

class ListShops(sessionContext: SessionContext) : Executable(
    CmdRegistry.LISTSHOPS, sessionContext
) {


    override suspend fun execute() {
        sessionContext.interrupt(ShopCustomer.activeStorefrontsToString(sessionContext.user))

        //perhaps find better way to use this call if not linked to any stores:
//        sessionUser.interrupt(PressAgent.customerNotLinked())


    }
}
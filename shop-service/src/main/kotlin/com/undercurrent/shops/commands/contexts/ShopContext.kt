package com.undercurrent.shops.commands.contexts

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.experimental.command_handling.GlobalCommand
import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.types.enums.SystemApp

open class ShopContext(
    systemApp: SystemApp = SystemApp.SHOP,
    cmdsForChildren: Set<RootCommand> = setOf(GlobalCommand.MYINFO),
) : ShoppingAppContext(
    systemApp = systemApp,
    cmdsForChildren = cmdsForChildren,
) {
    class Admin : ShopContext(
        SystemApp.SHOP, setOf(
            GlobalCommand.MYINFO
        )
    )

    //may want to pass in Channel instead of User?
    //also, define in abstract class to keep better wrapping here
    class Vendor(vendorUser: SystemUserNew? = null) : VendorCmdChecks(vendorUser = vendorUser)

    class Customer : ShopContext(
        SystemApp.SHOP, setOf(
            GlobalCommand.MYINFO
        )
    )
}
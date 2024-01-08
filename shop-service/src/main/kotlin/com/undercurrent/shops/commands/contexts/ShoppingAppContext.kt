package com.undercurrent.shops.commands.contexts

import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.experimental.command_handling.BaseModuleContext
import com.undercurrent.shared.types.enums.SystemApp
import com.undercurrent.shops.commands.ShopCommand.*
import kotlin.reflect.KClass


sealed class ShoppingAppContext(
    systemApp: SystemApp,
    cmdsForChildren: Set<RootCommand> = setOf(),
) : BaseModuleContext(
    systemApp = systemApp,
    cmdsForChildren = cmdsForChildren,
) {
    override fun contextToCommands(): Map<KClass<out BaseModuleContext>, Set<RootCommand>> {
        return mapOf(
            ShopContext.Admin::class to setOf(
                ADDVENDOR,
                LISTVENDORS,
            ),
            ShopContext.Customer::class to setOf(
                CHECKOUT,
                MENU,
            ),
            ShopContext.Vendor::class to setOf(
                CONFIRM,
                ADDPRODUCT,
                ADDITEMSKU,
                RMITEMSKU,
                RMPRODUCT,
                LISTPRODUCTS,
                LISTITEMSKUS,
            ),
        )
    }
}




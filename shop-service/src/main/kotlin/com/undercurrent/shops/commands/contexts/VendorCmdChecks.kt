package com.undercurrent.shops.commands.contexts

import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.experimental.command_handling.GlobalCommand
import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.types.enums.SystemApp
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.commands.ShopCommand.*
import com.undercurrent.shops.repository.proto_versions.ProtoShopUser

//todo look for better way to do this with generics
abstract class VendorCmdChecks(
    systemApp: SystemApp = SystemApp.SHOP,
    cmdsForChildren: Set<RootCommand> = setOf(GlobalCommand.MYINFO),
    val vendorUser: SystemUserNew? = null,
) : ShopContext(
    systemApp = systemApp,
    cmdsForChildren = cmdsForChildren,
) {
    private val shopUser: ProtoShopUser? = tx { vendorUser?.let { ProtoShopUser(it) } }

    private fun filterCmds(
        startingSet: MutableList<RootCommand>,
        condition: Boolean,
        trueSet: List<RootCommand>,
        falseSet: List<RootCommand>
    ): MutableList<RootCommand> {
        if (condition) {
            startingSet.addAll(trueSet)
        } else {
            startingSet.removeAll(falseSet)
        }
        return startingSet
    }

    override fun commands(): Set<RootCommand> {
        var cmds: MutableList<RootCommand> = super.commands().toMutableList()

        cmds = filterCmds(
            cmds, hasProducts(), listOf(
                LISTPRODUCTS,
                RMPRODUCT,
                ADDITEMSKU,
                ADDPRODUCT,
            ), listOf(
                LISTITEMSKUS,
                RMITEMSKU,
                ADDITEMSKU,
                RMPRODUCT,
                LISTPRODUCTS
            )
        )

        cmds = filterCmds(
            cmds, hasSaleItems(), listOf(
                LISTPRODUCTS,
                ADDPRODUCT,
                ADDITEMSKU,
                LISTITEMSKUS,
                RMPRODUCT,
                RMITEMSKU,
            ), listOf(
                LISTITEMSKUS,
                RMITEMSKU,
            )
        )

        cmds = filterCmds(
            startingSet = cmds, condition = isVendor(), trueSet = listOf(
                ADDPRODUCT,
            ), falseSet = listOf(
                LISTPRODUCTS,
                ADDPRODUCT,
                ADDITEMSKU,
                LISTITEMSKUS,
                RMPRODUCT,
                RMITEMSKU,
                CONFIRM,
                ORDERS,
                MENU,
                ADDVENDOR,
            )
        )

        cmds = filterCmds(
            cmds, hasOrdersToConfirm(), listOf(
                CONFIRM,
                ORDERS,
            ), listOf(
                CONFIRM,
            )
        )

        cmds = filterCmds(
            cmds, hasAnyOrders(), listOf(
                ORDERS,
            ), listOf(
                CONFIRM,
                ORDERS
            )
        )

        return cmds.toSet()
    }

    private fun hasProducts(): Boolean {
        if (!isVendor()) {
            return false
        }
        shopUser ?: return false
        return tx {
            shopUser.products
                .filter { it.isNotExpired() }
                .toList()
                .isNotEmpty()
        }
    }

    private fun hasSaleItems(): Boolean {
        if (!isVendor() || !hasProducts()) {
            return false
        }
        shopUser ?: return false
        return tx {
            shopUser
                .saleItems
                .filter { it.isNotExpired() }
                .toList()
                .isNotEmpty()
        }
    }

    private fun isVendor(): Boolean {
        shopUser ?: return false
        return tx {
            shopUser
                .shopVendors
                .filter { it.isNotExpired() }
                .toList()
                .isNotEmpty()
        }
    }

    private fun hasAnyOrders(): Boolean {
        if (!isVendor()) {
            return false
        }
        shopUser ?: return false
        return tx {
            shopUser.deliveryOrders
                .filter { it.isNotExpired() }
                .toList()
                .isNotEmpty()
        }
    }


    private fun hasOrdersToConfirm(): Boolean {
        if (!isVendor() || !hasAnyOrders()) {
            return false
        }
        shopUser ?: return false
        return tx {
            shopUser.ordersToConfirm
                .filter { it.isNotExpired() }
                .toList()
                .isNotEmpty()
        }
    }

    fun canAddProduct(): Boolean {
        return true
    }

    fun canAddItemSku(): Boolean {
        return true
    }

    fun canRemoveItemSku(): Boolean {
        return true
    }

    fun canRemoveProduct(): Boolean {
        return true
    }

    fun canConfirm(): Boolean {
        return true
    }

    fun canListProducts(): Boolean {
        return true
    }

    fun canListOrders(): Boolean {
        return true
    }


}
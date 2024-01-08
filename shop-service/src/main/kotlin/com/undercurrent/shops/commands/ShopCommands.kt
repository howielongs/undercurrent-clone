package com.undercurrent.shops.commands

import com.undercurrent.shared.experimental.command_handling.RootCommand
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole


enum class ShopCommand(
    override val hint: String = "",
    override val permissions: Set<AppRole>,
//    override val runnerFunc: RunnerFuncType = null
) : RootCommand {
    GEN_MANY_CODES(
        "Generate many join codes",
        setOf(ShopRole.ADMIN),
    ),

    MYCODES(
        "View join codes for this shop (will generate CSV if large number)",
        setOf(ShopRole.VENDOR),
    ),

    AUTOCONFIRM(
        "Set/unset auto-confirm for your orders",
        setOf(ShopRole.VENDOR),
    ),

    EDITFEES(
        hint = "Set fees to be collected by system (and then split to admins)",
        permissions = setOf(ShopRole.ADMIN),
    ),
    OPENORDERS(
        "List open confirmed orders",
        setOf(ShopRole.ADMIN, ShopRole.CUSTOMER, ShopRole.VENDOR),
    ),

    MARKPAID(
        "Mark an order as paid",
        setOf(ShopRole.ADMIN),
    ),

    ENABLE_STRIPE(
        hint = "Select a vendor to allow Stripe payments",
        permissions = setOf(ShopRole.ADMIN),
    ),
    UPLOADMENU(
        "Upload a PDF menu (will have option to overwrite/replace existing)",
        setOf(ShopRole.VENDOR),
    ),

    //todo come back and allow customer to view menu after prompt on MENU
    VIEWPDFMENU(
        "View current PDF menu",
        setOf(ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    REMOVEMENU(
        "Remove PDF menu from the system",
        setOf(ShopRole.VENDOR),
    ),



    SHARE(
        hint = "Shareable shop code to send to customers",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR),
    ),
    ADDPRODUCT(
        hint = "Add a new product to the shop",
        permissions = setOf(ShopRole.VENDOR),
    ),
    LISTPRODUCTS(
        hint = "List all products in the shop",
        permissions = setOf(ShopRole.VENDOR),
    ),
    RMPRODUCT(
        hint = "Remove a new product from the shop",
        permissions = setOf(ShopRole.VENDOR),
    ),
    ADDITEMSKU(
        hint = "Add a new SKU to an existing product",
        permissions = setOf(ShopRole.VENDOR),
    ),
    LISTITEMSKUS(
        hint = "List all SKUs for a product",
        permissions = setOf(ShopRole.VENDOR),
    ),
    RMITEMSKU(
        hint = "Remove a new SKU from the shop",
        permissions = setOf(ShopRole.VENDOR),
    ),
    LISTVENDORS(
        hint = "List all vendors in the shop",
        permissions = setOf(ShopRole.ADMIN),
    ),
    RMVENDOR(
        "Remove vendor",
        permissions = setOf(ShopRole.ADMIN),
    ),
    ADDVENDOR(
        hint = "Add a new vendor and send registration",
        permissions = setOf(ShopRole.ADMIN),
    ),
    CHECKOUT(
        hint = "Submit purchase of items",
        permissions = setOf(ShopRole.CUSTOMER),
    ),
    VENDORS(
        "System vendors",
        setOf(ShopRole.ADMIN),
    ),
    MENU(
        hint = "View items available to add to your cart",

        permissions = setOf(ShopRole.CUSTOMER),
    ),
    ORDERS(
        hint = "View orders submitted by customers",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    CONFIRM(
        hint = "Confirm orders submitted by customers",
        permissions = setOf(ShopRole.VENDOR),
    ),
    SHOPS(
        "View shops you are a member of",
        setOf(ShopRole.CUSTOMER),
    ),
    CART(
        "Customer cart",
        setOf(ShopRole.CUSTOMER),
    ),
    WALLET(
        "Display wallet status to admins",
        setOf(ShopRole.ADMIN),
    ),




    ;

    override fun handle(): String {
        return this.name
    }
}

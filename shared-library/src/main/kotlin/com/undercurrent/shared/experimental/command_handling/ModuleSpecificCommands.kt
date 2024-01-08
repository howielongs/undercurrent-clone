package com.undercurrent.shared.experimental.command_handling

import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole

const val COMMAND_NOT_FOUND_STR = "NO COMMAND FOUND"


interface RootCommand {
    val hint: String
    val permissions: Set<AppRole>
//    val runnerFunc: RunnerFuncType

    fun handle(): String
}

enum class GlobalCommand(
    override val hint: String = "",
    override val permissions: Set<AppRole>,
//    override val runnerFunc: RunnerFuncType = null

) : RootCommand {
    START(
        hint = "Display start menu with commands",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    CANCEL(
        hint = "Got it! The operation is canceled",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    FEEDBACK(
        hint = "Send Feedback Suggest improvements or report an issue",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    MYINFO(
        hint = "Show user info",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
    ),
    ;

    override fun handle(): String {
        return this.name
    }
}

package com.undercurrent.legacy.commands.executables.remove


import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.list.ListCartItems
import com.undercurrent.legacy.commands.registry.CmdRegistry.CLEARCART
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.promptables.dataprompts.VerifyInputsPrompt
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.CartItems
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

interface CanCheckIsEmpty {
    fun isEmpty(): Boolean
}

class ClearCart(sessionContext: SessionContext) : Executable(CLEARCART, sessionContext), CanCheckShouldShow,
    CanCheckIsEmpty {

    private fun clearCart(customerProfile: ShopCustomer): Boolean {
        return transaction {
            CartItem.find { CartItems.customer eq customerProfile.uid }
                .toList()
                .filter { it.isNotExpired() }
                .forEach {
                    it.expire()
                }
            return@transaction true
        }
    }

    override fun isEmpty(): Boolean {
        return try {
            ListCartItems(sessionContext).displayCartContents()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if cart has any items (impl displayWhen method)
     * If no items, inform user
     * If items, display
     * Ask and confirm if cart should be cleared out
     */
    override suspend fun execute() {
        VerifyInputsPrompt(
            sessionContext = sessionContext,
            prompt = "Are you sure you want to clear your cart?\n  Y. Yes\n  N. No",
            noText = "Cart has not been modified. Operation complete.",
            preFunc = {
                ListCartItems(sessionContext).displayCartContents()
            },
            yesFunc = { clearCart(thisCustomerProfile) }
        ) {
            ListCartItems(sessionContext).displayCartContents()
        }.promptUser()
    }

    override fun shouldShow(): Boolean {
        return !isEmpty()
    }

    //todo unsure if this should be implemented
    companion object Prompts
}
package com.undercurrent.legacy.commands.executables.admin

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.dinosaurs.prompting.InputValidator
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.context.SessionContext
import org.jetbrains.exposed.sql.transactions.transaction

class CleanPrices(sessionContext: SessionContext) : Executable(CmdRegistry.CLEANPRICES, sessionContext),
    CanSendToUserByRole {
    private val interrupter by lazy {
        sessionContext.interrupter
    }

    override fun sendOutputByRole(msgBody: String, role: AppRole) {
        interrupter.sendOutputByRole(msgBody, role)
    }


    override suspend fun execute() {

        transaction {
            SaleItem.all().toList().forEach { item ->
                InputValidator.validateCurrency(item.price)?.let { newPrice ->
                    if (newPrice != item.price) {
                        item.price = newPrice
                        Log.debug("Updated item #${item.uid} price to $newPrice")
                    } else {
                        Log.debug("No changes for item #${item.uid}'s price of ${item.price}")
                    }
                } ?: run {
                    Log.error(
                        "Error: Invalid format -> ${item.price}",
                        sourceClass = "InputOutputHandler"
                    )
                    sendOutputByRole("Error: Invalid format -> ${item.price}", ShopRole.ADMIN)
                    return@transaction
                }
            }
        }
        sendOutputByRole("Prices cleaned", ShopRole.ADMIN)
    }
}
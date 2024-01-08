package com.undercurrent.legacyshops.service

import com.undercurrent.legacyshops.repository.entities.shop_items.CartItem
import com.undercurrent.legacyshops.repository.entities.shop_items.SaleItem
import com.undercurrent.shared.formatters.formatPretty
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

//  YOUR CART:
//  • The Leopard Gecko (1 gecko) $0.25 x 1 ..... $0.25
//  Details: This is a cute leopard gecko. He's a gecko, that looks like a leopard. Kind of.
//  ......
//  TOTALS
//  Subtotal: $0.50
//  Fees:     $0.08
//  Total:     $0.58


interface CartItemPresenter {
    fun present(cartItem: CartItem): String
}

class CartItemStringPresenter : CartItemPresenter {

    override fun present(cartItem: CartItem): String {
        return transaction {
            buildCartItemString(cartItem)
        }
    }

    private fun buildCartItemString(cartItem: CartItem): String {
        var output = ""
        try {
            val saleItem = cartItem.saleItem
            if (BigDecimal(cartItem.quantity.toString()) > BigDecimal.ZERO) {
                val formattedPrice = formatPrice(saleItem, cartItem.quantity)
                val details = saleItem.details
                val notes = generateNotesStr(cartItem.notes, prefix = "\t", suffix = "\n\n")

                output = "${saleItem.name} (${saleItem.unitSize}) $formattedPrice\n\tDetails: $details$notes"
            }
        } catch (e: Exception) {
            println(e.stackTraceToString())
        }
        return output
    }

    private fun formatPrice(item: SaleItem, quantity: Int): String {
        val totalPrice = BigDecimal(item.price).multiply(BigDecimal(quantity.toString()))
        return "$${item.price} x $quantity ..... $${formatPretty(totalPrice)}"
    }

    private fun generateNotesStr(noteStr: String, prefix: String = "", suffix: String = ""): String {
        return if (noteStr.isBlank()) "" else "$prefix Notes: $noteStr$suffix"
    }
}



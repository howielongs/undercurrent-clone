package com.undercurrent.legacyshops.repository.entities.shop_items

import com.undercurrent.legacy.commands.executables.stripe.StripeManager
import com.undercurrent.legacy.repository.entities.payments.StripePrice
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrder
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal

class CartItem(id: EntityID<Int>) : ExposedEntityWithStatus2(id, CartItems) {
    companion object : RootEntityCompanion0<CartItem>(CartItems)

    var deliveryOrder by DeliveryOrder optionalReferencedOn CartItems.deliveryOrder
    var saleItem by SaleItem referencedOn CartItems.saleItem
    var customerProfile by ShopCustomer referencedOn CartItems.customer

    var notes by CartItems.notes
    var quantity by CartItems.quantity

    //todo make notes nullable
    @Deprecated("Get rid of this when possible")
    private fun generateNotesStr(noteStr: String, prefix: String = "", suffix: String = ""): String {
        return if (noteStr == "") {
            ""
        } else {
            "${prefix}Notes: $noteStr$suffix"
        }
    }

    //todo come back and do fetchThenCreate()
    fun toStripePrice(stripe: StripeManager): StripePrice.Entity? {
        return tx {

            //todo fix up this fetching...
            val feePercentDecimal = customerProfile.storefront.feePercentDecimal()

            with(saleItem) {
                stripe.createStripeProductAndPrice(
                    name = name.toString() + " (${unitSize})",
                    description = details.toString() + generateNotesStr(
                        noteStr = notes,
                        prefix = "\n\n"
                    ),
                    unitAmountCents = BigDecimal(price)
                        .multiply(BigDecimal(feePercentDecimal.toString()).add(BigDecimal(1)))
                        .multiply(BigDecimal(100)).toInt(),
                )?.let {
                    //todo don't save if this object already exists
                    StripePrice.Entity.new {
                        saleItem = this@with
                        priceStripeId = it.id
                    }
                }

            }
        }
    }

//   override fun toString(): String {
//        return transaction {
//            var outString = ""
//            try {
//                saleItem.let {
//                    if (BigDecimal(quantity.toString()) > BigDecimal(0)) {
//                        var bullet = ""
//                        outString += "$bullet${it.name} (${it.unitSize}) " +
//                                "$${it.price} x $quantity ..... $${
//                                    formatPretty(
//                                        BigDecimal(it.price).multiply(BigDecimal(quantity.toString()))
//                                    )
//                                }\n" +
//                                "\tDetails: ${it.details}\n" + generateNotesStr(notes, prefix = "\t", suffix = "\n\n")
//                    }
//                }
//            } catch (e: Exception) {
//                println(e.stackTraceToString())
//            }
//            return@transaction outString
//        }
//    }
}


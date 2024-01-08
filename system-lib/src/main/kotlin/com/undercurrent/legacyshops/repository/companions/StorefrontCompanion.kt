package com.undercurrent.legacyshops.repository.companions

import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomers
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.utils.time.unexpiredExpr
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import java.math.BigDecimal
import java.math.RoundingMode

open class StorefrontCompanion : RootEntityCompanion0<Storefront>(Storefronts) {

    //todo clean this up and have clearer source of truth
    val feePctDecimal: BigDecimal = BigDecimal(0.15).divide(BigDecimal("1"), 2, RoundingMode.UP)
    val feePctFull: BigDecimal = feePctDecimal.multiply(BigDecimal(100)).divide(BigDecimal("1"), 2, RoundingMode.UP)

//    fun fetchOrdersForStorefront(storefront: Storefront): List<DeliveryOrder> {
//        return tx {
//            DeliveryOrder.find { DeliveryOrders.storefront eq storefront.id }
//                .toList()
//        }
//    }

    suspend fun fetchActiveCustomers(thisStorefront: Storefront): List<ShopCustomer> {
        val linkedCustomersExpr =
            ShopCustomers.storefront eq thisStorefront.id and unexpiredExpr(ShopCustomers)

        return ctx {
            ShopCustomer.find { linkedCustomersExpr }
                .toList()
        }
    }


}
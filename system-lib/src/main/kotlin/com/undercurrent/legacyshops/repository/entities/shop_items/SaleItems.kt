package com.undercurrent.legacyshops.repository.entities.shop_items


import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.dinosaurs.prompting.selectables.NestedSkuSelectionMap
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.utils.VARCHAR_SIZE
import org.jetbrains.exposed.sql.transactions.transaction

object SaleItems : ExposedTableWithStatus2("shop_sale_items") {
    val product = reference("product_id", ShopProducts)

    //todo consider switching to BIGINT for money
    val price = varchar("price", VARCHAR_SIZE).clientDefault { "0" }
    val unitSize = varchar("unit_size", VARCHAR_SIZE)

    

    override fun singularItem(): String {
        return "SKU"
    }

    var unexpired: List<SaleItem> = listOf()
        get() {
            return transaction {
                SaleItem.all().filter { it.isNotExpired() }.toList()
            }
        }


    //todo will need to move removeCmd anyhow...
    //todo should this be different whether Vendor or Customer?
    suspend fun selectSkuFromNestedList(
        sessionContext: SessionContext,
        promptText: String = "Select an item:",
        emptyText: String = "No products available to select. \n${PressAgent.showHelp()}",
        storefront: Storefront,
        displayAttachments: Boolean = true,
        command: BaseCommand? = null,
    ): SaleItem? {
        val products = transaction { storefront.products }
        val saleItems = transaction { storefront.saleItems }
        if (saleItems.isNullOrEmpty()) {
            emptyText.let { sessionContext.interrupt(it) }
            return null
        }

        if (displayAttachments) {
            storefront.sendAllProductAttachments(
                sessionContext.user.uid,
                products,
                command = command,
                sessionContext = sessionContext
            )
            //todo a bit smelly: images need time to come in
            if (!sessionContext.isTestMode()) {
                Thread.sleep(1000L)
            }
        }

        //todo embed this all with existing SelectableEntity impl
        val selectableMenu = NestedSkuSelectionMap(
            products = products,
            headerText = promptText
        ).loadMenu()

        //todo improve this to actually pass entity reference instead of id
        UserInput.chooseSkuIdFromList(
            sessionContext,
            selectableMenu
        )?.let { selectedSku ->
            return transaction {
                return@transaction SaleItem.findById(selectedSku)
            }
        }
        return null
    }

    fun expireByProductId(productId: Int): Int {
        return transaction {
            var count = 0
            SaleItem.find { product eq productId }.toList().filter { it.isNotExpired() }.forEach {
                count++
                it.expire()
            }
            return@transaction count
        }
    }

}
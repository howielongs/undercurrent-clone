package com.undercurrent.legacyshops.repository.entities.storefronts

import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacy.types.enums.StorefrontPrefType
import com.undercurrent.legacy.types.string.PressAgent.StoreFront.storefrontWelcomeMsg
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.transactions.transaction

interface EntityFetcher<T>

interface CanFetchByJoinCode<T> {
    suspend fun fetchByJoinCode(code: JoinCode): T?
}


class StorefrontFetcherByJoinCode : EntityFetcher<Storefront>, CanFetchByJoinCode<Storefront> {

    override suspend fun fetchByJoinCode(code: JoinCode): Storefront? {
        return tx {
            if (code.entityType == JoinCodeType.STOREFRONT) {
                Storefront.findById(code.entityId)
            } else {
                null
            }
        }
    }
}


object Storefronts : ExposedTableWithStatus2(
    "shop_storefronts",
) {

    val vendor = reference("vendor_id", ShopVendors)

    val displayName = varchar("display_name", VARCHAR_SIZE).default("")
    val logoImgPath = varchar("logo_img_path", VARCHAR_SIZE).default("")
    val welcomeMsg = varchar("welcome_msg", VARCHAR_SIZE).default(storefrontWelcomeMsg())

    // ultimately make this nullable (or just id ref)
    val joinCode = varchar("join_code", VARCHAR_SIZE)

    fun autoConfirmStorefronts(): List<Storefront> {
        return StorefrontPrefs.fetchByKey(StorefrontPrefType.AUTOCONFIRM)
            .filter { transaction { it.value == true.toString() } }
            .distinctBy { transaction { it.storefront } }
            .mapNotNull { transaction { it.storefront } }
            .toList()
    }

    // figure out how to add this to abstract parent
    fun unexpired(): List<Storefront> {
        return transaction { Storefront.all().toList().filter { it.isNotExpired() } }
    }


}
package com.undercurrent.shops.repository.proto_versions.handlers

import com.undercurrent.shared.experimental.command_handling.ModuleContextOld
import com.undercurrent.shops.repository.proto_versions.ProtoShopProduct
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront


class CreateShopProductHandler(
    val context: ModuleContextOld,
) {

    fun create(
        storefront: ProtoStorefront,
        name: String,
        description: String,
        imagePaths: List<String>?
    ): ProtoShopProduct? {
        return ProtoShopProduct(storefront, name, description).create()
    }
}
package com.undercurrent.shops.types.exceptions

class VendorCreationException(private val s: String? = null) : Exception() {

    override val message: String?
        get() = s ?: "Vendor creation were not successful due to some reasons"
}

class ShopVendorAlreadyExistsException(private val s: String? = null) : Exception() {


    override val message: String?
        get() = s ?: "Shop vendor already exists"
}





package com.undercurrent.shared.repository.entities

import com.undercurrent.shared.UserWithSms
import com.undercurrent.shared.abstractions.CanOutputUid
import com.undercurrent.shared.abstractions.EntityWithExpiry
import com.undercurrent.shared.types.enums.ShopRole
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column


interface UserEntity : UserWithSms, EntityWithExpiry, CanOutputUid {
    var smsNumber: String
    var role: ShopRole
    var uuid: String?
}

interface UserTable {
    var smsNumber: Column<String>
    val role: Column<String>
    var uuid: Column<String?>
}

interface VendorEntity<T : UserEntity> {
    var user: T
    var nickname: String
}

interface StorefrontEntity<T : UserEntity> {
    val vendor: VendorEntity<T>
    var welcomeMsg: String
    var displayName: String
    var joinCode: String
}

interface CustomerEntity<T : UserEntity> {
    var user: T
    var storefront: StorefrontEntity<T>
}


interface VendorTable {
    val user: Column<EntityID<Int>>
    val nickname: Column<String>
}

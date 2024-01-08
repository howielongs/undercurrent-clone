package com.undercurrent.shared.types.enums

interface HasSystemRole {
    fun getSystemRole(): SystemRole
}

interface UserRole : HasSystemRole {
    val name: String
    val ordinal: Int
}


enum class SystemRole : UserRole {
    SUPERUSER,
    PARTNER,
    USER,
    ;

    override fun getSystemRole(): SystemRole {
        return this
    }
}


//this is for specific apps, such as shop or swap
interface AppRole : UserRole, HasSystemRole {
    fun getAppName(): SystemApp
}

//eventually will move to shop-service
enum class ShopRole : AppRole {
    ADMIN {
        override fun getSystemRole(): SystemRole {
            return SystemRole.SUPERUSER
        }
    },
    VENDOR {
        override fun getSystemRole(): SystemRole {
            return SystemRole.PARTNER
        }
    },
    CUSTOMER {
        override fun getSystemRole(): SystemRole {
            return SystemRole.USER
        }
    }
    ;


    override fun getAppName(): SystemApp {
        return SystemApp.SHOP
    }
}


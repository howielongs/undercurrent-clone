package com.undercurrent.legacyswaps

import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.SystemApp
import com.undercurrent.shared.types.enums.SystemRole

//todo ultimately move to swap-service
enum class SwapRole : AppRole {
    SWAP_ADMIN {
        override fun getSystemRole(): SystemRole {
            return SystemRole.SUPERUSER
        }
    },
    BANKER {
        override fun getSystemRole(): SystemRole {
            return SystemRole.PARTNER
        }
    },
    SWAPPER {
        override fun getSystemRole(): SystemRole {
            return SystemRole.USER
        }
    }

    ;

    override fun getAppName(): SystemApp {
        return SystemApp.SWAP
    }
}
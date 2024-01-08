package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class UserCryptoAddressFetcher : CanFetchCryptoAddresses<CryptoAddress> {
    override fun fetchCryptoAddresses(thisUser: User): List<CryptoAddress> {
        val query = CryptoAddresses.user eq thisUser.id and (unexpiredExpr(CryptoAddresses))

        return tx {
            CryptoAddress.find { query }.toList()
        }
    }
}
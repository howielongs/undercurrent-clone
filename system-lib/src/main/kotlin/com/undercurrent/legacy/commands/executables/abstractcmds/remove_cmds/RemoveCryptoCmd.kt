package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2

class RemoveCryptoCmd(
    sessionContext: SessionContext,
    private val fetchCryptoAddrFun: (User) -> List<CryptoAddress> = { UserCryptoAddressFetcher().fetchCryptoAddresses(it) },
) : RemoveCmds(CmdRegistry.RMCRYPTO, sessionContext, "crypto address"), CanFetchCryptoAddresses<CryptoAddress> {

    override fun fetchCryptoAddresses(user: User): List<CryptoAddress> {
        return fetchCryptoAddrFun(user)
    }

    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return fetchCryptoAddresses(sessionContext.user)
    }

    override fun entityTable(): ExposedTableWithStatus2 {
        return CryptoAddresses
    }


}


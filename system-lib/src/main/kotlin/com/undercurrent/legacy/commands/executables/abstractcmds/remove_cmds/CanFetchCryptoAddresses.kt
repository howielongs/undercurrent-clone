package com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds

import com.undercurrent.system.repository.entities.User
import com.undercurrent.shared.abstractions.CryptoAddressEntity

interface CanFetchCryptoAddresses<T : CryptoAddressEntity> {
    fun fetchCryptoAddresses(thisUser: User): List<T>
}


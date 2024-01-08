package com.undercurrent.legacy.repository.repository_service.payments.crypto





import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.undercurrent.legacy.repository.entities.payments.MobAccounts
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.legacy.repository.repository_service.payments.crypto.MobileCoinAcctExistenceChecker.checkIfAcctAlreadyImported
import com.undercurrent.legacy.service.crypto.mobilecoin.requests.*
import com.undercurrent.shared.utils.Log


sealed interface MobileCoinAcctLoaderInterface {
    suspend fun load(): MobAccounts.Entity?
}

object MobileCoinAcctExistenceChecker {
    suspend fun checkIfAcctAlreadyImported(name: MobAcctNameText = MobileCoinDefaultValues.name()): MobAccounts.Entity? {
        try {
            fetchImportedAccounts()?.let { importedAccts ->
                if (importedAccts.isEmpty()) {
                    Log.debug("Imported accounts is empty")
                    return null
                } else {
                    val accountJson = importedAccts.firstOrNull { it.toString().contains(name.cleanedValue) }
                    accountJson?.value?.let { jObj ->
                        with(jObj.asJsonObject) {
                            return MobAccounts.Entity.save(
                                accountIdIn = MobileCoinText(this["account_id"]),
                                mainAddressIn = MobileCoinText(this["main_address"]),
                                nameIn = MobAcctNameText(this["name"]),
                                rawJsonIn = jObj.asJsonObject.toString(),
                            )
                        }
                    } ?: Log.warn("No imported accounts found (checkIfAcctAlreadyImported)")
                }
            }
        } catch (e: Exception) {
            Admins.notifyError("MOB checkIfAcctAlreadyImported", e)
        }
        return null
    }

    //todo should be a layer above MobileCoinApiRequest
    private suspend fun fetchImportedAccounts(): List<MutableMap.MutableEntry<String, JsonElement>> {
        return try {
            GetAllAccounts.run()?.let { r ->
//                Log.debug("RESPONSE FOR GET-ALL-ACCOUNTS:\n${r.asJsonObject.toString()}")
                r.asJsonObject["account_map"].asJsonObject.entrySet().toList()
            } ?: listOf()
        } catch (e: Exception) {
            Admins.notifyError("fetchImportedAccounts MOB", e)
            listOf()
        }
    }


    private suspend fun isDefaultAcctImported(): Boolean {
        return GetAllAccounts.run()?.let { r ->
            try {
                Log.debug("RESPONSE FOR isDefaultAcctImported:\n${r.asJsonObject.toString()}")
                val name =
                    r.asJsonObject["account_map"].asJsonObject.entrySet().first().value.asJsonObject["name"].toString()
                Log.debug("Extracted name $name")
                name.contains(MobileCoinDefaultValues.name().cleanedValue)
                true
            } catch (e: Exception) {
                Admins.notifyError("is default MOB account imported", e)
                false
            }
        } ?: false

    }

    private suspend fun getDefaultAccountOnService(): JsonObject? {
        return GetAllAccounts.run()?.let { r ->
            try {
                Log.debug("RESPONSE FOR getDefaultAccountOnService:\n${r.asJsonObject.toString()}")
                r.asJsonObject["account_map"].asJsonObject.entrySet().first().value.asJsonObject
            } catch (e: Exception) {
                Admins.notifyError("getDefaultAccountOnService MOB ", e)
                null
            }
        }
    }
}

//impl secrets to mask the mnemonic
open class MobileCoinAccountLoader(
    private val name: MobAcctNameText,
    private val mnemonic: MobMnemonicText,
) : MobileCoinAcctLoaderInterface {

    override suspend fun load(): MobAccounts.Entity? {
        return try {
            MobAccounts.Entity.fetch(name) ?: checkIfAcctAlreadyImported() ?: importViaApi()
        } catch (e: Exception) {
            Admins.notifyError("MobileCoinAccountLoader load", e)
            null
        }
    }


    //if already imported, but not yet in db, will need to copy data
    private suspend fun importViaApi(): MobAccounts.Entity? {
        return ImportAccount(
            mnemonic = mnemonic,
            name = name,
        ).run()?.let {
            saveLocally(it)
        } ?: null
    }


    /**
     * Error types:
     * - InvalidMnemonic,
     * - Database(AccountAlreadyExists
     */
    private fun saveLocally(jsonObj: JsonObject): MobAccounts.Entity? {
        return try {
            with(jsonObj["account"].asJsonObject) {
                MobAccounts.Entity.save(
                    nameIn = MobAcctNameText(this["name"]),
                    accountIdIn = MobileCoinText(this["account_id"]),
                    mainAddressIn = MobileCoinText(this["main_address"]),
                    rawJsonIn = this.toString(),
                )
            }
        } catch (e: Exception) {
            Admins.notifyError(
                "MOB saveLocally",
                exception = e
            )
//            Admins.notifyError(
//                "MOB saveLocally",
//                exception = e
//            )
            null
        }
    }
}

class DefaultMobAccount :
    MobileCoinAccountLoader(MobileCoinDefaultValues.name(), MobileCoinDefaultValues.mnemonic())


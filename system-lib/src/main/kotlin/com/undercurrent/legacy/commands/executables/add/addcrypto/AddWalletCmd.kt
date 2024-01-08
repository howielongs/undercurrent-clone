package com.undercurrent.legacy.commands.executables.add.addcrypto

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.CanFetchCryptoAddresses
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.UserCryptoAddressFetcher
import com.undercurrent.legacy.commands.registry.CmdRegistry.ADDWALLET
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.InputField
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableCallback
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.payments.StripeApiKeys
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.CryptoType
import com.undercurrent.legacy.types.enums.StripeKeyType
import com.undercurrent.legacy.types.enums.currency.CurrencyLegacyInterface
import com.undercurrent.legacy.types.string.PressAgent.selectCryptoPrompt
import com.undercurrent.legacy.utils.expireAncestor
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.context.SystemContext
import org.jetbrains.exposed.sql.transactions.transaction

@Deprecated("Use Impl as Nodes")
class AddWalletCmd(sessionContext: SessionContext) : Executable(ADDWALLET, sessionContext),
    CanFetchCryptoAddresses<CryptoAddress> {


    override suspend fun execute() {
        overwriteCryptoCmd(sessionContext)
    }

    private val cryptoAddrFetcher: CanFetchCryptoAddresses<CryptoAddress> = UserCryptoAddressFetcher()
    override fun fetchCryptoAddresses(thisUser: User): List<CryptoAddress> {
        return cryptoAddrFetcher.fetchCryptoAddresses(thisUser)
    }


    private fun genCryptoAddressesString(cryptoAddresses: List<CryptoAddress>): String {
        var outString = ""
        transaction { cryptoAddresses }.forEach {
            outString += "• $it\n"
        }
        return outString
    }


    /**
     * Display current payments addresses (if one exists)
     * Ask which one to update
     * Confirm Y/N
     * Expire old key to entityAncestor
     * Write new key
     */
    private suspend fun overwriteCryptoCmd(sessionContext: SessionContext) {
        with(fetchCryptoAddresses(sessionContext.user)) {
            if (isNotEmpty()) {
                genCryptoAddressesString(this).let {
                    sessionContext.interrupt("Your current wallet addresses and keys:\n${it}")
                }
            }
        }

        val options = mutableListOf(
            SelectableCallback("BTC (Bitcoin)    <-- default", this::overwriteBtcCmd),
            SelectableCallback("MOB (MobileCoin)", this::overwriteMobCmd),
        )

        if (sessionContext.role == ShopRole.VENDOR) {
            if (thisStorefront.isEnabledForStripe()) {
                var keyType = StripeKeyType.TEST
                if (sessionContext.environment == Environment.PROD) {
                    keyType = StripeKeyType.LIVE
                    options.add(SelectableCallback("Stripe API Key (${keyType.name})", this::overwriteStripeLiveKey))
                } else {
                    options.add(SelectableCallback("Stripe API Key (${keyType.name})", this::overwriteStripeTestKey))
                }
            }
        }

        UserInput.selectAndRunCallback(
            sessionContext,
            options,
            headerText = selectCryptoPrompt(),
            headlineText = ADDWALLET.name.uppercase()
        )
    }


    // see about returning cryptoType enum to pass to 'promptCryptoOverwrite'
    private suspend fun overwriteMobCmd(sessionContext: SystemContext) {
        return promptCryptoOverwrite(sessionContext, CryptoType.MOB)
    }

    private suspend fun overwriteBtcCmd(sessionContext: SystemContext) {
        return promptCryptoOverwrite(sessionContext, CryptoType.BTC)
    }

    private suspend fun overwriteStripeLiveKey(sessionContext: SystemContext) {
        return promptOverwriteStripeKey(sessionContext, StripeKeyType.LIVE)
    }

    private suspend fun overwriteStripeTestKey(sessionContext: SystemContext) {
        return promptOverwriteStripeKey(sessionContext, StripeKeyType.TEST)
    }


    @Deprecated("Use Impl as Nodes")
    private suspend fun promptOverwriteStripeKey(
        sessionContext: SystemContext,
        stripeKeyType: StripeKeyType = StripeKeyType.TEST,
    ) {
        val currencyInterface: CurrencyLegacyInterface = CryptoType.STRIPE

        var typeStr = stripeKeyType.name.uppercase()
        val labelStr = "$currencyInterface API Secret Key (${typeStr})"

        var existingKeyOutputWithColon = ""
        var confirmVerb = "Add $labelStr"
        transaction { thisStorefront.fetchStripeKey(stripeKeyType) }?.let { existingKey ->
            var existingKeyStr = ""

            transaction { existingKey.stripeApiSecretKey }?.let { value ->
                existingKeyStr = value
                sessionContext.interrupt("Current $labelStr:\n${value}")
                confirmVerb = "Overwrite existing $labelStr"
                existingKeyOutputWithColon = ": $existingKeyStr"
            }

            // get rid of usage of this prompt method
            UserInput.promptAndConfirm(
                sessionContext = sessionContext,
                inputField = InputField(
                    name = StripeApiKeys.Table::stripeApiSecretKey.name,
                    type = stripeKeyType.validationType,
                    prompt = "Please input $labelStr",
                    displayName = "$labelStr"
                ),
                confirmTextVerb = confirmVerb,
                noText = "$labelStr unchanged$existingKeyOutputWithColon"
            )?.let { newKeyValue ->

                StripeApiKeys.save(
                    storefrontIn = thisStorefront,
                    keyValue = newKeyValue,
                    stripeKeyType = stripeKeyType
                )?.let { newStripeMapping ->
                    expireAncestor(
                        existingKey,
                        sessionContext,
                        tx { newStripeMapping.uid },
                        "updated $labelStr"
                    )

                    expireAncestor(
                        existingKey,
                        sessionContext = sessionContext,
                        newId = tx { newStripeMapping.uid },
                        newMemo = "updated $labelStr",
                    )
                    addOrOverwriteCryptoAddress(sessionContext, CryptoType.STRIPE, newAddress = newKeyValue)
                }
                sessionContext.interrupt(
                    "Successfully updated $labelStr to: $newKeyValue"
                )

                return
            }
            return
        } ?: run {
            // account does not allow stripe mapping

            "This account is not enabled to use Stripe. Please contact your administrator.".let {
                sessionContext.interrupt(it)
            }
            "$sessionContext attempted to add $labelStr".let {
                Admins.notifyError(it)
            }
            return
        }
    }

    private fun cryptoAddressForCurrency(
        user: User,
        currencyType: CryptoType = CryptoType.BTC
    ): CryptoAddress? {
        return fetchCryptoAddresses(user).firstOrNull {
            it.type.uppercase() == currencyType.abbrev().uppercase()
        }
    }


    // this needs major cleanup
    @Deprecated("Use Impl as Nodes")
    private suspend fun promptCryptoOverwrite(
        sessionContext: SystemContext,
        currencyType: CryptoType
    ) {

        var existingAddrStr = ""
        var confirmVerb = "Add address"
        var existingCryptoAddress: CryptoAddress? = null
        cryptoAddressForCurrency(sessionContext.user, currencyType)?.let {
            sessionContext.interrupt("Current $currencyType address:\n${it.address}")
            existingCryptoAddress = it
            existingAddrStr = ": ${it.address}"
            confirmVerb = "Overwrite existing address"
        }

        UserInput.promptAndConfirm(
            sessionContext = sessionContext,
            inputField = InputField(
                name = CryptoAddress::address.name,
                type = currencyType.validationType,
                prompt = "Please input address to your ${currencyType.fullName} wallet:",
                displayName = "Wallet address"
            ),
            confirmTextVerb = confirmVerb,
            noText = "${currencyType.fullName} address unchanged$existingAddrStr"
        )?.let {
            if (addOrOverwriteCryptoAddress(
                    sessionContext,
                    currencyType.abbrev(),
                    it,
                    existingCryptoAddress
                )
            ) {
                sessionContext.interrupt(
                    "Successfully updated ${currencyType.fullName} payments address to: $it"
                )

                existingCryptoAddress ?: run {
                    if (sessionContext.role == ShopRole.VENDOR) {
                        transaction { sessionContext.user.shopVendor }?.let { vendorProfile ->
                            "New payment method added: ${currencyType.selectableLineString()}".let {
                                vendorProfile.broadcastToActiveCustomers(it)
                            }
                        }
                    }
                }

                return
            }
        }
        sessionContext.interrupt("Crypto address for ${currencyType.fullName} unchanged.")
    }

    private fun addOrOverwriteCryptoAddress(
        sessionContext: SystemContext,
        incomingCurrencyType: String = "BTC",
        newAddress: String,
        oldCryptoAddress: CryptoAddress? = null,
    ): Boolean {
        return CryptoType.BTC.abbrevToType(incomingCurrencyType)?.let {
            addOrOverwriteCryptoAddress(
                sessionContext, it,
                newAddress, oldCryptoAddress
            )
        } ?: false
    }

    private fun addOrOverwriteCryptoAddress(
        sessionContext: SystemContext,
        incomingCurrencyType: CryptoType = CryptoType.BTC,
        newAddress: String,
        oldCryptoAddressIn: CryptoAddress? = null
    ): Boolean? {

        transaction {
            CryptoAddress.new {
                address = newAddress
                user = sessionContext.user
                type = incomingCurrencyType.abbrev()
            }.let {
                val oldAddress = oldCryptoAddressIn ?: CryptoAddresses.fetchUnexpiredByCurrency(
                    sessionContext.user,
                    incomingCurrencyType
                ).firstOrNull()

                oldAddress?.let {
                    expireAncestor(
                        it,
                        sessionContext = sessionContext,
                        // ensure this id actually exists at this point
                        newId = transaction { it.uid },
                        newMemo = "updated payments address"
                    )
                }
            }
        }
        return true
    }

}
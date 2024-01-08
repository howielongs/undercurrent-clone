package com.undercurrent.legacy.commands.executables.info

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.CanFetchCryptoAddresses
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.StorefrontFetcher
import com.undercurrent.legacy.commands.executables.abstractcmds.remove_cmds.UserCryptoAddressFetcher
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.service.VersionFetcher
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.ctx
import com.undercurrent.shared.view.components.CanSendTypingIndicator
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.service.dbus.TypingIndicatorSender
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime


class MyInfoCmd(sessionContext: SystemContext) : Executable(CmdRegistry.MYINFO, sessionContext),
    CanFetchCryptoAddresses<CryptoAddress>, CanSendTypingIndicator {


    override fun sendTypingIndicator(recipientHumanAddr: SignalSms) {
        TypingIndicatorSender(
            humanRecipientAddr = recipientHumanAddr,
            shouldCancel = false,
            dbusProps = sessionContext.routingProps,
        ).send()
    }


    override suspend fun execute() {
        sendTypingIndicator(recipientHumanAddr = sessionContext.userSms)
        val myInfo = getMyInfo(userIn = sessionContext.user, role = sessionContext.role)
        sessionContext.interrupt(myInfo)
    }

    val version by lazy {
        VersionFetcher.fetchVersion()
    }

    private suspend fun getMyInfo(userIn: User, role: AppRole): String = coroutineScope {
        val addresses = async { fetchCryptoAddressesString(userIn) }


        var userSms: String
        var userCreatedDate: LocalDateTime

        val thisVendor = async { userIn.fetchVendor() }

        var infoString: String = ctx {
            userSms = userIn.smsNumber
            userCreatedDate = userIn.createdDate

            "SMS: ${userSms}\nRole: $role\n" +
                    "Creation Date: $userCreatedDate"
        }

        when (role) {
            ShopRole.VENDOR -> {
                thisVendor.await()?.let { vendor ->
                    infoString += "\n\nYour storefronts:"
                    StorefrontFetcher().fetchStorefronts(vendor).forEach {
                        infoString += "\n• ${storefrontInfoString(it)}"
                    }
                }
            }

            ShopRole.ADMIN -> {
                infoString += "\nVersion: $version"
            }

            ShopRole.CUSTOMER -> {
                ctx {
                    infoString += "\nShops you've joined:"
                    userIn.activeCustomerProfiles.forEach {
                        infoString += "\n• ${"${it.storefront?.displayName} (Join code: ${it.joinCode})"}\n"
                    }
                }
            }

        }
        infoString + "\n\nCrypto addresses:\n${addresses.await()}"
    }

    private suspend fun storefrontInfoString(storefront: Storefront): String = coroutineScope {
        val countJob = async { Storefront.fetchActiveCustomers(storefront).count() }

        val str1 = async {
            ctx { "${storefront.displayName} (Join code: ${storefront.joinCode})," }
        }

        "${str1.await()} ${countJob.await()} customers)"
    }

    private val cryptoAddrFetcher: CanFetchCryptoAddresses<CryptoAddress> = UserCryptoAddressFetcher()

    override fun fetchCryptoAddresses(thisUser: User): List<CryptoAddress> {
        return cryptoAddrFetcher.fetchCryptoAddresses(thisUser)
    }


    private fun fetchCryptoAddressesString(user: User): String {
        var outString = ""
        fetchCryptoAddresses(user).forEach {
            outString += "• ${it}\n"
        }
        return outString
    }

}
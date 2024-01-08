package com.undercurrent.legacy.commands.executables.abstractcmds

import com.undercurrent.legacy.commands.executables.ExecutableExceptions
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.CanStartCommand
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.command_execution.CommandStarter
import org.jetbrains.exposed.sql.transactions.transaction

interface FetchableVendorCryptoAddresses {
    fun fetchVendorCryptoAddresses(): List<CryptoAddress>
}

class SystemCommandStarter(
    context: SystemContext,
) : CommandStarter(context = context)

abstract class Executable(
    open val thisCommand: BaseCommand,
    open val sessionContext: SessionContext,
    private val commandStarter: CommandStarter = SystemCommandStarter(sessionContext)
) : CanStartCommand {
    abstract suspend fun execute()

    override suspend fun startNewCommand(cmd: Any) {
        commandStarter.startNewCommand(cmd)
    }

    open val thisStorefront: Storefront by lazy {
        when (sessionContext.role) {
            ShopRole.VENDOR -> {
                transaction { thisShopVendor.currentStorefront } ?: throw ExecutableExceptions.LazyLoadException(
                    sessionContext, "Storefront", thisCommand.lower()
                )
            }

            ShopRole.CUSTOMER -> {
                transaction { thisCustomerProfile.storefront }
            }

            else -> {
                throw ExecutableExceptions.PermissionMismatchForCommand(sessionContext, thisCommand)
            }
        }
    }

    open val thisCustomerProfile: ShopCustomer by lazy {
        when (sessionContext.role) {
            ShopRole.CUSTOMER -> {
                transaction { sessionContext.user.currentCustomerProfile }
                    ?: throw ExecutableExceptions.CustomerNotLinked(
                        sessionContext,
                    )

            }

            else -> {
                throw ExecutableExceptions.PermissionMismatchForCommand(sessionContext, thisCommand)
            }
        }
    }

    open val thisShopVendor: ShopVendor by lazy {
        transaction {
            when (sessionContext.role) {
                ShopRole.VENDOR -> {
                    sessionContext.user.shopVendor ?: throw ExecutableExceptions.LazyLoadException(
                        sessionContext, "Vendor", thisCommand.lower()
                    )
                }

                ShopRole.CUSTOMER -> {
                    thisCustomerProfile.shopVendor ?: throw ExecutableExceptions.LazyLoadException(
                        sessionContext, "Vendor", thisCommand.lower()
                    )

                }

                else -> {
                    throw ExecutableExceptions.PermissionMismatchForCommand(sessionContext, thisCommand)
                }
            }
        }
    }

    /**
     * VENDOR cmds: confirm
     * CUSTOMER: checkout
     *
     * Both involve fetching the vendor's addresses
     */
    val vendorAddresses: List<CryptoAddress> by lazy {
        transaction {
            thisShopVendor.cryptoAddresses
        }.apply {
            ifEmpty {
                when (sessionContext.role) {
                    ShopRole.VENDOR -> ExecutableExceptions.VendorNoCryptoAtConfirm(sessionContext).action()
                    ShopRole.CUSTOMER -> throw ExecutableExceptions.CheckoutVendorNoCryptoAddresses(
                        sessionContext, thisShopVendor
                    )

                    else -> throw ExecutableExceptions.LazyLoadException(
                        sessionContext, "vendorCryptoAddresses", thisCommand.lower()
                    )
                }

            }
        }
    }


}
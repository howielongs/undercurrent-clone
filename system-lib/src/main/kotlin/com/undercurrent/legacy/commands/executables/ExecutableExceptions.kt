package com.undercurrent.legacy.commands.executables

import com.undercurrent.legacy.commands.executables.abstractcmds.CanCheckShouldShow
import com.undercurrent.legacy.commands.registry.BaseCommand
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.service.PermissionsValidator
import com.undercurrent.legacy.service.crypto.BtcWalletAddresses
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.CanSendToUserByRole
import com.undercurrent.shared.types.enums.Environment.DEV
import com.undercurrent.shared.types.enums.Environment.QA
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.messaging.outbound.sendInterrupt
import com.undercurrent.system.messaging.outbound.sendNotify
import org.jetbrains.exposed.sql.transactions.transaction

abstract class AbstractException(val sessionContext: SystemContext) : Exception() {

    open fun action() {
        Admins.notifyError(this.stackTraceToString())
        sessionContext.interrupt("Unable to complete operation. Check with your admin")
    }

}

object ExecutableExceptions {

    class GenericException(
        sessionContext: SystemContext,
        val sessionMsg: String? = null,
        private val adminsMsg: String? = null,
        val shopVendor: ShopVendor? = null,
        private val vendorMsg: String? = null,
        val customerProfile: ShopCustomer? = null,
        private val customerMsg: String? = null,
        val errorLogMsg: String? = null,
        private val sendMsg: Boolean = true,
    ) : AbstractException(sessionContext) {
        override fun action() {
            Log.error(errorLogMsg ?: sessionMsg ?: adminsMsg)
            if (sendMsg) {
                sessionMsg?.let {
                    sendInterrupt(
                        user = sessionContext.user,
                        role = sessionContext.routingProps.role,
                        environment = sessionContext.routingProps.environment,
                        msg = it,
                    )
                }

                adminsMsg?.let {
                    notifyAdmins(it)
                }

                vendorMsg?.let { msg ->
                    shopVendor?.let {
                        sendNotify(
                            tx { shopVendor.user },
                            ShopRole.VENDOR,
                            sessionContext.routingProps.environment,
                            msg,
                        )
                    }
                }

                customerMsg?.let { msg ->
                    customerProfile?.let {
                        sendNotify(
                            tx { customerProfile.user },
                            ShopRole.CUSTOMER,
                            sessionContext.routingProps.environment,
                            msg,
                        )
                    }
                }
            }
        }
    }

    class StorefrontNotFoundException(
        sessionContext: SessionContext
    ) : AbstractException(sessionContext) {

        override fun action() {
            Admins.notifyError("Storefront not found for $sessionContext for some reason...")
            sessionContext.interrupt("Unable to complete operation. Check with your admin")
        }
    }


    class CartSubtotalNullException(
        sessionContext: SessionContext
    ) : AbstractException(sessionContext) {
        override fun action() {
            Admins.notifyError("Cart for $sessionContext was null for some reason...")
            sessionContext.interrupt("Unable to complete operation. Check with your admin")
        }
    }

    class PermissionMismatchForCommand(
        sessionContext: SystemContext,
        val command: BaseCommand,
    ) : AbstractException(sessionContext) {
        override fun action() {
            sessionContext.interrupt(PermissionsValidator.insufficientPermissionsString(
                sessionContext = sessionContext,
                cmd = command,
                asAdmin = false
            ))

            with(PermissionsValidator.insufficientPermissionsString(
                sessionContext = sessionContext,
                cmd = command,
                asAdmin = true
            )) {
                notifyAdmins(this)
                if (sessionContext.isTestMode()) {
                    println(this)
                }
            }
        }
    }

    class VendorNoCryptoAtConfirm(
        sessionContext: SessionContext,
    ) : AbstractException(sessionContext), CanCheckShouldShow, CanSendToUserByRole {

        private val interrupter by lazy {
            sessionContext.interrupter
        }

        override fun sendOutputByRole(msgBody: String, role: AppRole) {
            interrupter.sendOutputByRole(msgBody, role)
        }

        private fun displayTestCryptoAddresses(): String {
            val mobAddressAdmin1 = ""
            return """
                       [TEST MODE] Sample addresses:
                       BTC
                       • vendor1: ${BtcWalletAddresses.addressV1}
                       • vendor2: ${BtcWalletAddresses.addressV2}
                       • admin1: ${BtcWalletAddresses.addressA1}
                       • admin2: ${BtcWalletAddresses.addressA2}
                       
                       MOB
                       • admin1: $mobAddressAdmin1
                       
                       • test1: SAMPLE_VALUE
                    """.trimIndent()

        }


        override fun action() {
            if (this@VendorNoCryptoAtConfirm.shouldShow()) {
                sendOutputByRole(displayTestCryptoAddresses(), ShopRole.VENDOR)
            }
            interrupter.sendOutput(PressAgent.Crypto.noCryptoAddressForUser())
        }

        override fun shouldShow(): Boolean {
            if (setOf(DEV).contains(RunConfig.environment)) {
                return true
            }
            if (sessionContext.user.hasAdminProfile && setOf(QA, DEV).contains(RunConfig.environment)) {
                return true
            }
            return false
        }

    }


    class CheckoutVendorNoCryptoAddresses(
        sessionContext: SessionContext,
        val shopVendor: ShopVendor,
    ) : AbstractException(sessionContext) {
        override fun action() {
            //todo export these strings to a better home
            //mostly for checkout cmd (should probably separate exceptions by command a bit better)
            PressAgent.vendorNoAddressYet().let { sessionContext.interrupt(it) }

            shopVendor.notify(
                "A customer would like to order some items, " +
                        "\nbut you do not yet have a payment method added. " +
                        "\n\nPlease use ${CmdRegistry.ADDWALLET.upper()} to add one. " +
                        "\n\nRefer to 'payments' for more help."
            )
            notifyAdmins(
                "Customer attempting to order, but vendor does not have payment method yet.\n\n" +
                        "Vendor notified to add payment method immediately."
            )
        }
    }

    class EmptyListException(
        sessionContext: SystemContext,
        private val entitiesName: String,
        private val operationTag: String = "something important",
    ) : AbstractException(sessionContext) {

        override fun action() {
            sessionContext.interrupt("No $entitiesName found")
            Admins.notifyError(
                "Empty list of $entitiesName for ${UserToIdString.toIdStr(sessionContext.user)} " +
                        "while performing ${operationTag}."
            )
        }
    }

    class NullLoadException(
        sessionContext: SystemContext,
        private val objectName: String,
        private val operationTag: String = "something important",
    ) : AbstractException(sessionContext) {

        override fun action() {
            sessionContext.interrupt("$objectName not found")
            Admins.notifyError(
                "Unable to fetch $objectName for ${UserToIdString.toIdStr(sessionContext.user)} " +
                        "while performing ${operationTag}."
            )
        }
    }


    class LazyLoadException(
        sessionContext: SessionContext,
        private val objectName: String,
        private val operationTag: String = "something important",
    ) : AbstractException(sessionContext) {

        override fun action() {
            sessionContext.interrupt("$objectName not found")
            Admins.notifyError(
                "Unable to fetch $objectName for ${UserToIdString.toIdStr(sessionContext.user)} " +
                        "while performing ${operationTag}."
            )
        }
    }

    class EmptyCartException(
        sessionContext: SessionContext,
    ) : AbstractException(sessionContext) {
        override fun action() {
            sessionContext.interrupt(PressAgent.customerEmptyCart())
        }
    }

    class CustomerNotLinked(
        sessionContext: SystemContext
    ) : AbstractException(sessionContext) {

        override fun action() {
            transaction {
                Admins.notifyError(
                    "User ${sessionContext.user.uid} not a member of a shop yet (attempted /menu): \n" +
                            "${sessionContext.user}"
                )
                sessionContext.interrupt(PressAgent.customerNotLinked())
            }

        }
    }

}


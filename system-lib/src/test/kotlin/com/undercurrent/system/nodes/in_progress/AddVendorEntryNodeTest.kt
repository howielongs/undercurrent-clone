package com.undercurrent.system.nodes.in_progress

import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.shop.*
import com.undercurrent.legacy.service.fetchers.UserFetcherBySms
import com.undercurrent.legacyshops.nodes.admin_nodes.AddVendorNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeBurstEvent
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeUsages
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.ctx
import com.undercurrent.system.repository.entities.AdminProfile
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.messages.NotificationMessage
import com.undercurrent.system.repository.entities.messages.NotificationMessages
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddVendorEntryNodeTest : BaseTestClass() {

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables, sqlLoggerIsNull = false)
        setupMocks()
        setUpAdminContext()
    }

    @Test
    fun `test add vendor - user doesnt exist yet SUCCESS`() = runBlocking {

        val targetSms = SignalSms(TestConsoleCentral.defaultVendorSms0)

        assertCounts(
            map = mapOf(
                User to 1,
                AdminProfile to 1,
                ShopVendor to 0,
                Storefront to 0,
                JoinCode to 0,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 0,
            )
        )

        ctx {
            Assertions.assertTrue(
                UserFetcherBySms.fetch(targetSms) == null
            ) { "User should be NULL" }
        }

        runAdminInputs(
            { AddVendorNodes(it).execute() },
            targetSms.value,
            "Mock Vendor1",
            "y",
            hasMap = mapOf(
                "Phone number: $targetSms" to "Should display `Phone number: $targetSms`",
                "New vendor successfully created" to "Should display success message",
            ),
            doesNotHaveMap = mapOf(
                "Invalid SMS number" to "Should not have received invalid sms number message",
                "try again with a different phone number?" to "Should prompt for retry",
                "vendor with this phone number already exists" to "Should notify of existing vendor with SMS",
                "Vendor creation cancelled" to "Should display cancellation",
            ),
        )


        //todo also assert Vendor receiving messages on creation (User, Vendor, Storefront, JoinCode)

        assertCounts(
            map = mapOf(
                User to 2,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                ShopCustomer to 0,
            )
        )
        Assertions.assertFalse(
            adminOutputs.getOutput().contains("Invalid SMS number")
        ) { "Should not have received invalid sms number message" }


        ctx {
            Assertions.assertTrue(
                UserFetcherBySms.fetch(targetSms)?.role == ShopRole.VENDOR
            ) { "User should be VENDOR role" }
        }

//        assertMapHas(
//            map = mapOf(
//                "Phone number: $targetSms" to "Should display `Phone number: $targetSms`",
//                "New vendor successfully created" to "Should display success message",
//            ),
//            outputProvider = adminOutputs,
//        )
//
//        assertMapDoesNotHave(
//            map = mapOf(
//                "Invalid SMS number" to "Should not have received invalid sms number message",
//                "try again with a different phone number?" to "Should prompt for retry",
//                "vendor with this phone number already exists" to "Should notify of existing vendor with SMS",
//                "Vendor creation cancelled" to "Should display cancellation",
//            ),
//            outputProvider = adminOutputs,
//        )

        assertVendorNotifications(
            vendorSms = targetSms,
            shouldBeEmpty = false,
            hasStrings = mapOf(
                "Welcome! Your vendor account is ready to use" to "Should display vendor welcome message",
                "Your unique vendor code is" to "display vendor code",
                "Recommended next actions" to "Should display recommended next actions",
            ),
            minMsgCount = 1,
            maxMsgCount = 3,
        )

    }


    @Test
    fun `test add vendor - user exists, but vendor does not SUCCESS`() = runBlocking {

        setUpFirstCustomerUserContext()

        //targetSms is CustomerSms, as customer will be upgraded to VENDOR role
        val targetSms = SignalSms(TestConsoleCentral.defaultCustomerSms1)

        assertCounts(
            map = mapOf(
                User to 2,
                AdminProfile to 1,
                ShopVendor to 0,
                Storefront to 0,
                JoinCode to 0,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                CryptoAddress to 0,
                ShopCustomer to 0,
            )
        )

        ctx {
            Assertions.assertTrue(
                UserFetcherBySms.fetch(targetSms)?.role == ShopRole.CUSTOMER
            ) { "User should be CUSTOMER role" }
        }

        runAdminInputs(
            { AddVendorNodes(it).execute() },
            targetSms.value,
            "Mock Vendor1",
            "y",
            hasMap = mapOf(
                "Phone number: $targetSms" to "Should display `Phone number: $targetSms`",
                "New vendor successfully created" to "Should display success message",
            ),
            doesNotHaveMap = mapOf(
                "Invalid SMS number" to "Should not have received invalid sms number message",
                "try again with a different phone number?" to "Should prompt for retry",
                "vendor with this phone number already exists" to "Should notify of existing vendor with SMS",
                "Vendor creation cancelled" to "Should display cancellation",
                "Unable to create join code for new storefront" to "Should not display error message for join code creation",
            ),
        )

        assertCounts(
            map = mapOf(
                User to 2,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                CryptoAddress to 0,
                ShopCustomer to 0,
            )
        )


        ctx {
            Assertions.assertTrue(
                UserFetcherBySms.fetch(targetSms)?.role == ShopRole.VENDOR
            ) { "User should be VENDOR role" }
        }

        vendorOutputs.getOutput().let {
            println("VENDOR output:")
            println(it)
        }


        assertVendorNotifications(
            vendorSms = targetSms,
            shouldBeEmpty = false,
            hasStrings = mapOf(
                "Welcome! Your vendor account is ready to use" to "Should display vendor welcome message",
                "Your unique vendor code is" to "display vendor code",
                "Recommended next actions" to "Should display recommended next actions",
            ),
            minMsgCount = 1,
            maxMsgCount = 3,
        )


    }

    @Test
    fun `test addvendor user and vendor already exist FAIL`() = runBlocking {
        val targetSms = SignalSms(TestConsoleCentral.defaultVendorSms0)

        setUpFirstCustomerUserContext()
        setUpFirstVendorContext()

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                CryptoAddress to 0,
                ShopCustomer to 0,
            )
        )

        runAdminInputs(
            { AddVendorNodes(it).execute() },
            targetSms.value,          // input sms number
            "y",                // should retry after finding existing
            targetSms.value,          // input same sms number
            "n",                // should cancel after finding existing and declining to retry
            hasMap = mapOf(
                "vendor with this phone number already exists" to "Should notify of existing vendor with SMS",
                "try again with a different phone number?" to "Should prompt for retry",
                "Vendor creation cancelled" to "Should display cancellation",
                "Phone number: $targetSms" to "Should display `Phone number: $targetSms`",
            ),
            doesNotHaveMap = mapOf(
                "Invalid SMS number" to "Should not have received invalid sms number message",
                "New vendor successfully created" to "Should display success message",
            ),
        )

        assertCounts(
            map = mapOf(
                User to 3,
                AdminProfile to 1,
                ShopVendor to 1,
                Storefront to 1,
                JoinCode to 1,
                JoinCodeUsages.Entity to 0,
                JoinCodeBurstEvent.Entity to 0,
                CryptoAddress to 0,
                ShopCustomer to 0,
            )
        )

        assertVendorNotifications(
            vendorSms = targetSms,
            shouldBeEmpty = true,
            doesNotHaveStrings = mapOf(
                "Welcome! Your vendor account is ready to use" to "Should not display vendor welcome message",
                "Your unique vendor code is" to "display vendor code",
                "Recommended next actions" to "Should not display recommended next actions",
            ),
        )
    }

    private suspend fun assertVendorNotifications(
        vendorSms: SignalSms,
        hasStrings: Map<String, String> = mapOf(),
        doesNotHaveStrings: Map<String, String> = mapOf(),
        shouldBeEmpty: Boolean = false,
        minMsgCount: Int? = if (shouldBeEmpty) 0 else {
            1
        },
        maxMsgCount: Int? = if (shouldBeEmpty) 0 else {
            null
        },
        expectedMsgCount: Int? = if (shouldBeEmpty) 0.coerceAtMost(minMsgCount ?: 0) else {
            null
        },
    ) {
        ctx {
            val user = UserFetcherBySms.fetch(vendorSms)?.let {
                it
            } ?: run {
                assert(false) { "User should not be null" }
                return@ctx
            }


            val msgs = NotificationMessage.find {
                NotificationMessages.user eq user.id and (
                        NotificationMessages.role eq ShopRole.VENDOR.name and (
                                NotificationMessages.environment eq Environment.TEST.name
                                )) and (NotificationMessages.sendAfterEpochNano greaterEq start.value)
            }.toList()

            val allMsgs = msgs.toList().joinToString("\n\n------------------------\n") { it.body }

            val vendorOutStr = """
                |
                |++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                |VENDOR output ($vendorSms):
                |
                |${allMsgs}
                |++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                |
            """.trimMargin()

            println(vendorOutStr)

            if (shouldBeEmpty) {
                Assertions.assertTrue(
                    msgs.isNullOrEmpty()
                ) { "Should be empty: got \n\n${allMsgs}" }
                return@ctx
            }

            if (expectedMsgCount != null) {
                Assertions.assertEquals(
                    expectedMsgCount,
                    msgs.count()
                ) { "Should have $expectedMsgCount messages" }
            } else {
                minMsgCount?.let {
                    Assertions.assertTrue(
                        msgs.count() >= it
                    ) { "Should have at least $it messages" }
                }
                maxMsgCount?.let {
                    Assertions.assertTrue(
                        msgs.count() <= it
                    ) { "Should have at most $it messages" }
                }
            }



            hasStrings.forEach { (key, value) ->
                Assertions.assertTrue(
                    allMsgs.contains(key)
                ) { value }
            }

            msgs.forEach {
                val msg = it.body
                doesNotHaveStrings.forEach { (key, value) ->
                    Assertions.assertFalse(
                        msg.contains(key)
                    ) { value }
                }
            }

        }

    }

    //todo assert test cases for looping back to beginning for invalid input, already existing vendor, etc
}
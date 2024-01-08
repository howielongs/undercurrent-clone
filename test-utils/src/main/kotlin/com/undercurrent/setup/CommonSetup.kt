package com.undercurrent.setup

import com.undercurrent.legacy.repository.entities.payments.CryptoAddress
import com.undercurrent.legacy.repository.entities.payments.CryptoAddresses
import com.undercurrent.legacy.repository.entities.system.IntroEvents
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacy.utils.joincodes.RandomAbcStringGenerator
import com.undercurrent.legacyshops.nodes.customer_nodes.LinkCustomerToStorefrontCmdNodes
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeBurstEvent
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeUsages
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodes
import com.undercurrent.legacyshops.repository.entities.shop_items.*
import com.undercurrent.legacyshops.repository.entities.shop_orders.DeliveryOrders
import com.undercurrent.legacyshops.repository.entities.shop_orders.Invoices
import com.undercurrent.legacyshops.repository.entities.shop_orders.ShopOrderEvents
import com.undercurrent.legacyshops.repository.entities.storefronts.*
import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.time.EpochNano
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.context.SessionContext
import com.undercurrent.system.repository.entities.AdminProfile
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.repository.entities.Users
import com.undercurrent.system.repository.entities.messages.InboundMessages
import com.undercurrent.system.repository.entities.messages.NotificationMessage.Companion.logger
import com.undercurrent.system.repository.entities.messages.NotificationMessages
import com.undercurrent.system.repository.entities.messages.OutboundMessages
import com.undercurrent.testutils.MockInputProvider
import com.undercurrent.testutils.MockOutputProvider
import com.undercurrent.testutils.TestAssertUtils
import com.undercurrent.testutils.TestIOFormatterProvider
import org.jetbrains.exposed.sql.Table
import org.junit.jupiter.api.Assertions

const val FAIL_IF_NOT_IMPLEMENTED = false

fun failIfNotImplemented() {
    if (FAIL_IF_NOT_IMPLEMENTED) {
        throw NotImplementedError("This test is not implemented yet")
    }
}


val defaultSystemTables: List<Table> = listOf(
    Admins,
    Attachments.Table,
    CartItems,
    CryptoAddresses,
    DeliveryOrders,
    IntroEvents.Table,
    Invoices,
    JoinCodeBurstEvent.Table,
    JoinCodeUsages.Table,
    JoinCodes,
    InboundMessages,
    OutboundMessages,
    NotificationMessages,
    ShopProducts,
    SaleItems,
    ShopCustomers,
    ShopOrderEvents,
    ShopVendors,
    StorefrontPrefs.Table,
    Storefronts,
    Users,
)

abstract class BaseTestClass {
    var start: EpochNano = EpochNano(valueIn = 0L)

    protected lateinit var formatter: TestIOFormatterProvider

    protected lateinit var mockCustomerInputQueue1: MockInputProvider
    protected lateinit var mockCustomerInputQueue2: MockInputProvider
    protected lateinit var mockAdminInputQueue: MockInputProvider
    protected lateinit var mockVendorInputQueue: MockInputProvider

    protected lateinit var customerOutputs1: MockOutputProvider
    protected lateinit var customerOutputs2: MockOutputProvider
    protected lateinit var adminOutputs: MockOutputProvider
    protected lateinit var vendorOutputs: MockOutputProvider


    private var mockNotifyAdminsFunc: (String) -> Unit = { msg ->
        val dividerAdmin = "++++++++++++++++++++++++++++"
        println("ADMIN NOTIFICATION: \n$dividerAdmin\n$msg\n$dividerAdmin\n")
    }
    protected lateinit var asserts: TestAssertUtils

    protected lateinit var adminContext: SessionContext
    protected lateinit var customerContext1: SessionContext
    protected lateinit var customerContext2: SessionContext
    protected lateinit var vendorContext: SessionContext

    protected lateinit var mockAdminUser1: User
    protected lateinit var mockAdmin1: AdminProfile

    protected lateinit var mockCustomerUser1: User
    protected lateinit var mockCustomer1: ShopCustomer

    protected lateinit var mockCustomerUser2: User
    protected lateinit var mockCustomer2: ShopCustomer

    protected lateinit var mockVendorUser1: User
    protected lateinit var mockVendor1: ShopVendor
    protected lateinit var mockStore1: Storefront
    protected lateinit var mockJoinCode1: JoinCode

    protected lateinit var mockProduct1: ShopProduct

    protected lateinit var mockCustomerBtcAddr1: CryptoAddress
    protected lateinit var mockVendorBtcAddr1: CryptoAddress

    //todo impl types properly here
    protected lateinit var mockCustomerMobAddr1: CryptoAddress
    protected lateinit var mockVendorMobAddr1: CryptoAddress

    var adminContextSetUp = false
    var customer1ContextSetUp = false

    fun assertList(
        hasList: List<String>,
        doesNotHaveList: List<String> = listOf(),
        outputProvider: MockOutputProvider
    ) {
        assertMap(
            hasMap = hasList.map { it to it }.toMap(),
            doesNotHaveMap = doesNotHaveList.map { it to it }.toMap(),
            outputProvider = outputProvider,
        )
    }

    fun assertMap(
        hasMap: Map<String, String>,
        doesNotHaveMap: Map<String, String> = mapOf(),
        outputProvider: MockOutputProvider
    ) {
        assertMapHas(hasMap, outputProvider)
        assertMapDoesNotHave(doesNotHaveMap, outputProvider)
    }

    fun assertMapHas(map: Map<String, String>, outputProvider: MockOutputProvider) {
        map.forEach { (key, value) ->
            Assertions.assertTrue(
                outputProvider.getOutput().contains(key)
            ) { value }
        }
    }

    fun assertMapDoesNotHave(map: Map<String, String>, outputProvider: MockOutputProvider) {
        map.forEach { (key, value) ->
            Assertions.assertFalse(
                outputProvider.getOutput().contains(key)
            ) { value }
        }
    }

    suspend fun runMockUserInputs(
        startNode: suspend (SessionContext) -> Unit,
        vararg inputs: String,
        context: SessionContext,
        hasMap: Map<String, String> = mapOf(),
        doesNotHaveMap: Map<String, String> = mapOf(),
        inputProvider: MockInputProvider,
        outputProvider: MockOutputProvider,
    ) {
        inputs.forEach {
            inputProvider.add(it)
        }
        start = EpochNano()
        startNode(context)

        logger.info(outputProvider.getOutput())

        assertMap(
            hasMap,
            doesNotHaveMap,
            outputProvider,
        )

        val defaultDoesNotHave = mapOf(
            "com.undercurrent." to "Should not have java class names",
        )

        assertMap(
            hasMap = mapOf(),
            doesNotHaveMap = defaultDoesNotHave,
            outputProvider = outputProvider,
        )
    }

    suspend fun runVendorInputs(
        startNode: suspend (SessionContext) -> Unit,
        vararg inputs: String,
        hasMap: Map<String, String> = mapOf(),
        doesNotHaveMap: Map<String, String> = mapOf(),
    ) {
        runMockUserInputs(
            startNode = startNode,
            inputs = *inputs,
            context = vendorContext,
            inputProvider = mockVendorInputQueue,
            outputProvider = vendorOutputs,
            hasMap = hasMap,
            doesNotHaveMap = doesNotHaveMap,

            )
    }

    suspend fun runCustomerInputs(
        startNode: suspend (SessionContext) -> Unit,
        vararg inputs: String,
        hasMap: Map<String, String> = mapOf(),
        doesNotHaveMap: Map<String, String> = mapOf(),
        context: SessionContext = customerContext1,
        inputProvider: MockInputProvider = mockCustomerInputQueue1,
        outputProvider: MockOutputProvider = customerOutputs1,
    ) {
        runMockUserInputs(
            startNode = startNode,
            inputs = *inputs,
            context = context,
            inputProvider = inputProvider,
            outputProvider = outputProvider,
            hasMap = hasMap,
            doesNotHaveMap = doesNotHaveMap,
        )
    }

    suspend fun runAdminInputs(
        startNode: suspend (SessionContext) -> Unit,
        vararg inputs: String,
        hasMap: Map<String, String> = mapOf(),
        doesNotHaveMap: Map<String, String> = mapOf(),

        ) {
        runMockUserInputs(
            startNode = startNode,
            inputs = *inputs,
            context = adminContext,
            inputProvider = mockAdminInputQueue,
            outputProvider = adminOutputs,
            hasMap = hasMap,
            doesNotHaveMap = doesNotHaveMap,

            )
    }

    fun setupMocks() {
        formatter = TestIOFormatterProvider()

        mockCustomerInputQueue1 = MockInputProvider(formatter)
        customerOutputs1 = MockOutputProvider(formatter)

        mockCustomerInputQueue2 = MockInputProvider(formatter)
        customerOutputs2 = MockOutputProvider(formatter)

        mockAdminInputQueue = MockInputProvider(formatter)
        adminOutputs = MockOutputProvider(formatter)

        mockVendorInputQueue = MockInputProvider(formatter)
        vendorOutputs = MockOutputProvider(formatter)

        asserts = TestAssertUtils()
    }

    fun setUpAdminContext(mockAdminUserSms: String = TestConsoleCentral.fullValidNums[2]): SessionContext {
        mockAdminUser1 = tx {
            User.new {
                smsNumber = mockAdminUserSms
                role = ShopRole.ADMIN
            }
        }

        // should this be created automatically with new admin role user?
        mockAdmin1 = tx { AdminProfile.new { user = mockAdminUser1 } }

        adminContext = newMockContext(mockAdminUser1, ShopRole.ADMIN)

        adminContextSetUp = true

        assertNumItems(User, 1)
        assertNumItems(AdminProfile, 1)
        assertNumItems(ShopCustomer, 0)
        assertNumItems(ShopVendor, 0)
        assertNumItems(Storefront, 0)
        assertNumItems(JoinCode, 0)
        assertNumItems(ShopProduct, 0)
        assertNumItems(SaleItem, 0)
        assertNumItems(JoinCodeUsages.Entity, 0)
        assertNumItems(JoinCodeBurstEvent.Entity, 0)
        assertNumItems(CryptoAddress, 0)

        return adminContext
    }


    suspend fun setUpMockCustomerJoinedToMockStore(
        customerUser: User = mockCustomerUser1,
        customerContext: SessionContext = customerContext1,
        storefront: Storefront = mockStore1,
    ): Pair<ShopCustomer, SessionContext> {

        LinkCustomerToStorefrontCmdNodes(
            context = customerContext,
            body = tx { storefront.joinCode },
        ).execute()

        val thisCustomer = tx {
            ShopCustomer.find { ShopCustomers.user eq customerUser.id }.last()
        }

        return Pair<ShopCustomer, SessionContext>(thisCustomer, newMockContext(customerUser, ShopRole.CUSTOMER))
    }

    //todo do existence check first
    fun setUpFirstCustomerUserContext(
        mockCustomerUserSms: String = TestConsoleCentral.fullValidNums[1],
    ): SessionContext {
        mockCustomerUser1 = tx {
            User.new {
                smsNumber = mockCustomerUserSms
                role = ShopRole.CUSTOMER
            }
        }

        customerContext1 = newMockContext(mockCustomerUser1, ShopRole.CUSTOMER)
        return customerContext1
    }

    fun setUpFirstVendorContext(
        mockVendorUserSms: String = TestConsoleCentral.fullValidNums[0],
        mockVendorNickname: String = "Mock Vendor1",
        mockStoreDisplayName: String = "Mock Store1",
        newJoinCode: String = RandomAbcStringGenerator().generate()
    ) {
        mockVendorUser1 = tx {
            User.new {
                smsNumber = mockVendorUserSms
                role = ShopRole.VENDOR
            }
        }

        //todo consider using actual nodes in the midst of creation here
//        AddVendorNodes(adminContext).createVendorNode(
//            phoneNum = SignalSms(mockVendorUserSms),
//            nicknameIn = mockVendorNickname,
//            existingUser = mockVendorUser1,
//        )

        vendorContext = newMockContext(mockVendorUser1, ShopRole.VENDOR)

        mockVendor1 = tx {
            ShopVendor.new {
                user = mockVendorUser1
                nickname = mockVendorNickname
            }
        }



        mockStore1 = tx {
            Storefront.new {
                joinCode = newJoinCode
                vendor = mockVendor1
                displayName = mockStoreDisplayName
            }
        }

        mockJoinCode1 = tx {
            JoinCode.new {
                ownerUser = mockVendorUser1
                code = newJoinCode
                entityId = mockStore1.id.value
                entityType = JoinCodeType.STOREFRONT
            }
        }
    }

    fun setUpShopContext(
        joinCodeStr: String? = null,
        mockVendorUserSms: String = TestConsoleCentral.fullValidNums[0],
        mockVendorNickname: String = "Mock Vendor1",
        mockStoreDisplayName: String = "Mock Store1",
        mockCustomerUserSms: String = TestConsoleCentral.fullValidNums[1],
        mockAdminUserSms: String = TestConsoleCentral.fullValidNums[2],
    ) {
        val newJoinCode = joinCodeStr ?: RandomAbcStringGenerator().generate()

        setUpAdminContext(mockAdminUserSms)
        setUpFirstCustomerUserContext(mockCustomerUserSms)

        setUpFirstVendorContext(mockVendorUserSms, mockVendorNickname, mockStoreDisplayName, newJoinCode)



        assertNumItems(User, 3)
        assertNumItems(ShopVendor, 1)
        assertNumItems(ShopCustomer, 0)
        assertNumItems(Storefront, 1)
        assertNumItems(JoinCode, 1)
        assertNumItems(ShopProduct, 0)
        assertNumItems(SaleItem, 0)
        assertNumItems(JoinCodeUsages.Entity, 0)
        assertNumItems(JoinCodeBurstEvent.Entity, 0)
        assertNumItems(CryptoAddress, 0)
    }

    fun setUpProduct() {
        mockProduct1 = tx {
            ShopProduct.new {
                name = "Ticket To Ride"
                details = "Board game"
                storefront = mockStore1
            }
        }
        assertNumItems(ShopProduct, 1)
    }


    fun assertCounts(map: Map<RootEntityCompanion0<out RootEntity0>, Int>) {
        map.forEach {
            assertNumItems(it.key, it.value)
        }
    }


    fun <T : RootEntity0> assertNumItems(item: RootEntityCompanion0<T>, expected: Int) {
        tx { item.all().count().toInt() }.let {
            assert(it == expected) { "Expected $expected ${item::class.java.canonicalName}s, but found $it" }
        }
    }

    private fun newMockContext(
        user: User, role: ShopRole
    ): SessionContext {
        val dbusProps = DbusProps(roleIn = role, envIn = Environment.TEST)

        val inputProvider = when (role) {
            ShopRole.ADMIN -> mockAdminInputQueue
            ShopRole.CUSTOMER -> mockCustomerInputQueue1
            ShopRole.VENDOR -> mockVendorInputQueue
        }

        val interruptProvider: MockOutputProvider = when (role) {
            ShopRole.ADMIN -> adminOutputs
            ShopRole.CUSTOMER -> customerOutputs1
            ShopRole.VENDOR -> vendorOutputs
        }


        return SessionContext(
            user = user,
            routingProps = dbusProps,
            inputter = inputProvider,
            interrupter = interruptProvider,
        )
    }
}
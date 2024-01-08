package com.undercurrent.legacyshops.util

import com.undercurrent.legacy.utils.fetchLastActiveStr
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopCustomer
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.User

class NewCustomerNotifyStringBuilders(
    private val context: SystemContext,
    private val customerIn: ShopCustomer,
    private val joinCodeIn: JoinCode,
    private val storefrontIn: Storefront,
) {
    init {
        fetchAll()
    }

    val vendorOutString: String by lazy {
        fetchAll()
        "New customer linked to your store with $usedJoinCodeValue.\n" + "Current customer count: ${customerCount}\n$totalUsageStr"
    }

    val adminOutString: String by lazy {
        fetchAll()
        """
            |New customer added to storefront
            |
            |Customer: User #$userId
            | • Shops joined: ${shopsJoinedCount}${userLastActiveStr}
            | • Join code used: $usedJoinCodeValue
            |
            |Vendor #$vendorId (User #$vendorUserId)
            | • Name: $vendorNickname
            | • Total shops: $totalShopsForVendor
            | • Total customers: ${totalCustomersForVendor}${vendorLastActiveStr}
            |
            |Storefront #$shopId$storeDisplayNameStr
            | • 1st Join code: $shopJoinCodeValue
            | • Payment methods: $paymentMethodsListStr
            | • Customer count: $customerCount
            | • Products: $shopProductsCount
            | • SKUs: $shopSaleItemsCount
            |
            |Last users active:
            |$userActivityMapStr
            """.trimMargin()
    }

    private var isFetched = false

    lateinit var thisUser: User
    lateinit var thisVendor: ShopVendor
    lateinit var vendorUser: User
    private var customerCount: Int = 0
    private var totalUsages: Int = 0

    private lateinit var userLastActiveStr: String
    private lateinit var vendorLastActiveStr: String

    private lateinit var storeDisplayNameStr: String

    private val totalUsageStr: String by lazy {
        "Customers who have used this code: $totalUsages"
    }

    private var shopsJoinedCount: Int = 0
    private var totalShopsForVendor: Int = 0
    private var totalCustomersForVendor: Int = 0

    private var shopProductsCount: Int = 0
    private var shopSaleItemsCount: Int = 0

    private var userId: Int = 0
    private var shopId: Int = 0
    private var vendorId: Int = 0
    private var vendorUserId: Int = 0

    private lateinit var vendorNickname: String

    private lateinit var paymentMethodsListStr: String
    private lateinit var userActivityMapStr: String
    private lateinit var usedJoinCodeValue: String
    private lateinit var shopJoinCodeValue: String

    private fun blankOrBulletLine(str: String): String {
        return if (str == "") {
            ""
        } else {
            "\n • $str"
        }
    }

    //consider using coroutines or suspending here (awaits)
    private fun fetchAll() {

        if (isFetched) {
            return
        }

        //single fetch (no dependents)
        tx {
            usedJoinCodeValue = joinCodeIn.code
            totalUsages = joinCodeIn.usages.toList().count()
            thisUser = customerIn.user
        }

        //storefront-dependent
        tx {
            with(storefrontIn) {
                thisVendor = vendor

                shopId = id.value
                shopProductsCount = products.count()
                shopSaleItemsCount = saleItems.count()

                shopJoinCodeValue = joinCode
                customerCount = storefrontIn.activeCustomerCount

                storeDisplayNameStr = if (displayName == joinCode) {
                    ""
                } else {
                    " - ${com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts.displayName}"
                }

                userActivityMapStr = storefrontIn.userActivityMap()
            }
        }

        //user-dependent
        tx {
            with(thisUser) {
                userId = id.value
                shopsJoinedCount = activeCustomerProfiles.count()
            }

            //todo wrap some of these in own coroutines?
            userLastActiveStr = blankOrBulletLine(fetchLastActiveStr())
        }

        //vendor-dependent
        tx {
            with(thisVendor) {
                vendorUser = user
                vendorId = id.value
                vendorNickname = nickname

                totalShopsForVendor = activeStorefronts.count()
                totalCustomersForVendor = activeCustomerCountInt
                paymentMethodsListStr = cryptoAddresses.joinToString(", ") { it.type.uppercase() }
            }

        }

        //vendorUser-dependent
        tx {
            vendorUserId = vendorUser.id.value
            vendorLastActiveStr = blankOrBulletLine(fetchLastActiveStr())
        }

        isFetched = true
    }
}
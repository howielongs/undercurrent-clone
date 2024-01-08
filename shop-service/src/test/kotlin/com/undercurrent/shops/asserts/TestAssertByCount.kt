package com.undercurrent.shops.asserts

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shops.TestConsoleShops
import org.junit.jupiter.api.BeforeEach
import com.undercurrent.shops.commands.contexts.ShopContext


abstract class BasicDBIT {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val adminContext by lazy {
        ShopContext.Admin()
    }
    val vendorContext by lazy {
        ShopContext.Vendor()
    }
    val customerContext by lazy {
        ShopContext.Customer()
    }

//    fun <T : BaseError> assertException(actualErrorInstace: BaseError?, realErrorClass: Class<T>) {
//        Assertions.assertNotNull(actualErrorInstace)
//        Assertions.assertEquals(actualErrorInstace!!::class.java, realErrorClass)
//    }


//    fun assertNoException(actualErrorInstace: BaseError?) {
//        Assertions.assertNull(actualErrorInstace)
//    }

    @BeforeEach
    open fun setUp() {
        TestConsoleShops.setUpTests()
    }

    open fun assertCount(expected: Int) {
        assert(true)
    }

    fun assertCount(expected: Int, items: List<Any>) {
        assert(expected == items.count()) { "Expected $expected items, found ${items.count()}" }
    }

    fun assertNumAdmins(expected: Int) {
        assertCount(expected, SystemUserNew.fetchAdmins())
    }


}


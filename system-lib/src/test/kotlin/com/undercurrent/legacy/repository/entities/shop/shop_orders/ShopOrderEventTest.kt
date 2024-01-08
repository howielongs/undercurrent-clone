package com.undercurrent.legacy.repository.entities.shop.shop_orders

import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ShopOrderEventTest : BaseTestClass() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables, sqlLoggerIsNull = false)
        setupMocks()
        setUpShopContext()
    }

    fun assertCountOrdersToConfirm(expected: Int) {

    }

    @Test
    fun testNewEventTypeAndCount() {
        assertCountOrdersToConfirm(0)



        // build up: users, vendors, shops, orders, etc.

//        tx {
//            ShopOrderEvent.new {
//
//            }
//
//        }

        assertCountOrdersToConfirm(1)
    }
}
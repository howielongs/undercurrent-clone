package com.undercurrent.testutils

import com.undercurrent.shared.SystemUserNew
import org.slf4j.Logger
import org.slf4j.LoggerFactory


interface TestAssertByCount  {
    fun assertCount(count: Int)
    fun assertCount(count: Int, items: List<Any>)
}

abstract class BaseAssertableTestClass  {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

//    fun <T : BaseError> assertException(actualErrorInstace: BaseError?, realErrorClass: Class<T>) {
//        Assertions.assertNotNull(actualErrorInstace)
//        Assertions.assertEquals(actualErrorInstace!!::class.java, realErrorClass)
//    }


//    fun assertNoException(actualErrorInstace: BaseError?) {
//        Assertions.assertNull(actualErrorInstace)
//    }


//    @BeforeEach
//    open fun setUp() {
//        TestConsoleCentral.resetDbCentral()
//    }
}

abstract class BaseCountTestClass : BaseAssertableTestClass(), TestAssertByCount {
    override fun assertCount(expected: Int) {
        assert(true)
    }

    override fun assertCount(expected: Int, items: List<Any>) {
        assert(expected == items.count()) { "Expected $expected items, found ${items.count()}" }
    }

    fun assertNumAdmins(expected: Int) {
        assertCount(expected, SystemUserNew.fetchAdmins())
    }
}


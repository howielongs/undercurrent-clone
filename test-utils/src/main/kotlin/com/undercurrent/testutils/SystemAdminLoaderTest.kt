package com.undercurrent.testutils

import com.undercurrent.shared.SystemUserNew
import org.junit.jupiter.api.Test

class SystemAdminLoaderTest : BaseCountTestClass() {

    override fun assertCount(expected: Int) {
        AssertCount(SystemUserNew).assertCount(expected)
        assertNumAdmins(expected)
    }

    //    @Test
//    fun `test admins do not exist then create`() {
//        assertCount(0)
//
//        loadAndAssertAdminsCount(TEST, 1)
//
//        TestConsole.resetDb()
//
//        assertCount(0)
//
//        loadAndAssertAdminsCount(QA, 3)
//        loadAndAssertAdminsCount(QA, 3)
//
//    }

    @Test
    fun `test admin systemUser exists, but not system admins themselves`() {

    }

    @Test
    fun `test system admins and their system users fully exist on startup`() {

    }

    /**
     * If removed from list, this should sync
     */
    @Test
    fun `test sys admins removed between runs (one removed from list)`() {

    }

    @Test
    fun `test load system admins by environment`() {

    }
}
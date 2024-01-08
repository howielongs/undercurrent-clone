package com.undercurrent.testutils

import com.undercurrent.setup.TestConsoleCentral.FUTURE_TESTS_ENABLED
import com.undercurrent.shared.SystemUserNew
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class SystemAdminTest : BaseCountTestClass() {

    override fun assertCount(expected: Int) {
        val numAdmins = SystemUserNew.fetchAdmins().count()
        assert(expected == numAdmins) { "Expected $expected admins, found $numAdmins" }
    }

    @Ignore
    @Test
    fun `test admins added at startup`() {
        if (!FUTURE_TESTS_ENABLED) {
            assertCount(0)
            return
        }
        assertCount(1)
    }
}
package com.undercurrent.testutils

import com.undercurrent.setup.TestConsoleCentral.baseSmsNums
import com.undercurrent.shared.SystemUserNew
import org.junit.jupiter.api.Test
import kotlin.test.Ignore


class SystemUsersTest : BaseCountTestClass() {

    override fun assertCount(expected: Int) {
        CountUserAccounts().assertCount(expected)
    }

    @Ignore

    @Test
    fun testNoUsersExist() {
        assertCount(0)
    }
    @Ignore

    @Test
    fun fetchExistingUser() {
        assertCount(0)
        SystemUserNew.create(baseSmsNums[0], "test1")
        assertCount(1)
    }


}
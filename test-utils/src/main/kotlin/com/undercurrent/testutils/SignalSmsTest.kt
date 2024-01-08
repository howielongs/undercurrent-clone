package com.undercurrent.testutils

import com.github.michaelbull.result.Result
import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.types.errors.BaseError

const val SHOULD_ASSERT_ERROR_MESSAGE = false

typealias SystemUserBaseError = Result<SystemUserNew?, BaseError>

fun highlightErrorAssert() {
    if (SHOULD_ASSERT_ERROR_MESSAGE) {
        assert(false) {
            "Might want to check this test: previously was returning an error type rather than custom exception."
        }
    }
}
//
//class SignalSmsTest : CountUserAccounts() {
//
//    @Test
//    fun `test create new admin user`() {
//
//        val index = 0
//
//        assertCount()
//
//        val result: SystemUserBaseError = createAdminUser(index)
//
//        assertCount(0, 1, 1)
//        assertUserFields(
//            user = result.component1(),
//            signalSms = TestConsoleCentral.baseSmsNums[index],
//            signalUuid = TestConsoleCentral.uuids[index],
//            isAdmin = true
//        )
//
//    }
//
//    @Test
//    fun `test create new non-admin user`() {
//
//        val index = 1
//        assertCount()
//
//        val result: SystemUserBaseError = createNonAdminUser(index)
//
//        assertCount(1, 0, 1)
//        assertUserFields(
//            result.component1(),
//            TestConsoleCentral.fullValidNums[index],
//            TestConsoleCentral.uuids[index],
//            false
//        )
//    }
//
//    @Test
//    fun `try create new admin user error already exists`() {
//        assertCount()
//
//        createAdminUser(1)
//
//        assertCount(0, 1)
//
////        val result: SystemUserBaseError = createAdminUser(1)
//
//        assertCount(0, 1)
//        highlightErrorAssert()
//        return
////        assertException(result.getError(), ExistsError2::class.java)
//    }
//
//
//    @Test
//    fun `try create new non-admin user error already exists`() {
//        assertCount()
//        createNonAdminUser(1)
//        assertCount(1, 0, 1)
//        createNonAdminUser(1)
//        assertCount(1, 0, 1)
//    }
//
//    /**
//     * Unsure what the desired behavior should be here...
//     */
//    @Test
//    fun `downgrade admin to non-admin`() {
//
//        assertCount()
//
//        val adminUser = createAdminUser(0).component1()!!
//
//        assertCount(0, 1, 1)
//
//        createNonAdminUser(1)
//
//        assertCount(1, 1)
//
//        createNonAdminUser(0)
//        assertCount(1, 1)
//
//        SystemUserNew.downgradeFromAdmin(adminUser)
//        assertCount(2, 0)
//    }
//
//    /**
//     * Unsure what the desired behavior should be here...
//     */
//    @Test
//    fun `upgrade non admin to admin user`() {
//        assertCount()
//        createAdminUser(0)
//        assertCount(0, 1, 1)
//
//        val nonAdminToUpgrade = createNonAdminUser(1).component1()!!
//        assertCount(1, 1)
//
//        createAdminUser(1)
//        assertCount(1, 1)
//
//        SystemUserNew.upgradeToAdmin(nonAdminToUpgrade)
//        assertCount(0, 2)
//    }
//
//    private fun assertUserFields(user: SystemUserNew?, signalSms: String?, signalUuid: String?, isAdmin: Boolean) {
//        tx {
//            Assertions.assertNotNull(user)
//            Assertions.assertEquals(signalSms, user!!.signalSms)
//            Assertions.assertEquals(signalUuid, user.signalUuid)
//            Assertions.assertEquals(isAdmin, user.isAdmin)
//        }
//    }
//
//
//    private fun assertCount(nonAdmins: Int = 0, admins: Int = 0, totalUsers: Int = nonAdmins + admins) {
//        with(fetchAll()) {
//            assert(nonAdmins == this.count { !it.isAdmin }) {
//                "Expected $nonAdmins non-admins, but found ${this.count { !it.isAdmin }}"
//            }
//            assert(admins == this.count { it.isAdmin }) {
//                "Expected $admins admins, but found ${this.count { it.isAdmin }}"
//            }
//            assert(totalUsers == this.count()) {
//                "Expected $totalUsers total users, but found ${this.count()}"
//            }
//        }
//    }
//
//
//    private fun createAdminUser(index: Int): SystemUserBaseError {
//        val signalSms: String = TestConsoleCentral.baseSmsNums[index]
//        val signalUuid: String? = TestConsoleCentral.uuids[index]
//
//        return SystemUserNew.create(signalSms, signalUuid, true)
//    }
//
//    private fun createNonAdminUser(index: Int): SystemUserBaseError {
//        val signalSms = TestConsoleCentral.baseSmsNums[index]
//        val signalUuid = TestConsoleCentral.uuids[index]
//        return SystemUserNew.create(signalSms, signalUuid, false)
//    }


    /**
     * Ensure nothing gets set to null, and see about changing isAdmin at any point
     */
//    @Test
//    fun `test try upsert new uuid from null`() {
//        assertCount()
//
//        val sms = TestConsole.validNums[1]
//
//        val newUser: User = createUser(sms, null, false).component1()!!
//        assertCount(1, 0)
//
//        User.upsert(sms, TestConsole.uuids[1])
//        assertCount(1, 0)
//    }
//
//
//    @Test
//    fun `test try upsert new signalSms from null`() {
//        assertCount()
//
//        val sms = TestConsole.validNums[1]
//        val uuid = TestConsole.uuids[1]
//
//        createUser(null, uuid, false)
//        assertCount(1, 0)
//
//        createUser(sms, uuid, false)
//        assertCount(1, 0)
//    }
//
//    @Test
//    fun `test try upsert new sms and uuids from non-null`() {
//        assertCount()
//        createAdminUser(0)
//        assertCount(0, 1)
//
//        User.create(TestConsole.validNums[1], TestConsole.uuids[0])
//        assertCount(0, 1)
//
//        User.create(TestConsole.validNums[1], TestConsole.uuids[1])
//        assertCount(0, 1)
//    }

//}
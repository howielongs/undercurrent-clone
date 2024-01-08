package com.undercurrent.testutils
//
//import com.github.michaelbull.result.Err
//import com.undercurrent.setup.TestConsoleCentral
//import com.undercurrent.setup.defaultSystemTables
//import com.undercurrent.shared.repository.repository.system.SystemUserNew
//import com.undercurrent.shared.types.errors.BaseError
//import com.undercurrent.shared.utils.Log
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import java.sql.SQLFeatureNotSupportedException
//
//class SystemUserCreatorTest : CountUserAccounts() {
//    @BeforeEach
//    fun setUp() {
//        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
//    }
//
//    fun assertCount(nonAdmins: Int = 0, admins: Int = 0, totalUsers: Int = nonAdmins + admins) {
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
//    private fun createUser(signalSms: String, signalUuid: String?, isAdmin: Boolean): SystemUserBaseError {
//        return try {
//            SystemUserNew.create(signalSms, signalUuid, isAdmin)
//        } catch (e: SQLFeatureNotSupportedException) {
//            Log.error("SQLFeatureNotSupportedException: ${e.message}", e)
//            // Handle the exception
//            e.printStackTrace()
//            Err(BaseError("SQLFeatureNotSupportedException: ${e.message}"))
//        }
//
//    }
//
//    private fun createAdminUser(index: Int): SystemUserBaseError {
//        val signalSms = TestConsoleCentral.baseSmsNums[index]
//        val signalUuid = TestConsoleCentral.uuids[index]
//        return createUser(signalSms, signalUuid, true)
//    }
//
//    private fun createNonAdminUser(index: Int): SystemUserBaseError {
//        val signalSms = TestConsoleCentral.baseSmsNums[index]
//        val signalUuid = TestConsoleCentral.uuids[index]
//        return createUser(signalSms, signalUuid, false)
//    }
//
//    @Test
//    fun `test create new admin user`() {
//        assertCount()
//        createAdminUser(0)
//        assertCount(0, 1, 1)
//    }
//
//    @Test
//    fun `test create new non-admin user`() {
//        assertCount()
//        createNonAdminUser(1)
//        assertCount(1, 0, 1)
//    }
//
//    @Test
//    fun `try create new admin user error already exists`() {
//        assertCount()
//        createAdminUser(1)
//        assertCount(0, 1)
//        createAdminUser(1)
//        assertCount(0, 1)
//    }
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
//        assertCount()
//        val adminUser = createAdminUser(0).component1()!!
//        assertCount(0, 1, 1)
//
//        createNonAdminUser(1)
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
//    fun `upgrade non-admin to admin user`() {
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
//    /**
//     * Ensure nothing gets set to null, and see about changing isAdmin at any point
//     */
////    @Test
////    fun `test try upsert new uuid from null`() {
////        assertCount()
////
////        val sms = TestConsole.validNums[1]
////
////        val newUser: User = createUser(sms, null, false).component1()!!
////        assertCount(1, 0)
////
////        User.upsert(sms, TestConsole.uuids[1])
////        assertCount(1, 0)
////    }
////
////
////    @Test
////    fun `test try upsert new signalSms from null`() {
////        assertCount()
////
////        val sms = TestConsole.validNums[1]
////        val uuid = TestConsole.uuids[1]
////
////        createUser(null, uuid, false)
////        assertCount(1, 0)
////
////        createUser(sms, uuid, false)
////        assertCount(1, 0)
////    }
////
////    @Test
////    fun `test try upsert new sms and uuids from non-null`() {
////        assertCount()
////        createAdminUser(0)
////        assertCount(0, 1)
////
////        User.create(TestConsole.validNums[1], TestConsole.uuids[0])
////        assertCount(0, 1)
////
////        User.create(TestConsole.validNums[1], TestConsole.uuids[1])
////        assertCount(0, 1)
////    }
//}
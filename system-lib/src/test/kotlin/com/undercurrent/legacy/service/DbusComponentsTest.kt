package com.undercurrent.legacy.service

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.system.context.DbusProps
import com.undercurrent.system.service.dbus.DbusPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DbusComponentsTest {

    val smsAlone = "4158555588"
    val dbusExt = "_14158555588"
    val smsWithCountryCode = "+14158555588"
    val validDbusPath = "/org/asamk/Signal/_14158555588"

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `test from sms without country code to correct dbuspath`() {
        val dbusComponents = DbusProps().fromSms(smsAlone)
        assertEquals(validDbusPath, dbusComponents.toFullPathStr())
    }

    @Test
    fun `test from sms with country code to correct dbuspath`() {
        val dbusComponents = DbusProps().fromSms(smsWithCountryCode)
        assertEquals(validDbusPath, dbusComponents.toFullPathStr())
    }

    @Test
    fun `test from dbus extension to correct dbuspath`() {
        val dbusComponents = DbusProps().fromDbusExtension("_14158555588")
        assertEquals(validDbusPath, dbusComponents.toFullPathStr())
    }

    @Test
    fun `test from fullpath to correct dbuspath`() {
        val dbusComponents = DbusProps().fromFullPath(DbusPath(validDbusPath))
        assertEquals(validDbusPath, dbusComponents.toFullPathStr())
    }

    @Test
    fun `test from intl sms to dbuspath`() {
        val dbusComponents = DbusProps().fromSms("+444158555588")
        val validIntlDbusPath = "/org/asamk/Signal/_444158555588"

        assertEquals(validIntlDbusPath, dbusComponents.toFullPathStr())
    }

//    @Test
//    fun `test from ext but parse as full path to dbuspath`() {
//        val inputVal = "_444158555588"
//        val dbusComponents = DbusProps().fromFullPath(DbusPath(inputVal))
//
//        val validIntlDbusPath = "/org/asamk/Signal/_444158555588"
//        val validIntlCountryCode = "44"
//
//
//        assertEquals(validIntlCountryCode, dbusComponents.countryCode)
//        assertEquals(validIntlDbusPath, dbusComponents.toFullPathStr())
//    }

    //                SmsRoute("+19109999999", Environment.TEST, Rloe.ADMIN),
//            SmsRoute("+18158888888", Environment.TEST, Rloe.VENDOR),
//            SmsRoute("+17857777777", Environment.TEST, Rloe.CUSTOMER),
    @Test
    fun `fetch from sms routing`() {
        val env = Environment.TEST
        val role = ShopRole.CUSTOMER

        val expectedSms = "+17857777777"

        val dbusComponents = DbusProps(envIn = env, roleIn = role)
        assertEquals("1", dbusComponents.countryCode)
        assertEquals(expectedSms, dbusComponents.toBotSms().value)
    }

}
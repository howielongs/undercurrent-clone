package com.undercurrent.shops.unittests

import com.undercurrent.legacy.utils.joincodes.UniqueJoinCodeGenerator
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.TestConsoleShops
import com.undercurrent.shops.TestConsoleShops.validNumsShops
import com.undercurrent.shops.asserts.BasicDBIT
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCode
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendor
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class JoinCodeGeneratorTest : BasicDBIT() {
    @BeforeEach
    override fun setUp() {
        TestConsoleShops.setUpTests()
    }


    @Test
    fun alreadyExists() {
        TestConsoleShops.setUpTests()

        val result: ProtoShopVendor = ProtoShopVendor.create(validNumsShops[0], "test1")!!

        val storefront: ProtoStorefront = tx { result.storefronts.first() }
        val joinCodeText = tx { storefront.joinCodes.first().code.value.toString() }

        val existenceChecker = { code: String ->
            ProtoShopJoinCode.fetchByCode(code) != null
        }

        val codeGenerator = UniqueJoinCodeGenerator(existenceChecker = { code ->
            existenceChecker(code)
        })

        with(codeGenerator) {
            assertTrue(alreadyExists(joinCodeText)) { "Join code $joinCodeText should already exist" }

            val joinCode: String = generate()!!
            assertTrue(!alreadyExists(joinCode))

            ProtoShopJoinCode(storefront, joinCode).create()
        }
//        val joinCode = JoinCodeGenerator().generate()
//        val joinCode2 = JoinCodeGenerator().generate()
//        assertNotEquals(joinCode, joinCode2)
    }
}
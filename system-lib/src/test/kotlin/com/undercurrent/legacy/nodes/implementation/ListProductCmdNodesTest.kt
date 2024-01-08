package com.undercurrent.legacy.nodes.implementation

import com.undercurrent.setup.BaseTestClass
import com.undercurrent.setup.TestConsoleCentral
import com.undercurrent.setup.defaultSystemTables
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ListProductCmdNodesTest : BaseTestClass() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @BeforeEach
    fun setUp() {
        TestConsoleCentral.setUpTestsCentral(defaultSystemTables)
        setupMocks()
        setUpShopContext()
        setUpProduct()
    }
}
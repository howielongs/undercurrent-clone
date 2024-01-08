package com.undercurrent.setup

import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.repository.database.TestDatabase
import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.FLYWAY_ENABLED
import com.undercurrent.shared.utils.PROMPT_RETRIES
import com.undercurrent.shared.utils.TEST_MODE
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.Table
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TestConsoleCentral {

    fun dropTestTable(table: Table) {
        TestDatabase().dropTable(table)
    }

    private const val DB_FILENAME = "lemur_test"

    const val FUTURE_TESTS_ENABLED = false

    val baseSmsNums = listOf("+12625637281", "2625637291")
    val fullValidNums = listOf("+12625637281", "+12625637291", "+16235637292")
    val uuids = listOf("1be33331-5de7-4fef-b02f-98888888888f", "2be33331-5de7-4fef-b02f-00000000888f")

    val defaultVendorSms0 = TestConsoleCentral.fullValidNums[0]
    val defaultCustomerSms1 = TestConsoleCentral.fullValidNums[1]
    val defaultAdminSms2 = TestConsoleCentral.fullValidNums[2]

    fun setUpTestsCentral(
        tablesToReset: List<Table>,
        dbFileName: String = DB_FILENAME,
        sqlLoggerIsNull: Boolean = false
    ) {
        RunConfig.environment = Environment.TEST

        TEST_MODE = true
        FLYWAY_ENABLED = false
        PROMPT_RETRIES = 1


        val configString = """
            |-------------------------------------
            |Test Configuration:
            |
            |DB_FILENAME: $DB_FILENAME
            |TEST_MODE: $TEST_MODE
            |FLYWAY_ENABLED: $FLYWAY_ENABLED
            |FUTURE_TESTS_ENABLED: $FUTURE_TESTS_ENABLED
            |
            |Loading tables: ${tablesToReset.joinToString(",\n")}
            |-------------------------------------
        """.trimMargin()

        if (!sqlLoggerIsNull) {
            val logger: Logger = LoggerFactory.getLogger(this::class.java)
            logger.info(configString)
        }

        return with(TestDatabase(dbFileName, sqlLoggerIsNull = sqlLoggerIsNull)) {
            this@with.db
            tx {
                dropTables(defaultSystemTables)
                dropTables(tablesToReset)
                createTables(tablesToReset)
            }
        }

    }

}
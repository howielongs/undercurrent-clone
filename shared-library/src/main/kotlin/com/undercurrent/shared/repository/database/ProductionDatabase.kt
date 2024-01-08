package com.undercurrent.shared.repository.database

import com.undercurrent.shared.types.enums.Environment
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import javax.sql.DataSource

interface CanCreateTables {
    fun createTables(tables: List<Table>)
}

interface CanDropTables {
    fun dropTables(tables: List<Table>)
}

abstract class ExposedSqliteDatabase<T : DataSource>(
    private val dbPathsProvider: DatabasePathsProvider,
    private val dataSourceProvider: DataSourceProvider<T>,
    val getDataSourceFunc: (DatabasePathsProvider) -> T = { dataSourceProvider.getDataSource(it) },
    val sqlLoggerIsNull: Boolean = false,
    val exposedConfigLambda: DatabaseConfig.Builder.() -> Unit = {
        if (sqlLoggerIsNull) {
            sqlLogger = null
        }
        useNestedTransactions = true
        defaultMinRepetitionDelay = 500
        defaultMaxRepetitionDelay = 10000
//        keepLoadedReferencesOutOfTransaction = true
//        defaultIsolationLevel = TRANSACTION_SERIALIZABLE

    },
) : DataSourceProvider<T>, DatabasePathsProvider, CanCreateTables, CanDropTables {

    override val url: String = dbPathsProvider.url
    override val fullPathToFile: String = dbPathsProvider.fullPathToFile

    val db by lazy {
        loadDatabase()
    }

    abstract fun loadDatabase(): Database

    fun connectToDatabase(): Database {
        return Database.connect(
            datasource = dataSource, databaseConfig = sqliteConfig
        )
    }

    private val sqliteConfig: DatabaseConfig by lazy {
        DatabaseConfig.invoke {
            exposedConfigLambda()
        }
    }

    protected val dataSource: T by lazy {
        getDataSource(dbPathsProvider)
    }

    override fun getDataSource(pathsProvider: DatabasePathsProvider): T {
        return getDataSourceFunc(pathsProvider)
    }

    // put into companion object
    private fun createTable(table: Table) {
        SchemaUtils.create(table, inBatch = false)
    }

    // should pull this out of here
    override fun createTables(tables: List<Table>) {
        tables.forEach { table ->
            SchemaUtils.create(table, inBatch = false)
        }
    }

    // put into companion object
    fun dropTable(table: Table) {
        SchemaUtils.drop(table, inBatch = false)
    }

    override fun dropTables(tables: List<Table>) {
        tables.forEach {
            dropTable(it)
        }
    }
}

fun loadDatabase(environment: Environment, shouldRunMigrations: Boolean = false): Database {
    return ProductionDatabase(environment, shouldRunMigrations).db
}

open class ProductionDatabase(
    environment: Environment,
    private val shouldRunMigrations: Boolean = false,
    name: String? = null,
    private val migrationProvider: MigrationProvider = FlywayMigrationProvider(
        shouldRunMigrations
    ),
    sqlLoggerIsNull: Boolean = false,
) : ExposedSqliteDatabase<HikariDataSource>(
    sqlLoggerIsNull = sqlLoggerIsNull,
    dbPathsProvider = SqliteJdbcPathsProvider(environment = environment, name = name),
    dataSourceProvider = HikariDataSourceProvider(),
), MigrationProvider {

    override fun runMigrations(dataSource: DataSource) {
        migrationProvider.runMigrations(dataSource)
    }

    override fun loadDatabase(): Database {
        runMigrations(dataSource)
        return connectToDatabase()
    }
}


class MessagesDatabase(
    environment: Environment,
    shouldRunMigrations: Boolean = false, //enable migrations on a different path
) : ProductionDatabase(
    environment = environment, name = "msgs_${environment.name.lowercase()}.db",
    shouldRunMigrations = shouldRunMigrations
)

class TestDatabase(
    name: String? = null,
    environment: Environment = Environment.TEST,
    sqlLoggerIsNull: Boolean = false,
) : ProductionDatabase(
    environment = environment,
    name = name,
    shouldRunMigrations = false,
    sqlLoggerIsNull = sqlLoggerIsNull
)

//class ZipcodesDatabase(
//    name: String = "zipcodes.db",
//) : ExposedSqliteDatabase<HikariDataSource>(
//    dbPathsProvider = SqliteJdbcPathsProvider(environment = Environment.DEV, name = name),
//    dataSourceProvider = HikariDataSourceProvider(),
//) {
//    override fun loadDatabase(): Database {
//        return connectToDatabase()
//    }
//
//}

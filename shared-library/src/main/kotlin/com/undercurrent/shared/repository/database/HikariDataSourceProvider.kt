package com.undercurrent.shared.repository.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

interface DataSourceProvider<T> {
    fun getDataSource(dbPathsProvider: DatabasePathsProvider): T
}

class HikariDataSourceProvider(
    private val driverClassNameIn: String = "org.sqlite.JDBC",
    private val configFields: HikariConfig.(
        DatabasePathsProvider, String
    ) -> Unit = { pathsProvider: DatabasePathsProvider, driverName: String ->
        jdbcUrl = pathsProvider.url
        driverClassName = driverName
    },
) : DataSourceProvider<HikariDataSource> {

    override fun getDataSource(dbPathsProvider: DatabasePathsProvider): HikariDataSource {
        with(HikariConfig()) {
            configFields(dbPathsProvider, driverClassNameIn)
            return HikariDataSource(this)
        }
    }
}
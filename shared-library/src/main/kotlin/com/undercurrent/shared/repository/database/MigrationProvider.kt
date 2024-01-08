package com.undercurrent.shared.repository.database

import com.undercurrent.shared.utils.Log
import org.flywaydb.core.Flyway
import javax.sql.DataSource

interface MigrationProvider {
    fun runMigrations(dataSource: DataSource)
}

class FlywayMigrationProvider(private val shouldRunMigrations: Boolean = true) : MigrationProvider {
    override fun runMigrations(dataSource: DataSource) {
        if (!shouldRunMigrations) {
            return
        }
        with(Flyway.configure().dataSource(dataSource).load()) {
            try {
                migrate()
            } catch (e: Exception) {
                Log.warn("Will try doing baseline before migrate", e)
                try {
                    baseline()
                    migrate()
                } catch (e: Exception) {
                    Log.error("Hit a second snag while trying to do migration...", e)
                }
            }

        }
    }
}

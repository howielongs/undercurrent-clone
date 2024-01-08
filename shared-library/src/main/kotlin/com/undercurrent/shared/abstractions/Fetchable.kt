package com.undercurrent.shared.abstractions

import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import org.jetbrains.exposed.sql.Column

interface Fetchable : CompanionInterface {
    fun <T : RootEntity0> fetchUnexpired(entityCompanion: RootEntityCompanion0<T>): List<T>

    fun <T : RootEntity0, S : Comparable<S>> findAllByColumn(
        baseClass: RootEntityCompanion0<T>,
        column: Column<S?>,
        value: S
    ): List<T>

    fun <T : RootEntity0, S : Comparable<S>> findByColumn(
        entityCompanion: RootEntityCompanion0<T>,
        column: Column<S>,
        value: S
    ): T?

    fun <T : RootEntity0, S : Comparable<S>> findByNullableColumn(
        entityCompanion: RootEntityCompanion0<T>,
        column: Column<S?>,
        value: S
    ): T?

}
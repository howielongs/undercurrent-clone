package com.undercurrent.shared.repository.bases

import com.undercurrent.shared.messages.CanNotifyCreated
import com.undercurrent.shared.abstractions.Fetchable
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.Util
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.EntityChangeType
import org.jetbrains.exposed.dao.EntityHook
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.toEntity
import org.jetbrains.exposed.sql.Column

//todo consider renaming to RootEntityService to RootEntityCompanion
abstract class RootEntityCompanion0<E : RootEntity0>(table: RootTable0) : IntEntityClass<E>(table), Fetchable {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.let {
                        it.updatedDate = Util.currentUtc()
                    }
                } catch (e: Exception) {
                    //nothing much to do here
                }
            }
        }
        // consider adding field for Create and Remove handlers
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Created) {
                try {
                    action.toEntity(this)?.let {
                        when (this) {
//                            is Message.Companion, TestInputs.Entity, ZipCodeLookup, ZipCodeLookup,
////                            InboundMessage,
//                            ScanEvents.Entity -> {
//                                //ignore
//                                return@subscribe
//                            }
//
                            else -> {
                                if (it is CanNotifyCreated) {
                                    //ensure this casting works
                                    it.notifyCreated()
                                }
                                val entityLabel = it::class.java.simpleName.capitalize()
                                val id = tx { it.fetchId() }
//                                val label = "$entityLabel #$id"

                                "ENTITY CREATED: $entityLabel #${id}\n------------\n".let { msg ->
                                    Log.debug(msg)
//                                    notifyAdmins(msg)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    //nothing much to do here
                }
            }
        }
    }

    //todo look at removing need for entityCompanion parameter
    override fun <T : RootEntity0> fetchUnexpired(entityCompanion: RootEntityCompanion0<T>): List<T> {
        return tx { entityCompanion.all().filter { it.isNotExpired() } }.toList()
    }

    override fun <T : RootEntity0, S : Comparable<S>> findAllByColumn(
        entityCompanion: RootEntityCompanion0<T>,
        column: Column<S?>,
        value: S
    ): List<T> {
        return tx {
            entityCompanion.find { column eq value }.filter { it.isNotExpired() }.toList()
        }
    }

    override fun <T : RootEntity0, U : Comparable<U>> findByColumn(
        entityCompanion: RootEntityCompanion0<T>,
        column: Column<U>,
        value: U
    ): T? {
        return tx {
            entityCompanion.find { column eq value }.singleOrNull { it.isNotExpired() }
        }
    }

    override fun <T : RootEntity0, S : Comparable<S>> findByNullableColumn(
        entityCompanion: RootEntityCompanion0<T>,
        columnNullable: Column<S?>,
        value: S
    ): T? {
        return tx {
            entityCompanion.find { columnNullable eq value }.singleOrNull { it.isNotExpired() }
        }
    }
}

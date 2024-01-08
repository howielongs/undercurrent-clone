package com.undercurrent.shared.repository.dinosaurs

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.SystemUsersNew
import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.types.errors.BaseError
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or


@Deprecated("get rid of system users anyhow")
abstract class SystemUserEntityCompanion1<E : SystemUserNew>(table: RootTable0) : RootEntityCompanion0<E>(table),
    CreatableEntity<E> {

    private lateinit var thisSms: String
    private var thisUuid: String? = null
    private var thisIsAdmin: Boolean = false

    operator fun invoke(
        smsIn: String,
        uuidIn: String? = null,
        isAdminIn: Boolean = false,
    ): SystemUserEntityCompanion1<E> {
        this.thisSms = smsIn
        this.thisUuid = uuidIn
        this.thisIsAdmin = isAdminIn
        return this
    }

    override fun create(): E? {
        return tx {
            new {
                this.signalSms = thisSms
                this.signalUuid = thisUuid
                this.isAdmin = thisIsAdmin
            }
        }
    }


    fun findOrCreate(
        signalSms: String,
        signalUuid: String? = null,
        isAdmin: Boolean = false,
    ): SystemUserNew? {
        return tx {
            val existingUser =
                SystemUserNew.find {
                    (SystemUsersNew.signalSms eq signalSms and (SystemUsersNew.signalSms neq null)) or
                            (SystemUsersNew.signalUuid eq signalUuid and (SystemUsersNew.signalUuid neq null))
                }.singleOrNull { it.isNotExpired() }

            return@tx existingUser
                ?: SystemUserNew(signalSms, signalUuid, isAdmin).create()
        }
    }



    //todo update to inline class values
    fun create(
        signalSms: String,
        signalUuid: String? = null,
        isAdmin: Boolean = false,
    ): Result<E?, BaseError> {
        val existingUser: SystemUserNew? = tx {
            SystemUserNew.find {
                (SystemUsersNew.signalSms eq signalSms and (SystemUsersNew.signalSms neq null)) or
                        (SystemUsersNew.signalUuid eq signalUuid and (SystemUsersNew.signalUuid neq null))
            }.singleOrNull { it.isNotExpired() }
        }

        existingUser?.let {
            return Err(BaseError("User already exists for signalSms: $signalSms, signalUuid: $signalUuid"))
        }

        return Ok(tx {
            this@SystemUserEntityCompanion1.new {
                this.signalSms = signalSms
                this.signalUuid = signalUuid
                this.isAdmin = isAdmin
            }
        })
    }

    fun fetchAdmins(): List<SystemUserNew> {
        return tx {
            find { SystemUsersNew.isAdmin eq true }
                .filter { it.isNotExpired() }.toList()
        }
    }


    fun findBySms(sms: String): SystemUserNew? {
        return findByNullableColumn(SystemUserNew, SystemUsersNew.signalSms, sms)

    }


}
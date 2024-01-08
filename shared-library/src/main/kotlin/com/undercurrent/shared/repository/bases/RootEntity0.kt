package com.undercurrent.shared.repository.bases

import com.undercurrent.shared.abstractions.CanOutputUid
import com.undercurrent.shared.abstractions.EntityWithExpiry
import com.undercurrent.shared.abstractions.Expirable
import com.undercurrent.shared.utils.time.EpochProvider
import com.undercurrent.shared.utils.time.SystemEpochNanoProvider
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

interface EntityMappedToTable : CanOutputUid {
    val matchingTable: RootTable0
}


/**
 * If expiry is null -> not expired
 * If current epoch is less than expiry -> not expired
 *
 * Expired iff (expiry epoch is not null) AND (expiry epoch is less than current epoch)
 */
abstract class RootEntity0(
    id: EntityID<Int>,
    override val matchingTable: RootTable0,
    private val epochProvider: EpochProvider = SystemEpochNanoProvider(),
    private val epochFunc: () -> Long = { epochProvider.getEpochNanoLong() }
) : IntEntity(id),
    Expirable,
    EntityWithExpiry,
    CanOutputUid {

    var createdDate by matchingTable.createdDate
    var updatedDate by matchingTable.updatedDate
    override var expiryEpoch by matchingTable.expiryEpoch

    override fun fetchId(): Int {
        return tx { this@RootEntity0.id.value }
    }

    override var uid: Int = 0
        get() = fetchId()

    override fun isExpired(): Boolean {
        return isExpired(epochFunc())
    }

    override fun isExpired(epoch: Long): Boolean {
//        tx { this@RootEntity.isUnexpiredEntity() }
        tx { expiryEpoch }?.let {
            return it - epoch <= 0
        }
        return false
    }

    override fun unexpire() {
        tx {
            if (this@RootEntity0.isExpired()) {
                this@RootEntity0.expiryEpoch = null
            }
        }
    }

    override fun expire(): Boolean {
        return expire(epochFunc())
    }

    override fun expire(epoch: Long): Boolean {
        return if (this@RootEntity0.isNotExpired()) {
            tx { this@RootEntity0.expiryEpoch = epoch }
            true
        } else {
            false
        }
    }

    override fun isNotExpired(): Boolean {
        return isNotExpired(epochFunc())
    }

    @Deprecated("Favor unexpiredExpr()")
    override fun isNotExpired(epoch: Long): Boolean {
        return !isExpired(epoch)
    }

    private fun checkExpired(expiryEpoch: Long, epoch: Long = SystemEpochNanoProvider.getEpochNano()): Boolean {
        return expiryEpoch - epoch > 0
    }


}


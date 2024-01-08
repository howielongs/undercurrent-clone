package com.undercurrent.system.repository.entities

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.utils.Log
import com.undercurrent.system.messaging.outbound.notifyAdmins
import org.jetbrains.exposed.dao.id.EntityID

object Admins : ExposedTableWithStatus2("system_admins") {
    val user = reference("user_id", Users)

    override fun singularItem(): String {
        return "Admin"
    }

    @Deprecated("Import and use AdminsNotifier")
    fun notifyError(
        messageBody: String, exception: Exception? = null, sourceClass: String? = null
    ) {
        Log.error(messageBody, exception, sourceClass)
        notifyAdmins(messageBody, subject = "ERROR")
    }
}

class AdminProfile(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Admins) {
    companion object : RootEntityCompanion0<AdminProfile>(Admins)

    var user by User referencedOn Admins.user

}
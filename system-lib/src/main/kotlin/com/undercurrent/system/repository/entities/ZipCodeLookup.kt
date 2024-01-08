package com.undercurrent.system.repository.entities

import com.undercurrent.shared.repository.bases.RootEntity0
import com.undercurrent.shared.repository.bases.RootTable0
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.undercurrent.system.repository.companions.ZipCodeLookupCompanion
import org.jetbrains.exposed.dao.id.EntityID

object ZipCodeLookups : RootTable0("address_zipcodes") {
    val zipcode = varchar("zipcode", VARCHAR_SIZE)
    val city = varchar("city", VARCHAR_SIZE)
    val state = varchar("state", VARCHAR_SIZE)
    val stateAbbr = varchar("state_abbr", VARCHAR_SIZE)
    val timezone = varchar("timezone", VARCHAR_SIZE)
}

class ZipCodeLookup(id: EntityID<Int>) : RootEntity0(id, ZipCodeLookups) {
    var zipcode by ZipCodeLookups.zipcode
    var city by ZipCodeLookups.city
    var state by ZipCodeLookups.state
    var stateAbbr by ZipCodeLookups.stateAbbr
    var timezone by ZipCodeLookups.timezone

    companion object : ZipCodeLookupCompanion()

}




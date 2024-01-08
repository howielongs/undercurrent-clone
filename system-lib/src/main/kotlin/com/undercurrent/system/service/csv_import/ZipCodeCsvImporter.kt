package com.undercurrent.system.service.csv_import

import com.undercurrent.system.repository.entities.ZipCodeLookup

//todo make use of this
//interface ZipcodeProvider {
//    suspend fun importIfCodeNotFound(zipcodeIn: String): Boolean
//    suspend fun import()
//    suspend fun fetch(zipcodeIn: String): ZipCodeLookup?
//}

//todo pull out into a service from Exposed classes
class ZipCodeCsvImporter {
    suspend fun runImport() {
//        Log.debug("STARTING IMPORT ZIP ")
        ZipCodeLookup.importIfCodeNotFound("90265")
    }
}
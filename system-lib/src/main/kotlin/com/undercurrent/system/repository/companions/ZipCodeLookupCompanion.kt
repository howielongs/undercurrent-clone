package com.undercurrent.system.repository.companions

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.repository.entities.ZipCodeLookup
import com.undercurrent.system.repository.entities.ZipCodeLookups
import java.io.InputStream

open class ZipCodeLookupCompanion : RootEntityCompanion0<ZipCodeLookup>(ZipCodeLookups) {

    //todo still needed for other operations, like fetching zipcode for orders
//        private val zipDb: Database by lazy {
//            DatabaseConfig.invoke {
//                useNestedTransactions = true
//                defaultMinRepetitionDelay = 500
//                defaultMaxRepetitionDelay = 10000
//            }
//
//            val thisDb = DatabaseFilePathBuilder("zipcodes.db").build().let {
//                Database.connect(url = it.toString(), driver = it.driverStr)
//            }
//            thisDb
//        }

    //todo pull this out into separate CSV importer
    suspend fun importIfCodeNotFound(zipcodeIn: String): Boolean {
        fetch(zipcodeIn) ?: run {
            importFromCsvFile()
            return true
        }
        return false
    }

    suspend fun importFromCsvFile() {
        "Importing zip codes...".let {
            Log.debug(it)
            // notifyAdmins(it)
        }

        val inputFilePath = "uszips_embed.csv"
        javaClass.classLoader.getResourceAsStream(inputFilePath)?.use {
            readCsv(inputStream = it)
            "Finished importing zip codes to database".let {
                Log.debug(it)
                // notifyAdmins(it)
            }
        }
    }

    private suspend fun readCsv(inputStream: InputStream) {
        val reader = inputStream.bufferedReader()
        reader.lineSequence().forEach { zipCodeEntry ->
            zipCodeEntry.split(",").toTypedArray().let {
                if (!it[0].contains("zip")) {
                    save(
                        zipcodeIn = it[0], cityIn = it[1], stateIn = it[3], stateAbbrIn = it[2], timezoneIn = it[4]
                    )
                }
            }
        }
    }

    fun fetch(zipcodeIn: String): ZipCodeLookup? {
        return tx {
            find { ZipCodeLookups.zipcode eq zipcodeIn }.limit(1).firstOrNull()
        }
    }

    fun save(
        zipcodeIn: String, cityIn: String, stateIn: String, stateAbbrIn: String, timezoneIn: String
    ): ZipCodeLookup? {
        var zipcodeToSave = zipcodeIn
        while (zipcodeToSave.length < 5) {
            zipcodeToSave = "0$zipcodeToSave"
        }

        val existingZipcodeEntry = fetch(zipcodeIn = zipcodeToSave)

        return tx {
            if (existingZipcodeEntry != null) {
                null
            } else {
                new {
                    zipcode = zipcodeToSave
                    city = cityIn
                    state = stateIn
                    stateAbbr = stateAbbrIn
                    timezone = timezoneIn
                }
            }
        }
    }
}
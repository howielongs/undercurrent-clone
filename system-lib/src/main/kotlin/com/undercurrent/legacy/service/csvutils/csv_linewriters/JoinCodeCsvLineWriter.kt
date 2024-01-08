package com.undercurrent.legacy.service.csvutils.csv_linewriters

import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.shared.utils.tx


class JoinCodeCsvLineWriter<T : JoinCode>(val joinCode: T) : CsvLineWriter<T>(joinCode) {
    override suspend fun write(): String {
        val outStr = StringBuilder()

        tx {
            val codeValue = joinCode.code
            val tag = joinCode.tag ?: ""
            val createdDate = joinCode.createdDate.toString()
            val usages = joinCode.usages.count().toInt().toString()

            outStr.appendValue(codeValue)
                .appendValue(tag)
                .appendValue(createdDate)
                .appendValue(usages)
        }
        return outStr.toString()
    }
}



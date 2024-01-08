package com.undercurrent.legacy.service.csvutils.csv_handlers

import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacy.service.csvutils.csv_linewriters.JoinCodeCsvLineWriter
import com.undercurrent.system.context.SessionContext

//clean  up how header is formatted
class JoinCodesCsvHandler<T : JoinCode>(joinCodes: List<T>, sessionContext: SessionContext) : CsvHandler<T>(
    items = joinCodes,
    headers = "Code\tTag\tCreatedDate\tUsages",
    csvLineWriterFactory = { code -> JoinCodeCsvLineWriter(code) },
    sessionContext = sessionContext,
)
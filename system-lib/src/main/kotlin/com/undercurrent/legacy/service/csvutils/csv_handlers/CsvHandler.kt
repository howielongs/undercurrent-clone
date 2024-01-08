package com.undercurrent.legacy.service.csvutils.csv_handlers


import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.repository.repository_service.FileAttachmentLinker
import com.undercurrent.legacy.service.csvutils.RandomCsvFilePathGenerator
import com.undercurrent.legacy.service.csvutils.csv_linewriters.CsvLineWriter
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.shared.messages.RoutingProps
import com.undercurrent.system.context.SessionContext
import java.io.FileOutputStream
import java.io.OutputStream

interface CsvAttachmentExporter {
    suspend fun send(user: User, dbusProps: RoutingProps)
}

interface CsvFileWriter<T> {
    suspend fun write(filepath: String)
}

sealed class CsvHandler<T>(
    val items: List<T>,
    private val headers: String,
    private val csvLineWriterFactory: (T) -> CsvLineWriter<T>,
    val sessionContext: SessionContext,
    private val filename: String = RandomCsvFilePathGenerator(sessionContext).generate(),
) : CsvFileWriter<T>, CsvAttachmentExporter {

    override suspend fun write(filepath: String) {
        with(filepath) {
            FileOutputStream(this).apply { writeCsv() }
        }
    }

    override suspend fun send(
        user: User, dbusProps: RoutingProps,
    ) {
        with(filename) {
            write(this)

            //perhaps join with interface to this
            FileAttachmentLinker(
                thisUser = user,
                savePath = this,
                captionIn = "",
                attachmentType = AttachmentType.CSV_REPORT,
                routingProps = sessionContext.routingProps
            ).link()?.send(user, dbusPropsIn = sessionContext.routingProps)
        }
    }

    private fun formatHeaders(): String {
        return headers.split("\t").joinToString(prefix = "\"", postfix = "\"", separator = "\",\"")
    }

    private suspend fun OutputStream.writeCsv() {
        val writer = bufferedWriter()
        writer.write(formatHeaders())
        writer.newLine()
        items.forEach {
            writer.write(csvLineWriterFactory(it).write())
            writer.newLine()
        }
        writer.flush()
    }
}
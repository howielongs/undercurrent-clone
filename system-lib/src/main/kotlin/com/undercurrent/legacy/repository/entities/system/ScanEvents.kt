package com.undercurrent.legacy.repository.entities.system

import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.legacy.types.enums.ScanTask
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.legacy.utils.TimeAndDateProvider
import com.undercurrent.shared.utils.VARCHAR_SIZE
import com.vdurmont.emoji.EmojiParser
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class ScanEvents {
    object Table : ExposedTableWithStatus2("system_scan_events") {
        val eventTag = varchar("event_tag", VARCHAR_SIZE)
        val env = varchar("env", VARCHAR_SIZE)
        val scanGroup = varchar("scan_group", VARCHAR_SIZE)
        val periodSec = integer("period").default(500)
        


        //todo is this the most efficient way to do this?
        //either fetch with sql, or use filtering in Kotlin to pull: probably best to use sql up front
        fun fetchLatestOfEach(): String {
            return transaction {
                var outString = "Latest scans:\n"
                ScanTask.values().forEach { task ->
                    Entity.find {
                        eventTag eq task.name
                    }.lastOrNull { it.isNotExpired() }.let { latestEvent ->
                        outString += if (latestEvent == null || latestEvent.toString() == "null") {
                            "${EmojiParser.parseToUnicode(":exclamation:")} ${task.name}: no data\n"
                        } else {
                            "${latestEvent.getStatusEmoji()} $latestEvent\n"
                        }
                    }
                }
                outString
            }
        }

        fun save(eventTagIn: String,
                 envIn: String,
                 scanGroupIn: String,
                 periodSecondsIn: Int): Entity {
            return transaction {
                Entity.new {
                    eventTag = eventTagIn
                    env = envIn
                    scanGroup = scanGroupIn
                    periodSec = periodSecondsIn
                }
            }
        }

    }

    class Entity(id: EntityID<Int>) : ExposedEntityWithStatus2(id, Table) {
        companion object : RootEntityCompanion0<Entity>(Table)

        var eventTag by Table.eventTag
        var env by Table.env
        var scanGroup by Table.scanGroup
        var periodSec by Table.periodSec


        fun getTimeDiffString(): String {
            return TimeAndDateProvider.getTimeAgoString(
                UtilLegacy.getEpoch() - createdDate.nano,
                prependValue = ""
            )
        }

        override fun toString(): String {
            return "$eventTag -> ${getTimeDiffString()} (${UtilLegacy.formatDbDatetime(createdDate.toString())})"
        }

        //todo Implement check for major time gap in members of a ScanGroup
        fun getStatusEmoji(): String {
            val diff = UtilLegacy.getEpoch() - createdDate.nano
            val emojiCode = if (UtilLegacy.valueLessThan(
                            BigDecimal(diff),
                            BigDecimal(periodSec).multiply(BigDecimal(1000)))) {
                ":white_check_mark:"
            } else {
                ":exclamation:"
            }

            return EmojiParser.parseToUnicode(emojiCode)
        }

    }

}

package com.undercurrent.shared.messages

import org.jetbrains.exposed.sql.Column
import java.time.LocalDateTime

interface OutboundMessageTable : MessageTable {
    val botSenderSms: Column<String>
    val humanReceiverAddr: Column<String>
    val sendAfterEpochNano: Column<Long?>
    val sentAtDate: Column<LocalDateTime?>
    val serverTimestamp: Column<Long?>
}
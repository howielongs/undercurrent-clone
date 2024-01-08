package com.undercurrent.shared.messages

import java.time.LocalDateTime

interface OutboundMessageEntity : MessageEntity {
    var timestamp: Long?
    var sentAtDate: LocalDateTime?
    var sendAfterEpochNano: Long?
}

interface InterrupterMessageEntity : OutboundMessageEntity
interface NotificationMessageEntity : OutboundMessageEntity

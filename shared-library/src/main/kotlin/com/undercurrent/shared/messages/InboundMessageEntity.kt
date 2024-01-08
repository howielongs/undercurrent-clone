package com.undercurrent.shared.messages

import java.time.LocalDateTime

interface InboundMessageEntity : MessageEntity {
    var timestamp: Long
    var readAtDate: LocalDateTime?
}
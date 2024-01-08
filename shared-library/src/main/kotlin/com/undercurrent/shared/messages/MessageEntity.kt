package com.undercurrent.shared.messages

import com.undercurrent.shared.abstractions.EntityWithExpiry
import com.undercurrent.shared.abstractions.TableWithExpiry

interface MessageEntity : EntityWithExpiry {
    var body: String
    var senderSms: String
    var receiverSms: String
    var uuid: String?
}

interface MessageTable : TableWithExpiry
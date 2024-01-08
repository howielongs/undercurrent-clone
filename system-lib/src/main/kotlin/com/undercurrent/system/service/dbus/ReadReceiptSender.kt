package com.undercurrent.system.service.dbus

import com.undercurrent.prompting.messaging.DbusMethod
import com.undercurrent.system.context.DbusProps

class ReadReceiptSender(
    private val recipientSms: String,
    private val timestamp: Long,
    dbusProps: DbusProps,
) : DbusMethodSender(DbusMethod.READ_RECEIPT, dbusProps) {

    override fun buildArgArray(): Array<String> {
        DbusSendArrayBuilder().apply {
            newRoot(
                fullDbusPath = dbusProps.toPath().value,
                dbusMethod = dbusMethod,
                thirdString = recipientSms
            )
            return addArr(timestamp)
        }
    }
}
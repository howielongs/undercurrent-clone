package com.undercurrent.prompting.messaging


//
//object SendMessageDbus : DbusSendOperation {
//
//    //todo include a way to do this without having to create OutboundMsg first
//    fun send(msg: DaemonOutboundMessage) {
//        val msgBody = tx { MsgBodyText(msg.body) }
//        val destPair = msg.destPair()
//
//        val methodType = if (msg.isGroupBound()) {
//            DbusMethod.SEND_GROUP_MSG
//        } else {
//            DbusMethod.SEND_MESSAGE
//        }
//
//        send(
//            destPair = destPair,
//            body = msgBody,
//            methodType = methodType
//        )
//    }
//
//    fun send(
//        destPair: DestPair,
//        body: MsgBodyText = MsgBodyText(""),
//        methodType: DbusMethod = DbusMethod.SEND_MESSAGE,
//        attachmentStr: AttachmentsText = AttachmentsText(""),
//    ) {
//        TypingIndicatorCancellor(destPair.destAddr, DefaultDbusObjectProvider()).send()
//        SendMessage(
//            destPair = destPair, methodType = methodType, body = body, attachmentStr = attachmentStr
//        ).send()
//    }
//
//    private class SendMessage(
//        override val destPair: DestPair,
//        methodType: DbusMethod = DbusMethod.SEND_MESSAGE,
//        body: MsgBodyText = MsgBodyText(""),
//        attachmentStr: AttachmentsText = AttachmentsText(""),
//    ) : DbusSender(
//        builder = SendMessageBuilder(
//            methodType = methodType,
//            destPair = destPair,
//            body = body,
//            attachmentStr = attachmentStr
//        )
//    ) {
//        private class SendMessageBuilder(
//            destPair: DestPair,
//            override val methodType: DbusMethod = DbusMethod.SEND_MESSAGE,
//            private val body: MsgBodyText,
//            private val attachmentStr: AttachmentsText = AttachmentsText(""),
//        ) : BaseDbusSendOperationBuilder(
//            destPair,
//            methodType = methodType
//        ) {
//            override fun arraySuffix(): Array<String>? {
//                return if (methodType == DbusMethod.SEND_GROUP_MSG) {
//                    arrayOf(
//                        "string:${body.clean()}",
//                        "array:string:${attachmentStr.value}",
//                        "array:byte:${destPair.destAddr}"
//                    )
//                } else {
//                    arrayOf(
//                        "string:${body.clean()}",
//                        "array:string:${attachmentStr.value}",
//                        "string:${destPair.destAddr}"
//                    )
//                }
//            }
//        }
//    }
//}

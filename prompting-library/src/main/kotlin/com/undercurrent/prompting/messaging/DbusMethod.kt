package com.undercurrent.prompting.messaging

/**
 * todo: See about sending read receipts, etc. via GroupId instead of just sms
 *
 * Also: should prefer UUID usage?
 */
enum class DbusMethod(
    val methodName: String,
    val destAddrAfterMethodCall: Boolean = false,
    val isGroup: Boolean = false,
) {
    /**
     * dbus-send --session --type=method_call --print-reply --dest="org.asamk.Signal"
     * /org/asamk/Signal/_11119999111
     * org.asamk.Signal.sendMessage string:"Hello there!"
     * array:string:"/home/ubuntu/.local/share/signal-cli/attachments/3136820769134007417"
     * string:"+11119999111"
     */
    SEND_MESSAGE(methodName = "sendMessage"),


    /**
     * dbus-send --session --type=method_call
     * --print-reply       --dest=org.asamk.Signal
     * /org/asamk/Signal/_19222222222
     * org.asamk.Signal.setExpirationTimer
     * string:+14555555555 int32:604800
     *
     * Defaults to 1 week (604800 seconds)
     */
    SET_EXPIRATION_TIMER(methodName = "setExpirationTimer", destAddrAfterMethodCall = true),

    /**
     *     dbus-send --session --type=method_call --print-reply
     *      --dest=org.asamk.Signal       /org/asamk/Signal/_14777777777
     *      org.asamk.Signal.sendReadReceipt
     *      string:+14809999999
     *      array:int64:1663507540371
     */
    READ_RECEIPT(methodName = "sendReadReceipt", destAddrAfterMethodCall = true),

    /**
     * dbus-send --session --type=method_call --print-reply
     *  --dest=org.asamk.Signal
     *  /org/asamk/Signal/_14196666666
     *  org.asamk.Signal.sendTyping
     *  string:+15555555555 boolean:false
     */
    TYPING_INDICATOR(methodName = "sendTyping", destAddrAfterMethodCall = true),

    /**
     * dbus-send --session --print-reply --type=method_call
     * --dest=org.asamk.Signal /org/asamk/Signal/_14158893421 org.asamk.Signal.quitGroup
     * array:byte:"0x63,0x74,0x47,0x65,...
     */
    QUIT_GROUP(methodName = "quitGroup", isGroup = true),

    /**
     * Non-admins need to be removed before final admin can be removed
     *
     * dbus-send --session --print-reply --type=method_call --dest=org.asamk.Signal
     * /org/asamk/Signal/_14555555558/Groups/Y3RHZXDWbE1ypMKwkzy6UI_Ye7ONylvEyjo_
     * org.asamk.Signal.removeMembers array:string:"+14888888884"
     */
    REMOVE_MEMBERS(methodName = "removeMembers", isGroup = true),

    /**
     *    ./signal-cli -a +14156666777 send -g lHOh3K+0Sa9wpGdar+ErohprVNWqGX7r1BaygDo= -m "Hey there"
     *     org.asamk.Signal.sendGroupMessage string:"message text" array:string: array:byte:0x82,0x77,0x42,0x03,0x71,0x6d,0xf7,0xf5,0x3b,...
     *
     *     dbus-send --session --print-reply --type=method_call --dest=org.asamk.Signal /org/asamk/Signal/_14666670001 org.asamk.Signal.sendGroupMessage
     *      string:"Hey there" array:string: array:byte:"0x94,0x73,0xa1,0xdc,0xaf,0xb4,0x49,0xaf,0x70,0xa4,0x67,0x5a,0xaf,0xe1,0x2b,0xa2,0x1a,0x6b,0x54,0xd5,0xaa,..."
     */
    SEND_GROUP_MSG(methodName = "sendGroupMessage", isGroup = true),

    /**
     *     dbus-send --session --print-reply --type=method_call --dest=org.asamk.Signal
     *     /org/asamk/Signal/_14159999031/Groups/NqGVJ4xV7dlxtn_szvAGIDxPaeQfQNKO8iXC5X1oIIY_
     *     org.asamk.Signal.enableLink boolean:true
     */
    ENABLE_INVITE_LINK(methodName = "enableLink", isGroup = true)

}

//    val groupIdB64 = varchar("group_id_b64", VARCHAR_SIZE).nullable()


//    var groupIdB64 by DaemonOutboundMessages.groupIdB64

//        var groupId: String? = null
//        get() {
//            return tx { groupIdB64 }?.let {
//                Base64.getDecoder().decode(it).toHex()
//            } ?: null
//        }


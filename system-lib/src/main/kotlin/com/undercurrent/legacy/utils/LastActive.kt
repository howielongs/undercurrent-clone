package com.undercurrent.legacy.utils



import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.formatters.UserToIdString

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.system.repository.entities.messages.OutboundMessage

@Deprecated("Not entirely accurate: needs to account for latest inbound messaging")
data class LastActive(
    val user: User,
    val startEpoch: Long = UtilLegacy.getEpoch(),
    val roles: List<AppRole> = listOf(),
) {
    var nanoSec: Long = 0L
    private var labeledString: String = ""
    private var newestMsgToBot: OutboundMessage? = null

    init {
        if (roles.isEmpty()) {
            newestMsgToBot = latestMsg()
        } else if (roles.size == 1) {
            newestMsgToBot = latestMsg(roles[0])
        } else {
            var rolesMap: HashMap<AppRole, OutboundMessage?> = hashMapOf()
            roles.forEach {
                rolesMap[it] = latestMsg(it)
            }
        }

        nanoSec = newestMsgToBot?.timestamp?.let {
            startEpoch - it
        } ?: run {
            //handle if message or timestamp are invalid
            -1L
        }

        if (nanoSec > -1L) {
            labeledString = TimeAndDateProvider.getTimeAgoString(nanoSec)
        }
    }

    private fun latestMsg(role: AppRole? = null, envIn: Environment = RunConfig.environment): OutboundMessage? {
//        val statusExpr = statusMatchExpr(MessageStatus.TO_BOT) or statusMatchExpr(MessageStatus.CAPTION_TO_BOT)
//        val userIdExpr = Messages.user eq user.id

//        val expr = role?.let {
//            val roleExpr = Messages.role eq role.name
//            userIdExpr and roleExpr and statusExpr
//        } ?: userIdExpr and statusExpr

//        if (role != null) {
//            MessagesToBot.latestMessageForEachSender(roleIn = role)
//        }

        return null
//        return transaction {
//            Message.find { expr }.limit(1).sortedByDescending { it.timestamp }.firstOrNull()
//        }
    }


    override fun toString(): String {
        return "${UserToIdString.toIdStr(user)} $labeledString"
    }
}
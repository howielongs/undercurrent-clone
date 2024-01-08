package com.undercurrent.system.command_execution

import com.undercurrent.legacy.commands.registry.*
import com.undercurrent.system.context.SessionContext

interface CrossoverCommandChecker {
    suspend fun supportedCrossoverCommand(): Pair<SessionContext, BaseCommand>?
}


//todo come back and determine how to get this working
//class CrossoverCmdInputHandlerNode(
//    val context: SessionContext,
//    val newContext: SessionContext,
//    val parsedCmd: BaseCommand,
//) : OutputNode(context.interrupter), CrossoverCommandChecker, StringToCmdParser {
//
//    suspend fun handle(): TreeNode? {
//        return supportedCrossoverCommand()?.let {
//            CrossoverCmdInputHandlerNode(
//                context = context,
//                newContext = it.first,
//                parsedCmd = it.second
//            )
//        } ?: null
//    }
//
//    override suspend fun supportedCrossoverCommand(): Pair<SessionContext, BaseCommand>? {
//        val roles = stx { context.user.roles }
//        roles.forEach { role ->
//            val newContext = SessionContext(context.user, role)
//            parseStringToCmd(msgBody, newContext)?.let {
//                return Pair(newContext, it.commandRef)
//            }
//        }
//        return null
//    }
//
//    override fun parseStringToCmd(data: String, sessionContext: SessionContext): CommandWrapper? {
//        return CommandWrapper.parseStr(data, context)
//    }
//
//    override suspend fun next(): TreeNode? {
//        sendOutput("You don`t have permission for this operation (your current role: ${context.role})")
//
//        val newRole = newContext.role
//
//        val channelUrl = PressAgent.Routing.getRoutingUrl(role = newRole)
//        val channelNum = PressAgent.Routing.getRoutingSms(role = newRole)
//
//        sendOutput(
//            "Please refer to your " +
//                    "$newRole channel for this command:\n$channelNum\n\n$channelUrl"
//        )
//
//        // may want to save history to database (this message)
//        //should this open the new context? What about both?
//        return BotEntryNode(
//            msgBody = parsedCmd.handle(),
//            context = newContext
//        )
//    }
//}
//
//

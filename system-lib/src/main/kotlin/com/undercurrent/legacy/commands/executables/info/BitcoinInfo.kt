package com.undercurrent.legacy.commands.executables.info


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.AttachmentType

class BitcoinInfo(sessionContext: SessionContext) : Executable(CmdRegistry.BITCOIN, sessionContext) {


    override suspend fun execute() {
        val sessionUser = sessionContext.user

        val bitcoinText = "Wallet app for iOS: https://www.coinomi.com/en/\n" +
                "Wallet app for Android: https://samouraiwallet.com/\n\n" +
                "We recommend using Cash app to buy and transfer Bitcoin.\n" +
                "Refer to cash.app/help/us/en-us/31021-sending-and-receiving-bitcoin "

        Attachments.Table.byCaption(AttachmentType.BITCOIN)
            ?.let {
                it.send(
                    recipientUserId = sessionUser.uid,
                    captionText = bitcoinText,
                    currentOperationTag = thisCommand.lower(),
                    dbusPropsIn = sessionContext.routingProps
                )
                return
            }
        sessionContext.interrupt(bitcoinText)
    }
}
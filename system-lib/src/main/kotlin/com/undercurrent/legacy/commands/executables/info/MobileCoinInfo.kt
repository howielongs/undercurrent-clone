package com.undercurrent.legacy.commands.executables.info


import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacy.repository.entities.system.attachments.Attachments
import com.undercurrent.legacy.types.enums.AttachmentType
import com.undercurrent.system.context.SessionContext

class MobileCoinInfo(sessionContext: SessionContext) : Executable(CmdRegistry.MOBILECOIN, sessionContext) {


    override suspend fun execute() {
        val sessionUser = sessionContext.user

        Attachments.Table.byCaption(AttachmentType.MOBILECOIN)?.let {
            it.send(
                recipientUser = sessionUser,
                captionText = "",
                currentOperationTag = thisCommand.lower(),
                dbusPropsIn = sessionContext.routingProps,
            )
        }

        //sleep to allow image to appear on top
        if (!sessionContext.isTestMode()) {
            Thread.sleep(1000L)
        }


        val mobInfoText =
            "MobileCoin is the only cryptocurrency supported by Signal app.\n" +
                    "To learn more about MobileCoin and how to use:\n\n" +
                    "• Refer to https://mobilecoin.com/overview/how-to-use-mobilecoin/activate " +
                    "\n\n• Refer to https://mobilecoin.com/overview/how-to-use-mobilecoin/fennec " +
                    "\n\n• Refer to https://mobilecoin.com/overview/how-to-use-mobilecoin/mixin/ " +
                    "\n\n• Refer to https://mobilecoin.com/media/videos/#how-to-use-mobilecoin "
        sessionContext.interrupt(
            "End-to-end Encryption\n" +
                    "Signal can't read your messaging or view your MobileCoin transactions, and no one else can either. Information about the transaction is encrypted: sender, receiver, amount transferred, and message are all unknown to Signal.\n" +
                    "\n" +
                    "Easy Wallet Recovery\n" +
                    "Securely recover your wallet if you lose your phone, using only your Signal PIN and phone number. Also, securely recover your 24 word phrase if you lose it - without trusting a provider with your private keys.\n"
        )
        sessionContext.interrupt(
            mobInfoText
        )

    }
}
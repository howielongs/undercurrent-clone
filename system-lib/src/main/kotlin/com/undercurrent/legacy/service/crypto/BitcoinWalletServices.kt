package com.undercurrent.legacy.service.crypto

import com.google.common.base.Preconditions
import com.google.common.util.concurrent.MoreExecutors
import com.undercurrent.legacy.repository.entities.payments.BtcReceivedEvents
import com.undercurrent.legacy.repository.entities.payments.BtcWalletEvents
import com.undercurrent.legacy.repository.entities.payments.UserCreditLedger
import com.undercurrent.system.repository.entities.Admins
import com.undercurrent.system.repository.entities.User
import com.undercurrent.system.messaging.outbound.notifyAdmins
import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.legacy.service.crypto.BtcWalletAddresses.Companion.ALLOW_LIVE_BTC_WALLET_WITH_TESTS
import com.undercurrent.legacy.types.enums.status.LedgerEntryStatus
import com.undercurrent.legacy.utils.UtilLegacy
import com.undercurrent.system.types.WalletMemo
import com.undercurrent.system.payments.service.BtcAddressGenerator
import com.undercurrent.shared.formatters.UserToIdString
import com.undercurrent.shared.messages.RoutingProps

import com.undercurrent.shared.types.enums.Environment
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.Util.currentUtc
import com.undercurrent.system.messaging.outbound.sendNotify
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.core.Context
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Transaction
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.crypto.KeyCrypterException
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.Wallet
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode


//todo need to break this apart into Runner and Service
object BitcoinWalletServices {
    val filePrefix = "forwarding-service-testnet"
    fun params(env: Environment): NetworkParameters = when (env) {
        Environment.PROD -> {
            MainNetParams.get()
        }

        else -> {
            TestNet3Params.get()
        }
    }

    var kit: WalletAppKit? = null

    private fun isKitNull(): Boolean {
        if (kit == null) {
            Admins.notifyError("Wallet kit is null")
            return true
        }
        return false
    }

    fun getWallet(): Wallet? {
        if (BtcWalletAddresses.useMockWalletForTests()) {
            //todo add test values here?
            return null
        }
        if (kit != null && isWalletRunning()) {
            with(kit!!.wallet()) {
//                WalletSnapshot(balanceSat = WalletSnapshots.parseBalance(this)).create()
                return this
            }
        }
        Log.warn("getWallet: Wallet is not running")
//        startWallet()
        return null
    }

    //todo pull this in as private member
    fun isWalletRunning(): Boolean {
        if (BtcWalletAddresses.useMockWalletForTests()) {
            return true
        }
        kit?.let {
            return it.isRunning
        }
        Log.warn("getFreshSendAddress: Wallet is not running")
        return false
    }

    fun getFreshSendAddress(): String? {
        if (BtcWalletAddresses.useMockWalletForTests()) {
            return "test_" + UtilLegacy.getEpoch().toString()
        }

        kit?.let {
            BtcAddressGenerator(it).generateFreshAddress()?.let { newAddr ->
                return newAddr
            }
        }
        Log.warn("getFreshSendAddress: Wallet kit is null")
        return null
    }

    /**
     * Needs to write success to multiple CryptoSendEvents from listener
     */
//    fun sendCoinsByInvoiceGroup(): Boolean {
//        // impl this
//
//
//        return true
//    }

    fun tryToSend(
        destAddressString: String,
        amountBtc: String,
        user: User,
        ledgerEntry: UserCreditLedger.Entity,
        memo: String,
        dbusProps: RoutingProps
    ): Boolean {
        try {
            if (dbusProps.isTestMode() && !ALLOW_LIVE_BTC_WALLET_WITH_TESTS) {
                notifyAdmins(
                    "Payment not sent as TEST mode is enabled"
                )
                return true
            }
            var wallet = getWallet() ?: run {
                Log.error("Wallet is null when trying to send $amountBtc for ${UserToIdString.toIdStr(user)}")
                return false
            }

            var balanceSat = BigDecimal(wallet.balance.toSat())
            Log.debug("Current BTC balance: $balanceSat sat")

            val btcToSend = BigDecimal(amountBtc).divide(BigDecimal(1), 8, RoundingMode.HALF_UP)

            var satCoinsToSend = Coin.parseCoin(btcToSend.toString())

            var satToSend = satCoinsToSend.toSat()

            if (BigDecimal(satToSend).subtract(balanceSat) <= BigDecimal("0")) {
                "${UserToIdString.toIdStr(user)}\nSufficient balance: $balanceSat sat\nWill send promptly: $amountBtc BTC".let {
                    Log.debug(it)
                    notifyAdmins(it, subject = "SEND_BTC_PENDING")
                }

                Log.debug("Sending $amountBtc to $destAddressString")

                return sendCoinsForLedgerEntry(destAddressString, satCoinsToSend, ledgerEntry, memo, dbusProps)

            }

        } catch (e: IllegalArgumentException) {
            Log.fatal(
                "${UserToIdString.toIdStr(user)} You should not use fractional satoshis! Very bad man! $amountBtc",
                sourceClass = this::class.java.simpleName, exception = e
            )
            notifyAdmins(
                "${UserToIdString.toIdStr(user)} You should not use fractional satoshis! Very bad man! $amountBtc",
                "Fractional satoshis issue"
            )

        }

        return false
    }

    private fun sendCoinsForLedgerEntry(
        destAddressString: String,
        satCoinsToSend: Coin,
        ledgerEntry: UserCreditLedger.Entity,
        memo: String,
        dbusProps: RoutingProps
    ): Boolean {
        if (kit == null) {
            Log.warn("Kit is null for current user")
            return false
        }

        try {
            //todo resolve this with other duplicated bits
            var sendAddress: Address = Address.fromString(params(dbusProps.environment), destAddressString)

            val sendResult: Wallet.SendResult =
                kit!!.wallet().sendCoins(kit?.peerGroup(), sendAddress, satCoinsToSend)
            Preconditions.checkNotNull(sendResult) // We should never try to send more coins than we have!
            Log.debug("Sending $satCoinsToSend BTC to $destAddressString")

            //todo update status to SENT for ledger
            transaction {
                ledgerEntry.status = LedgerEntryStatus.SENT.name
                ledgerEntry.raw = destAddressString
            }

            // Register a callback that is invoked when the transaction has propagated across the network.
            // This shows a second style of registering ListenableFuture callbacks, it works when you don't
            // need access to the object the future returns.
            sendResult.broadcastComplete.addListener({


                //todo create new table for this (to reflect balance change on ledger)

                // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
//                CryptoWalletEventOld(
//                    walletAddress = destAddressString,
//                    amountSat = amountBtc.toSat().toString(),
//                    type = CryptoWalletEventsOld.CryptoEventType.SEND_SUCCESS.toString(),
//                    invoiceId = invoiceId
//                ).create()

                Log.debug("Sent coins onwards! Transaction hash is " + sendResult.tx.txId)
                notifyAdmins(
                    "Transaction sent to $destAddressString for amount $satCoinsToSend $memo was successful",
                    "WALLET_NOTIF"
                )

                //todo update user role that submitted OUTBOX item in ledger


                //todo update VERIFY field on Ledger for user and amount
                transaction {
                    ledgerEntry.verifiedTime = currentUtc().toString()
                }

                transaction {
                    val thisUser = ledgerEntry.user
                    val thisRole = ledgerEntry.roleEnum

                    sendNotify(
                        user = thisUser,
                        role = thisRole,
                        environment = dbusProps.environment,
                        msg = "Check your BTC wallet! You should have received ${
                            ledgerEntry.amount.replace(
                                "-",
                                ""
                            )
                        } BTC"
                    )
                }


//                CryptoSendEventsOld.markReceived(destAddressString, invoiceId, memo)

            }, MoreExecutors.directExecutor())

            return true

        } catch (e: KeyCrypterException) {
            // We don't use encrypted wallets in this example - can never happen.
            throw RuntimeException(e)
            return false
        } catch (e: InsufficientMoneyException) {
            Admins.notifyError("Insufficient funds for sending $$satCoinsToSend sat. Will retry...", e)
//            CryptoWalletEventOld(
//                walletAddress = destAddressString,
//                amountSat = amountBtc.toSat().toString(),
//                type = CryptoWalletEventsOld.CryptoEventType.INSUFFICIENT_BALANCE_RETRY.toString(),
//                invoiceId = invoiceId
//            ).create()

//            CryptoSendEventsOld.markForRetry(destAddressString, invoiceId, memo)
            return false
        }

        return false
    }


    // figure out how to call this on coroutine exit
    fun killWallet() {
        kit.let {
            notifyAdmins("Stopping wallet...")
            Log.debug("Stopping wallet...")
            kit!!.stopAsync()
            kit!!.awaitTerminated()
        }
    }

    private var walletFilePrefix: String = "test"
        get() = when (RunConfig.environment) {
            Environment.DEV, Environment.TEST -> {
                "test"
            }

            else -> {
                RunConfig.environment.toString().lowercase()
            }
        }


    /**
     * Guard against this being enabled in test modes unless explicitly granted
     */
    fun startWallet(environment: Environment) {
        if (BtcWalletAddresses.useMockWalletForTests()) {
            return
        }

        var startupParams: NetworkParameters = when (environment) {
            Environment.PROD -> {
                MainNetParams.get()
            }

            else -> {
                TestNet3Params.get()
            }
        }


        Log.debug("Starting BTC wallet...")

        val userhome = "user.home"
        val path = System.getProperty(userhome)


        val context = Context(startupParams)
        Context.propagate(context)

        kit = WalletAppKit(startupParams, File("$path/install_dir/shm"), walletFilePrefix)

        kit?.apply {
            setBlockingStartup(false)  // Don't wait for blockchain synchronization before entering RUNNING state
            startAsync()               // Connect to the network and start downloading transactions
            awaitRunning()             // Wait for the service to reach the RUNNING state
            peerGroup().maxConnections = 8
        }

        if (kit == null) {
            Log.error("Wallet kit is null")
            return
        }



        Log.debug("Starting wallet ${kit!!.directory()}/${walletFilePrefix}")


        //todo consider ways to wrap this in coroutines to have tighter control
//        kit!!.startAsync()
//        kit!!.awaitRunning()

        kit!!.wallet()
            .addCoinsReceivedEventListener { wallet: Wallet?, tx: Transaction, prevBalance: Coin?, newBalance: Coin? ->
                if (!isKitNull()) {
                    BtcReceivedEvents.save(wallet, tx)?.let {
                        Log.debug("-----> coins received: at ${transaction { it.receivingAddressStr }}")
                    }
                    BtcWalletEvents().save(
                        wallet = wallet,
                        rawIn = tx.toString(),
                        memoIn = WalletMemo.RECEIVE
                    )
                } else {
                    Admins.notifyError("Wallet is null on receive BTC")
                }

                Log.debug("received: " + tx.getValue(wallet))
                Log.debug("Kit wallet:" + kit!!.wallet().toString())

                //todo impl wallet snapshot as well
//                with(kit!!.wallet()) {
//                    with(
//                        WalletSnapshotOld(
//                            balanceSat = WalletSnapshotsOld.parseBalance(this),
//                            latestReceivedTx = tx.toString(),
//                            latestReceivedAddress = receivingAddress.toString(),
//                        )
//                    ) {
//                        this.create()
//                        this.notifyAdmins()
//                    }
//
//
//                }

                // Runs in the dedicated "user thread" (see bitcoinj docs for more info on this).
                //
                // The transaction "tx" can either be pending, or included into a block (we didn't see the broadcast).
                val value = tx.getValueSentToMe(wallet)
                Log.debug("Received tx for " + value.toFriendlyString() + ": " + tx)
//                Log.test("Transaction will be forwarded after it confirms.")

//                sendCoins(LegacyAddress.fromBase58(params, addressV1), value.value * 0.85)


                // Wait until it's made it into the block chain (may run immediately if it's already there).
                //
                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
                // to be double spent, no harm done. Wallet.allowSpendingUnconfirmedTransactions() would have to
                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
                // case of waiting for a block.
//                Futures.addCallback(
//                    tx.confidence.getDepthFuture(1),
//                    object : FutureCallback<TransactionConfidence?> {
//                        override fun onSuccess(result: TransactionConfidence?) {
//                            Log.test("Confirmation received.")
//                            forwardCoins(forwardingAddress)
//                        }
//
//                        override fun onFailure(t: Throwable) {
//                            // This kind of future can't fail, just rethrow in case something weird happens.
//                            throw RuntimeException(t)
//                        }
//                    },
//                    MoreExecutors.directExecutor()
//                )
//                Log.test("send next payment to: " + kit!!.wallet().freshReceiveAddress().toString())

            }
        kit!!.wallet()
            .addCoinsSentEventListener { wallet: Wallet?, tx: Transaction?, prevBalance: Coin?, newBalance: Coin? ->
                //todo Use this to update users for various stages of status
                //may queue up some messaging to be sent once this is triggered
                //todo add more details about what is being sent here
                Log.debug(
                    "coins sent"
                )
                BtcWalletEvents().save(wallet, tx.toString(), WalletMemo.SEND)

            }

        kit!!.wallet().addKeyChainEventListener { keys: List<ECKey?>? ->
            Log.debug("new key added")
        }

        kit!!.wallet()
            .addScriptsChangeEventListener { wallet: Wallet?, scripts: List<Script?>?, isAddingScripts: Boolean ->
                Log.debug("new script added")
            }

        kit!!.wallet().addTransactionConfidenceEventListener { wallet: Wallet?, tx: Transaction ->
            Log.trace("-----> confidence changed: " + tx.txId)
            val confidence = tx.confidence
            Log.trace("confidence changed ---> new block depth: " + confidence.depthInBlocks)
        }

        Log.debug(kit!!.wallet().toString())
        with(kit!!.wallet()) {
            BtcWalletEvents().save(wallet = this, null, null)
        }
    }

}
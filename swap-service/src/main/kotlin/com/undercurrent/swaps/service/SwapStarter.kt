package com.undercurrent.swaps.service

//import com.undercurrent.legacy.repository.repository.system.Ancestors
//import com.undercurrent.legacy.routing.MessageRouting
//import com.undercurrent.legacy.routing.RoutingConfig
//import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.repository.entities.Sms
import com.undercurrent.shared.utils.Log
import com.undercurrent.shared.utils.tx
import com.undercurrent.swaps.repository.entities.SwapAdmin
import com.undercurrent.swaps.repository.entities.SwapUser

/**
 * See about having a custom-built bot that runs
 * from here.
 *
 * Make use of Groups for other roles.
 *
 *
 * Steps:
 * - Pass in host sms to use
 * - Start up dbus (for particular sms)
 * - Start up telegram bot
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting up...")

//        val baseSms = MessageRouting.getSmsForSession(Rloe.ADMIN, Environment.DEV)
//        RunConfig.environment = Environment.DEV
//        RunConfig.role = Rloe.ADMIN
//        RunConfig.sessionSms = baseSms
//
//        val adminSms = RoutingConfig.getAdminSmsNumbers(Environment.DEV).keys.first()

        Log.debug("Starting SwapBot as ADMIN")

        //get exposed set up
        // will be independent of main project
        val dbFilename = "swapbot-dev.db"

//        SwapSchema(dbFilename).loadTables()

//        val newAdmin = provisionFirstAdmin(adminSms)

        //start runBlocking in here someplace

    }

    // check if this user/admin already exists in database
    private fun provisionFirstAdmin(adminSms: String): SwapAdmin {
        return tx {
            SwapUser.new {
                signalSms = Sms(adminSms)
                tag = "admin1"
            }.let {
                SwapAdmin.new {
                    swapUser = it
                }.let {
                    return@tx it
                }
            }
        }
    }

}
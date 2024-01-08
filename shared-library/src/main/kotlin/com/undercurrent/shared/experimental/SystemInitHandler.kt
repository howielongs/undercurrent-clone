package com.undercurrent.shared.experimental

import com.undercurrent.shared.types.enums.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * This class is used to initialize the system.
 * It is used to create the system user and the system channel.
 *
 * System could be running on multiple SMS instances or hosted on a single one
 *
 */
class SystemInitHandler(


    val environment: Environment,
) {

    val logger: Logger = LoggerFactory.getLogger(SystemInitHandler::class.java)

    /**
     * Ensure system admins are created as users and system admins
     */
    fun start() {

        logger.info("SystemInitHandler was started")
//        SystemAdminLoader().load(environment)

    }

}
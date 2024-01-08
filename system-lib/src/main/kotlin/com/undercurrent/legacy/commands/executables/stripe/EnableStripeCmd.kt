package com.undercurrent.legacy.commands.executables.stripe

import com.undercurrent.legacy.commands.executables.abstractcmds.select_and_update_cmds.SelectAndUpdateCmd
import com.undercurrent.legacy.commands.registry.CmdRegistry
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefronts
import com.undercurrent.shared.repository.dinosaurs.ExposedEntityWithStatus2
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import com.undercurrent.system.context.SessionContext

/**
 * Select vendor from list for enabling Stripe
 *
 * Selected vendor then should see option for Stripe in 'addCrypto' (rework this?)
 */
@Deprecated("Use nodes for this")
class EnableStripeCmd(sessionContext: SessionContext) :
    SelectAndUpdateCmd(CmdRegistry.ENABLE_STRIPE, sessionContext) {

    override val operationInfinitiveVerb: String
        get() = "to enable Stripe"

    override fun sourceList(): List<ExposedEntityWithStatus2> {
        return tx {
            Storefront.find { unexpiredExpr(Storefronts) }.filter { it.isEnabledForStripe() }.toList()
        }
    }
}
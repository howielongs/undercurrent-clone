package com.undercurrent.legacyshops.repository.companions

import com.undercurrent.system.repository.entities.User
import com.undercurrent.legacy.types.enums.JoinCodeType
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodeBurstEvent
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCodes
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.abstractions.CanFetchByCode
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.utils.cleanInboundCommand
import com.undercurrent.shared.utils.cleanJoinCode
import com.undercurrent.shared.utils.time.unexpiredExpr
import com.undercurrent.shared.utils.tx
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

open class JoinCodeCompanion : RootEntityCompanion0<JoinCode>(JoinCodes), CanFetchByCode<JoinCode> {

    fun save(
        codeStrIn: String,
        ownerUserIn: User,
        entityIdIn: Int,
        tagIn: String?,
        entityTypeIn: JoinCodeType = JoinCodeType.STOREFRONT,
        parentIn: JoinCode? = null,
        burstEventIn: JoinCodeBurstEvent.Entity? = null,
    ): JoinCode? {
        return transaction {
            new {
                ownerUser = ownerUserIn
                parent = parentIn
                entityId = entityIdIn
                tag = tagIn
                code = codeStrIn.cleanJoinCode()
                entityType = entityTypeIn
                burstEvent = burstEventIn
            }
        }
    }

    override fun fetchByCode(codeStr: String): JoinCode? {
        val cleanedCode = codeStr.cleanInboundCommand()

        return tx {
            find {
                unexpiredExpr(JoinCodes)
            }.firstOrNull {
                it.code.cleanInboundCommand() == cleanedCode
            }
        }
    }

    fun listCodesForStorefront(storefront: Storefront): List<JoinCode> {
        return tx {
            find {
                JoinCodes.entityType eq JoinCodeType.STOREFRONT.name and
                        unexpiredExpr(JoinCodes)
            }.filter { it.entityId == storefront.uid }.toList()
        }
    }

    fun codesForStorefrontString(storefront: Storefront, codes: List<JoinCode> = listOf()): String? {
        val source = codes.ifEmpty { listCodesForStorefront(storefront) }
        return source.ifEmpty { null }?.joinToString(separator = "\n") { "• $it" }
    }


}
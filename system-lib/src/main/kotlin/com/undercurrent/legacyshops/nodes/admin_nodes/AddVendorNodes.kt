package com.undercurrent.legacyshops.nodes.admin_nodes

import com.undercurrent.legacy.service.fetchers.ShopVendorFetcher
//todo add Interface and 'createVendor()' method (returns ShopVendor)
import com.undercurrent.legacy.service.fetchers.UserFetcherBySms
import com.undercurrent.legacy.types.enums.status.ActiveMutexStatus
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.utils.encryption.SystemUserManagement.Companion.createNewUser
import com.undercurrent.legacy.utils.joincodes.RandomStringType
import com.undercurrent.legacy.utils.joincodes.UniqueJoinCodeGenerator
import com.undercurrent.legacyshops.repository.entities.joincodes.JoinCode
import com.undercurrent.legacyshops.repository.entities.storefronts.ShopVendor
import com.undercurrent.legacyshops.repository.entities.storefronts.Storefront
import com.undercurrent.shared.abstractions.CanFetchByField
import com.undercurrent.shared.repository.entities.SignalSms
import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.utils.cleanJoinCode
import com.undercurrent.shared.utils.tx
import com.undercurrent.shared.view.treenodes.TreeNode
import com.undercurrent.system.context.SystemContext
import com.undercurrent.system.repository.entities.User

//todo add Interface and 'createVendor()' method (returns ShopVendor)

class AddVendorNodes(
    context: SystemContext,
    private val userFetcher: CanFetchByField<SignalSms, User> = UserFetcherBySms,
    private val vendorFetcher: CanFetchByField<User, ShopVendor> = ShopVendorFetcher(),
    private val fetchUserFunc: suspend (SignalSms) -> User? = { userFetcher.fetch(it) },
    private val fetchVendorFunc: suspend (User) -> ShopVendor? = { vendorFetcher.fetch(it) },
) : AbstractShopAdminNode(context) {
    suspend fun listVendorsNode(): TreeNode? {
        //todo impl this


        return null
    }

    override suspend fun next(): TreeNode? {
        return startNode()
    }

    suspend fun startNode(): TreeNode? {
        return smsInputNode("Enter phone number for vendor:", ifSuccess = { rawSmsIn ->
            rawSmsIn.value?.let { SignalSms(it) }?.let {
                checkVendorExistenceNode(it)
            } ?: run {
                sendOutput("Invalid phone number. Please try again.")
                startNode()
            }
        })
    }

    suspend fun checkVendorExistenceNode(phoneNum: SignalSms): TreeNode? {
        val existingUser = fetchUserFunc(phoneNum)?.let { user ->

            fetchVendorFunc(user)?.let {
                val prompt = """
                    |A vendor with this phone number already exists:
                    | • Phone number: ${phoneNum.value}
                    | • Nickname: ${it.nickname}
                """.trimMargin()

                sendOutput(prompt)

                return chooseToReEnterOrCancel()
            }
            logger.info("A user with this number already exists, but is not a vendor yet ${user.id}")
            user
        }
        return addNicknameNode(phoneNum, existingUser)
    }

    suspend fun chooseToReEnterOrCancel(): TreeNode? {
        return yesNoNode("Would you like to try again with a different phone number?",
            ifYes = {
                startNode()
            },
            ifNo = {
                sendOutput("Vendor creation cancelled.")
                null
            })
    }

    suspend fun addNicknameNode(phoneNum: SignalSms, existingUser: User?): TreeNode? {
        return textInputNode("Please enter a nickname for this vendor:",
            ifSuccess = {
                confirmNode(
                    phoneNum = phoneNum,
                    nickname = it,
                    existingUser = existingUser,
                )
            })
    }

    suspend fun confirmNode(phoneNum: SignalSms, nickname: String, existingUser: User?): TreeNode? {
        val prompt = """
                |New vendor to create:
                | • Phone number: ${phoneNum.value}
                | • Nickname: $nickname
                | 
                | Save?
            """.trimMargin()

        return yesNoNode(prompt, ifYes = {
            createVendorNode(phoneNum, nickname, existingUser)
        }, ifNo = {
            //todo give opportunity to input again
            sendOutput("Vendor creation cancelled.")
            null
        })
    }

//    var vendorUser: User? = null
//    var savedVendor: ShopVendor? = null

    suspend fun createVendorNode(phoneNum: SignalSms, nicknameIn: String, existingUser: User?): TreeNode? {
        val userToSave = existingUser ?: fetchUserFunc(phoneNum) ?: phoneNum.value?.let {
            createNewUser(
                numberIn = it, roleIn = ShopRole.VENDOR
            )
        } ?: run {
            sendOutput("Unable to find or create user with phone number $phoneNum")
            //todo give options to either try again or cancel
            //add exception here?
            return null
        }
//        vendorUser = userToSave
        val newVendor = tx {
            val newItem = ShopVendor.new {
                user = userToSave
                nickname = nicknameIn
            }
            existingUser?.let {
                if (it.role == ShopRole.CUSTOMER) {
                    it.role = ShopRole.VENDOR
                }
            }
            newItem
        }

//        savedVendor = newVendor

        //todo add vendor messaging here as well

        val vendorPropsToAdminStr = tx {
            """
            |New vendor successfully created:
            | • Phone number: ${phoneNum.value}
            | • Nickname: $nicknameIn
            | • Vendor ID: ${newVendor.uid}
            | • User ID: ${newVendor.user.id}
        """.trimMargin()
        }

        sendOutput(vendorPropsToAdminStr)
        return tryCreateStorefrontNode(newVendor)
    }

    suspend fun tryCreateStorefrontNode(newVendor: ShopVendor): TreeNode? {
        val newJoinCode = UniqueJoinCodeGenerator(
            type = RandomStringType.MIXED
        ).generate().cleanJoinCode()

        val newStorefront = tx {
            Storefront.new {
                vendor = newVendor
                joinCode = newJoinCode
                displayName = newJoinCode
                status = ActiveMutexStatus.CURRENT.name
                welcomeMsg = PressAgent.CustomerStrings.welcomeToStorefrontMsg(newJoinCode)
            }
        }
        //todo add vendor messaging here as well
        sendOutput("New storefront successfully created.")
        return tryCreateJoinCodeNode(newStorefront = newStorefront, codeStr = newJoinCode)
    }

    suspend fun tryCreateJoinCodeNode(newStorefront: Storefront, codeStr: String): TreeNode? {
        val newJoinCode = tx {
            JoinCode.save(
                codeStrIn = codeStr,
                ownerUserIn = newStorefront.vendor.user,
                entityIdIn = newStorefront.uid,
                tagIn = newStorefront.displayName,
            )
        }
        //todo add vendor messaging here as well

        if (newJoinCode == null) {
            sendOutput("Unable to create join code for new storefront.")
            //consider rollback here (or throw exception)
        } else {
            sendOutput("Join code: $newJoinCode")
        }
        return listVendorsNode()
    }


} // DONE WITH INNER CLASSES FOR ADDVENDOR








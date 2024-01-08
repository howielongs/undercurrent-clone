package com.undercurrent.shops.repository.proto_versions.companions

import com.undercurrent.legacy.utils.joincodes.UniqueJoinCodeGenerator
import com.undercurrent.shared.SystemUserNew
import com.undercurrent.shared.abstractions.CreatableEntity
import com.undercurrent.shared.repository.bases.RootEntityCompanion0
import com.undercurrent.shared.types.strings.SmsText
import com.undercurrent.shared.utils.tx
import com.undercurrent.shops.repository.proto_versions.ProtoShopJoinCode
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendor
import com.undercurrent.shops.repository.proto_versions.ProtoShopVendorsNew
import com.undercurrent.shops.repository.proto_versions.ProtoStorefront
import com.undercurrent.shops.types.exceptions.VendorCreationException
import com.undercurrent.shops.types.exceptions.ShopVendorAlreadyExistsException


open class ProtoShopVendorCompanion<E : ProtoShopVendor> : RootEntityCompanion0<E>(ProtoShopVendorsNew),
    CreatableEntity<E> {

    lateinit var thisSystemUser: SystemUserNew
    lateinit var thisNickname: String

    var thisSmsStr: String? = null

    operator fun invoke(
        userIn: SystemUserNew,
        nicknameIn: String
    ): ProtoShopVendorCompanion<E> {
        this.thisSystemUser = userIn
        this.thisNickname = nicknameIn
        return this
    }

    override fun create(): E? {
        return tx {
            new {
                this.user = thisSystemUser
                this.nickname = thisNickname
            }
        }
    }

    fun fetchByUser(user: SystemUserNew): ProtoShopVendor? {
        return tx {
            all().singleOrNull { it.isNotExpired() && it.user.id == user.id }
        }
    }

    //todo look into reuse from other areas
    fun fetchBySignalSms(signalSms: String): ProtoShopVendor? {
        return tx {
            all().singleOrNull { it.isNotExpired() && it.sms == signalSms }
        }
    }

    fun vendorExists(user: SystemUserNew): Boolean {
        return ProtoShopVendor.fetchByUser(user) != null
    }

    //todo look into reuse from other areas
    fun vendorExists(smsIn: String): Boolean {
        return fetchBySignalSms(smsIn) != null
    }


    //todo move as much to Service layer as possible, and denote side effects more clearly
    fun create(smsString: String, nickname: String): ProtoShopVendor {
        SmsText(smsString).validate().let { validatedSms ->
            if (ProtoShopVendor.vendorExists(validatedSms)) {
                throw ShopVendorAlreadyExistsException("Vendor with SMS $validatedSms already exists.")
            }

            SystemUserNew.findOrCreate(validatedSms)?.let {
                return saveVendorAndStorefront(it, nickname)
            } ?: throw VendorCreationException(
                "Vendor not created due to error fetching or creating user for $validatedSms."
            )


        }
        throw VendorCreationException("Vendor not created due to invalid sms.")
    }

    @Deprecated("Move to a more robust Service layer class")
    private fun saveVendorAndStorefront(user: SystemUserNew, nickname: String): ProtoShopVendor {
        val existenceChecker = { code: String ->
            ProtoShopJoinCode.fetchByCode(code) != null
        }


        return tx {
            new {
                this.user = user
                this.nickname = nickname
            }.also { newVendor ->
                commit()

                //todo CLEAN THIS UP
                ProtoStorefront(newVendor).create().let { newStorefront ->
                    val newJoinCodeText: String? = UniqueJoinCodeGenerator(existenceChecker = existenceChecker).generate()
                    val newJoinCode: ProtoShopJoinCode? = newJoinCodeText?.let {
                        newStorefront?.let { it1 ->
                            ProtoShopJoinCode(
                                storefrontIn = it1,
                                codeStrIn = it
                            ).create()
                        }
                    }

                }

                return@tx newVendor
            }
        }
    }


}
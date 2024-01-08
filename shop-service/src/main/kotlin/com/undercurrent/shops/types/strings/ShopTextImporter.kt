package com.undercurrent.shops.types.strings

import com.undercurrent.shared.types.strings.CoreText
import com.undercurrent.shared.types.strings.TextLookup
import java.util.*

abstract class ShopTextLookup(fileName: String, key: String) :
    TextLookup(ResourceBundle.getBundle(fileName, Locale.getDefault()), key) {
    constructor(key: String, bundle: ResourceBundle) : this(bundle.baseBundleName, key)
}

object ShopTextImporter {
    val shopBundle: ResourceBundle = ResourceBundle.getBundle("shop_questions", Locale.getDefault())
    val shopSelectionBundle: ResourceBundle = ResourceBundle.getBundle("shop_selections", Locale.getDefault())
    val shopNotificationBundle: ResourceBundle = ResourceBundle.getBundle("shop_notifications", Locale.getDefault())
    val shopPromptBundle: ResourceBundle = ResourceBundle.getBundle("shop_prompts", Locale.getDefault())

    abstract class Question(key: String) : TextLookup(bundle = shopBundle, key = key) {
        abstract class YesNoShopQuestion(key: String) :
            CoreText.Questions.YesNoQuestion(key, shopBundle) {

            class AddToInventory : YesNoShopQuestion("addToInventoryQuestion")
            class AddToCart : YesNoShopQuestion("addToCartQuestion")
            class AttachImage : YesNoShopQuestion("attachYesNoQuestionSingle")

            class AttachImages(private val numImages: Int) :
                YesNoShopQuestion("attachYesNoQuestion") {
                override fun invoke(vararg args: Any): String {
                    return super.invoke(numImages)
                }
            }
        }
    }

    abstract class Selection(key: String) : TextLookup(shopSelectionBundle, key) {
        class SelectCryptoPrompt : Selection("selectCryptoPrompt")
    }

    abstract class Notification(key: String) : TextLookup(shopNotificationBundle, key) {
        class AddedToCartString : Notification("addedToCartString")
        class AddCryptoLineString : Notification("addCryptoLineString")

        class VendorCreatedAdminMsg(private val nickname: String, private val joinCode: String) :
            Notification("vendorCreatedAdminMsg") {

            override fun invoke(vararg args: Any): String {
                return super.invoke(nickname, joinCode)
            }
        }

        class VendorCreatedWelcomeMsg(private val joinCode: String) : Notification("vendorCreatedWelcomeMsg") {
            override fun invoke(vararg args: Any): String {
                return super.invoke(joinCode)
            }
        }
    }

    abstract class Prompt(key: String) : TextLookup(shopPromptBundle, key) {
        class UnitPricePrompt : Prompt("enterPricePrompt")
    }

}

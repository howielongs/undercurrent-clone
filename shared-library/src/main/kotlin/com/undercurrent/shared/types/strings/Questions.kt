package com.undercurrent.shared.types.strings

import java.util.*

object CoreText {
    val questionsBundle: ResourceBundle = ResourceBundle.getBundle("questions", Locale.getDefault())
    abstract class Questions(key: String, bundle: ResourceBundle = questionsBundle) : TextLookup(bundle, key) {
        class YesNoOptions : Questions("yesNoOptions")

        abstract class YesNoQuestion(key: String, bundle: ResourceBundle = questionsBundle) : Questions(key, bundle) {
            override fun invoke(vararg args: Any): String {
                return super.invoke(*args, YesNoOptions().invoke())
            }

            class Correct : YesNoQuestion("correctYesNoQuestion")
            class Overwrite : YesNoQuestion("overwriteYesNoQuestion")
        }
    }
}
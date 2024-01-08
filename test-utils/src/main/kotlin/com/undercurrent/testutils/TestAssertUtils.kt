package com.undercurrent.testutils

class TestAssertUtils(
    private val shouldPrint: Boolean = true
) {
    fun assertContains(resultStr: String, vararg contains: String) {
        return assertContains(resultStr, contains.toList())
    }

    fun assertDoesntContain(resultStr: String, vararg badStrings: String) {
        return assertDoesntContain(resultStr, badStrings.toList())
    }

    fun assertContains(resultStr: String, containsList: List<String>) {
        containsList.forEach {
            assert(resultStr.contains(it.replace("–", "-"))) {
                "Result should contain `$it`\n\nResult:\n$resultStr"
            }
        }
        if (shouldPrint) {
            println(resultStr)
        }
    }

    fun assertDoesntContain(resultStr: String, badStrings: List<String>) {
        badStrings.forEach {
            assert(!resultStr.contains(it.replace("–", "-"))) {
                "Result should not contain `$it`\n\nResult:\n$resultStr"
            }
        }
        if (shouldPrint) {
            println(resultStr)
        }
    }

    inline fun <reified T> assertClassType(item: Any) {
        assert(item is T) { "Expecting ${T::class.java.canonicalName}, got ${item::class.java.canonicalName}" }
    }


    inline fun <reified T> assertNotClassType(item: Any) {
        assert(item !is T) { "Not expecting ${T::class.java.canonicalName}, got ${item::class.java.canonicalName}" }
    }

}

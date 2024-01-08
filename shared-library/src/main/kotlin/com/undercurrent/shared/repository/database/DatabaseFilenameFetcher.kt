package com.undercurrent.shared.repository.database


import com.undercurrent.shared.types.enums.Environment

// Test db files that need resolving:
// core_lemur_test.db
// shop_lemur_test.db

object DatabaseFilenameFetcher {
    fun fetchFilename(
        environment: Environment,
        fullname: String? = null,
        prefix: String = "lemur",
        tag: String = environment.name.lowercase(),
        extension: String = "db",
    ): String {
        return fullname ?: "${prefix}_$tag.$extension"
    }
}


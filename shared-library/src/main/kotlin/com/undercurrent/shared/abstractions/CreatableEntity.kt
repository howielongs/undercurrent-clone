package com.undercurrent.shared.abstractions

import com.undercurrent.shared.repository.bases.RootEntity0

interface CompanionInterface

interface CreatableEntity<E : RootEntity0> : CompanionInterface {
    fun create(): E?
}


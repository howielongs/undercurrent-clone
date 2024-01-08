package com.undercurrent.shared.repository.dinosaurs

interface EntityHasStatusField {
    fun hasStatus(status: String): Boolean
}
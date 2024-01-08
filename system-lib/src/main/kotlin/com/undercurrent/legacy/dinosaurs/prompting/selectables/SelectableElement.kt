package com.undercurrent.legacy.dinosaurs.prompting.selectables


import com.undercurrent.legacy.types.enums.ListIndexTypeOld

/**
 * Example:
 *      dereferencedValue -> vendor.uid -> "1"
 *      displayedIndex -> "A"
 *      entityType -> Vendor (java class)
 *      displayText -> "Vendor 3 (Nickname)"
 */
data class SelectableElement(
    var dereferencedUid: Int,
    var displayedIndex: String,
    var indexType: ListIndexTypeOld,
)
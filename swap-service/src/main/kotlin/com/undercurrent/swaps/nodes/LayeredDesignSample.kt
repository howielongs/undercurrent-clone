package com.undercurrent.swaps.nodes

import com.undercurrent.shared.types.enums.ShopRole
import com.undercurrent.shared.types.enums.AppRole

/**
 *     User Interaction Layer: This layer will handle the user prompts and data input. It will also be
 *     responsible for presenting the summary back to the user.
 *
 *     Validation Layer: Once the user provides the input, this layer will validate the data. If the
 *     data is invalid, prompt the user again.
 *
 *     Database Interaction Layer: This layer will handle all interactions with the Exposed DB.
 *     It will be responsible for querying the database, checking permissions, and determining
 *     which options to show or hide.
 *
 *     Decision Layer: Based on the user's input and the database query results, this layer
 *     will decide the next steps, such as whether to prompt the user for confirmation
 *     or to hide certain options.
 *
 *
 *     This design ensures that each layer has a single responsibility, adhering to the
 *     Single Responsibility Principle (SRP) of SOLID. The layers can be further modularized
 *     into separate classes or files for better organization and scalability.
 *
 *     By separating the concerns, you can easily extend each layer without affecting
 *     the others. For example, you can add more validation rules in the ValidationLayer
 *     or support more database operations in the DatabaseLayer without changing the
 *     user interaction logic.
 */

object LayeredDesignSample {
    // Database Interaction Layer
    object DatabaseLayer {
        fun isCartEmpty(userId: String): Boolean {
            // Query the database using Exposed DB to check if the cart is empty
            // ...
            return true // Placeholder
        }

        fun hasPermission(userId: String, action: String): Boolean {
            // Check if the user has permission for a particular action
            // ...
            return true // Placeholder
        }

        fun saveData(data: String) {
            // Save data to the database
            // ...
        }
    }

    // Validation Layer
    object ValidationLayer {
        fun validateData(data: String): Boolean {
            // Validate the data
            // ...
            return true // Placeholder
        }
    }

    // User Interaction Layer
    object UserInteractionLayer {
        fun promptUser(message: String): String {
            println(message)
            return readLine() ?: ""
        }

        fun displaySummary(data: String) {
            println("Summary of what you entered: $data")
            val confirmation = promptUser("Do you want to save this data? (Yes/No)")
            if (confirmation == "Yes") {
                DatabaseLayer.saveData(data)
                println("Data saved successfully!")
            } else {
                println("Data not saved.")
            }
        }
    }

    object PermissionsLayer {
        private val rolePermissions = mapOf(
            ShopRole.CUSTOMER to setOf("shop", "swapCrypto", "message"),
            ShopRole.VENDOR to setOf("manageInventory", "message"),
            ShopRole.ADMIN to setOf("manageAll")
        )

        fun canPerformAction(role: AppRole, action: String): Boolean {
            return rolePermissions[role]?.contains(action) == true
        }

        fun hasPermission(userId: String, action: String): Boolean {
            // Check if the user has permission for a particular action
            // ...
            return DatabaseLayer.hasPermission(userId, action)
        }


    }

    // Decision Layer
    object DecisionLayer {
        fun decideNextAction(userId: String, data: String, role: AppRole) {
            if (!DatabaseLayer.isCartEmpty(userId)) {
                // Show cart-related options
                // ...
            }
            if (PermissionsLayer.canPerformAction(role, "someAction")) {
                // Show options related to "someAction"
                // ...
            }
            if (PermissionsLayer.hasPermission(userId, "someAction")) {
                // Show options related to "someAction"
                // ...
            }
            if (ValidationLayer.validateData(data)) {
                UserInteractionLayer.displaySummary(data)
            } else {
                val newData = UserInteractionLayer.promptUser("Invalid data. Please enter again:")

                //loops back to start of this function with new data: interesting
                decideNextAction(userId, newData, role)
            }
        }
    }

    private fun runThisThingy() {
        val userId = "someUserId"
        val data = UserInteractionLayer.promptUser("Enter some data:")
        val role = ShopRole.VENDOR
        DecisionLayer.decideNextAction(userId, data, role)
    }

}
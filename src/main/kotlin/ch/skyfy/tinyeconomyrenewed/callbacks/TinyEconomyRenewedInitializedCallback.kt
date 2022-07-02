package ch.skyfy.tinyeconomyrenewed.callbacks

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager

fun interface TinyEconomyRenewedInitializedCallback {
    fun onInitialized(databaseManager: DatabaseManager)
}
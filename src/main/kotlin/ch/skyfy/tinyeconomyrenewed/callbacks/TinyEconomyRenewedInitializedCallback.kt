package ch.skyfy.tinyeconomyrenewed.callbacks

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface TinyEconomyRenewedInitializedCallback {
    companion object {
        @JvmField
        val EVENT: Event<TinyEconomyRenewedInitializedCallback> = EventFactory.createArrayBacked(TinyEconomyRenewedInitializedCallback::class.java){ listeners ->
            TinyEconomyRenewedInitializedCallback{ databaseManager ->
                for(listener in listeners) listener.onInitialized(databaseManager)
            }
        }
    }
    fun onInitialized(databaseManager: DatabaseManager)
}
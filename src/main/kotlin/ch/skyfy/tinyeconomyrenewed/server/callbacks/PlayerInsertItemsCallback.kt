package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory

fun interface PlayerInsertItemsCallback {
    companion object {
        @JvmField
        val EVENT: Event<PlayerInsertItemsCallback> = EventFactory.createArrayBacked(PlayerInsertItemsCallback::class.java) { listeners ->
            PlayerInsertItemsCallback { playerEntity, inventory ->
                for (listener in listeners) {
                    val result = listener.onInsertItems(playerEntity, inventory)
                    if (!result) return@PlayerInsertItemsCallback false
                }
                true
            }
        }
    }

    fun onInsertItems(playerEntity: PlayerEntity, inventory: Inventory): Boolean
}
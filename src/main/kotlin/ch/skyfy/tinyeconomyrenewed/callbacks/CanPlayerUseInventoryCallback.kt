package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory

fun interface CanPlayerUseInventoryCallback {
    companion object {
        @JvmField
        val EVENT: Event<CanPlayerUseInventoryCallback> = EventFactory.createArrayBacked(CanPlayerUseInventoryCallback::class.java) { listeners ->
            CanPlayerUseInventoryCallback { playerEntity, inventory ->
                for (listener in listeners) {
                    val result = listener.onInsertItems(playerEntity, inventory)
                    if (!result) return@CanPlayerUseInventoryCallback false
                }
                true
            }
        }
    }

    fun onInsertItems(playerEntity: PlayerEntity, inventory: Inventory): Boolean
}
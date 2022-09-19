package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.util.ActionResult

fun interface PlayerTakeItemsCallback {
    companion object {
        @JvmField
        val EVENT: Event<PlayerTakeItemsCallback> = EventFactory.createArrayBacked(PlayerTakeItemsCallback::class.java) { listeners ->
            PlayerTakeItemsCallback { playerEntity, inventory ->
                for (listener in listeners) {
                    val result = listener.onTakeItems(playerEntity, inventory)
                    if (result != ActionResult.PASS) return@PlayerTakeItemsCallback result
                }
                ActionResult.PASS
            }
        }
    }

    fun onTakeItems(playerEntity: PlayerEntity, inventory: Inventory): ActionResult
}
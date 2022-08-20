package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult

fun interface VillagerTradeCallback {
    companion object {
        @JvmField
        val EVENT: Event<VillagerTradeCallback> = EventFactory.createArrayBacked(VillagerTradeCallback::class.java){ listeners ->
            VillagerTradeCallback{ sellItem, serverPlayerEntity ->
                for(listener in listeners) {
                    val result = listener.trade(sellItem, serverPlayerEntity)
                    if(result != ActionResult.PASS)
                        return@VillagerTradeCallback result
                }
                ActionResult.PASS
            }
        }
    }

    fun trade(sellItem: ItemStack, serverPlayerEntity: ServerPlayerEntity) : ActionResult
}
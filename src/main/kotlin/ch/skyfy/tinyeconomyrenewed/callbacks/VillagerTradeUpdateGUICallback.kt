package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.village.MerchantInventory

fun interface VillagerTradeUpdateGUICallback {
    companion object {
        @JvmField
        val EVENT: Event<VillagerTradeUpdateGUICallback> = EventFactory.createArrayBacked(VillagerTradeUpdateGUICallback::class.java){ listeners ->
            VillagerTradeUpdateGUICallback{ merchantInventory, playerEntity ->
                for(listener in listeners) listener.updateOffers(merchantInventory, playerEntity)
            }
        }
    }

    fun updateOffers(merchantInventory: MerchantInventory, playerEntity: PlayerEntity)
}
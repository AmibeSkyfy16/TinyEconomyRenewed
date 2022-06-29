package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.advancement.Advancement
import net.minecraft.server.network.ServerPlayerEntity

fun interface AdvancementCompletedCallback {
    companion object {
        @JvmField
        val EVENT: Event<AdvancementCompletedCallback> = EventFactory.createArrayBacked(AdvancementCompletedCallback::class.java) { listeners ->
            AdvancementCompletedCallback { serverPlayerEntity, advancement, criterionName ->
                for (listener in listeners) listener.completed(serverPlayerEntity, advancement, criterionName)
            }
        }
    }

    fun completed(serverPlayerEntity: ServerPlayerEntity, advancement: Advancement, criterionName: String)
}
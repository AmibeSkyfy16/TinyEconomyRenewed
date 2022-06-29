package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.advancement.AdvancementDisplay
import net.minecraft.util.Identifier

fun interface AdvancementCreatedCallback {
    companion object {
        @JvmField
        val EVENT: Event<AdvancementCreatedCallback> = EventFactory.createArrayBacked(AdvancementCreatedCallback::class.java){ listeners ->
            AdvancementCreatedCallback{ id, display ->
                for(listener in listeners) listener.init(id, display)
            }
        }
    }

    fun init(id: Identifier, display: AdvancementDisplay)
}
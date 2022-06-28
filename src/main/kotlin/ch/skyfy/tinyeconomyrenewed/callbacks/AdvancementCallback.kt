package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.advancement.AdvancementDisplay
import net.minecraft.util.Identifier

fun interface AdvancementCallback {

    companion object {
        @JvmField
        val EVENT: Event<AdvancementCallback> = EventFactory.createArrayBacked(AdvancementCallback::class.java){listeners ->
            AdvancementCallback{ id, display ->
                for(listener in listeners){
                    listener.init(id, display)
                }
            }
        }
    }

    fun init(id: Identifier, display: AdvancementDisplay)

}
package ch.skyfy.tinyeconomyrenewed.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

fun interface EntityDamageCallback {
    companion object {
        @JvmField
        val EVENT: Event<EntityDamageCallback> = EventFactory.createArrayBacked(EntityDamageCallback::class.java){ listeners ->
            EntityDamageCallback{ livingEntity, damageSource,amount ->
                for(listener in listeners) listener.onDamage(livingEntity, damageSource, amount)
            }
        }
    }

    fun onDamage(livingEntity: LivingEntity, damageSource: DamageSource, amount: Float)
}
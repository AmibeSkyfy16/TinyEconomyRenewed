package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.explosion.Explosion
import net.minecraft.world.explosion.Explosion.DestructionType
import net.minecraft.world.explosion.ExplosionBehavior

fun interface CreateExplosionCallback {
    companion object {
        @JvmField
        val EVENT: Event<CreateExplosionCallback> = EventFactory.createArrayBacked(CreateExplosionCallback::class.java) { listeners ->
            CreateExplosionCallback { explosion, serverWorld, entity, damageSource, behavior, x, y, z, power, createFire, destructionType ->
                for (listener in listeners)
                    listener.createExplosion(explosion, serverWorld, entity, damageSource, behavior, x, y, z, power, createFire, destructionType)
            }
        }
    }

    fun createExplosion(
        explosion: Explosion,
        serverWorld: ServerWorld,
        entity: Entity?,
        damageSource: DamageSource?,
        behavior: ExplosionBehavior?,
        x: Double,
        y: Double,
        z: Double,
        power: Float,
        createFire: Boolean,
        destructionType: DestructionType
    )
}
package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.Hopper
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface HopperCallback {
    companion object {
        @JvmField
        val EVENT: Event<HopperCallback> = EventFactory.createArrayBacked(HopperCallback::class.java) { listeners ->
            HopperCallback { world, pos, state, hopper ->
                for (listener in listeners) {
                    val result = listener.insertAndExtract(world, pos, state, hopper)
                    if (!result.value) return@HopperCallback result
                }
                TypedActionResult.pass(false)
            }
        }
    }

    fun insertAndExtract(world: World, pos: BlockPos, state: BlockState, hopper: Hopper) : TypedActionResult<Boolean>
}
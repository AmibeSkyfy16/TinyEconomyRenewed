package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.util.ActionResult

fun interface BlockPlacedCallback {
    companion object {
        @JvmField
        val EVENT: Event<BlockPlacedCallback> = EventFactory.createArrayBacked(BlockPlacedCallback::class.java){ listeners ->
            BlockPlacedCallback{ blockItem, itemPlacementContext, actionResult ->
                for(listener in listeners) listener.onPlaced(blockItem, itemPlacementContext, actionResult)
            }
        }
    }

    fun onPlaced(blockItem: BlockItem, itemPlacementContext: ItemPlacementContext, actionResult: ActionResult)
}
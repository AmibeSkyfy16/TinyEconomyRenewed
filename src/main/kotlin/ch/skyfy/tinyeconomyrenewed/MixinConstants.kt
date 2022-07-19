package ch.skyfy.tinyeconomyrenewed;

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory

object MixinConstants {
    @JvmField
    val OPENED_INVENTORIES: Map<Inventory, List<PlayerEntity>> = HashMap()
}

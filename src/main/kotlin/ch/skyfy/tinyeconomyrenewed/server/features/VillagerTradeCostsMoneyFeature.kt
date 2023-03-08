package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.callbacks.VillagerTradeCallback
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style.EMPTY
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting.RED

class VillagerTradeCostsMoneyFeature(private val databaseManager: DatabaseManager, private val economy: Economy) {

    init { VillagerTradeCallback.EVENT.register(this::trade) }

    private fun trade(sellItem: ItemStack, serverPlayerEntity: ServerPlayerEntity): ActionResult {
        val config = Configs.VILLAGER_TRADE_COSTS_MONEY_CONFIG.serializableData
        if (!config.enabled) return ActionResult.PASS

        val priceToPay: Double = sellItem.count * config.price / config.amount

        val player = databaseManager.cachePlayers.find { it.uuid == serverPlayerEntity.uuidAsString }

        if (player != null) {
            if (player.money < priceToPay) {
                serverPlayerEntity.sendMessage(Text.literal("You don't have enough money to trade").setStyle(EMPTY.withColor(RED)))
                return ActionResult.FAIL
            }
            economy.withdraw(player.uuid, priceToPay)
        }
        return ActionResult.PASS
    }
}
package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeCallback
import ch.skyfy.tinyeconomyrenewed.config.Configs
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style.EMPTY
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting.RED

class VillagerTradeCostsMoneyFeature(private val databaseManager: DatabaseManager, private val economy: Economy) {

    init { VillagerTradeCallback.EVENT.register(this::trade) }

    private fun trade(sellItem: ItemStack, serverPlayerEntity: ServerPlayerEntity): ActionResult {
        val config = Configs.VILLAGER_TRADE_COSTS_MONEY_CONFIG.`data`
        if (!config.enabled) return ActionResult.PASS

        val priceToPay: Float = sellItem.count * config.price / config.amount

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
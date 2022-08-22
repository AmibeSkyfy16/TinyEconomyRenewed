package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeCallback
import ch.skyfy.tinyeconomyrenewed.config.Configs
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
//import ch.skyfy.tinyeconomyrenewed.db.players
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style.EMPTY
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting.RED
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.update

class VillagerTradeCostsMoneyFeature(private val databaseManager: DatabaseManager) {

    init {
//        VillagerTradeCallback.EVENT.register(this::trade)
    }

    private fun trade(sellItem: ItemStack, serverPlayerEntity: ServerPlayerEntity): ActionResult {
//        val config = Configs.VILLAGER_TRADE_COSTS_MONEY_CONFIG.`data`
//        if (!config.enabled) return ActionResult.PASS
//
//        val priceToPay: Float = sellItem.count * config.price / config.amount
//
//        val player = databaseManager.db.players.find { it.uuid like serverPlayerEntity.uuidAsString }
//
//        if (player != null) {
//            if (player.money < priceToPay) {
//                serverPlayerEntity.sendMessage(Text.literal("You don't have enough money to trade").setStyle(EMPTY.withColor(RED)))
//                return ActionResult.FAIL
//            }
//            player.money -= priceToPay
//            databaseManager.db.players.update(player)
//        }
        return ActionResult.PASS
    }
}
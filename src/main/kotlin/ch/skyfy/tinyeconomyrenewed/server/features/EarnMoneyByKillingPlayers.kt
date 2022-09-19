package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.config.Configs.EARN_MONEY_BY_KILLING_PLAYERS_CONFIG
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class EarnMoneyByKillingPlayers(private val economy: Economy) {

    init {
        ServerPlayerEvents.ALLOW_DEATH.register{ player, damageSource, _ ->
            if(damageSource.attacker is ServerPlayerEntity){
                val attacker = damageSource.attacker as ServerPlayerEntity
                val serializableData = EARN_MONEY_BY_KILLING_PLAYERS_CONFIG.serializableData
                val amount =  serializableData.amount
                if(serializableData.shouldKilledPlayerLostMoney) {
                    economy.withdraw(player.uuidAsString, amount)
                    player.sendMessage(Text.literal("You have lost $amount cause you have been killed by ${attacker.name.string}").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                }
                economy.deposit(attacker, attacker.uuidAsString) { amount }

                attacker.sendMessage(Text.literal("You have earned $amount cause you have killed ${player.name.string}").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
            }
            return@register true
        }
    }

}
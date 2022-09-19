package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class LossMoneyDyingFeature(private val economy: Economy) {

    init {
        ServerPlayerEvents.ALLOW_DEATH.register{ player, _, _ ->
            val amount = Configs.LOSS_MONEY_DYING_CONFIG.serializableData.amount
            player.sendMessage(Text.literal("You are dead ! You loss $amount of your money").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
            economy.withdraw(player.uuidAsString, amount)
            return@register true
        }
    }

}
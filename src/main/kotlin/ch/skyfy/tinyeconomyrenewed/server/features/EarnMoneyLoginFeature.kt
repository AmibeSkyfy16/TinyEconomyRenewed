package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.jsonconfiglib.updateIterable
import ch.skyfy.jsonconfiglib.updateNested
import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.persisent.Persistents.PLAYER_INFOS
import ch.skyfy.tinyeconomyrenewed.server.persisent.PlayerInfo
import ch.skyfy.tinyeconomyrenewed.server.persisent.PlayerInfosPersistent
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EarnMoneyLoginFeature(private val economy: Economy) {

    private val formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())

    init {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            val playerInfo = PLAYER_INFOS.serializableData.infos.find { playerInfo -> playerInfo.uuid == player.uuidAsString }
            if (playerInfo == null) earn(player, null, Instant.now().atZone(ZoneId.systemDefault()))
            else {
                val today = Instant.now().atZone(ZoneId.systemDefault())
                val lastLogin = Instant.from(formatter.parse(playerInfo.lastLoginDate)).atZone(ZoneId.systemDefault())
                if (today.dayOfYear != lastLogin.dayOfYear) earn(player, playerInfo, today)
            }
        }
    }

    private fun earn(player: ServerPlayerEntity, playerInfo: PlayerInfo?, zonedDateTime: ZonedDateTime) {
        val amount = Configs.EARN_MONEY_LOGIN_CONFIG.serializableData.amount
        player.sendMessage(Text.literal("First login for today ! You've just earn $amount").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
        economy.deposit(player, player.uuidAsString) { amount }

        if (playerInfo == null) PLAYER_INFOS.updateIterable(PlayerInfosPersistent::infos) { it.add(PlayerInfo(player.uuidAsString, formatter.format(zonedDateTime))) }
        else PLAYER_INFOS.updateNested(PlayerInfo::lastLoginDate, playerInfo, formatter.format(zonedDateTime))
    }

}
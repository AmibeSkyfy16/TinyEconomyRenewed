package ch.skyfy.tinyeconomyrenewed.logic

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.ScoreboardManager2
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.db.players
import ch.skyfy.tinyeconomyrenewed.features.RewardFeature
import ch.skyfy.tinyeconomyrenewed.features.ShopFeature
import ch.skyfy.tinyeconomyrenewed.features.VillagerTradeCostsMoneyFeature
import net.minecraft.network.ClientConnection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.update


class Game(private val databaseManager: DatabaseManager, minecraftServer: MinecraftServer) {

    private val scoreboardManager: ScoreboardManager2 = ScoreboardManager2(databaseManager)
    private val economy: Economy = Economy(databaseManager, scoreboardManager)

    init {
        RewardFeature(databaseManager, economy)
        ShopFeature(databaseManager, economy, scoreboardManager, minecraftServer)
        VillagerTradeCostsMoneyFeature(databaseManager)
        registerEvents()
    }

    private fun registerEvents() {
        PlayerJoinCallback.EVENT.register(this::onPlayerJoin)
    }

    private fun onPlayerJoin(@Suppress("UNUSED_PARAMETER") connection: ClientConnection, serverPlayerEntity: ServerPlayerEntity) {
        val p = databaseManager.db.players.find { it.uuid like serverPlayerEntity.uuidAsString }
        if (p == null) {
            databaseManager.db.players.add(Player {
                uuid = serverPlayerEntity.uuidAsString
                name = serverPlayerEntity.name.string
            })
        } else { // Update name (maybe some players can change their name)
            if (p.name != serverPlayerEntity.name.string) {
                TinyEconomyRenewedMod.LOGGER.info("Player ${p.name} has changed his name to ${serverPlayerEntity.name.string}")
                p.name = serverPlayerEntity.name.string
                databaseManager.db.players.update(p)
            }
        }
    }
}
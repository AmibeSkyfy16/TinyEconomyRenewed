package ch.skyfy.tinyeconomyrenewed.server.logic

import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.db.Player
import ch.skyfy.tinyeconomyrenewed.server.features.*
import kotlinx.coroutines.launch
import net.minecraft.network.ClientConnection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class Game(val databaseManager: DatabaseManager, minecraftServer: MinecraftServer) {

    companion object {

        val PLAYER_JOIN_CALLBACK_FIRST = Identifier("fabric", "player_join_callback_first")
        val PLAYER_JOIN_CALLBACK_SECOND = Identifier("fabric", "player_join_callback_second")

        init { setUpEventPhaseOrdering() }

        private fun setUpEventPhaseOrdering() {
            PlayerJoinCallback.EVENT.addPhaseOrdering(PLAYER_JOIN_CALLBACK_FIRST, PLAYER_JOIN_CALLBACK_SECOND)
        }
    }

    val scoreboardManager: ScoreboardManager = ScoreboardManager(databaseManager)
    private val moneyEarnedRewardFeature: MoneyEarnedRewardFeature = MoneyEarnedRewardFeature()
    private val economy: Economy = Economy(databaseManager, scoreboardManager, moneyEarnedRewardFeature)

    init {
        LossMoneyDyingFeature(economy)
        EarnMoneyLoginFeature(economy)
        EarnMoneyFeature(databaseManager, economy)
        ShopFeature(databaseManager, economy, minecraftServer)
        VillagerTradeCostsMoneyFeature(databaseManager, economy)
        registerEvents()
    }

    private fun registerEvents() {
        PlayerJoinCallback.EVENT.register(PLAYER_JOIN_CALLBACK_FIRST, this::onPlayerJoin)
    }

    private fun onPlayerJoin(@Suppress("UNUSED_PARAMETER") connection: ClientConnection, serverPlayerEntity: ServerPlayerEntity) {
        val playerUUID = serverPlayerEntity.uuidAsString
        val playerName = serverPlayerEntity.name.string

        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            var player = databaseManager.cachePlayers.find { it.uuid == playerUUID }
            if (player == null) {
                player = Player { uuid = playerUUID; name = playerName }
                databaseManager.modifyPlayers { databaseManager.cachePlayers.add(player) }
                databaseManager.addPlayer(player)
            } else if (player.name != playerName) { // If a player changed his name, we have to update it in the database
                    TinyEconomyRenewedMod.LOGGER.info("Player ${player.name} has changed his name to $playerName")
                    player.name = playerName
                    databaseManager.updatePlayers(player)
            }
        }
    }
}
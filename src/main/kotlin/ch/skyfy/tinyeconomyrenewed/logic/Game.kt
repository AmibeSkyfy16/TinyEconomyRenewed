package ch.skyfy.tinyeconomyrenewed.logic

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.features.RewardFeature
import ch.skyfy.tinyeconomyrenewed.features.ShopFeature
import ch.skyfy.tinyeconomyrenewed.features.VillagerTradeCostsMoneyFeature
import kotlinx.coroutines.launch
import net.minecraft.network.ClientConnection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier


class Game(private val databaseManager: DatabaseManager, minecraftServer: MinecraftServer) {

    companion object {

        val PLAYER_JOIN_CALLBACK_FIRST = Identifier("fabric", "player_join_callback_first")
        val PLAYER_JOIN_CALLBACK_SECOND = Identifier("fabric", "player_join_callback_second")

        init {
            setUpEventPhaseOrdering()
        }


        private fun setUpEventPhaseOrdering() {
            PlayerJoinCallback.EVENT.addPhaseOrdering(PLAYER_JOIN_CALLBACK_FIRST, PLAYER_JOIN_CALLBACK_SECOND)
        }
    }

    private val scoreboardManager: ScoreboardManager = ScoreboardManager(databaseManager)
    private val economy: Economy = Economy(databaseManager, scoreboardManager)

    init {
        RewardFeature(databaseManager, economy)
        ShopFeature(databaseManager, economy, minecraftServer)
        VillagerTradeCostsMoneyFeature(databaseManager)
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
package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.logic.Game.Companion.PLAYER_JOIN_CALLBACK_SECOND
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.text.Text
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.task.silkCoroutineScope
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.game.sideboard.Sideboard
import net.silkmc.silk.game.sideboard.SideboardLine
import net.silkmc.silk.game.sideboard.sideboard

class ScoreboardManager(private val databaseManager: DatabaseManager) {

    private data class PlayerSideboard(
        val uuid: String,
        val sideboard: Sideboard,
        val updatableLine: SideboardLine.Updatable
    )

    private val sideboards = mutableSetOf<PlayerSideboard>()

    init { initialize() }

    /**
     * Update Money field in the sideboard
     *
     * @param uuid A [String] object that represent the uuid of the player that we have to update the money on his sideboard
     */
    private fun updatePlayerMoney(uuid: String) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.modifyPlayers {
                val amount = databaseManager.cachePlayers.find { player: Player -> player.uuid == uuid }?.money ?: -1f
                sideboards.find { it.uuid == uuid }?.updatableLine?.launchUpdate("Money: $amount".literal)
            }
        }
    }

    fun updatePlayerMoney(uuid: String, amount: Float) = sideboards.find { it.uuid == uuid }?.updatableLine?.launchUpdate("Money: $amount".literal)

    @OptIn(DelicateSilkApi::class)
    private fun initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> sideboards.removeIf { it.uuid == handler.player.uuidAsString } }

        PlayerJoinCallback.EVENT.register(PLAYER_JOIN_CALLBACK_SECOND) { _, player ->

            val playerUUID = player.uuidAsString

            LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
                if (sideboards.none { it.uuid == playerUUID }) {
                    val moneyLine = SideboardLine.Updatable("Money: -1.0".literal)
                    val mySideboard = sideboard("<< Main Board >>".literal) {
                        line(Text.empty())
                        line(moneyLine)
                    }
                    mySideboard.displayToPlayer(player)
                    sideboards.add(PlayerSideboard(playerUUID, mySideboard, moneyLine))
                    silkCoroutineScope.launch {
                        delay(1000)
                        updatePlayerMoney(playerUUID)
                    }
                }
            }
        }
    }
}
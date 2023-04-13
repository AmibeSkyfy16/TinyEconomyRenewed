package ch.skyfy.tinyeconomyrenewed.server

import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.db.Player
import ch.skyfy.tinyeconomyrenewed.server.logic.Game.Companion.PLAYER_JOIN_CALLBACK_SECOND
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.world.WorldEvents
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.event.EntityEvents
import net.silkmc.silk.core.kotlin.ticks
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

    init {
        initialize()
    }

    /**
     * Update Money field in the sideboard
     *
     * @param uuid A [String] object that represent the uuid of the player that we have to update the money on his sideboard
     */
    fun updatePlayerMoney(uuid: String) {
        val amount = databaseManager.cachePlayers.find { player: Player -> player.uuid == uuid }?.money ?: -1.0
        sideboards.find { it.uuid == uuid }?.updatableLine?.launchUpdate("Money: ${String.format("%.3f", amount)}".literal)

        // OLD CODE
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {

//            sideboards.find { it.uuid == uuid }?.updatableLine?.update("Money: $amount".literal)
//            println("updated player money $amount")
//            databaseManager.modifyPlayers {
//                val amount = databaseManager.cachePlayers.find { player: Player -> player.uuid == uuid }?.money ?: -1f
//                sideboards.find { it.uuid == uuid }?.updatableLine?.launchUpdate("Money: $amount".literal)
//                sideboards.find { it.uuid == uuid }?.updatableLine?.update("Money: $amount".literal)
//            }
        }
    }

    fun updatePlayerMoney(uuid: String, amount: Double) = sideboards.find { it.uuid == uuid }?.updatableLine?.launchUpdate("Money: ${String.format("%.3f", amount)}".literal)

    @OptIn(DelicateSilkApi::class)
    private fun initialize() {

        // After clicking just one time on a block, the sideboard is updated with correct line and values !
        // But why when connecting for the first time, the sideboard isn't updated ? I created it, then called mySideboard.displayToPlayer(player) and updateLine
        // If I mine blocks the lines of the sideboard will be updated, but again nothing happens. It will work again only if I deco reco or right click on a block
//        UseBlockCallback.EVENT.register { p, _, _, _ ->
//            sideboards.forEach {
//                it.sideboard.displayToPlayer(p as ServerPlayerEntity)
//            }
//            ActionResult.PASS
//        }

        // Just a test to see what's happening when updating sideboard everytime a block is broken
//        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, entity ->
//            sideboards.first { it.uuid == player.uuidAsString }.let {
//                it.updatableLine.launchUpdate("NEW TEXT".literal)
//            }
//        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> sideboards.removeIf { it.uuid == handler.player.uuidAsString } }

        PlayerJoinCallback.EVENT.register(PLAYER_JOIN_CALLBACK_SECOND) { _, player ->
            val playerUUID = player.uuidAsString

            if (sideboards.none { it.uuid == playerUUID }) {
                val moneyLine = SideboardLine.Updatable("Money: -1.0".literal)
                val mySideboard = sideboard("<< Main Board >>".literal) {
                    line(Text.empty())

                    // Test, not work the first time I joined the server
                    line(moneyLine)

                    // Test 2, not work the first time I joined the server
//                    updatingLine(period = 80.ticks, updater = {
//                        val amount = databaseManager.cachePlayers.find { player: Player -> player.uuid == playerUUID }?.money ?: -1f
//                        println("money: $amount")
//                        "Money: $amount".literal
//                    })
                }
//                mySideboard.displayToPlayer(player)
                sideboards.add(PlayerSideboard(playerUUID, mySideboard, moneyLine))

                updatePlayerMoney(playerUUID)

                // Fix the bug by calling displayToPlayer after 2000ms
                silkCoroutineScope.launch {
                    delay(2000)
                    mySideboard.displayToPlayer(player)
                }
            }
        }
    }
}
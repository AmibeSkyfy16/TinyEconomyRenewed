@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed.server

import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.db.Player
import ch.skyfy.tinyeconomyrenewed.server.features.MoneyEarnedRewardFeature
import kotlinx.coroutines.launch
import net.minecraft.server.network.ServerPlayerEntity

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager, private val scoreboardManager: ScoreboardManager, private val moneyEarnedRewardFeature: MoneyEarnedRewardFeature) {

    /**
     * Make a deposit for the [uuid] passed in parameter
     *
     * @param uuid A [String] object that represent the player uuid to whom the money must be deposited
     * @param block A code that will return the amount earned by the player
     */
    fun deposit(player: ServerPlayerEntity?, uuid: String, block: () -> Double) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.modifyPlayers {
                databaseManager.cachePlayers.find { player: Player -> player.uuid == uuid }?.let { cachePlayer ->
                    val earnedAmount = block.invoke()
                    val totalAmount = deposit(cachePlayer, earnedAmount)
                    moneyEarnedRewardFeature.rewardPlayer(player, uuid, totalAmount)
                    scoreboardManager.updatePlayerMoney(uuid, totalAmount)
                }
            }
        }
    }

    private fun deposit(player: Player, amount: Double): Double {
        player.money += amount
        return player.money
    }

    fun withdraw(uuid: String, amount: Double) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.modifyPlayers {
                databaseManager.cachePlayers.find { it.uuid == uuid }?.let {
                    scoreboardManager.updatePlayerMoney(uuid, withdraw(it, amount))
                }
            }
        }
    }

    private fun withdraw(player: Player, amount: Double): Double {
        player.money -= amount
        return player.money
    }

}
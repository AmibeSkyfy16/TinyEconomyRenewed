@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import kotlinx.coroutines.launch

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager, private val scoreboardManager: ScoreboardManager) {

    /**
     * Make a deposit for the [uuid] passed in parameter
     *
     * @param uuid A [String] object that represent the player uuid to whom the money must be deposited
     * @param block A code that will return the amount earned by the player
     */
    fun deposit(uuid: String, block: () -> Float) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.modifyPlayers {
                databaseManager.cachePlayers.find { player: Player -> player.uuid == uuid }?.let {
                    scoreboardManager.updatePlayerMoney(uuid, deposit(it, block.invoke()))
                }
            }
        }
    }

    private fun deposit(player: Player, amount: Float): Float {
        player.money += amount
        return player.money
    }

    fun withdraw(uuid: String, amount: Float) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.modifyPlayers {
                databaseManager.cachePlayers.find { it.uuid == uuid }?.let {
                    scoreboardManager.updatePlayerMoney(uuid, withdraw(it, amount))
                }
            }
        }
    }

    private fun withdraw(player: Player, amount: Float) : Float {
        player.money -= amount
        return player.money
    }

}
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
//            databaseManager.getValue{
//                databaseManager.cachePlayers.find { player -> player.uuid == uuid }
//            }?.let { player: Player -> deposit(player, block.invoke()) }
            val player = databaseManager.cachePlayers.access { players ->
                players.find { player -> player.uuid == uuid }?.let {
                    deposit(it, block.invoke())
                    scoreboardManager.updatePlayerMoney(uuid)
                }
            }
//            if (player != null){
//                deposit(player, block.invoke())
//                scoreboardManager.updatePlayerMoney(uuid)
//            }
//                databaseManager.cachePlayers.find { player -> player.uuid == uuid }?.let { player -> deposit(player, block.invoke()) }
//            scoreboardManager.updatePlayerMoney(uuid)
        }
    }


    private fun deposit(player: Player, amount: Float) {
        player.money += amount
    }

    fun withdraw(uuid: String, amount: Float) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            databaseManager.cachePlayers.access { players ->
                players.find { it.uuid == uuid }?.let {
                    withdraw(it, amount)
                    scoreboardManager.updatePlayerMoney(uuid)
                }
            }
//            databaseManager.cachePlayers.find { it.uuid == uuid }?.let {
//                withdraw(it, amount)
//                scoreboardManager.updatePlayerMoney(uuid)
//            }
        }

    }

    private fun withdraw(player: Player, amount: Float) : Float {
        player.money -= amount
        return player.money
    }

}
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager, private val scoreboardManager: ScoreboardManager) {

    /**
     * Make a deposit for the [uuid] passed in parameter
     *
     * @param uuid A [String] object that represent the player uuid to whom the money must be deposited
     * @param block A code that will return the amount earned by the player
     */
    fun deposit(uuid: String, block: () -> Float) =
        databaseManager.executor.execute { // Remind: all thing related to the database must be executed on the DATABASE THREAD
            databaseManager.cachePlayers.find { player -> player.uuid == uuid }?.let { player -> deposit(player, block.invoke()) }
            scoreboardManager.updatePlayerMoney(uuid)
        }

    private fun deposit(player: Player, amount: Float) {
        player.money += amount
    }

    fun withdraw(uuid: String, amount: Float) = databaseManager.executor.execute {
        databaseManager.cachePlayers.find { it.uuid == uuid }?.let {
            withdraw(it, amount)
            scoreboardManager.updatePlayerMoney(uuid)
        }
    }

    private fun withdraw(player: Player, amount: Float) {
        player.money -= amount
    }

}
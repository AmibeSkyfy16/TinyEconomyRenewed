@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager, private val scoreboardManager: ScoreboardManager2)  {

    fun deposit(uuid: String, block: () -> Float) =
        databaseManager.executor.execute {
            databaseManager.cachePlayers.find { player -> player.uuid == uuid }.let { player -> if (player != null) deposit(player, block.invoke()) }
        }

    fun deposit(player: Player, amount: Float) { player.money += amount }

    fun withdraw(uuid: String, amount: Float) = databaseManager.cachePlayers.find { it.uuid == uuid }.let { if (it != null) withdraw(it, amount) }

    fun withdraw(player: Player, amount: Float) { player.money -= amount }

}
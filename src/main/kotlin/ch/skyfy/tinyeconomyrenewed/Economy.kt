@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class Economy(
    private val databaseManager: DatabaseManager,
    private val scoreboardManager: ScoreboardManager2, override val coroutineContext: CoroutineContext = Dispatchers.IO,
)  : CoroutineScope{

    fun deposit(uuid: String, block: () -> Float) {
        launch {
            databaseManager.cachePlayers.access { list ->
                list.find { player -> player.uuid == uuid }.let { player -> if (player != null) deposit(player, block.invoke()) }
            }
        }
    }

    fun deposit(player: Player, amount: Float) {
        player.money += amount
    }

    fun withdraw(uuid: String, amount: Float) {
        databaseManager.cachePlayers.access {list ->
            list.find { it.uuid == uuid }.let { if (it != null) withdraw(it, amount) }
        }
    }

    fun withdraw(player: Player, amount: Float) {
        player.money -= amount
    }

}
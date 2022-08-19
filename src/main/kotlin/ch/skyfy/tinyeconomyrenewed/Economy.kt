@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.db.players
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ktorm.dsl.like
import org.ktorm.entity.find
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager, override val coroutineContext: CoroutineContext = Dispatchers.IO) : CoroutineScope {

    fun deposit(uuid: String, am: () -> Float?) {
        launch { deposit(uuid, am.invoke()) }
    }

    fun deposit(uuid: String, amount: Float?) {
        launch {
            databaseManager.db.players.find { it.uuid like uuid }.let { if (it != null && amount != null) deposit(it, amount) }
        }
    }

    fun deposit(player: Player, amount: Float) {
        launch {
            player.money += amount
            player.flushChanges()
        }
//        databaseManager.db.players.update(player)
    }

    fun withdraw(uuid: String, amount: Float) {
        launch {
            databaseManager.db.players.find { it.uuid like uuid }.let { if (it != null) withdraw(it, amount) }
        }
    }

    fun withdraw(player: Player, amount: Float) {
        launch {
            player.money -= amount
            player.flushChanges()
        }
//        databaseManager.db.players.update(player)
    }

}
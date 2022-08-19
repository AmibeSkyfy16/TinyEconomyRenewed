@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.db.players
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.text.Text
import org.ktorm.dsl.like
import org.ktorm.entity.find
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class Economy(
    private val databaseManager: DatabaseManager,
    private val scoreboardManager: ScoreboardManager2,
    override val coroutineContext: CoroutineContext = Dispatchers.IO,
) : CoroutineScope {

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
        launch {
            flow<Text> {
                scoreboardManager.dollarMap[player.uuid]?.emit(Text.literal("dollar -> $amount"))
            }.collect()
        }
        channelFlow<Text> {
            println("Flow in deposit fun")
            scoreboardManager.dollarMap[player.uuid]?.emit(Text.literal("DEPOSIT -> $amount"))
        }
//
//        f
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
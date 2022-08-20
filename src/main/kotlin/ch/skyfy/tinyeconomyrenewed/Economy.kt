@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player

@Suppress("unused")
class Economy(
    private val databaseManager: DatabaseManager,
    private val scoreboardManager: ScoreboardManager2
)  {

//    fun deposit(uuid: String, block: () -> Float) {
//        deposit(uuid, block.invoke())
////        launch { deposit(uuid, am.invoke()) }
//    }

    fun deposit(uuid: String, block: () -> Float) {
        databaseManager.cachePlayers.access { list ->
            list.find { player -> player.uuid == uuid }.let { player -> if (player != null) deposit(player, block.invoke()) }
        }
//        databaseManager.players.find { it.get().freeze().uuid == uuid }.let { if (it != null) it.get().money += block.invoke().freeze()}
//        launch {
//            databaseManager.db.players.find { it.uuid like uuid }.let { if (it != null && amount != null) deposit(it, amount) }
//        }
    }

    fun deposit(player: Player, amount: Float) {
        player.money += amount
//        println("deposit -> Thread id: " + Thread.currentThread().id)
//        println("deposit -> Thread name: " + Thread.currentThread().name)
//        launch {
//            player.flushChanges()
//        }
//        runBlocking {
//            scoreboardManager.dollarMap[player.uuid]?.emit(Text.literal("dollar -> $amount"))
//        }
//        launch {
//            flow<Text> {
//                scoreboardManager.dollarMap[player.uuid]?.emit(Text.literal("dollar -> $amount"))
//            }
//        }
//        channelFlow<Text> {
//            println("Flow in deposit fun")
//            scoreboardManager.dollarMap[player.uuid]?.emit(Text.literal("DEPOSIT -> $amount"))
//        }
//
//        f
//        databaseManager.db.players.update(player)
    }

    fun withdraw(uuid: String, amount: Float) {
        databaseManager.cachePlayers.access {list ->
            list.find { it.uuid == uuid }.let { if (it != null) withdraw(it, amount) }
        }
//        launch {
//            databaseManager.db.players.find { it.uuid like uuid }.let { if (it != null) withdraw(it, amount) }
//        }
    }

    fun withdraw(player: Player, amount: Float) {
        player.money -= amount
//        launch {
//            player.money -= amount
//            player.flushChanges()
//        }
//        databaseManager.db.players.update(player)
    }

}
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.db.players
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.update

@Suppress("unused")
class Economy(private val databaseManager: DatabaseManager) {

    fun deposit(uuid: String, am: () -> Float?){
        deposit(uuid, am.invoke())
    }

    fun deposit(uuid: String, amount: Float?){
        databaseManager.db.players.find { it.uuid like uuid }.let { if (it != null && amount != null)deposit(it, amount) }
    }

    fun deposit(player: Player, amount: Float){
        player.money += amount
        databaseManager.db.players.update(player)
    }

    fun withdraw(uuid: String, amount: Float){
        databaseManager.db.players.find { it.uuid like uuid }.let { if(it != null)withdraw(it, amount) }
    }

    fun withdraw(player: Player, amount: Float){
        player.money -= amount
        databaseManager.db.players.update(player)
    }

}
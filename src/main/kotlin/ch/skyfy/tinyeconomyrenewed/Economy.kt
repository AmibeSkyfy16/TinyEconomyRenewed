@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.*
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update

class Economy(private val databaseManager: DatabaseManager) {

    private val Database.players get() = this.sequenceOf(Players)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    fun deposit(uuid: String, am: () -> Float?){
        deposit(uuid, am.invoke())
    }

    fun deposit(uuid: String, amount: Float?){
        val player = databaseManager.db.players.find { it.uuid like uuid }

        if(player != null && amount != null){
            player.money += amount
            databaseManager.db.players.update(player)
        }

    }

    fun withdraw(uuid: String, amount: Float){
        val player = databaseManager.db.players.find { it.uuid like uuid }
        if(player != null) {
            player.money -= amount
            databaseManager.db.players.update(player)
        }
    }

}
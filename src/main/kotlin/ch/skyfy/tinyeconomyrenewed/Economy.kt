@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.*
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update

object Economy {

    private val Database.players get() = this.sequenceOf(Players)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {

    }

    fun deposit(uuid: String, am: () -> Float?){
        deposit(uuid, am.invoke())
    }

    fun deposit(uuid: String, amount: Float?){
        val player = DatabaseManager.db.players.find { it.uuid like uuid }

        if(player != null && amount != null){
            player.money += amount
            DatabaseManager.db.players.update(player)
        }

    }

    fun withdraw(uuid: String, amount: Float){

    }

}
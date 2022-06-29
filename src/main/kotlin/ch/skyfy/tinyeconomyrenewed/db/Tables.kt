@file:Suppress("unused")

package ch.skyfy.tinyeconomyrenewed.db

import org.ktorm.schema.Table
import org.ktorm.schema.float
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Player : Table<Nothing>("player"){
    val id = int("id").primaryKey()
    val name = varchar("name")
    val money = float("money")
}


interface Item : org.ktorm.entity.Entity<Item>{
    companion object : org.ktorm.entity.Entity.Factory<Item>()
    val id: Int
    var translationKey: String
}
interface Entity : org.ktorm.entity.Entity<Entity>{
    companion object : org.ktorm.entity.Entity.Factory<Entity>()
    val id: Int
    var translationKey: String
}
interface Advancement : org.ktorm.entity.Entity<Advancement>{
    companion object : org.ktorm.entity.Entity.Factory<Advancement>()
    val id: Int
    var identifier: String
    var frame: String
    var title: String
    var description: String
}

interface MinedBlockReward : org.ktorm.entity.Entity<MinedBlockReward>{
    companion object : org.ktorm.entity.Entity.Factory<MinedBlockReward>()
    val id: Int
    var amount: Float
    var item: Item
}
interface EntityKilledReward : org.ktorm.entity.Entity<EntityKilledReward>{
    companion object : org.ktorm.entity.Entity.Factory<EntityKilledReward>()
    val id: Int
    var amount: Float
    var entity: Entity
}
interface AdvancementReward : org.ktorm.entity.Entity<AdvancementReward>{
    companion object : org.ktorm.entity.Entity.Factory<AdvancementReward>()
    val id: Int
    var amount: Float
    var advancement: Advancement
}


object Items : Table<Item>("item"){
    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}
object Entities : Table<Entity>("entity"){
    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}
object Advancements : Table<Advancement>("advancement"){
    val id = int("id").primaryKey().bindTo { it.id }
    val identifier = varchar("identifier").bindTo { it.identifier }
    val frame = varchar("frame").bindTo { it.frame }
    val title = varchar("title").bindTo { it.title }
    val description = varchar("description").bindTo { it.description }
}


object MinedBlockRewards : Table<MinedBlockReward>("mined_block_reward"){
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = float("amount").bindTo { it.amount }
    val itemId = int("item_id").references(Items){it.item}
}
object EntityKilledRewards : Table<EntityKilledReward>("entity_killed_reward"){
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = float("amount").bindTo { it.amount }
    val EntityId = int("entity_id").references(Entities){it.entity}
}
object AdvancementRewards : Table<AdvancementReward>("advancement_reward"){
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = float("amount").bindTo { it.amount }
    val AdvancementId = int("advancement_id").references(Advancements){it.advancement}
}






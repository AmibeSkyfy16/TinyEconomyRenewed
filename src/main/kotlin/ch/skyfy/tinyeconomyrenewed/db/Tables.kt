package ch.skyfy.tinyeconomyrenewed.db

import org.ktorm.database.Database
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.float
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Player : Table<Nothing>("player"){
    val id = int("id").primaryKey()
    val name = varchar("name")
    val money = float("money")
}

//object Item : Table<Nothing>("item"){
//    val id = int("id").primaryKey()
//    val translationKey = varchar("translation_key")
//}

object Entity : Table<Nothing>("entity"){
    val id = int("id").primaryKey()
    val translationKey = varchar("translation_key")
}

object Advancement : Table<Nothing>("advancement"){
    val id = int("id").primaryKey()
    val identifier = varchar("identifier")
    val frame = varchar("frame")
    val title = varchar("title")
    val description = varchar("description")
}

interface Item : org.ktorm.entity.Entity<Item>{
    companion object : org.ktorm.entity.Entity.Factory<Item>()
    val id: Int
    var translationKey: String

}

interface MinedBlockReward : org.ktorm.entity.Entity<MinedBlockReward>{
    companion object : org.ktorm.entity.Entity.Factory<MinedBlockReward>()
    val id: Int
    var amount: Float
    var item: Item
}

object Items : Table<Item>("item"){
    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}

object MinedBlockRewards : Table<MinedBlockReward>("mined_block_reward"){
    val id = int("id").primaryKey().bindTo { it.id }
    val amount = float("amount").bindTo { it.amount }
    val itemId = int("item_id").references(Items){it.item}
}




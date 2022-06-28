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

object Item : Table<Nothing>("item"){
    val id = int("id").primaryKey()
    val translationKey = varchar("translation_key")
}

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
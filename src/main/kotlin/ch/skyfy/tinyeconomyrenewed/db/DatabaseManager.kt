package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.DataRetriever
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import net.fabricmc.loader.api.FabricLoader
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import kotlin.io.path.inputStream

val Database.players get() = this.sequenceOf(Players)
val Database.items get() = this.sequenceOf(Items)
val Database.entities get() = this.sequenceOf(Entities)
val Database.advancements get() = this.sequenceOf(Advancements)
val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

/**
 * This class connects to the database and creates the required tables and populate it with default data.
 *
 * All this takes time and until it is finished, players will not be able to connect.
 *
 * Also, during the installation, the console will display messages about the progress of the installation.
 * In order for the server administrator to be able to follow what is going on in the console correctly,
 * this class is instantiated in a coroutine right after the server is started @see TinyEconomyRenewedInitializer
 */
class DatabaseManager {

    val db: Database

    init {

        TinyEconomyRenewedMod.LOGGER.info("[Database Manager init block] > current thread name ${Thread.currentThread().name}")

        createDatabase()

        db = Database.connect(
            url = "jdbc:mariadb://localhost:3308/TinyEconomyRenewed",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )

        initDatabase()
    }

    @Suppress("SqlNoDataSourceInspection")
    private fun createDatabase(){
        val database = Database.connect(
            "jdbc:mariadb://localhost:3308",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )
        database.useConnection { conn ->
            val sql = "create database if not exists `TinyEconomyRenewed`;"
            conn.prepareStatement(sql).use { statement ->
               statement.executeQuery()
            }
        }
    }

    private fun initDatabase() {
        TinyEconomyRenewedMod.LOGGER.info("Initializing database with init.sql script \uD83D\uDCC3")

        val stream = FabricLoader.getInstance().getModContainer(TinyEconomyRenewedMod.MOD_ID).get().findPath("assets/tinyeconomyrenewed/sql/init.sql").get().inputStream()
        db.useConnection { connection ->
            connection.createStatement().use { statement ->
                stream.bufferedReader().use { reader ->
                    for (sql in reader.readText().split(';'))
                        if (sql.any { it.isLetterOrDigit() })
                            statement.executeUpdate(sql)
                }
            }
        }

        populateDatabase()
    }

    private fun populateDatabase() {
        TinyEconomyRenewedMod.LOGGER.info("Populating database \uD83D\uDE8C")

        // Iterate through all minecraft item identifier
        for (itemTranslationKey in DataRetriever.items) {
            var item = db.items.find { it.translationKey like itemTranslationKey }
            if (item == null) { // If item is not already in database, we create a new one and add it to the database
                item = Item { translationKey = itemTranslationKey }
                db.items.add(item)
            }
            if (DataRetriever.blocks.contains(itemTranslationKey)) { // Now, if the current itemTranslationKey is also a block, we repeat the same process, but for minedBlockReward table
                val minedBlockReward = db.minedBlockRewards.find { it.itemId eq item.id }
                if (minedBlockReward == null) {
                    db.minedBlockRewards.add(MinedBlockReward {
                        amount = 0f
                        this.item = item
                    })
                }
            }
        }

        // Iterate through all minecraft entity identifier
        for (entityTranslationKey in DataRetriever.entities) {
            var entity = db.entities.find { it.translationKey like entityTranslationKey }
            if (entity == null) { // If Entity is not already in database, we create a new one and add it to the database
                entity = Entity { translationKey = entityTranslationKey }
                db.entities.add(entity)
            }
            val entityKilledReward = db.entityKilledRewards.find { it.entity.id eq entity.id }
            if (entityKilledReward == null) {
                db.entityKilledRewards.add(EntityKilledReward {
                    amount = 0f
                    this.entity = entity
                })
            }
        }

        // Iterate through all minecraft advancement
        for (advancementObj in DataRetriever.advancements) {
            var advancement = db.advancements.find { it.identifier like advancementObj.advancementId }
            if (advancement == null) { // If Advancement is not already in database, we create a new one and add it to the database
                advancement = Advancement {
                    identifier = advancementObj.advancementId
                    frame = advancementObj.advancementFrame
                    title = advancementObj.advancementTitle
                    description = advancementObj.advancementDescription
                }
                db.advancements.add(advancement)
            }
            val advancementReward = db.advancementRewards.find { it.advancement.id eq advancement.id }
            if (advancementReward == null) {
                db.advancementRewards.add(AdvancementReward {
                    amount = 0f
                    this.advancement = advancement
                })
            }
        }
    }

}
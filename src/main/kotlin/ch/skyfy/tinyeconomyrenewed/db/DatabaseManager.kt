package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.config.Configs
import net.fabricmc.loader.api.FabricLoader
import net.silkmc.silk.game.cooldown.Cooldown
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update
import kotlin.io.path.inputStream

val Database.players get() = this.sequenceOf(Players)
val Database.items get() = this.sequenceOf(Items)
val Database.blocks get() = this.sequenceOf(Blocks)
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
class DatabaseManager(private val retrievedData: TinyEconomyRenewedInitializer.RetrievedData) {

    val db: Database

//    val cooldown: Cooldown = Cooldown()

    init {

        TinyEconomyRenewedMod.LOGGER.info("[Database Manager init block] > current thread name ${Thread.currentThread().name}")

        createDatabase() // First we have to create the new database
        val (url, user, password) = Configs.DB_CONFIG.data
        db = Database.connect("$url/TinyEconomyRenewed", "org.mariadb.jdbc.Driver", user, password)
        initDatabase() // Then create tables and populate it with data

//        cooldown.withCooldown()
    }

    @Suppress("SqlNoDataSourceInspection", "SqlDialectInspection")
    private fun createDatabase() {
        val (url, user, password) = Configs.DB_CONFIG.data
        val database = Database.connect(url, "org.mariadb.jdbc.Driver", user, password)
        database.useConnection { conn ->
            val sql = "create database if not exists `TinyEconomyRenewed`;"
            conn.prepareStatement(sql).use { statement -> statement.executeQuery() }
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
        for (itemTranslationKey in retrievedData.items) {
            var item = db.items.find { it.translationKey like itemTranslationKey }
            if (item == null) { // If item is not already in database, we create a new one and add it to the database
                item = Item { translationKey = itemTranslationKey }
                db.items.add(item)
            }
//            if (retrievedData.blocks.contains(itemTranslationKey)) { // Now, if the current itemTranslationKey is also a block, we repeat the same process, but for minedBlockReward table
//                val minedBlockReward = db.minedBlockRewards.find { it.itemId eq item.id }
//                val amountFromConfig = Configs.MINED_BLOCK_REWARD_CONFIG.data.map[itemTranslationKey]!!
//                if (minedBlockReward == null) {
//                    db.minedBlockRewards.add(MinedBlockReward {
//                        amount = amountFromConfig
//                        this.item = item
//                    })
//                } else {
//                    if (minedBlockReward.amount != amountFromConfig) minedBlockReward.amount = amountFromConfig
//                    db.minedBlockRewards.update(minedBlockReward)
//                }
//            }
        }

        for (blockTranslationKey in retrievedData.blocks) {
            var block = db.blocks.find { it.translationKey like blockTranslationKey }
            if (block == null) { // If item is not already in database, we create a new one and add it to the database
                block = Block { translationKey = blockTranslationKey }
                db.blocks.add(block)
            }

            if (retrievedData.blocks.contains(blockTranslationKey)) { // Now, if the current itemTranslationKey is also a block, we repeat the same process, but for minedBlockReward table
                val minedBlockReward = db.minedBlockRewards.find { it.blockId eq block.id }
                val amountFromConfig = Configs.MINED_BLOCK_REWARD_CONFIG.data.map[blockTranslationKey]!!
                if (minedBlockReward == null) {
                    db.minedBlockRewards.add(MinedBlockReward {
                        amount = amountFromConfig
                        this.block = block
                    })
                } else {
                    if (minedBlockReward.amount != amountFromConfig) minedBlockReward.amount = amountFromConfig
                    db.minedBlockRewards.update(minedBlockReward)
                }
            }
        }

        // Iterate through all minecraft entity identifier
        for (entityTranslationKey in retrievedData.entities) {
            var entity = db.entities.find { it.translationKey like entityTranslationKey }
            if (entity == null) { // If Entity is not already in database, we create a new one and add it to the database
                entity = Entity { translationKey = entityTranslationKey }
                db.entities.add(entity)
            }
            val entityKilledReward = db.entityKilledRewards.find { it.entity.id eq entity.id }
            val amountFromConfig = Configs.ENTITY_KILLED_REWARD_CONFIG.data.map[entityTranslationKey]!!
            if (entityKilledReward == null) {
                db.entityKilledRewards.add(EntityKilledReward {
                    amount = amountFromConfig
                    this.entity = entity
                })
            } else {
                if (entityKilledReward.amount != amountFromConfig) entityKilledReward.amount = amountFromConfig
                db.entityKilledRewards.update(entityKilledReward)
            }
        }

        // Iterate through all minecraft advancement
        for (advancementObj in retrievedData.advancements) {
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
            val amountFromConfig = Configs.ADVANCEMENT_REWARD_CONFIG.data.map[advancementObj.advancementId]!!
            if (advancementReward == null) {
                db.advancementRewards.add(AdvancementReward {
                    amount = amountFromConfig
                    this.advancement = advancement
                })
            } else {
                if (advancementReward.amount != amountFromConfig) advancementReward.amount = amountFromConfig
                db.advancementRewards.update(advancementReward)
            }
        }
    }

}
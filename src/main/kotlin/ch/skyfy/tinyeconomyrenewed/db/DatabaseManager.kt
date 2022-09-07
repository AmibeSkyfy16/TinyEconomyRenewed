package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.config.Configs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.loader.api.FabricLoader
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.*
import kotlin.io.path.inputStream
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.seconds

private val Database.players get() = this.sequenceOf(Players)
private val Database.items get() = this.sequenceOf(Items)
private val Database.blocks get() = this.sequenceOf(Blocks)
private val Database.entities get() = this.sequenceOf(Entities)
private val Database.advancements get() = this.sequenceOf(Advancements)
private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

/**
 * This class connects to the database and creates the required tables and populate it with default data.
 *
 * All this takes time and until it is finished, players will not be able to connect.
 *
 * Also, during the installation, the console will display messages about the progress of the installation.
 * In order for the server administrator to be able to follow what is going on in the console correctly,
 * this class is instantiated in a custom thread right after the server is started in [TinyEconomyRenewedInitializer]
 */
@Suppress("unused")
class DatabaseManager(private val retrievedData: TinyEconomyRenewedInitializer.RetrievedData) {

    private val db: Database

    private val cachePlayersMutex: Mutex
    private val cacheMinedBlockRewardsMutex: Mutex
    private val cacheEntityKilledRewardsMutex: Mutex
    private val cacheAdvancementRewardsMutex: Mutex

    val cachePlayers: MutableList<Player>
    val cacheMinedBlockRewards: List<MinedBlockReward>
    val cacheEntityKilledRewards: List<EntityKilledReward>
    val cacheAdvancementRewards: List<AdvancementReward>

    inline fun <reified T> getValue(crossinline block: () -> T): T = runBlocking(LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT) { block.invoke() }

    fun addPlayer(player: Player) = db.players.add(player)

    fun updatePlayers(player: Player) = db.players.update(player)

    suspend fun modifyPlayers(block: MutableList<Player>.() -> Unit) {
        cachePlayersMutex.withLock { cachePlayers.block() }
    }
    suspend fun modifyMinedBlockRewards(block: List<MinedBlockReward>.() -> Unit) {
        cacheMinedBlockRewardsMutex.withLock { cacheMinedBlockRewards.block() }
    }
    suspend fun modifyEntityKilledRewards(block: List<EntityKilledReward>.() -> Unit) {
        cacheEntityKilledRewardsMutex.withLock { cacheEntityKilledRewards.block() }
    }
    suspend fun modifyAdvancementRewards(block: List<AdvancementReward>.() -> Unit) {
        cacheAdvancementRewardsMutex.withLock { cacheAdvancementRewards.block() }
    }

    init {
        val (url, user, password) = Configs.DB_CONFIG.`data`
        createDatabase() // Create a new database called TinyEconomyRenewed (if it is not already exist)
        db = Database.connect("$url/TinyEconomyRenewed", "org.mariadb.jdbc.Driver", user, password) // Connect to it
        initDatabase() // Then create tables and populate it with data

        cachePlayersMutex = Mutex()
        cacheMinedBlockRewardsMutex = Mutex()
        cacheEntityKilledRewardsMutex = Mutex()
        cacheAdvancementRewardsMutex = Mutex()

        cachePlayers = db.players.toMutableList()
        cacheMinedBlockRewards = db.minedBlockRewards.toList()
        cacheEntityKilledRewards = db.entityKilledRewards.toList()
        cacheAdvancementRewards = db.advancementRewards.toList()

        /**
         * In order to optimize the queries to the database, we will retrieve the data once, then update it every 2 minutes.
         * Without this, if for example 5 players are mining, it will make 600 database requests per minute executed on the minecraft server thread,
         * which would cause lag. Here we only update every 2 minutes from a separate thread
         */
        infiniteMcCoroutineTask(sync = false, client = false, period = 30_000.milliseconds) {
            val job = launch {
                println("UPDATING DATABASE ${Thread.currentThread().name}")
                modifyPlayers { cachePlayers.forEach(db.players::update) }
//                cachePlayers.forEach(db.players::update)
            }
            while (true) {
                if (job.isCompleted || job.isCancelled) break
            }
        }

    }

    @Suppress("SqlNoDataSourceInspection", "SqlDialectInspection")
    private fun createDatabase() {
        val (url, user, password) = Configs.DB_CONFIG.`data`
        Database.connect(url, "org.mariadb.jdbc.Driver", user, password).useConnection { conn ->
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
                        if (sql.any { it.isLetterOrDigit() }) statement.executeUpdate(sql)
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
        }

        for (blockTranslationKey in retrievedData.blocks) {
            var block = db.blocks.find { it.translationKey like blockTranslationKey }
            if (block == null) { // If item is not already in database, we create a new one and add it to the database
                block = Block { translationKey = blockTranslationKey }
                db.blocks.add(block)
            }

            if (retrievedData.blocks.contains(blockTranslationKey)) { // Now, if the current itemTranslationKey is also a block, we repeat the same process, but for minedBlockReward table
                val minedBlockReward = db.minedBlockRewards.find { it.blockId eq block.id }
                val amountFromConfig = Configs.MINED_BLOCK_REWARD_CONFIG.`data`.map[blockTranslationKey]!!
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
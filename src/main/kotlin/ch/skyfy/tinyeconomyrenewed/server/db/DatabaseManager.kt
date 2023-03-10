package ch.skyfy.tinyeconomyrenewed.server.db

import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer
import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT
import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.*
import kotlin.io.path.inputStream
import kotlin.time.Duration.Companion.minutes

private val Database.players get() = this.sequenceOf(Players)
private val Database.items get() = this.sequenceOf(Items)
private val Database.blocks get() = this.sequenceOf(Blocks)
private val Database.entities get() = this.sequenceOf(Entities)
private val Database.advancements get() = this.sequenceOf(Advancements)
private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)
private val Database.blackListedPlacedBlocks get() = this.sequenceOf(BlackListedPlacedBlocks)

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
    private val cacheBlackListedPlacedBlocksMutex: Mutex

    val cachePlayers: MutableList<Player>
    val cacheMinedBlockRewards: List<MinedBlockReward>
    val cacheEntityKilledRewards: List<EntityKilledReward>
    val cacheAdvancementRewards: List<AdvancementReward>
    val cacheBlackListedPlacedBlocks: MutableList<BlackListedPlacedBlock>

    inline fun <reified T> getValue(crossinline block: () -> T): T = runBlocking(LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT) { block.invoke() }

    fun getAllPlayersAsMutableList() = db.players.toMutableList()

    fun addPlayer(player: Player) = db.players.add(player)

    fun updatePlayers(player: Player) = db.players.update(player)

    fun lockPlayers(block: MutableList<Player>.(MutableList<Player>) -> Unit) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            cachePlayersMutex.withLock { cachePlayers.block(cachePlayers) }
        }
    }

    fun lockMinedBlockRewards(block: List<MinedBlockReward>.(List<MinedBlockReward>) -> Unit) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            cacheMinedBlockRewardsMutex.withLock { cacheMinedBlockRewards.block(cacheMinedBlockRewards) }
        }
    }

    fun lockEntityKilledRewards(block: List<EntityKilledReward>.(List<EntityKilledReward>) -> Unit) {
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            cacheEntityKilledRewardsMutex.withLock { cacheEntityKilledRewards.block(cacheEntityKilledRewards) }
        }
    }

    private suspend fun lockAdvancementRewards(block: List<AdvancementReward>.() -> Unit) {
        cacheAdvancementRewardsMutex.withLock { cacheAdvancementRewards.block() }
    }

    private suspend fun lockBlackListedPlacedBlocks(block: List<BlackListedPlacedBlock>.(MutableList<BlackListedPlacedBlock>) -> Unit) {
        cacheBlackListedPlacedBlocksMutex.withLock { cacheBlackListedPlacedBlocks.block(cacheBlackListedPlacedBlocks) }
    }

    init {
        val (url, user, password) = Configs.DB_CONFIG.serializableData
        createDatabase(url, user, password) // Create a new database called TinyEconomyRenewed (if it is not already exist)
        db = Database.connect("$url/TinyEconomyRenewed", "org.mariadb.jdbc.Driver", user, password) // Connect to it
        initDatabase() // Then create tables and populate it with data

        registerEvents()

        cachePlayersMutex = Mutex()
        cacheMinedBlockRewardsMutex = Mutex()
        cacheEntityKilledRewardsMutex = Mutex()
        cacheAdvancementRewardsMutex = Mutex()
        cacheBlackListedPlacedBlocksMutex = Mutex()

        cachePlayers = db.players.toMutableList()
        cacheMinedBlockRewards = db.minedBlockRewards.toList()
        cacheEntityKilledRewards = db.entityKilledRewards.toList()
        cacheAdvancementRewards = db.advancementRewards.toList()
        cacheBlackListedPlacedBlocks = db.blackListedPlacedBlocks.toMutableList()

        /**
         * In order to optimize the queries to the database, we will retrieve the data once, then update it every 2 minutes.
         * Without this, if for example 5 players are mining, it will make 600 database requests per minute executed on the minecraft server thread,
         * which would cause lag. Here we only update every 2 minutes from a separate thread.
         *
         * If 10 player are mining with efficiency 5 and haste 2. 1000 * 10 = 10_000 database requests per minute will be executed on the minecraft server thread,
         * So instead of making so many frequent request to the database, we use cacheList of our database entity, and we update it to the database only every 2 minutes
         */
        infiniteMcCoroutineTask(sync = false, client = false, period = 2.minutes) {
            updateDatabase()
//            val job = launch {
//                modifyPlayers { cachePlayers -> cachePlayers.forEach(db.players::update) }
//                modifyBlackListedPlacedBlocks { cacheBlackListedPlacedBlocks ->
//                    cacheBlackListedPlacedBlocks.forEach {
//                        if (db.blackListedPlacedBlocks.find { c -> c.x.eq(it.x).and(c.y.eq(it.y)).and(c.z.eq(it.z)) } == null)
//                            db.blackListedPlacedBlocks.add(it)
//                    }
//                }
//                modifyMinedBlockRewards {
//                    cacheMinedBlockRewards.forEach(db.minedBlockRewards::update)
//                }
//            }
//            while (true) {
//                if (job.isCompleted || job.isCancelled) break
//            }
        }
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
    }

    private fun onServerStopped(minecraftServer: MinecraftServer) {
        TinyEconomyRenewedMod.LOGGER.info("Update cached data to the database")
        updateDatabase()
        TinyEconomyRenewedMod.LOGGER.info("Done, database updated !")
    }

    private fun updateDatabase() {
        val job = LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            delay(2000) // A test
            lockPlayers { cachePlayers -> cachePlayers.forEach(db.players::update) }
            lockBlackListedPlacedBlocks { cacheBlackListedPlacedBlocks ->
                cacheBlackListedPlacedBlocks.forEach {
                    if (db.blackListedPlacedBlocks.find { c -> c.x.eq(it.x).and(c.y.eq(it.y)).and(c.z.eq(it.z)) } == null)
                        db.blackListedPlacedBlocks.add(it)
                }
            }
            lockMinedBlockRewards {
                cacheMinedBlockRewards.forEach(db.minedBlockRewards::update)
            }
            lockEntityKilledRewards {
                cacheEntityKilledRewards.forEach(db.entityKilledRewards::update)
            }
        }
        while (true) {
            if (job.isCompleted || job.isCancelled) break
        }
    }


    @Suppress("SqlNoDataSourceInspection", "SqlDialectInspection")
    private fun createDatabase(url: String, user: String, password: String) {
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
                val minedBlockRewardData = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { it.translationKey == blockTranslationKey }
                if (minedBlockReward == null) {
                    db.minedBlockRewards.add(MinedBlockReward {
                        this.currentPrice = minedBlockRewardData.currentPrice
                        this.maximumMinedBlockPerMinute = minedBlockRewardData.maximumPerMinute
                        this.cryptoCurrencyName = minedBlockRewardData.cryptoCurrencyName
                        this.lastCryptoPrice = minedBlockRewardData.lastCryptoPrice
                        this.block = block
                    })
                } else {
                    // We have to update value is database if the user modify some value in the json config file
                    if (minedBlockReward.currentPrice != minedBlockRewardData.currentPrice) minedBlockReward.currentPrice = minedBlockRewardData.currentPrice
                    if (minedBlockReward.maximumMinedBlockPerMinute != minedBlockRewardData.maximumPerMinute) minedBlockReward.maximumMinedBlockPerMinute = minedBlockRewardData.maximumPerMinute
                    if (minedBlockReward.cryptoCurrencyName != minedBlockRewardData.cryptoCurrencyName) {
                        minedBlockReward.cryptoCurrencyName = minedBlockRewardData.cryptoCurrencyName
                        minedBlockReward.lastCryptoPrice = -1.0 // reset
                    }
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
            val entityKilledRewardData = Configs.ENTITY_KILLED_REWARD_CONFIG.serializableData.list.first { it.translationKey == entityTranslationKey }

            if (entityKilledReward == null) {
                db.entityKilledRewards.add(EntityKilledReward {
                    this.currentPrice = entityKilledRewardData.currentPrice
                    this.maximumEntityKilledPerMinute = entityKilledRewardData.maximumPerMinute
                    this.cryptoCurrencyName = entityKilledRewardData.cryptoCurrencyName
                    this.lastCryptoPrice = entityKilledRewardData.lastCryptoPrice
                    this.entity = entity
                })
            } else {
                if (entityKilledReward.currentPrice != entityKilledRewardData.currentPrice) entityKilledReward.currentPrice = entityKilledRewardData.currentPrice
                if (entityKilledReward.maximumEntityKilledPerMinute != entityKilledRewardData.maximumPerMinute) entityKilledReward.maximumEntityKilledPerMinute = entityKilledRewardData.maximumPerMinute
                if (entityKilledReward.cryptoCurrencyName != entityKilledRewardData.cryptoCurrencyName) {
                    entityKilledReward.cryptoCurrencyName = entityKilledRewardData.cryptoCurrencyName
                    entityKilledReward.lastCryptoPrice = -1.0 // reset
                }
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
            val amountFromConfig = Configs.ADVANCEMENT_REWARD_CONFIG.serializableData.map[advancementObj.advancementId]!!
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
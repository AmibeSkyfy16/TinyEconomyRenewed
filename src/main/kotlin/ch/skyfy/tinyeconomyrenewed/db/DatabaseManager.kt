package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.DataRetriever
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import kotlin.io.path.inputStream

/**
 * This class first handles the installation of the database.
 * It will copy the required files for the database server as a zip file,
 * and then it will extract the data.
 * After that it starts the database server (MariaDB), connects to it and creates the required tables and peoples with default data.
 *
 * All this takes time and until it is finished, players will not be able to connect.
 *
 * Also, during the installation, the console will display messages about the progress of the installation.
 * In order for the server administrator to be able to follow what is going on in the console correctly,
 * this object is called a very first time in a thread right after the server is started
 */
class DatabaseManager(private val dataRetriever: DataRetriever) : Runnable {

    private val embeddedDatabase: EmbeddedDatabase = EmbeddedDatabase()

    val database: Database

    private val Database.items get() = this.sequenceOf(Items)
    private val Database.entities get() = this.sequenceOf(Entities)
    private val Database.advancements get() = this.sequenceOf(Advancements)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {

        embeddedDatabase.startMariaDBServer()
        embeddedDatabase.db.createDB("TinyEconomyRenewed")
        database = Database.connect(
            "jdbc:mariadb://localhost:3307/TinyEconomyRenewed",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )

        initDatabase()
        registerEvents()

        TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect")
    }

    private fun initDatabase() {
        TinyEconomyRenewedMod.LOGGER.info("Initializing database with init.sql script")

        val stream = FabricLoader.getInstance().getModContainer(TinyEconomyRenewedMod.MOD_ID).get().findPath("assets/tinyeconomyrenewed/sql/init.sql").get().inputStream()
        database.useConnection { connection ->
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
        for (itemTranslationKey in dataRetriever.items) {
            var item = database.items.find { it.translationKey like itemTranslationKey }
            if (item == null) { // If item is not already in database, we create a new one and add it to the database
                item = Item { translationKey = itemTranslationKey }
                database.items.add(item)
            }
            if (dataRetriever.blocks.contains(itemTranslationKey)) { // Now, if the current itemTranslationKey is also a block, we repeat the same process, but for minedBlockReward table
                val minedBlockReward = database.minedBlockRewards.find { it.itemId eq item.id }
                if (minedBlockReward == null) {
                    database.minedBlockRewards.add(MinedBlockReward {
                        amount = 0f
                        this.item = item
                    })
                }
            }
        }

        // Iterate through all minecraft entity identifier
        for (entityTranslationKey in dataRetriever.entities) {
            var entity = database.entities.find { it.translationKey like entityTranslationKey }
            if (entity == null) { // If Entity is not already in database, we create a new one and add it to the database
                entity = Entity { translationKey = entityTranslationKey }
                database.entities.add(entity)
            }
            val entityKilledReward = database.entityKilledRewards.find { it.entity.id eq entity.id }
            if (entityKilledReward == null) {
                database.entityKilledRewards.add(EntityKilledReward {
                    amount = 0f
                    this.entity = entity
                })
            }
        }

        // Iterate through all minecraft advancement
        for (advancementObj in dataRetriever.advancements) {
            var advancement = database.advancements.find { it.identifier like advancementObj.advancementId }
            if (advancement == null) { // If Advancement is not already in database, we create a new one and add it to the database
                advancement = Advancement {
                    identifier = advancementObj.advancementId
                    frame = advancementObj.advancementFrame
                    title = advancementObj.advancementTitle
                    description = advancementObj.advancementDescription
                }
                database.advancements.add(advancement)
            }
            val advancementReward = database.advancementRewards.find { it.advancement.id eq advancement.id }
            if (advancementReward == null) {
                database.advancementRewards.add(AdvancementReward {
                    amount = 0f
                    this.advancement = advancement
                })
            }
        }
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register {
            embeddedDatabase.db.stop()
        }
    }

    override fun run() {}

}
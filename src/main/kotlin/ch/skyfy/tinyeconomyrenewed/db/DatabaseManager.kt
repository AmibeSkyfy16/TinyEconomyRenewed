package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.DataRetriever
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import io.netty.util.concurrent.CompleteFuture
import kotlinx.coroutines.GlobalScope.coroutineContext
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
 * This object first handles the installation of the database.
 * It will copy the required files for the database server as a zip file,
 * and then it will extract the data.
 * After that it starts the database server (MariaDB), connects to it and creates the required tables and peoples with default data.
 *
 * All this takes time and until it is finished, players will not be able to connect.
 *
 * Also, during the installation, the console will display messages about the progress of the installation.
 * In order for the server administrator to be able to follow what is going on in the console correctly,
 * this object is called a very first time in a thread right after the server is started @see TinyEconomyRenewedInitializer
 */
object DatabaseManager : Runnable {

    val db: Database

    private val Database.items get() = this.sequenceOf(Items)
    private val Database.entities get() = this.sequenceOf(Entities)
    private val Database.advancements get() = this.sequenceOf(Advancements)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {

        println("current thread name: ${Thread.currentThread().name}")

        // In order to make sure that the DatabaseManager object is not used somewhere else in the code before it is first created in the TinyEconomyRenewedInitializer class
//        if(!Thread.currentThread().name.contains(TinyEconomyRenewedInitializer.initializerThreadName)){
//            throw TinyEconomyModException("DatabaseManager was used before it was supposed to be used for the first time")
//        }

        // TODO create maven repo for project MariaDVServerFabricMC
//        EmbeddedDatabaseAPI.db.createDB("TinyEconomyRenewed")

        db = Database.connect(
            "jdbc:mariadb://localhost:3308/TinyEconomyRenewed",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )

        initDatabase()

        TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect")
    }

    private fun initDatabase() {
        TinyEconomyRenewedMod.LOGGER.info("Initializing database with init.sql script")

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

    override fun run() {}

}
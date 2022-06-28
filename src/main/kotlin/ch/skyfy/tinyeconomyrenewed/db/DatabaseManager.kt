package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.DataRetriever
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.mysql.insertOrUpdate
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

    private val database: Database

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

        for (itemTranslationKey in dataRetriever.items) {

            try {

                // NOT WORK
                val idd = database.from(Items).selectDistinct(Items.id, Items.translationKey).where { Items.translationKey like itemTranslationKey }.rowSet.getInt(0)
                println("ID: {$idd}")

            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }

            database.insertOrUpdate(Items) {
                set(it.translationKey, itemTranslationKey)
                onDuplicateKey {
                    set(it.translationKey, itemTranslationKey)
                }
            }

            val itemId = database.from(Items).select().where { Items.translationKey like itemTranslationKey }.limit(0,1).rowSet[Items.id]
            println("item id: $itemId")

            if(dataRetriever.blocks.contains(itemTranslationKey)){
                database.insertOrUpdate(MinedBlockRewards){
                    set(it.amount, 0f)
                    set(it.itemId, itemId)
                    onDuplicateKey {
                        set(it.itemId, itemId)
                    }
                }
            }
        }


        for (entity in dataRetriever.entities) {
            database.insertOrUpdate(Entity) {
                set(it.translationKey, entity)
                onDuplicateKey {
                    set(it.translationKey, entity)
                }
            }
        }

        for (advancement in dataRetriever.advancements) {
            database.insertOrUpdate(Advancement) {
                set(it.identifier, advancement.advancementId)
                set(it.frame, advancement.advancementFrame)
                set(it.title, advancement.advancementTitle)
                set(it.description, advancement.advancementDescription)
                onDuplicateKey {
                    set(it.identifier, advancement.advancementId)
                }
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
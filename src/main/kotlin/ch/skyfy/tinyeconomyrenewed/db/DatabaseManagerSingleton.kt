package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.utils.ModUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.ktorm.database.Database
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
 * this object is called a very first time in a thread right after the server is started
 */
object DatabaseManagerSingleton : Runnable {

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

        ModUtils.populateDatabase(database)
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register {
            embeddedDatabase.db.stop()
        }
    }

    override fun run() {}

}
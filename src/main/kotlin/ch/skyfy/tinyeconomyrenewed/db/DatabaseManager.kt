package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.utils.ModUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.inputStream

class DatabaseManager {

    private val embeddedDatabase: EmbeddedDatabase = EmbeddedDatabase()

    private var database: org.ktorm.database.Database

    init {
        embeddedDatabase.startMariaDBServer()

        embeddedDatabase.db.createDB("TinyEconomyRenewed")

        database = org.ktorm.database.Database.connect(
            "jdbc:mariadb://localhost:3307/TinyEconomyRenewed",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = ""
        )

        initDb()

        ModUtils.populateDatabase(database)

        registerEvents()
    }

    private fun initDb() {
        val stream = FabricLoader.getInstance().getModContainer(TinyEconomyRenewedMod.MOD_ID).get()
            .findPath("assets/tinyeconomyrenewed/sql/init.sql").get().inputStream()
        database.useConnection { connection ->
            connection.createStatement().use { statement ->
                stream.bufferedReader().use { reader ->
                    for (sql in reader.readText().split(';'))
                        if (sql.any { it.isLetterOrDigit() })
                            statement.executeUpdate(sql)
                }
            }
        }
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register {
            embeddedDatabase.db.stop()
        }
    }

}
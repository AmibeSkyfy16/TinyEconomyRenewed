package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.MOD_ID
import ch.vorburger.exec.ManagedProcessException
import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class EmbeddedDatabase {

    private val databaseFolderName: String = "database"
    private val databaseFolder: Path = TinyEconomyRenewedMod.CONFIG_DIRECTORY.resolve(databaseFolderName)

    private val mariadbFolderName: String = "mariadb-10.8.3-winx64"
    private val mariadbFolder: Path = databaseFolder.resolve(mariadbFolderName)

    val db: DB

    init {

        if(!databaseFolder.exists())databaseFolder.toFile().mkdir()

        installMariaDB()

        val builder = DBConfigurationBuilder.newBuilder()

        builder.isUnpackingFromClasspath = false
        builder.baseDir = mariadbFolder.toAbsolutePath().toString()

        builder.port = 3307
        builder.dataDir = databaseFolder.resolve("data").toAbsolutePath().toString()

        db = DB.newEmbeddedDB(builder.build())
    }

    /**
     * If this is the first time the minecraft server is started with the mod. Then you have to install the files for the mariadb server
     */
    private fun installMariaDB() {
        val dest: Path = TinyEconomyRenewedMod.CONFIG_DIRECTORY.resolve("$mariadbFolderName.zip")

        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/tinyeconomyrenewed/$mariadbFolderName.zip")
        if (!dest.exists() && !mariadbFolder.exists())
            Files.copy(t3.get(), dest, StandardCopyOption.REPLACE_EXISTING)

        if (dest.exists() && !mariadbFolder.exists()) {
            ZipFile(dest.toFile()).extractAll(mariadbFolder.toAbsolutePath().toString())
            dest.deleteIfExists()
        }
    }

    fun startMariaDBServer() {
        try {
            db.start()
        } catch (e: ManagedProcessException) {
            e.printStackTrace()
            println("already started")
        }
    }


}
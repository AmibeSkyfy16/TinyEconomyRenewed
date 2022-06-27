package ch.skyfy.tinyeconomyrenewed.db

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.MOD_ID
import ch.vorburger.exec.ManagedProcessException
import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.nio.file.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class EmbeddedDatabase {

    private val mariadbFolder: Path = TinyEconomyRenewedMod.CONFIG_DIRECTORY.resolve("mariadb-10.8.3-winx64")

    val db: DB

    init {

        installMariaDB()

        val builder = DBConfigurationBuilder.newBuilder()

        builder.isUnpackingFromClasspath = false
        builder.baseDir = mariadbFolder.toAbsolutePath().toString()

        builder.port = 3307
        builder.dataDir = mariadbFolder.resolve("data").toAbsolutePath().toString()

        db = DB.newEmbeddedDB(builder.build())
    }

    private fun installMariaDB() {
        val dest: Path = TinyEconomyRenewedMod.CONFIG_DIRECTORY.resolve("mariadb-10.8.3-winx64.zip")

        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/tinyeconomyrenewed/mariadb-10.8.3-winx64.zip")
        if (!dest.exists() && !mariadbFolder.exists())
            Files.copy(t3.get(), dest, StandardCopyOption.REPLACE_EXISTING)

        if (dest.exists() && !mariadbFolder.exists()) {
            ZipFile(dest.toFile()).extractAll(mariadbFolder.toAbsolutePath().toString())
            dest.deleteIfExists()
        }
    }

    private fun exportResource(resourceName: String, dest: Path) {

        // seems to work
//        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get()
//            .findPath("assets/tinyeconomy/mariadb-10.8.3-winx64.zip")
//        if (!dest.exists())
//            Files.copy(t3.get(), dest, StandardCopyOption.REPLACE_EXISTING)

        //  WORK NICE !!!
//        val optFile = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/tinyeconomy/mariadb-10.8.3-winx64.zip")
//        val file = optFile.get().inputStream()
//
//        var bis: InputStream? = null
//        var bos: OutputStream? = null
//        try {
//            bis = BufferedInputStream(file)
//            bos = BufferedOutputStream(FileOutputStream(dest.toFile()))
//
//            val buffer = ByteArray(1024)
//            var lengthRead: Int
//            while (bis.read(buffer).also { lengthRead = it } > 0) {
//                bos.write(buffer, 0, lengthRead)
//                bos.flush()
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        } finally {
//            bis?.close()
//            bos?.close()
//        }


        // Not work
//        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/tinyeconomy/mariadb-10.8.3-winx64.zip")
//        Files.copy(t3.get().toFile().inputStream(), dest, StandardCopyOption.REPLACE_EXISTING)

        // Not work
//        val optFile = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/tinyeconomy/mariadb-10.8.3-winx64.zip")
//        val file = optFile.get().toFile()
//
//        var bis: InputStream? = null
//        var bos: OutputStream? = null
//        try {
//            bis = BufferedInputStream(FileInputStream(file))
//            bos = BufferedOutputStream(FileOutputStream(dest.toFile()))
//
//            val buffer = ByteArray(1024)
//            var lengthRead: Int
//            while (bis.read(buffer).also { lengthRead = it } > 0) {
//                bos.write(buffer, 0, lengthRead)
//                bos.flush()
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        } finally {
//            bis?.close()
//            bos?.close()
//        }


    }

    fun isDatabaseRunning(): Boolean {

        return false
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
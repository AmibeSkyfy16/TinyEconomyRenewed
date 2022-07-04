package ch.skyfy.tinyeconomyrenewed


import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

class TinyEconomyRenewedMod : DedicatedServerModInitializer {

    companion object {
        const val MOD_ID: String = "tinyeconomyrenewed"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        val LOGGER: Logger = LogManager.getLogger(TinyEconomyRenewedMod::class.java)
    }

    init {
        DataRetriever // Get data like all minecraft items identifier, all advancements data (time to mine + id, etc., etc.)
        createConfigDir()
    }

    override fun onInitializeServer() {
        TinyEconomyRenewedInitializer(Dispatchers.IO)
    }

    private fun createConfigDir() {
        try {
            if(!CONFIG_DIRECTORY.exists()) CONFIG_DIRECTORY.createDirectory()
        } catch (e: java.lang.Exception) {
            LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
            throw TinyEconomyModException(e)
        }
    }

}
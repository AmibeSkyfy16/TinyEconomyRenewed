package ch.skyfy.tinyeconomyrenewed


import ch.skyfy.jsonconfig.JsonConfig
import ch.skyfy.tinyeconomyrenewed.config.Configs
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import ch.skyfy.tinyeconomyrenewed.utils.setupConfigDirectory
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
        setupConfigDirectory()
        JsonConfig.loadConfigs(arrayOf(Configs.javaClass))
        DataRetriever // Get data like all minecraft items identifier, all advancements data (time to mine + id, etc., etc.)
    }

    override fun onInitializeServer() {
        TinyEconomyRenewedInitializer()
    }

}
package ch.skyfy.tinyeconomyrenewed


import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerTakeItemsCallback
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.util.ActionResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
class TinyEconomyRenewedMod : DedicatedServerModInitializer {

    companion object {
        const val MOD_ID: String = "tinyeconomyrenewed"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        val LOGGER: Logger = LogManager.getLogger(TinyEconomyRenewedMod::class.java)
    }

    init {
        DataRetriever
        createConfigDir()
    }

    override fun onInitializeServer() {
        TinyEconomyRenewedInitializer(Dispatchers.Default)
    }

    private fun createConfigDir() {
        try {
            val file = CONFIG_DIRECTORY.toFile()
            if (!file.exists()) file.mkdir()
        } catch (e: java.lang.Exception) {
            LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
            throw TinyEconomyModException(e)
        }
    }

}
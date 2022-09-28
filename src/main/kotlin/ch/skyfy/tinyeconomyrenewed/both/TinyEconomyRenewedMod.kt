package ch.skyfy.tinyeconomyrenewed.both

import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedModServer
import ch.skyfy.tinyeconomyrenewed.server.utils.setupConfigDirectory
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

class TinyEconomyRenewedMod : ModInitializer {

    companion object {
        const val MOD_ID: String = "tinyeconomyrenewed"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        val PERSISTENT_DIRECTORY: Path = CONFIG_DIRECTORY.resolve("persistent")
        val LOGGER: Logger = LogManager.getLogger(TinyEconomyRenewedModServer::class.java)
    }

    init { setupConfigDirectory() }

    override fun onInitialize() { Registry.register(Registry.SOUND_EVENT, CustomSounds.DOGECOIN_ID, CustomSounds.DOGECOIN_EVENT) }


}
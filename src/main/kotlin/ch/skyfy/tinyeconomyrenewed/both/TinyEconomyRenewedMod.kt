package ch.skyfy.tinyeconomyrenewed.both

import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedModServer
import ch.skyfy.tinyeconomyrenewed.server.utils.setupConfigDirectory
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.IDN
import java.nio.file.Path

class TinyEconomyRenewedMod : ModInitializer {

    companion object {
        const val MOD_ID: String = "tinyeconomyrenewed"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        val PERSISTENT_DIRECTORY: Path = CONFIG_DIRECTORY.resolve("persistent")
        val LOGGER: Logger = LogManager.getLogger(TinyEconomyRenewedModServer::class.java)

        val CLIENT_HAS_THE_MOD: Identifier = Identifier(MOD_ID, "client_has_the_mod")
    }

    init { setupConfigDirectory() }

    override fun onInitialize() {
        Registry.register(Registries.SOUND_EVENT, CustomSounds.DOGECOIN_ID, CustomSounds.DOGECOIN_EVENT)
    }


}
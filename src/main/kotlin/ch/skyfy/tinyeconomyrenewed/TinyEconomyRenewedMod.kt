package ch.skyfy.tinyeconomyrenewed


import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
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

    private var isInitializationComplete = false

    private val dataRetriever: DataRetriever = DataRetriever()

    init {
        createConfigDir()
    }

    override fun onInitializeServer() {
        manageInitialization()
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun manageInitialization() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("TinyEconomyRenewed is being initialized")

            val deferred = GlobalScope.async { DatabaseManager(dataRetriever) }
            deferred.invokeOnCompletion { isInitializationComplete = true }
        }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
            if (!isInitializationComplete)
                serverPlayNetworkHandler.disconnect(
                    Text.literal("TinyEconomyRenewed has not finished to be initialized")
                        .setStyle(Style.EMPTY.withColor(Formatting.GOLD))
                )
        }
    }

}
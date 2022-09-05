package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.jsonconfiglib.ConfigManager
import ch.skyfy.tinyeconomyrenewed.config.Configs
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.logic.Game
import ch.skyfy.tinyeconomyrenewed.utils.setupConfigDirectory
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Language
import net.minecraft.util.registry.Registry
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.absolutePathString
import kotlin.io.path.inputStream

/**
 * This class is used to initialize the mod. This is where it all begins
 * It is initialized when the server is ready, when the first tick of the server is finished
 *
 * As the initialization takes time, the operations are performed in a separate thread.
 * While the initialization is in progress, players will not be able to connect
 */
class TinyEconomyRenewedInitializer {

    /**
     * This will be use in future for registered command because command must be
     * registered in [TinyEconomyRenewedMod.onInitializeServer] so we can pass this ref to every command object
     */
    private val optGameRef: AtomicReference<Optional<Game>> = AtomicReference(Optional.empty())

    private var isInitializationComplete = false

    private var firstEndServerTick: Boolean = false

    data class RetrievedData(val advancements: List<Advancement>, val items: List<String>, val blocks: List<String>, val entities: List<String>)
    data class Advancement(val advancementId: String, val advancementFrame: String, val advancementTitle: String, val advancementDescription: String)

    private val executor: ExecutorService = Executors.newFixedThreadPool(1) {
       val t = Thread(it)
        t.name = "DATABASE THREAD"
        t.isDaemon = true
        t
    }


    init {
        setupConfigDirectory()

        ServerTickEvents.END_SERVER_TICK.register { minecraftServer ->
            if (!firstEndServerTick){
                firstEndServerTick = true
                executor.execute {
                    TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed is being initialized \uD83D\uDE9A \uD83D\uDE9A \uD83D\uDE9A")

                    ConfigManager.loadConfigs(arrayOf(Configs.javaClass))

                    val retrievedData = retrieveDataAndPopulateDefaultConfiguration(minecraftServer)

                    // It's a temporary code that I need for populate my Excel file
                    val sb = java.lang.StringBuilder()
                    retrievedData.advancements.forEach { sb.append("\t\t\t$['map']['${it.advancementId}']\t1.0\t1.0\r\n") }
//                println(sb)

                    optGameRef.set(Optional.of(Game(DatabaseManager(retrievedData, executor), minecraftServer)))
                    isInitializationComplete = true
                    TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect \uD83D\uDC4C ✅")
                }
            }
        }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
            if (!isInitializationComplete)
                serverPlayNetworkHandler.disconnect(Text.literal("TinyEconomyRenewed has not finished to be initialized ⛔").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
        }
    }

    /**
     * This fun will retrieve data like all available advancement, all items, etc.
     * Once all data retrieved, it will update some JSON configuration files that have been created earlier with data and default value
     *
     * @param minecraftServer A [MinecraftServer] object used to retrieve some data
     * @return A [RetrievedData] object representing the data that have been retrieved
     */
    private fun retrieveDataAndPopulateDefaultConfiguration(minecraftServer: MinecraftServer): RetrievedData {
        val extractedFiles: MutableList<String> = mutableListOf()
        val retrievedData = RetrievedData(minecraftServer.advancementLoader.advancements.filter { !it.id.toString().contains("recipes") }.map {
            var title = ""
            var description = ""
            if (it.display != null) {
                val display = it.display!!

                title = display.title.string
                description = display.description.string

                // Check if it's a key or a real value
                if (!title.contains(" ") && title.contains(".") && title.chars().noneMatch { c -> c.toChar().isUpperCase() }) {
                    val mods = FabricLoader.getInstance().allMods
                    val filtered = mods.filter { mod -> mod.metadata.id == it.id.namespace }
                    if (filtered.isNotEmpty()) {
                        val fileLocation = "assets/${it.id.namespace}/lang/en_us.json"

                        if (!extractedFiles.contains(fileLocation)) {
                            ZipFile(filtered[0].origin.paths[0].toFile()).extractFile(fileLocation, TinyEconomyRenewedMod.CONFIG_DIRECTORY.resolve("extracted").absolutePathString())
                            extractedFiles.add(fileLocation)
                        }

                        try {
                            Language.load(Paths.get(TinyEconomyRenewedMod.CONFIG_DIRECTORY.toString(), "extracted/$fileLocation").inputStream()) { key, value ->
                                if (key == display.title.string) title = value
                                if (key == display.description.string) description = value
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            Advancement(it.id.toString(), it.display?.frame.toString(), title, description)
        }.sortedWith(compareBy { it.advancementId }),
            Registry.ITEM.ids.map { "item.${it.toTranslationKey()}" }.sortedWith(compareBy { it }),
            Registry.BLOCK.ids.map { "block.${it.toTranslationKey()}" }.sortedWith(compareBy { it }),
            Registry.ENTITY_TYPE.ids.map { "entity.${it.toTranslationKey()}"}.sortedWith(compareBy { it })
        )

        // Populating with default value
        retrievedData.advancements.forEach { Configs.ADVANCEMENT_REWARD_CONFIG.`data`.map.putIfAbsent(it.advancementId, 10f) }
        retrievedData.entities.forEach { Configs.ENTITY_KILLED_REWARD_CONFIG.`data`.map.putIfAbsent(it, 2f) }
        retrievedData.blocks.forEach { Configs.MINED_BLOCK_REWARD_CONFIG.`data`.map.putIfAbsent(it, 0.5f) }
        ConfigManager.save(Configs.ADVANCEMENT_REWARD_CONFIG)
        ConfigManager.save(Configs.ENTITY_KILLED_REWARD_CONFIG)
        ConfigManager.save(Configs.MINED_BLOCK_REWARD_CONFIG)

        return retrievedData
    }

}
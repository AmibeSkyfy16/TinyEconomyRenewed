package ch.skyfy.tinyeconomyrenewed.server

import ch.skyfy.jsonconfiglib.ConfigManager
import ch.skyfy.jsonconfiglib.updateIterable
import ch.skyfy.jsonconfiglib.updateMap
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.server.config.*
import ch.skyfy.tinyeconomyrenewed.server.config.Configs.ADVANCEMENT_REWARD_CONFIG
import ch.skyfy.tinyeconomyrenewed.server.config.Configs.ENTITY_KILLED_REWARD_CONFIG
import ch.skyfy.tinyeconomyrenewed.server.config.Configs.MINED_BLOCK_REWARD_CONFIG
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.logic.Game
import ch.skyfy.tinyeconomyrenewed.server.utils.setupConfigDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Language
import org.ktorm.dsl.min
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolutePathString
import kotlin.io.path.inputStream

/**
 * This class is used to initialize the mod. This is where it all begins
 * It is initialized when the server is ready, when the first tick of the server is finished
 *
 * As the initialization takes time, the operations are performed in a separate thread.
 * While the initialization is in progress, players will not be able to connect
 */
class TinyEconomyRenewedInitializer(private val optGameRef: AtomicReference<Optional<Game>>, override val coroutineContext: CoroutineContext = LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT) : CoroutineScope {

    companion object {
        val LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT = Dispatchers.Default
        val LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE = CoroutineScope(LEAVE_THE_MINECRAFT_THREAD_ALONE_CONTEXT)
    }


    private var isInitializationComplete = false

    private var firstEndServerTick: Boolean = false

    data class RetrievedData(val advancements: List<Advancement>, val items: List<String>, val blocks: List<String>, val entities: List<String>)
    data class Advancement(val advancementId: String, val advancementFrame: String, val advancementTitle: String, val advancementDescription: String)

    init {
        setupConfigDirectory()

        ServerTickEvents.END_SERVER_TICK.register { minecraftServer -> initialize(minecraftServer) }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
            if (!isInitializationComplete) serverPlayNetworkHandler.disconnect(Text.literal("TinyEconomyRenewed has not finished to be initialized ⛔").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
        }

    }

    private fun initialize(minecraftServer: MinecraftServer) {
        if (!firstEndServerTick) {
            firstEndServerTick = true

            launch {
                TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed is being initialized \uD83D\uDE9A \uD83D\uDE9A \uD83D\uDE9A")

                ConfigManager.loadConfigs(arrayOf(Configs.javaClass))

                val retrievedData = retrieveDataAndPopulateDefaultConfiguration(minecraftServer)

                // It's a temporary code that I need for populate my Excel file
                val sb = java.lang.StringBuilder()
                retrievedData.advancements.forEach { sb.append("\t\t\t$['map']['${it.advancementId}']\t1.0\t1.0\r\n") }
//                    println(sb)

                optGameRef.set(Optional.of(Game(DatabaseManager(retrievedData), minecraftServer)))
                isInitializationComplete = true
                TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect \uD83D\uDC4C ✅")
            }
        }
    }

    /**
     * This fun will retrieve data like all available advancement, all items, etc.
     * Once all data retrieved, it will update some json5 configuration files that have been created earlier with data and default value
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
                        val fileLocation = "assets/${it.id.namespace}/lang/en_us.json5"

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
            Registries.ITEM.ids.map { "item.${it.toTranslationKey()}" }.sortedWith(compareBy { it }),
            Registries.BLOCK.ids.map { "block.${it.toTranslationKey()}" }.sortedWith(compareBy { it }),
            Registries.ENTITY_TYPE.ids.map { "entity.${it.toTranslationKey()}" }.sortedWith(compareBy { it })
        )

        // Populating with default value
        retrievedData.advancements.forEach { advancement ->
            ADVANCEMENT_REWARD_CONFIG.serializableData.map.putIfAbsent(advancement.advancementId, 500.0)
//            ADVANCEMENT_REWARD_CONFIG.updateMap(AdvancementRewardConfig::map) { it.putIfAbsent(advancement.advancementId, 100.0) }
        }
        retrievedData.entities.forEach { translationKey ->
            if (ENTITY_KILLED_REWARD_CONFIG.serializableData.list.none { entityKilledReward -> entityKilledReward.translationKey == translationKey }) {
                ENTITY_KILLED_REWARD_CONFIG.serializableData.list.add(EntityKilledReward(translationKey, 0.8,100.0, "RENUSDT", -1.0))
            }
//            ENTITY_KILLED_REWARD_CONFIG.updateMap(EntityKilledRewardConfig::map) { it.putIfAbsent(translationKey, 2.0) }
        }
        retrievedData.blocks.forEach { translationKey ->
            if (MINED_BLOCK_REWARD_CONFIG.serializableData.list.none { minedBlockReward -> minedBlockReward.translationKey == translationKey }) {
                MINED_BLOCK_REWARD_CONFIG.serializableData.list.add(MinedBlockReward(translationKey, 0.5, 800.0, "RENUSDT", -1.0))
//                MINED_BLOCK_REWARD_CONFIG.updateIterable(MinedBlockRewardConfig::list) {
//                    // I test in survival with effi. 5 and haste 2, in one mn I got 1000 sand
//                    it.add(MinedBlockReward(translationKey, 100.0, 2.0, "RENUSDT", -1.0))
//                }
            }
//            MINED_BLOCK_REWARD_CONFIG.updateIterable(MinedBlockRewardConfig::list){
//                if(it.none { minedBlockReward -> minedBlockReward.translationKey == translationKey }){
//                    it.add(MinedBlockReward(translationKey, Average(50f, 100f, 50f), 150, 30))
//                }
//            }
//            MINED_BLOCK_REWARD_CONFIG.updateMap(MinedBlockRewardConfig::map) { it.putIfAbsent(translationKey, 0.5f) }
        }
        ConfigManager.save(ADVANCEMENT_REWARD_CONFIG)
        ConfigManager.save(ENTITY_KILLED_REWARD_CONFIG)
        ConfigManager.save(MINED_BLOCK_REWARD_CONFIG)
        return retrievedData
    }

}
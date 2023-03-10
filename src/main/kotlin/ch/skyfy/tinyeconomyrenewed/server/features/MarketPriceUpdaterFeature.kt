package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.jsonconfiglib.ConfigManager
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.config.CryptoBasedPriceReward
import ch.skyfy.tinyeconomyrenewed.server.config.EntityKilledRewardData
import ch.skyfy.tinyeconomyrenewed.server.config.MinedBlockRewardData
import ch.skyfy.tinyeconomyrenewed.server.db.*
import com.binance.connector.client.impl.WebsocketStreamClientImpl
import com.binance.connector.client.utils.WebSocketCallback
import com.jayway.jsonpath.JsonPath
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


/**
 * Here is the context. The money that players earn by breaking blocks is set by default at $50
 * for an average of 100 blocks broken per minute.
 *
 * If the player breaks a lot of blocks and his average of broken blocks per minute is higher
 * than the default average (100 blocks), he doesn't earn anything anymore.
 *
 * If this average is smaller than the default, then the price is determined by a simple rule of three.
 *
 * So it would be nice if the $50 price could change a little randomly, a little like cryptos do.
 *
 *
 */
class MarketPriceUpdaterFeature @OptIn(DelicateCoroutinesApi::class) constructor(
    private val databaseManager: DatabaseManager,
    override val coroutineContext: CoroutineContext = newSingleThreadContext("MyOwnThread")
) : CoroutineScope {

    private val wss: MutableList<WebsocketStreamClientImpl> = mutableListOf()

    init {

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            println("Closing all connections")
            wss.forEach {
                it.closeAllConnections()
            }
        }

        // Saving MINED_BLOCK_REWARD_CONFIG every minute
        infiniteMcCoroutineTask(sync = false, client = false, period = 20.seconds) {
//            ConfigManager.save(Configs.MINED_BLOCK_REWARD_CONFIG)
            // Locking the list while saving
            databaseManager.lockMinedBlockRewards {
                ConfigManager.save(Configs.MINED_BLOCK_REWARD_CONFIG)
            }
            databaseManager.lockEntityKilledRewards {
                ConfigManager.save(Configs.ENTITY_KILLED_REWARD_CONFIG)
            }
        }

//        val map = mutableMapOf<String, MutableList<ch.skyfy.tinyeconomyrenewed.server.db.MinedBlockReward>>()
//        databaseManager.cacheMinedBlockRewards.forEach {
//            if (!map.containsKey(it.cryptoCurrencyName)) map[it.cryptoCurrencyName] = mutableListOf(it)
//            else map[it.cryptoCurrencyName]!!.add(it)
//        }

        // TEST 1
        val map = mutableMapOf<String, MutableList<CryptoBasedPriceReward>>()
        databaseManager.cacheMinedBlockRewards.forEach {
            if (!map.containsKey(it.cryptoCurrencyName)) map[it.cryptoCurrencyName] = mutableListOf(it.toMinedBlockRewardData() as CryptoBasedPriceReward)
            else map[it.cryptoCurrencyName]!!.add(it.toMinedBlockRewardData() as CryptoBasedPriceReward)
        }
        databaseManager.cacheEntityKilledRewards.forEach {
            if (!map.containsKey(it.cryptoCurrencyName)) map[it.cryptoCurrencyName] = mutableListOf(it.toEntityKilledRewardData() as CryptoBasedPriceReward)
            else map[it.cryptoCurrencyName]!!.add(it.toEntityKilledRewardData() as CryptoBasedPriceReward)
        }
        // TEST 1

//        val map = mutableMapOf<String, MutableMap<KClass<*>, Any>>()
//        databaseManager.cacheMinedBlockRewards.forEach {
//            if (!map.containsKey(it.cryptoCurrencyName)) map[it.cryptoCurrencyName] = mutableMapOf(MinedBlockReward::class to it)
//            else map[it.cryptoCurrencyName]!![MinedBlockReward::class] = it
//        }
//        databaseManager.cacheEntityKilledRewards.forEach {
//            if (!map.containsKey(it.cryptoCurrencyName)) map[it.cryptoCurrencyName] = mutableMapOf(EntityKilledReward::class to it)
//            else map[it.cryptoCurrencyName]!![EntityKilledReward::class] = it
//        }


        map.forEach { (key, list) ->
            val ws = WebsocketStreamClientImpl()
            wss.add(ws)
            launch {
                ws.symbolTicker(key, WebSocketCallback { response ->
                    val cryptoPrice = JsonPath.read<String>(response, "$['b']").toDouble()

                    list.forEach { cryptoBasedPriceReward ->
                        if (cryptoBasedPriceReward.lastCryptoPrice != -1.0) {
                            if (cryptoPrice == cryptoBasedPriceReward.lastCryptoPrice) return@WebSocketCallback

                            var percentDiff = (cryptoPrice * 100.0 / cryptoBasedPriceReward.lastCryptoPrice) - 100.0 // Get the percent changed between lastPrice and currentPrice


                            if (percentDiff * 20 >= -90.0) {
                                percentDiff *= 20 // Increase a bit
                            }

                            val previousPrice = cryptoBasedPriceReward.currentPrice
                            val newPrice = (100.0 + (percentDiff)) * cryptoBasedPriceReward.currentPrice / 100.0

                            // ---------- Updating price in the database ---------- \\
                            if (cryptoBasedPriceReward is MinedBlockRewardData) {
                                cryptoBasedPriceReward.currentPrice = newPrice
//                                databaseManager.lockMinedBlockRewards { cacheMinedBlockRewards ->
//                                    cacheMinedBlockRewards.find { m -> m.block.translationKey == cryptoBasedPriceReward.translationKey }?.let { m ->
//                                        m.currentPrice = newPrice
//                                    }
//                                }
                            } else if (cryptoBasedPriceReward is EntityKilledRewardData) {
                                cryptoBasedPriceReward.currentPrice = newPrice
//                                databaseManager.lockEntityKilledRewards { cacheEntityKilledRewards ->
//                                    cacheEntityKilledRewards.find { e -> e.entity.translationKey == cryptoBasedPriceReward.translationKey }?.let { e ->
//                                        e.currentPrice = newPrice
//                                    }
//                                }
                            }
                            // ---------- Updating price in the database ---------- \\

                            if (cryptoBasedPriceReward.translationKey == "block.minecraft.sandstone") {
//                                println("                    \tprevious price was $previousPrice")
//                                println("basic percent: ${percentDiff / 20.0}\tnew percent: $percentDiff\tnew price: ${newPrice}")
                            }
                        }

                        // ---------- Updating in the database ---------- \\
                        var rewardData: CryptoBasedPriceReward? = null
                        if (cryptoBasedPriceReward is MinedBlockRewardData) {
                            rewardData = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { m -> m.translationKey == cryptoBasedPriceReward.translationKey }
                            databaseManager.lockMinedBlockRewards { cacheMinedBlockRewards ->
                                cacheMinedBlockRewards.find { m -> m.block.translationKey == cryptoBasedPriceReward.translationKey }?.let { m ->
                                    m.lastCryptoPrice = cryptoPrice
                                    m.currentPrice = cryptoBasedPriceReward.currentPrice
                                    cryptoBasedPriceReward.lastCryptoPrice = cryptoPrice
                                }
                            }
                        } else if (cryptoBasedPriceReward is EntityKilledRewardData) {
                            rewardData = Configs.ENTITY_KILLED_REWARD_CONFIG.serializableData.list.first { m -> m.translationKey == cryptoBasedPriceReward.translationKey }
                            databaseManager.lockEntityKilledRewards { cacheEntityKilledRewards ->
                                cacheEntityKilledRewards.find { e -> e.entity.translationKey == cryptoBasedPriceReward.translationKey }?.let { e ->
                                    e.lastCryptoPrice = cryptoPrice
                                    e.currentPrice = cryptoBasedPriceReward.currentPrice
                                    cryptoBasedPriceReward.lastCryptoPrice = cryptoPrice
                                }
                            }
                        }
                        rewardData!!.currentPrice = cryptoBasedPriceReward.currentPrice
                        rewardData.lastCryptoPrice = cryptoBasedPriceReward.lastCryptoPrice
                        // ---------- Updating price in the database ---------- \\

//                        databaseManager.modifyMinedBlockRewards {
//                            databaseManager.cacheMinedBlockRewards.find {m -> m.block.translationKey == cryptoBasedPriceReward.translationKey }?.let {m ->
//                                m.lastCryptoPrice = price
//                            }
////                            cryptoBasedPriceReward.lastCryptoPrice = price
//                        }
                        // Also updating config
//                        val d = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { m -> m.translationKey == cryptoBasedPriceReward.translationKey }
//                        d.currentPrice = cryptoBasedPriceReward.currentPrice
//                        d.lastCryptoPrice = cryptoBasedPriceReward.lastCryptoPrice
                    }
                })
            }
        }

    }

    private fun oldCode() {
//        map.forEach { (key, list) ->
//            val ws = WebsocketStreamClientImpl()
//            wss.add(ws)
//            launch {
//                ws.symbolTicker(key, WebSocketCallback {
//                    val price = JsonPath.read<String>(it, "$['b']").toDouble()
//
//                    list.forEach { minedBlockReward ->
//                        if (minedBlockReward.lastCryptoPrice != -1.0) {
//                            if (price == minedBlockReward.lastCryptoPrice) return@WebSocketCallback
//
//                            var percentDiff = (price * 100.0 / minedBlockReward.lastCryptoPrice) - 100.0 // Get the percent changed between lastPrice and currentPrice
//
//
//                            if (percentDiff * 20 >= -90.0) {
//                                percentDiff *= 20 // Increase a bit
//                            }
//
//                            val previousPrice = minedBlockReward.currentPrice
//                            val newPrice = (100.0 + (percentDiff)) * minedBlockReward.currentPrice / 100.0
//
//                            databaseManager.modifyMinedBlockRewards {
//                                minedBlockReward.currentPrice = newPrice
//                            }
//                            if (minedBlockReward.block.translationKey == "block.minecraft.sandstone") {
//                                println("                    \tprevious price was $previousPrice")
//                                println("basic percent: ${percentDiff / 20.0}\tnew percent: $percentDiff\tnew price: ${newPrice}")
//                            }
//                        }
//                        databaseManager.modifyMinedBlockRewards {
//                            minedBlockReward.lastCryptoPrice = price
//                        }
//                        // Also updating config
//                        val d = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first {m -> m.translationKey  == minedBlockReward.block.translationKey }
//                        d.currentPrice = minedBlockReward.currentPrice
//                        d.lastCryptoPrice = minedBlockReward.lastCryptoPrice
//                    }
//                })
//            }
//        }
    }

//    init {
//
//        val timeout = Duration.ofSeconds(30)
//        val okHttpClient = OkHttpClient().newBuilder()
//            .callTimeout(timeout)
//            .readTimeout(timeout)
//            .writeTimeout(timeout)
//            .connectTimeout(timeout)
//            .build()
//
//
////        var lastPrice = -1.0
//        infiniteMcCoroutineTask(sync = false, client = false, period = 15.seconds) {
//
//            val map = mutableMapOf<String, MutableList<MinedBlockReward>>()
//            Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.forEach {
//                if (!map.containsKey(it.basedCryptoCurrencyName)) map[it.basedCryptoCurrencyName] = mutableListOf(it)
//                else map[it.basedCryptoCurrencyName]!!.add(it)
//            }
//
//            map.forEach { (key, list) ->
//
//                val request: Request = Request.Builder()
//                    .url("https://data.binance.com/api/v3/ticker/price?symbol=$key")
//                    .build()
//                val response = okHttpClient.newCall(request).execute()
//                if (response.isSuccessful) {
//                    val jsonContent = response.body.string()
//                    val price = JsonPath.read<String>(jsonContent, "$['price']").toDouble()
//
//                    list.forEach { minedBlockReward ->
//
//                        if (minedBlockReward.lastCryptoPrice != -1.0) {
//                            var percentDiff = (price * 100.0 / minedBlockReward.lastCryptoPrice) - 100.0 // Get the percent changed between lastPrice and currentPrice
//                            percentDiff *= 1.2 // Increase a bit
//                            minedBlockReward.currentPrice = (100.0 + (percentDiff)) * minedBlockReward.currentPrice / 100.0
//
//                            if (minedBlockReward.translationKey == "block.minecraft.sandstone") {
//                                println("percent $percentDiff\tnew price is ${minedBlockReward.currentPrice}")
//                            }
//                        }
//
//                        minedBlockReward.lastCryptoPrice = price
//                    }
//                } else {
//                    println("An error occurred $response")
//                }
//            }
//
//            ConfigManager.save(Configs.MINED_BLOCK_REWARD_CONFIG)
//
////            if(0 == 0)return@infiniteMcCoroutineTask
////            val request: Request = Request.Builder()
////                .url("https://data.binance.com/api/v3/ticker/price?symbol=RENUSDT")
////                .build()
////            val response = okHttpClient.newCall(request).execute()
////            val jsonContent = response.body.string()
////            val price = JsonPath.read<String>(jsonContent, "$['price']").toDouble()
////
////            if (lastPrice != -1.0) {
////                var p = price * 100.0 / lastPrice - 100.0 // Get the percent changed between lastPrice and currentPrice
////                println("percent $p")
////
////                Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { it.translationKey == "block.minecraft.sandstone" }.let {
////                    println("currentDirtPrice ${it.average.currentPrice}")
////                    it.average.currentPrice = (100.0 + (p)) * it.average.currentPrice / 100.0
////                    println("newP ${it.average.currentPrice}")
////                }
////            }
////
////            lastPrice = price
////            if (0 == 0) return@infiniteMcCoroutineTask
////            Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.forEach {
////
////                val upOrDown = Random.nextInt(0, 2)
////                val randomPercent = if (upOrDown == 1)
////                    Random.nextInt(0, it.percentUp)
////                else
////                    Random.nextInt(0, it.percentDown)
////
////                val newPrice = (if (upOrDown == 1) 100 + randomPercent else 100 - randomPercent) * it.average.defaultPrice / 100
////                it.average.currentPrice = newPrice
////
//////                println("newPrice: $newPrice")
//////                println("oldPrice: ${it.average.defaultPrice}")
////
////            }
//
////            Configs.MINED_BLOCK_REWARD_CONFIG.updateNested(Average::price, 0f)
//
//        }
//    }

}
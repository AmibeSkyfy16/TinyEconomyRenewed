package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.logic.Game
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * This mod is initialized when the minecraft server has started
 */
class TinyEconomyRenewedInitializer {

    companion object{
        const val initializerThreadName = "initializer_thread"
    }

    private val optGameRef: AtomicReference<Optional<Game>> = AtomicReference(Optional.empty())

    private val isInitializationComplete = false

    init {

        ServerLifecycleEvents.SERVER_STARTED.register { minecraftServer ->
            TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed is being initialized")


            // Work but its blocking minecraft server thread
//            DatabaseManager // Here we create database, table and populate it with data like all minecraft advancement, item, ...

//            val dispatcher = Executors.newFixedThreadPool(1) {
//                val thread = Thread(it)
//                thread.name = initializerThreadName
//                thread.isDaemon = true
//                thread
//            }.asCoroutineDispatcher()
//
//            @Suppress("OPT_IN_USAGE") val deferred = GlobalScope.async(dispatcher) { DatabaseManager }
//
//            deferred.invokeOnCompletion {
//                optGameRef.set(Optional.of(Game(minecraftServer)))
//            }
        }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
//            if (!isInitializationComplete)
//                serverPlayNetworkHandler.disconnect(Text.literal("TinyEconomyRenewed has not finished to be initialized").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
        }
    }

}
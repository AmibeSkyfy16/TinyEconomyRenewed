@file:Suppress("OPT_IN_USAGE")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.callbacks.TinyEconomyRenewedInitializedCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import ch.skyfy.tinyeconomyrenewed.logic.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * This mod is initialized when the minecraft server has started
 */
class TinyEconomyRenewedInitializer(override val coroutineContext: CoroutineContext) : CoroutineScope {

    private val optGameRef: AtomicReference<Optional<Game>> = AtomicReference(Optional.empty())

    private var isInitializationComplete = false

    init {

        ServerLifecycleEvents.SERVER_STARTED.register { minecraftServer ->
            TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed is being initialized \uD83D\uDE9A \uD83D\uDE9A \uD83D\uDE9A")

            val deferred : Deferred<DatabaseManager> = async {
                val db = DatabaseManager()
                TinyEconomyRenewedMod.LOGGER.info("[async block] > The database has been successfully initialized and populated with data \uD83D\uDC4C ✅")
                db
            }

            deferred.invokeOnCompletion {
                if(it == null){
                    TinyEconomyRenewedMod.LOGGER.info("[invokeOnCompletion block] [it is null] > The database has been successfully initialized and populated with data \uD83D\uDC4C ✅")
                    TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect")
                    isInitializationComplete = true
                    val databaseManager = deferred.getCompleted()
                    optGameRef.set(Optional.of(Game(databaseManager, minecraftServer)))
                    TinyEconomyRenewedInitializedCallback.EVENT.invoker().onInitialized(databaseManager)
                }else
                    throw it.cause?.let { it1 -> TinyEconomyModException(it1) }!!
            }

        }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
            if (!isInitializationComplete)
                serverPlayNetworkHandler.disconnect(Text.literal("TinyEconomyRenewed has not finished to be initialized ⛔").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
        }
    }

}
@file:Suppress("OPT_IN_USAGE")

package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.logic.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.GameRules
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

/**
 * This mod is initialized when the minecraft server has started
 */
class TinyEconomyRenewedInitializer(override val coroutineContext: CoroutineContext = Dispatchers.IO) : CoroutineScope {

    private val optGameRef: AtomicReference<Optional<Game>> = AtomicReference(Optional.empty())

    private var isInitializationComplete = false

    init {

        ServerLifecycleEvents.SERVER_STARTED.register { minecraftServer ->
            launch {
                TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed is being initialized \uD83D\uDE9A \uD83D\uDE9A \uD83D\uDE9A")
                val db = DatabaseManager()
                optGameRef.set(Optional.of(Game(db, minecraftServer)))
                isInitializationComplete = true

                TinyEconomyRenewedMod.LOGGER.info("TinyEconomyRenewed >> done ! Players can now connect \uD83D\uDC4C ✅")
            }
        }

        ServerPlayConnectionEvents.INIT.register { serverPlayNetworkHandler, _ ->
            if (!isInitializationComplete)
                serverPlayNetworkHandler.disconnect(Text.literal("TinyEconomyRenewed has not finished to be initialized ⛔").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
        }
    }

}
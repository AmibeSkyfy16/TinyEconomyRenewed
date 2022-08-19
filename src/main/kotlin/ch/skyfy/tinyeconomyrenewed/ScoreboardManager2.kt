package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import kotlinx.coroutines.flow.FlowCollector
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.silkmc.silk.game.sideboard.SideboardBuilder
import net.silkmc.silk.game.sideboard.sideboard

class ScoreboardManager2(private val databaseManager: DatabaseManager) {

    val sidebarMap: MutableMap<String, SideboardBuilder> = mutableMapOf()
    val dollarMap: MutableMap<String, FlowCollector<Text>> = mutableMapOf()

    init {
        initialize()
    }

    private fun initialize() {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            sidebarMap.remove(handler.player.uuidAsString)
        }

        PlayerJoinCallback.EVENT.register { _, player ->
            if (!sidebarMap.containsKey(player.uuidAsString)) {

                var c: FlowCollector<Text>?
                val sb2 = sideboard(Text.literal("<< Main Board >>").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true))) {
                    sidebarMap[player.uuidAsString] = this
                    line(Text.literal("TEST"))
                    lineChanging {
                        emit(Text.literal("TEST2"))
                        if(!dollarMap.containsKey(player.uuidAsString))dollarMap[player.uuidAsString] = this
                    }

                }
                sb2.displayToPlayer(player)
            }
        }

        fun updateSidebar(serverPlayerEntity: ServerPlayerEntity) {

        }

    }
}
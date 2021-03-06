package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.players
import eu.pb4.sidebars.api.Sidebar
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.ktorm.dsl.like
import org.ktorm.entity.find

class ScoreboardManager(private val databaseManager: DatabaseManager) {

    private val sidebarMap: MutableMap<String, Sidebar> = HashMap()

   init {
       initialize()
   }

    private fun initialize(){
        ServerPlayConnectionEvents.DISCONNECT.register{handler, _ -> sidebarMap.remove(handler.player.uuidAsString) }

        PlayerJoinCallback.EVENT.register{ player, _ ->
            if(!sidebarMap.containsKey(player.uuidAsString)){
                val sb = Sidebar(Sidebar.Priority.HIGH)
                sb.title = Text.literal(">> Tiny Economy Renewed <<").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true))
                sb.updateRate = 20
                sb.addPlayer(player)
                sidebarMap[player.uuidAsString] = sb
            }

            updateSidebar(player)
            sidebarMap[player.uuidAsString]?.show()
        }
    }

    fun updateSidebar(serverPlayerEntity: ServerPlayerEntity){
        val sb = sidebarMap[serverPlayerEntity.uuidAsString] ?: return

        val list = ArrayList<Text>()

        list.add(Text.literal("").setStyle(Style.EMPTY))
        list.add(Text.literal("Money: ${databaseManager.db.players.find { it.uuid like serverPlayerEntity.uuidAsString}?.money}").setStyle(Style.EMPTY))

        for (i in list.indices.reversed()) sb.setLine(i, list[list.size - 1 - i])
    }

}
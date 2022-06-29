package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.db.*
import eu.pb4.sidebars.api.Sidebar
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

object ScoreboardManager {

    private val sidebarMap: MutableMap<String, Sidebar> = HashMap()

    private lateinit var databaseManager: DatabaseManager

    private val Database.players get() = this.sequenceOf(Players)

    fun initialize(databaseManager: DatabaseManager){
        this.databaseManager = databaseManager

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
        list.add(Text.literal("Money: ${databaseManager.database.players.find { it.uuid like serverPlayerEntity.uuidAsString}?.money}").setStyle(Style.EMPTY))

        for (i in list.indices.reversed()) sb.setLine(i, list[list.size - 1 - i])
    }

}
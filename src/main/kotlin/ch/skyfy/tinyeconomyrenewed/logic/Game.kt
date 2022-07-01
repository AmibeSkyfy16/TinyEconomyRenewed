package ch.skyfy.tinyeconomyrenewed.logic

import ch.skyfy.tinyeconomyrenewed.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import ch.skyfy.tinyeconomyrenewed.db.Players
import ch.skyfy.tinyeconomyrenewed.features.RewardFeature
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update


class Game(minecraftServer: MinecraftServer) {

    private val Database.players get() = this.sequenceOf(Players)
    init {
        RewardFeature()
        registerEvents()
        ScoreboardManager.initialize()
    }

    private fun registerEvents() {
        PlayerJoinCallback.EVENT.register(this::onPlayerJoin)
    }

    private fun onPlayerJoin(serverPlayerEntity: ServerPlayerEntity, @Suppress("UNUSED_PARAMETER") server: MinecraftServer) {
        val p = DatabaseManager.db.players.find { it.uuid like serverPlayerEntity.uuidAsString }
        if (p == null) {
            DatabaseManager.db.players.add(Player {
                uuid = serverPlayerEntity.uuidAsString
                name = serverPlayerEntity.name.string
            })
        } else { // Update name (maybe some players can change their name)
            p.name = serverPlayerEntity.name.string
            DatabaseManager.db.players.update(p)
        }
    }
}
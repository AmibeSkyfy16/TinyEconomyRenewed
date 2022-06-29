package ch.skyfy.tinyeconomyrenewed.logic

import ch.skyfy.tinyeconomyrenewed.db.*
import eu.pb4.sidebars.api.Sidebar
import me.bymartrixx.playerevents.api.event.PlayerJoinCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.update

@Suppress("unused")
class Game(private val databaseManager: DatabaseManager, val minecraftServer: MinecraftServer) {

    private val Database.players get() = this.sequenceOf(Players)
    private val Database.items get() = this.sequenceOf(Items)
    private val Database.entities get() = this.sequenceOf(Entities)
    private val Database.advancements get() = this.sequenceOf(Advancements)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {
        registerEvents()
    }

    private fun registerEvents() {
        PlayerJoinCallback.EVENT.register(this::onPlayerJoin)
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBlockBreakEvent)
    }

    private fun onPlayerJoin(serverPlayerEntity: ServerPlayerEntity, @Suppress("UNUSED_PARAMETER") server: MinecraftServer) {
        val p = databaseManager.database.players.find { it.uuid like serverPlayerEntity.uuidAsString }
        if (p == null) {
            databaseManager.database.players.add(Player {
                uuid = serverPlayerEntity.uuidAsString
                name = serverPlayerEntity.name.string
            })
        } else { // Update name (maybe some players can change their name)
            p.name = serverPlayerEntity.name.string
            databaseManager.database.players.update(p)
        }
    }

    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, @Suppress("UNUSED_PARAMETER") state: BlockState, @Suppress("UNUSED_PARAMETER") blockEntity: BlockEntity?): Boolean {

        val translationKey = world.getBlockState(pos).block.translationKey
        val minedBlockReward = databaseManager.database.minedBlockRewards.find { it.item.translationKey like translationKey }

        if (minedBlockReward != null) {
            val p = databaseManager.database.players.find { it.uuid like player.uuidAsString }
            if (p != null) {
                p.money += minedBlockReward.amount
                databaseManager.database.players.update(p)
            }
        }
        return true
    }

}
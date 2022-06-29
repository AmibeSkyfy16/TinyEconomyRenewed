package ch.skyfy.tinyeconomyrenewed.logic

import ch.skyfy.tinyeconomyrenewed.db.*
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class Game(private val databaseManager: DatabaseManager, val minecraftServer: MinecraftServer) {

    private val Database.items get() = this.sequenceOf(Items)
    private val Database.entities get() = this.sequenceOf(Entities)
    private val Database.advancements get() = this.sequenceOf(Advancements)
    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {
        registerEvents()
    }

    private fun registerEvents(){
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBlockBreakEvent)
    }

    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?) : Boolean{

        print("")
        val translationKey = world.getBlockState(pos).block.translationKey
        val minedBlockReward = databaseManager.database.minedBlockRewards.find { it.item.translationKey eq translationKey }

        if (minedBlockReward != null) {
            println("minedBlockReward : " + minedBlockReward.item.translationKey)
            println("amount : " + minedBlockReward.amount)
        }

        return true
    }

}
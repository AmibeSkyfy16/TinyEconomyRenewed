package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCompletedCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.EntityDamageCallback
import ch.skyfy.tinyeconomyrenewed.db.*
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.advancement.Advancement
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class RewardFeature(private val databaseManager: DatabaseManager, private val economy: Economy, private val scoreboardManager: ScoreboardManager) {

    private val Database.minedBlockRewards get() = this.sequenceOf(MinedBlockRewards)
    private val Database.entityKilledRewards get() = this.sequenceOf(EntityKilledRewards)
    private val Database.advancementRewards get() = this.sequenceOf(AdvancementRewards)

    init {
        registerEvents()
    }

    private fun registerEvents() {
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBlockBreakEvent)
        EntityDamageCallback.EVENT.register(this::onEntityDamaged)
        AdvancementCompletedCallback.EVENT.register(this::onAdvancementCompleted)
    }

    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, @Suppress("UNUSED_PARAMETER") state: BlockState, @Suppress("UNUSED_PARAMETER") blockEntity: BlockEntity?): Boolean {
        economy.deposit(player.uuidAsString) {
            databaseManager.db.minedBlockRewards.find { it.item.translationKey like world.getBlockState(pos).block.translationKey }?.amount
        }
        scoreboardManager.updateSidebar(player as ServerPlayerEntity)
        return true
    }

    private fun onEntityDamaged(livingEntity: LivingEntity, damageSource: DamageSource, @Suppress("UNUSED_PARAMETER") amount: Float) {
        val attacker = damageSource.attacker
        if (attacker !is PlayerEntity) return

        if (livingEntity.health <= 0) {
            economy.deposit(attacker.uuidAsString) {
                databaseManager.db.entityKilledRewards.find { it.entity.translationKey like livingEntity.type.translationKey }?.amount
            }
            scoreboardManager.updateSidebar(attacker as ServerPlayerEntity)
        }
    }

    private fun onAdvancementCompleted(serverPlayerEntity: ServerPlayerEntity, advancement: Advancement, @Suppress("UNUSED_PARAMETER") criterionName: String) {
        economy.deposit(serverPlayerEntity.uuidAsString) {
            databaseManager.db.advancementRewards.find { it.advancement.identifier like advancement.id.toString() }?.amount
        }
        scoreboardManager.updateSidebar(serverPlayerEntity)
    }

//    private fun updatePlayerMoney(serverPlayerEntity: ServerPlayerEntity, player: Player, amount: Float) {
//        player.money += amount
//        databaseManager.database.players.update(player)
//        ScoreboardManager.updateSidebar(serverPlayerEntity)
//    }

}
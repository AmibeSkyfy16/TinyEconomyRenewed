package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.LOGGER
import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCompletedCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.EntityDamageCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.advancementRewards
import ch.skyfy.tinyeconomyrenewed.db.entityKilledRewards
import ch.skyfy.tinyeconomyrenewed.db.minedBlockRewards
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
import org.ktorm.dsl.like
import org.ktorm.entity.find

class RewardFeature(private val databaseManager: DatabaseManager, private val economy: Economy, private val scoreboardManager: ScoreboardManager) {

    private val nerfEntitiesRewards: MutableMap<Long, Pair<String, BlockPos>> = mutableMapOf()

    private val nerfBlocksRewards: MutableMap<Long, Pair<String, BlockPos>> = mutableMapOf()

    init {
        registerEvents()
    }

    private fun registerEvents() {
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBlockBreakEvent)
        EntityDamageCallback.EVENT.register(this::onEntityDamaged)
        AdvancementCompletedCallback.EVENT.register(this::onAdvancementCompleted)
    }

    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, @Suppress("UNUSED_PARAMETER") state: BlockState, @Suppress("UNUSED_PARAMETER") blockEntity: BlockEntity?): Boolean {

        if (shouldNerf(player.uuidAsString, player.blockPos, nerfBlocksRewards)) return true

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

            if (shouldNerf(attacker.uuidAsString, attacker.blockPos, nerfEntitiesRewards)) return

            economy.deposit(attacker.uuidAsString) {
                databaseManager.db.entityKilledRewards.find { it.entity.translationKey like livingEntity.type.translationKey }?.amount
            }
            scoreboardManager.updateSidebar(attacker as ServerPlayerEntity)
        }
    }

    private fun onAdvancementCompleted(serverPlayerEntity: ServerPlayerEntity, advancement: Advancement, @Suppress("UNUSED_PARAMETER") criterionName: String) {
        LOGGER.error(advancement.id.toString())
        LOGGER.error(advancement.display?.title?.string ?: "")
        economy.deposit(serverPlayerEntity.uuidAsString) {
            databaseManager.db.advancementRewards.find { it.advancement.identifier like advancement.id.toString() }?.amount
        }
        scoreboardManager.updateSidebar(serverPlayerEntity)
    }

    private fun shouldNerf(uuid: String, pos: BlockPos, nerf: MutableMap<Long, Pair<String, BlockPos>>): Boolean {
        nerf[System.currentTimeMillis()] = Pair(uuid, pos)

        // Remove all entries more than 5 minute ago
        nerf.entries.removeIf { it.value.first == uuid && System.currentTimeMillis() - it.key > 5 * 60 * 1000 }

        val entries2 = nerf.entries.filter { it.value.first == uuid }.toMutableList()
        if (entries2.count() == 1) return true

        val last = entries2.last()

        val ite = entries2.subList(0, entries2.count() - 1).reversed().withIndex().iterator()
        var isPlayerMove = false

        while (ite.hasNext()) {
            val next = ite.next()
            val entry = next.value
            val index = next.index

            val zDistance = entry.value.second.z.coerceAtLeast(last.value.second.z) - entry.value.second.z.coerceAtMost(last.value.second.z)
            val xDistance = entry.value.second.x.coerceAtLeast(last.value.second.x) - entry.value.second.x.coerceAtMost(last.value.second.x)

            if (zDistance >= 10 || xDistance >= 10)
                isPlayerMove = true

            if (last.key - entry.key >= 1 * 60 * 1000) {

                // If player kill 20 or more entities without moving in the last one minute, we nerf
                if (index >= 20 && !isPlayerMove)
                    return true

                // If player kill 40 or more entities while moving in the last one minute, we nerf
                if (index >= 40)
                    return true

                break
            } else if (!ite.hasNext()) {
                // If player kill more than 15 entities in last few seconds
                if (index >= 15)
                    return true
            }
        }
        return false
    }

}
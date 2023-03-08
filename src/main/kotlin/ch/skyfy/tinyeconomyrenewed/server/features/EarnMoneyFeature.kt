package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.callbacks.AdvancementCompletedCallback
import ch.skyfy.tinyeconomyrenewed.server.callbacks.EntityDamageCallback
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
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

class EarnMoneyFeature(private val databaseManager: DatabaseManager, private val economy: Economy) {

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

    @Suppress("UNUSED_PARAMETER")
    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?): Boolean {

//        if (shouldNerf(player.uuidAsString, player.blockPos, nerfBlocksRewards, 2, 2, 60, 500, 100)) return true

        val price = getPrice(world, player, pos, state)
        economy.deposit(player as ServerPlayerEntity?, player.uuidAsString) {
            price
//            databaseManager.cacheMinedBlockRewards.find { it.block.translationKey == state.block.translationKey }?.amount ?: 0f
        }

        return true
    }

    val playersMiningAverage = mutableMapOf<String, MutableMap<Long, BlockPos>>()

    private fun getPrice(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState): Double {
        val blockTranslationKey = state.block.translationKey

        val minedBlockReward = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { it.translationKey == blockTranslationKey }

        if (!playersMiningAverage.containsKey(player.uuidAsString)) playersMiningAverage[player.uuidAsString] = mutableMapOf()

        val averageMap = playersMiningAverage[player.uuidAsString]!!

        averageMap[System.currentTimeMillis()] = pos

        val elapsedTimeInMillis = averageMap.keys.last() - averageMap.keys.first()
        val elapsedTimeInMinute = elapsedTimeInMillis / 1000.0 / 60.0
        val minedBlockPerMinute = if (elapsedTimeInMinute <= 1) averageMap.size * elapsedTimeInMinute else averageMap.size / elapsedTimeInMinute

        if (minedBlockPerMinute <= minedBlockReward.average.numberPerMinute) {

            if (minedBlockPerMinute == 0.0) return minedBlockReward.currentPrice
            val diff = minedBlockReward.average.numberPerMinute - minedBlockPerMinute
            val percent = 100.0 * diff / minedBlockReward.average.numberPerMinute
            val price = minedBlockReward.currentPrice * (percent / 100)
            println("price: $price")
            return price

            // --------- Solution #1 ---------
            val doSolutionOne = false
            if(doSolutionOne) {
                // 100 % of currentPrice so currentPrice
                if (minedBlockPerMinute == minedBlockReward.average.numberPerMinute) {
                    return minedBlockReward.currentPrice
                }
                val diff = minedBlockReward.average.numberPerMinute - minedBlockPerMinute
                val newPrice = diff * minedBlockReward.currentPrice / 100
                println("price: $newPrice")
                return newPrice
            }
            // --------- Solution #1 ---------


//            println("price: $newPrice")
//            return newPrice
//            val diffPercent = 100.0 * diff / minedBlockReward.average.numberPerMinute // 20 is 40% of 50


//            val newPrice = (100.0 + diffPercent) * (minedBlockReward.average.numberPerMinute) / 100.0 // More my blockPerMinuteAverage is getting closer to default average, less is my price. I will get 100 + 40 = 140% of currentPrice (50 here)

//            val price = minedBlockReward.currentPrice * minedBlockPerMinute / minedBlockReward.average.numberPerMinute
        } else {
            val diff = minedBlockPerMinute - minedBlockReward.average.numberPerMinute
            println("player average is greater price will be 0")
            return 0.0
        }

    }

    private fun onEntityDamaged(livingEntity: LivingEntity, damageSource: DamageSource, @Suppress("UNUSED_PARAMETER") amount: Float) {
        val attacker = damageSource.attacker
        if (attacker !is PlayerEntity) return

        if (livingEntity.health <= 0) {
            if (shouldNerf(attacker.uuidAsString, attacker.blockPos, nerfEntitiesRewards, 10, 10, 40, 80, 15)) return

            economy.deposit(attacker as ServerPlayerEntity?, attacker.uuidAsString) {
                databaseManager.cacheEntityKilledRewards.find { it.entity.translationKey == livingEntity.type.translationKey }?.amount ?: 0.0
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun onAdvancementCompleted(serverPlayerEntity: ServerPlayerEntity, advancement: Advancement, @Suppress("UNUSED_PARAMETER") criterionName: String) {
        economy.deposit(serverPlayerEntity, serverPlayerEntity.uuidAsString) {
            databaseManager.cacheAdvancementRewards.find { it.advancement.identifier == advancement.id.toString() }?.amount ?: 0.0
        }
    }

    @Suppress("SameParameterValue")
    private fun shouldNerf(
        uuid: String,
        pos: BlockPos,
        nerf: MutableMap<Long, Pair<String, BlockPos>>,
        minZDistance: Int,
        minXDistance: Int,
        minAmount1: Int,
        minAmount2: Int,
        minAmount3: Int,
    ): Boolean {
        nerf[System.currentTimeMillis()] = Pair(uuid, pos)

        // Remove all entries more than 5 minute ago
        nerf.entries.removeIf { it.value.first == uuid && System.currentTimeMillis() - it.key > 5 * 60 * 1000 }

        val entries2 = nerf.entries.filter { it.value.first == uuid }.toMutableList()

        val last = entries2.last()

        val ite = entries2.subList(0, entries2.count() - 1).reversed().withIndex().iterator()
        var isPlayerMove = false

        while (ite.hasNext()) {
            val next = ite.next()
            val entry = next.value
            val index = next.index

            val zDistance = entry.value.second.z.coerceAtLeast(last.value.second.z) - entry.value.second.z.coerceAtMost(last.value.second.z)
            val xDistance = entry.value.second.x.coerceAtLeast(last.value.second.x) - entry.value.second.x.coerceAtMost(last.value.second.x)

            if (zDistance >= minZDistance || xDistance >= minXDistance)
                isPlayerMove = true

            if (last.key - entry.key >= 1 * 30 * 1000) {

                // If player kill 20 or more entities without moving in the last one minute, we nerf
                if (index >= minAmount1 && !isPlayerMove) return true

                // If player kill 40 or more entities while moving in the last one minute, we nerf
                if (index >= minAmount2) return true

                break
            } else if (!ite.hasNext()) {
                // If player kill more than 15 entities in last few seconds
                if (index >= minAmount3) return true
            }
        }
        return false
    }


}
package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.jsonconfiglib.updateIterable
import ch.skyfy.jsonconfiglib.updateNested
import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.callbacks.AdvancementCompletedCallback
import ch.skyfy.tinyeconomyrenewed.server.callbacks.BlockPlacedCallback
import ch.skyfy.tinyeconomyrenewed.server.callbacks.EntityDamageCallback
import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerJoinCallback
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.db.BlackListedPlacedBlock
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.persisent.Persistents
import ch.skyfy.tinyeconomyrenewed.server.persisent.PlayerInfo
import ch.skyfy.tinyeconomyrenewed.server.persisent.PlayerInfosPersistent
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.advancement.Advancement
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.network.ClientConnection
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EarnMoneyFeature(private val databaseManager: DatabaseManager, private val economy: Economy) {

    private val formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())

    private val playersMiningAverage = mutableMapOf<String, MutableMap<Long, Pair<String, BlockPos>>>()
    private val playersKillingAverage = mutableMapOf<String, MutableMap<Long, Pair<String, BlockPos>>>()

    init { registerEvents() }

    private fun registerEvents() {
        PlayerJoinCallback.EVENT.register(::onPlayerJoined)
        BlockPlacedCallback.EVENT.register(::onPlayerPlaceBlockEvent)
        PlayerBlockBreakEvents.BEFORE.register(this::onPlayerBlockBreakEvent)
        EntityDamageCallback.EVENT.register(this::onEntityDamaged)
        AdvancementCompletedCallback.EVENT.register(this::onAdvancementCompleted)
    }

    private fun onPlayerJoined(@Suppress("UNUSED_PARAMETER") connection: ClientConnection, player: ServerPlayerEntity){
        earnLoginImpl(player)
    }

    private fun earnLoginImpl(player: ServerPlayerEntity){
        fun earnLogin(playerInfo: PlayerInfo?, zonedDateTime: ZonedDateTime) {
            val amount = Configs.EARN_MONEY_FEATURE_CONFIG.serializableData.priceToEarnForFirstLoggingOfTheDay
            player.sendMessage(Text.literal("First login for today ! You've just earn $amount").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
            economy.deposit(player, player.uuidAsString) { amount }
            if (playerInfo == null) Persistents.PLAYER_INFOS.updateIterable(PlayerInfosPersistent::infos) { it.add(PlayerInfo(player.uuidAsString, formatter.format(zonedDateTime))) }
            else Persistents.PLAYER_INFOS.updateNested(PlayerInfo::lastLoginDate, playerInfo, formatter.format(zonedDateTime))
        }

        val playerInfo = Persistents.PLAYER_INFOS.serializableData.infos.find { playerInfo -> playerInfo.uuid == player.uuidAsString }
        if (playerInfo == null) earnLogin(null, Instant.now().atZone(ZoneId.systemDefault()))
        else {
            val today = Instant.now().atZone(ZoneId.systemDefault())
            val lastLogin = Instant.from(formatter.parse(playerInfo.lastLoginDate)).atZone(ZoneId.systemDefault())
            if (today.dayOfYear != lastLogin.dayOfYear) earnLogin(playerInfo, today)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPlayerPlaceBlockEvent(blockItem: BlockItem, itemPlacementContext: ItemPlacementContext, actionResult: ActionResult) {
        if (actionResult != ActionResult.CONSUME) return
        val b = itemPlacementContext.blockPos
        databaseManager.cacheBlackListedPlacedBlocks
        if (databaseManager.cacheBlackListedPlacedBlocks.none { it.x == b.x && it.y == b.y && it.z == b.z })
//            databaseManager.modifyBlackListedPlacedBlocks {  }
            databaseManager.cacheBlackListedPlacedBlocks.add(BlackListedPlacedBlock { x = b.x; y = b.y; z = b.z })
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPlayerBlockBreakEvent(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?): Boolean {
        if (databaseManager.cacheBlackListedPlacedBlocks.any { it.x == pos.x && it.y == pos.y && it.z == pos.z }) return true

//        val minedBlockReward = Configs.MINED_BLOCK_REWARD_CONFIG.serializableData.list.first { it.translationKey == state.block.translationKey }
        val minedBlockReward = databaseManager.cacheMinedBlockRewards.first { it.block.translationKey == state.block.translationKey }
        economy.deposit(player as ServerPlayerEntity?, player.uuidAsString) {
            getPrice(
                player,
                pos,
                state.block.translationKey,
                minedBlockReward.maximumMinedBlockPerMinute,
                minedBlockReward.currentPrice,
                playersMiningAverage
            )
        }
        return true
    }

    private fun onEntityDamaged(livingEntity: LivingEntity, damageSource: DamageSource, @Suppress("UNUSED_PARAMETER") amount: Float) {
        val attacker = damageSource.attacker
        if (attacker !is PlayerEntity) return

        if (livingEntity.health <= 0) {
//            val entityKilledReward = Configs.ENTITY_KILLED_REWARD_CONFIG.serializableData.list.first { it.translationKey == livingEntity.type.translationKey }
            val entityKilledReward = databaseManager.cacheEntityKilledRewards.first { it.entity.translationKey == livingEntity.type.translationKey }
            economy.deposit(attacker as ServerPlayerEntity?, attacker.uuidAsString) {
                getPrice(
                    attacker,
                    attacker.blockPos,
                    livingEntity.type.translationKey,
                    entityKilledReward.maximumEntityKilledPerMinute,
                    entityKilledReward.currentPrice,
                    playersKillingAverage
                )
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun onAdvancementCompleted(serverPlayerEntity: ServerPlayerEntity, advancement: Advancement, @Suppress("UNUSED_PARAMETER") criterionName: String) {
        economy.deposit(serverPlayerEntity, serverPlayerEntity.uuidAsString) {
            databaseManager.cacheAdvancementRewards.find { it.advancement.identifier == advancement.id.toString() }?.amount ?: 0.0
        }
    }

    private fun getPrice(
        player: PlayerEntity,
        pos: BlockPos,
        translationKey: String,
        maximumNumberPerMinute: Double,
        currentPrice: Double,
        map: MutableMap<String, MutableMap<Long, Pair<String, BlockPos>>>
    ): Double {
        map.computeIfAbsent(player.uuidAsString) { mutableMapOf(System.currentTimeMillis() to Pair(translationKey, pos)) }
        val timeMap = map.computeIfPresent(player.uuidAsString) { _, v -> v[System.currentTimeMillis()] = Pair(translationKey, pos); v }!!

        // Getting all broken block in the last 5 minutes
        val lastEntry = timeMap.entries.last()
        val entries = timeMap.filter { ((lastEntry.key - it.key) / 1000.0) <= 300.0 && it.value.first == translationKey }.entries
        val elapsedTimeInMillis = entries.last().key - entries.first().key
        val elapsedTimeInSeconds = elapsedTimeInMillis / 1000.0
        val elapsedTimeInMinute = elapsedTimeInSeconds / 60.0
        val perMinuteForLast5Mn = if (elapsedTimeInMinute <= 1) entries.size * elapsedTimeInMinute else entries.size / elapsedTimeInMinute

        if (perMinuteForLast5Mn <= maximumNumberPerMinute) {
            if (perMinuteForLast5Mn == 0.0) return currentPrice
            val diff = maximumNumberPerMinute - perMinuteForLast5Mn
            val percent = 100.0 * diff / maximumNumberPerMinute
            val price = currentPrice * (percent / 100)
            println("price: $price")
            return price
        }

        return 0.0
    }












//    @Deprecated("use getPrice2")
//    private fun getPriceOld(
//        player: PlayerEntity,
//        pos: BlockPos,
//        maximumNumberPerMinute: Double,
//        currentPrice: Double,
//        map: MutableMap<String, MutableMap<Long, BlockPos>>
//    ): Double {
//        map.putIfAbsent(player.uuidAsString, mutableMapOf())
//
//        val averageMap = map[player.uuidAsString]!!
//
//        averageMap[System.currentTimeMillis()] = pos
//
//        if (isAFKDetected(averageMap, rewardType)) return 0.0
//
//        val elapsedTimeInMillis = averageMap.keys.last() - averageMap.keys.first()
//        val elapsedTimeInMinute = elapsedTimeInMillis / 1000.0 / 60.0
//        val minedBlockPerMinute = if (elapsedTimeInMinute <= 1) averageMap.size * elapsedTimeInMinute else averageMap.size / elapsedTimeInMinute
//
//        if (minedBlockPerMinute <= maximumNumberPerMinute) {
//            // --------- Solution #1 ---------
//            // The more the player mines and his value of mined blocks per minute is close to the maximum value determined
//            // by the type of block, the less money he earns
//            val doSolutionOne = true
//            if (doSolutionOne) {
//                if (minedBlockPerMinute == 0.0) return currentPrice
//                val diff = maximumNumberPerMinute - minedBlockPerMinute
//                val percent = 100.0 * diff / maximumNumberPerMinute
//                val price = currentPrice * (percent / 100)
//                println("price: $price")
//                return price
//            }
//            // --------- Solution #1---------
//
//            // --------- Solution #2 ---------
//            val doSolutionTwo = false
//            if (doSolutionTwo) {
//                // 100 % of currentPrice so currentPrice
//                if (minedBlockPerMinute == maximumNumberPerMinute) {
//                    return currentPrice
//                }
//                val diff = maximumNumberPerMinute - minedBlockPerMinute
//                val newPrice = diff * currentPrice / 100
//                println("price: $newPrice")
//                return newPrice
//            }
//            // --------- Solution #2 ---------
//        }
//
//        return 0.0
//    }
//
//    private fun isAFKDetected(averageMap: Map<Long, BlockPos>, rewardType: RewardType): Boolean {
//        // AFK CHECK
//        val d = Configs.EARN_MONEY_FEATURE_CONFIG.serializableData
//        val lastXXXSeconds = if (rewardType == RewardType.MINED_BLOCK) d.minedBlockRewardNerfer.lastXXXSeconds else d.entityKilledRewardNerfer.lastXXXSeconds
//        val surfaceThatShouldNotBeExceeded = if (rewardType == RewardType.MINED_BLOCK) d.minedBlockRewardNerfer.surfaceThatShouldNotBeExceeded else d.entityKilledRewardNerfer.surfaceThatShouldNotBeExceeded
//        val maximumPerMinute = if (rewardType == RewardType.MINED_BLOCK) d.minedBlockRewardNerfer.maximumBlockPerMinute else d.entityKilledRewardNerfer.maximumEntityKilledPerMinute
//
//        val lastEntry = averageMap.entries.last()
//        // Getting all entries in the last 5 mn
//        val entries = averageMap.filter { ((lastEntry.key - it.key) / 1000.0) <= lastXXXSeconds }.entries
//        val elapsedTimeInMillis2 = entries.last().key - entries.first().key
//        val elapsedTimeInSeconds2 = elapsedTimeInMillis2 / 1000.0
//        val elapsedTimeInMinute2 = elapsedTimeInMillis2 / 1000.0 / 60.0
//        val perMinuteForLast5Mn = if (elapsedTimeInMinute2 <= 1) entries.size * elapsedTimeInMinute2 else entries.size / elapsedTimeInMinute2
//
//        val smallestDistanceX = entries.minBy { it.value.x }.value.x
//        val greatestDistanceX = entries.maxBy { it.value.x }.value.x
//        val smallestDistanceZ = entries.minBy { it.value.z }.value.z
//        val greatestDistanceZ = entries.maxBy { it.value.z }.value.z
//        val surface = (greatestDistanceX - smallestDistanceX) * (greatestDistanceZ - smallestDistanceZ)
//
//        // Check if it's been at least 5 minutes and not less than 280 seconds (4.66mn) that the player mines
//        if (elapsedTimeInSeconds2 >= lastXXXSeconds - 20 && elapsedTimeInSeconds2 <= lastXXXSeconds) {
//            if (perMinuteForLast5Mn > maximumPerMinute) {
////                if (surface <= surfaceThatShouldNotBeExceeded) {
//                println("afk detected price will be 0")
//                return true
////                }
//            }
//        }
//
//        println("surface $surface")
//        println("perMinuteForLast5Mn $perMinuteForLast5Mn")
//
//        return false
//    }
//
//    @Deprecated("An old fun")
//    @Suppress("SameParameterValue")
//    private fun shouldNerf(
//        uuid: String,
//        pos: BlockPos,
//        nerf: MutableMap<Long, Pair<String, BlockPos>>,
//        minZDistance: Int,
//        minXDistance: Int,
//        minAmount1: Int,
//        minAmount2: Int,
//        minAmount3: Int,
//    ): Boolean {
//        nerf[System.currentTimeMillis()] = Pair(uuid, pos)
//
//        // Remove all entries more than 5 minute ago
//        nerf.entries.removeIf { it.value.first == uuid && System.currentTimeMillis() - it.key > 5 * 60 * 1000 }
//
//        val entries2 = nerf.entries.filter { it.value.first == uuid }.toMutableList()
//
//        val last = entries2.last()
//
//        val ite = entries2.subList(0, entries2.count() - 1).reversed().withIndex().iterator()
//        var isPlayerMove = false
//
//        while (ite.hasNext()) {
//            val next = ite.next()
//            val entry = next.value
//            val index = next.index
//
//            val zDistance = entry.value.second.z.coerceAtLeast(last.value.second.z) - entry.value.second.z.coerceAtMost(last.value.second.z)
//            val xDistance = entry.value.second.x.coerceAtLeast(last.value.second.x) - entry.value.second.x.coerceAtMost(last.value.second.x)
//
//            if (zDistance >= minZDistance || xDistance >= minXDistance)
//                isPlayerMove = true
//
//            if (last.key - entry.key >= 1 * 30 * 1000) {
//
//                // If player kill 20 or more entities without moving in the last one minute, we nerf
//                if (index >= minAmount1 && !isPlayerMove) return true
//
//                // If player kill 40 or more entities while moving in the last one minute, we nerf
//                if (index >= minAmount2) return true
//
//                break
//            } else if (!ite.hasNext()) {
//                // If player kill more than 15 entities in last few seconds
//                if (index >= minAmount3) return true
//            }
//        }
//        return false
//    }


}
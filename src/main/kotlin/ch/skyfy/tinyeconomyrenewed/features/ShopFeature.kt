package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.callbacks.CreateExplosionCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.HopperCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerInsertItemsCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerTakeItemsCallback
import ch.skyfy.tinyeconomyrenewed.config.Configs
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BarrelBlock
import net.minecraft.block.BlockState
import net.minecraft.block.WallSignBlock
import net.minecraft.block.WallSignBlock.FACING
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.Hopper
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.TntEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import net.minecraft.world.explosion.ExplosionBehavior

class ShopFeature(private val databaseManager: DatabaseManager, private val economy: Economy, private val minecraftServer: MinecraftServer) {

    data class Shop(val barrelBlockEntity: BarrelBlockEntity, val signBlockEntities: MutableList<SignBlockEntity>, val signData: SignData)
    data class SignData(val vendorName: String, val itemAmount: Int, val price: Float)

    init {
        UseBlockCallback.EVENT.register(this::useBlockCallback)
        PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak)
        PlayerTakeItemsCallback.EVENT.register(this::cancelPlayerFromTakeItem)
        PlayerInsertItemsCallback.EVENT.register(this::cancelPlayerFromInsertItem)
        CreateExplosionCallback.EVENT.register(this::manageShopExplosion)
        HopperCallback.EVENT.register(this::cancelHopperFromStealingAShop)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun manageShopExplosion(
        explosion: Explosion,
        serverWorld: ServerWorld,
        entity: Entity?,
        damageSource: DamageSource?,
        behavior: ExplosionBehavior?,
        x: Double,
        y: Double,
        z: Double,
        power: Float,
        createFire: Boolean,
        destructionType: Explosion.DestructionType,
    ) {
        val sc = Configs.SHOP_CONFIG.`data`

        // If this setting is set to true
        // No kind of explosion can destroy a shop
        if (sc.shopsCannotBeDestroyedByAnyExplosion)
            return cancelShopToBeDestroyed(explosion, serverWorld, null)

        // A player cannot destroy the shop of another one using a tnt
        if (entity is TntEntity) {
            val causingEntity = entity.causingEntity
            if (causingEntity != null) {
                if (causingEntity is PlayerEntity)
                    return cancelShopToBeDestroyed(explosion, serverWorld, causingEntity.name.string)
            }
        }

        sc.allowShopsToBeDestroyedByAnExplosion.forEach { (explosionType, value) ->
            if (entity != null) {
                if (explosionType.id == entity.type.translationKey && !value)
                    return cancelShopToBeDestroyed(explosion, serverWorld)
            }
            if (damageSource != null) {
                if (damageSource.name == explosionType.id && !value) // bed in nether or respawn anchor
                    cancelShopToBeDestroyed(explosion, serverWorld)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun cancelHopperFromStealingAShop(world: World, pos: BlockPos, state: BlockState, hopper: Hopper): TypedActionResult<Boolean> {
        val shop = isAShop(BlockPos(pos.x, pos.y + 1, pos.z), world as ServerWorld)
        if (shop != null) return TypedActionResult.fail(false)
        return TypedActionResult.pass(true)
    }

    private fun cancelShopToBeDestroyed(explosion: Explosion, serverWorld: ServerWorld, vendorName: String? = null) {
        val it = explosion.affectedBlocks.iterator()
        while (it.hasNext()) {
            val affectedBlock = it.next()
            val shop = isAShop(affectedBlock, serverWorld)
            if ((vendorName == null && shop != null) || (vendorName != null && shop != null && shop.signData.vendorName != vendorName)) {
                explosion.affectedBlocks.removeAll { bPos ->
                    bPos == shop.barrelBlockEntity.pos || shop.signBlockEntities.stream().anyMatch { it.pos == bPos }
                }
            }
        }
    }

    private fun <T> cancelPlayerFromInsertOrTakeItems(playerEntity: PlayerEntity, inventory: Inventory, pass: T, fail: T): T {
        if (inventory is BarrelBlockEntity) {
            val shop = isAShop(inventory.pos, playerEntity.getWorld())
            if (shop != null && shop.signData.vendorName != playerEntity.name.string) return fail
        }
        return pass
    }

    private fun cancelPlayerFromInsertItem(playerEntity: PlayerEntity, inventory: Inventory): Boolean =
        cancelPlayerFromInsertOrTakeItems(playerEntity, inventory, pass = true, fail = false)

    private fun cancelPlayerFromTakeItem(playerEntity: PlayerEntity, inventory: Inventory): ActionResult =
        cancelPlayerFromInsertOrTakeItems(playerEntity, inventory, ActionResult.PASS, ActionResult.FAIL)

    @Suppress("UNUSED_PARAMETER")
    private fun beforeBlockBreak(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?): Boolean {
        val block = world.getBlockState(pos).block
        val shop = isAShop(pos, world)

        if (shop != null) {
            if (block is BarrelBlock || block is WallSignBlock) {
                if (player.hasPermissionLevel(4)) return true
                if (shop.signData.vendorName != player.name.string) return false
            }
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun useBlockCallback(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult): ActionResult {
        val block = world.getBlockState(hitResult.blockPos).block
        val shop = isAShop(hitResult.blockPos, world)

        if (shop != null) {
            if (block is WallSignBlock) {
                if (shop.signData.vendorName == player.name.string) {
                    player.sendMessage(Text.of("You cannot buy from your own shop"))
                    return ActionResult.PASS
                }
                processTransaction(player as ServerPlayerEntity, shop)
            }
        }
        return ActionResult.PASS
    }

    @Suppress("ImplicitThis", "FoldInitializerAndIfToElvis")
    private fun isAShop(blockPos: BlockPos, world: World): Shop? {
        val blockState = world.getBlockState(blockPos)

        var barrelBlockEntity: BarrelBlockEntity? = null
        val signBlockEntities: MutableList<SignBlockEntity> = mutableListOf()

        if (blockState.block is WallSignBlock) {
            barrelBlockEntity = getBlockBehind(blockPos, world, world.getBlockState(blockPos).get(FACING))
            (world.getBlockEntity(blockPos) as SignBlockEntity?)?.let { signBlockEntities.add(it) }
        } else if (blockState.block is BarrelBlock) {
            signBlockEntities.addAll(getBlocksAround(blockPos, world))
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is BarrelBlockEntity) barrelBlockEntity = blockEntity
        }

        if (barrelBlockEntity == null || signBlockEntities.isEmpty()) return null

        // If many wall sign are around the barrel, and they are the same, it's ok (it's a shop)
        val firstSignData = getWallSignData(signBlockEntities[0])
        if (firstSignData == null) return null
        for (i in 1 until signBlockEntities.count()) {
            val anotherSignData = getWallSignData(signBlockEntities[i])
            if (anotherSignData == null || anotherSignData != firstSignData) return null
        }

        // Check if it's all the same items in the shop
        var firstTranslationKey = ""
        var once = false
        for (i in 0 until barrelBlockEntity.size()) {
            if (!barrelBlockEntity.getStack(i).isEmpty) {
                if (!once) {
                    once = true
                    firstTranslationKey = barrelBlockEntity.getStack(i).translationKey
                }
                if (barrelBlockEntity.getStack(i).translationKey != firstTranslationKey)
                    return null
            }
        }

        return Shop(barrelBlockEntity, signBlockEntities, firstSignData)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun processTransaction(buyerPlayer: ServerPlayerEntity, shop: Shop) {

        val vendorPlayer: ServerPlayerEntity? = minecraftServer.playerManager.getPlayer(shop.signData.vendorName)

        val def = LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.async {
            val vendor = databaseManager.cachePlayers.access { players: MutableList<Player> -> players.find { it.name == if (vendorPlayer != null) vendorPlayer.name.string else shop.signData.vendorName } }
            val buyer = databaseManager.cachePlayers.access { players: MutableList<Player> -> players.find { it.uuid == buyerPlayer.uuidAsString } }
            return@async Pair(vendor, buyer)
        }

        def.invokeOnCompletion {
            println("Completed")
            val pair = def.getCompleted()
            val vendor = pair.first
            val buyer = pair.second

            buyerPlayer.server.execute {

                if (vendor == null || buyer == null) {
                    TinyEconomyRenewedMod.LOGGER.info("vendor or buyer was not found in database")
                    return@execute
                }
                if (buyer.money - shop.signData.price < 0) {
                    buyerPlayer.sendMessage(Text.of("You don't have enough money"), false)
                    return@execute
                }

                val barrelBlockEntity = shop.barrelBlockEntity

                // Count the numbers of item in the barrel
                var availableItemStack = 0
                for (i in 0 until barrelBlockEntity.size()) {
                    val stack = barrelBlockEntity.getStack(i)
                    if (!stack.isEmpty) availableItemStack += stack.count
                }

                if (availableItemStack < shop.signData.itemAmount) {
                    buyerPlayer.sendMessage(Text.of("There are not enough items in stock!"), false)
                    return@execute
                }

                val transfer = ArrayList<ItemStack>()
                var remainingPiece: Int = shop.signData.itemAmount

                for (i in 0 until barrelBlockEntity.size()) {
                    val originItemStack = barrelBlockEntity.getStack(i)
                    if (!originItemStack.isEmpty) {
                        if (remainingPiece <= 0) break
                        val newItem = ItemStack(originItemStack.item)
                        if (originItemStack.count - remainingPiece <= 0) {
                            barrelBlockEntity.setStack(i, ItemStack.EMPTY)
                        } else {
                            newItem.count = remainingPiece
                            originItemStack.count = originItemStack.count - remainingPiece
                        }
                        transfer.add(newItem)
                        remainingPiece -= originItemStack.count
                    }
                }

                if (remainingPiece <= 0) {
                    economy.withdraw(buyer.uuid, shop.signData.price)
                    economy.deposit(vendor.uuid) { shop.signData.price }

                    val args = transfer[0].item.translationKey.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val itemName = args[args.size - 1]

                    vendorPlayer?.sendMessage(Text.of("You sold for " + shop.signData.itemAmount + " of " + itemName + " to " + buyer.name), false)
                    buyerPlayer.sendMessage(Text.of("You have bought for ${shop.signData.itemAmount} of $itemName to ${shop.signData.vendorName}"), false)
                    for (itemStack in transfer) buyerPlayer.dropItem(itemStack, false)
                }
            }
        }
    }

    private fun getWallSignData(sign: SignBlockEntity): SignData? {
        val vendorName = sign.getTextOnRow(0, false).string

        // TODO this code, can potentially stuck the minecraft server thread
        databaseManager.cachePlayers.access { players -> players.find { player: Player ->  player.name == vendorName } } ?: return null

        val args = sign.getTextOnRow(2, false).string.split(" ")
        if (args.count() != 3) return null
        return try {
            val itemAmount = args[0].toInt()
            val price = args[2].toFloat()
            SignData(vendorName, itemAmount, price)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    /**
     * Returns the blockEntity behind the block corresponding to the "blockPos" parameter. If no blockEntity is found the returned value will be null
     */
    private inline fun <reified T : BlockEntity> getBlockBehind(blockPos: BlockPos, world: World, direction: Direction): T? {
        val pos: BlockPos = when (direction) {
            Direction.NORTH -> BlockPos(blockPos.x, blockPos.y, blockPos.z + 1)
            Direction.SOUTH -> BlockPos(blockPos.x, blockPos.y, blockPos.z - 1)
            Direction.EAST -> BlockPos(blockPos.x - 1, blockPos.y, blockPos.z)
            Direction.WEST -> BlockPos(blockPos.x + 1, blockPos.y, blockPos.z)
            else -> null
        } ?: return null
        val blockEntity = world.getBlockEntity(pos)
        return if (blockEntity?.javaClass == T::class.java) blockEntity as T
        else null
    }

    private inline fun <reified T : BlockEntity> getBlocksAround(blockPos: BlockPos, world: World): MutableList<T> {
        val list = mutableListOf<T>()
        listOf(
            BlockPos(blockPos.x, blockPos.y, blockPos.z + 1),
            BlockPos(blockPos.x, blockPos.y, blockPos.z - 1),
            BlockPos(blockPos.x - 1, blockPos.y, blockPos.z),
            BlockPos(blockPos.x + 1, blockPos.y, blockPos.z),
        ).forEach {
            world.getBlockEntity(it)?.let { blockEntity ->
                if (blockEntity::class.java == T::class.java)
                    list.add(blockEntity as T)
            }
        }
        return list
    }

}
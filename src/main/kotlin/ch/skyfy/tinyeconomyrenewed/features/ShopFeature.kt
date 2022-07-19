package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.ScoreboardManager
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerInsertItemsCallback
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerTakeItemsCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.players
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.block.BarrelBlock
import net.minecraft.block.BlockState
import net.minecraft.block.WallSignBlock
import net.minecraft.block.WallSignBlock.FACING
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.ktorm.dsl.like
import org.ktorm.entity.find

class ShopFeature(
    private val databaseManager: DatabaseManager,
    private val economy: Economy,
    private val scoreboardManager: ScoreboardManager,
    private val minecraftServer: MinecraftServer
) {

    data class Shop(val barrelBlockEntity: BarrelBlockEntity, val signBlockEntities: MutableList<SignBlockEntity>, val signData: SignData)
    data class SignData(val vendorName: String, val itemAmount: Int, val price: Float)

    enum class PlayerState {
        ONLINE,
        OFFLINE,
        NOT_EXIST
    }

    init {

        UseBlockCallback.EVENT.register(this::useBlockCallback)
        PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak)
        PlayerTakeItemsCallback.EVENT.register { playerEntity, inventory ->
            if (inventory is BarrelBlockEntity) {
                val shop = isAShop(inventory.pos, playerEntity.getWorld())
                if (shop != null && shop.signData.vendorName != playerEntity.name.string) {
                    return@register ActionResult.FAIL
                }
            }
            ActionResult.PASS
        }
        UseItemCallback.EVENT.register { player, world, hand ->

            TypedActionResult.pass(ItemStack.EMPTY)
        }

        PlayerInsertItemsCallback.EVENT.register { playerEntity, inventory ->
            if (inventory is BarrelBlockEntity) {
                val shop = isAShop(inventory.pos, playerEntity.getWorld())
                if (shop != null && shop.signData.vendorName != playerEntity.name.string) {
                    return@register false
                }
            }
            true
        }

//        CanPlayerUseInventoryCallback.EVENT.register{playerEntity, inventory ->
//            if (inventory is BarrelBlockEntity) {
//
//                val shop = isAShop(inventory.pos, playerEntity.getWorld())
//                if (shop != null && shop.signData.vendorName != playerEntity.name.string) {
//                    return@register false
//                }
//            }
//            true
//        }

    }

    @Suppress("UNUSED_PARAMETER")
    private fun beforeBlockBreak(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?): Boolean {
        val block = world.getBlockState(pos).block
        val shop = isAShop(pos, world)

        if (shop != null) {
            if (block is BarrelBlock || block is WallSignBlock) {
                if (shop.signData.vendorName != player.name.string) return false
            }
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun useBlockCallback(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult): ActionResult {

        // Prevents a player from robbing a shop with a hopper
        // There is a trick, player can still steal with hopper, I'll leave this trick available for crafty players
        for (itemStack in player.itemsHand) {
            println("itemStack.item.translationKey " + itemStack.item.translationKey)
            if (itemStack.item.translationKey == "block.minecraft.hopper" || itemStack.item.translationKey == "item.minecraft.hopper_minecart") {
                val shop = isAShop(BlockPos(hitResult.pos.x, hitResult.pos.y + 1, hitResult.pos.z), world)
                if (shop != null && shop.signData.vendorName != player.name.string) return ActionResult.FAIL
            }
        }

        val block = world.getBlockState(hitResult.blockPos).block
        val shop = isAShop(hitResult.blockPos, world)

        if (shop != null) {
            if (block is WallSignBlock) {
                if (shop.signData.vendorName == player.name.string) {
                    player.sendMessage(Text.of("You cannot buy from your own shop"))
                    return ActionResult.PASS
                }
                println("Process transaction")
                processTransaction(player as ServerPlayerEntity, shop)
            } else if (block is BarrelBlock) {
                println("its a shop, first block clicked = barrelBlock. Nothing to do")
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

    private fun processTransaction(buyerPlayer: ServerPlayerEntity, shop: Shop) {

        val vendorPlayer: ServerPlayerEntity? = minecraftServer.playerManager.getPlayer(shop.signData.vendorName)

        val vendor = databaseManager.db.players.find { it.name like if (vendorPlayer != null) vendorPlayer.name.string else shop.signData.vendorName }
        val buyer = databaseManager.db.players.find { it.uuid like buyerPlayer.uuidAsString }

        if (vendor == null || buyer == null) {
            TinyEconomyRenewedMod.LOGGER.info("vendor or buyer was not found in database (null)")
            return
        }
        if (buyer.money - shop.signData.price < 0) {
            buyerPlayer.sendMessage(Text.of("You don't have enough money"), false)
            return
        }

        val barrelBlockEntity = shop.barrelBlockEntity

        // Count the numbers of item in the barrel
        var availableItemStack = 0
        for (i in 0 until barrelBlockEntity.size()) {
            val it = barrelBlockEntity.getStack(i)
            if (!it.isEmpty) availableItemStack += it.count
        }

        if (availableItemStack < shop.signData.itemAmount) {
            buyerPlayer.sendMessage(Text.of("There are not enough items in stock!"), false)
            return
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
            economy.withdraw(buyer, shop.signData.price)
            economy.deposit(vendor, shop.signData.price)

            scoreboardManager.updateSidebar(buyerPlayer)
            if (vendorPlayer != null) scoreboardManager.updateSidebar(vendorPlayer)

            val args = transfer[0].item.translationKey.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val itemName = args[args.size - 1]

            vendorPlayer?.sendMessage(Text.of("You sold for " + shop.signData.itemAmount + " of " + itemName + " to " + buyer.name), false)
            buyerPlayer.sendMessage(Text.of("You have bought for ${shop.signData.itemAmount} of $itemName to ${shop.signData.vendorName}"), false)
            for (itemStack in transfer) buyerPlayer.dropItem(itemStack, false)
        }
    }

    private fun getWallSignData(sign: SignBlockEntity): SignData? {
        val vendorName = sign.getTextOnRow(0, false).string
        if (getPlayerState(vendorName) == PlayerState.NOT_EXIST) return null
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
            else -> {
                null
            }
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
                println("block entity : " + blockEntity.type.toString())
                println("block entity : " + blockEntity::class.java.toString())
                if (blockEntity::class.java == T::class.java)
                    list.add(blockEntity as T)
            }
        }
        return list
    }

    private fun getPlayerState(name: String): PlayerState {
        databaseManager.db.players.find { it.name like name }.let { if (it == null) return PlayerState.NOT_EXIST }
        minecraftServer.playerManager.getPlayer(name) ?: return PlayerState.OFFLINE
        return PlayerState.ONLINE
    }

}
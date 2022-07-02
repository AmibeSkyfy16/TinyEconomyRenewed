package ch.skyfy.tinyeconomyrenewed.features

import ch.skyfy.tinyeconomyrenewed.Economy
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerTakeItemsCallback
import ch.skyfy.tinyeconomyrenewed.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.db.Players
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.BarrelBlock
import net.minecraft.block.WallSignBlock
import net.minecraft.block.WallSignBlock.FACING
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.SignBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.ktorm.database.Database
import org.ktorm.dsl.like
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class ShopFeature(private val databaseManager: DatabaseManager, private val economy: Economy, private val minecraftServer: MinecraftServer) {

    data class ShopResult(val isShop: Boolean, val cancel: Boolean, val vendorName: String){
        companion object{
            fun notAShop():ShopResult = ShopResult(isShop = false, cancel = false, vendorName = "")
        }
    }

    enum class PlayerState{
        ONLINE,
        OFFLINE,
        NOT_EXIST
    }

    private val Database.players get() = this.sequenceOf(Players)


    init {

        UseBlockCallback.EVENT.register(this::useBlockCallback)

        PlayerTakeItemsCallback.EVENT.register { playerEntity, inventory ->

            if(inventory is BarrelBlockEntity){
                println("barrel block entity")
                val test = isAShop<PlayerTakeItemsCallback>(inventory.pos, playerEntity.getWorld(), playerEntity)
                println("is shop: $test")
            }

            println("take item event$inventory")


            ActionResult.PASS
        }

    }

    private fun useBlockCallback(player: PlayerEntity, world: World, hand: Hand, hitResult: BlockHitResult): ActionResult {

        // Prevents a player from robbing a shop with a hopper
        // There is a trick, player can still steal with hopper, I'll leave this trick available for crafty players
        for (itemStack in player.itemsHand) {
            if (itemStack.item.translationKey == "block.minecraft.hopper") {
                val shopResult: ShopResult = isAShop<UseBlockCallback>(BlockPos(hitResult.pos.x, hitResult.pos.y + 1, hitResult.pos.z), world, player)
                if (shopResult.isShop && shopResult.vendorName != player.name.string) return ActionResult.FAIL
            }
        }

        return ActionResult.PASS
    }

    @Suppress("ImplicitThis", "FoldInitializerAndIfToElvis")
    private inline fun <reified T> isAShop(blockPos: BlockPos, world: World, buyer: PlayerEntity): ShopResult {
        if (world.server == null) return ShopResult.notAShop()

        val blockState = world.getBlockState(blockPos)

        var isBarrelBehindSign = false
        var barrelBlockEntity: BarrelBlockEntity? = null
        var signFirstClicked = false
        val signBlockEntities: MutableList<SignBlockEntity> = mutableListOf()

        if (blockState.block is WallSignBlock) {
            val barrelBlockEntityFound = getBlockBehind<BarrelBlockEntity>(blockPos, world, world.getBlockState(blockPos).get(FACING))

            if ((barrelBlockEntityFound !is BarrelBlockEntity))return ShopResult.notAShop()

            isBarrelBehindSign = true
            signFirstClicked = true

            (world.getBlockEntity(blockPos) as SignBlockEntity?)?.let { signBlockEntities.add(it) }

            barrelBlockEntity = barrelBlockEntityFound
        } else if (blockState.block is BarrelBlock) {
            signBlockEntities.addAll(getBlocksAround(blockPos, world))
            barrelBlockEntity = world.getBlockEntity(blockPos) as BarrelBlockEntity
        }

        // If one of this two values are null, the player click on a sign or a barrel that is not a shop
       if (barrelBlockEntity == null)return ShopResult.notAShop()

        if(signBlockEntities.isEmpty())return ShopResult.notAShop()

        // If many wall sign are around the barrel, and they are the same, it's ok (it's a shop)
        val firstSignData = getWallSignData(signBlockEntities[0])
        if(firstSignData == null)return ShopResult.notAShop()
        for (i in 1 until signBlockEntities.count()) {
            val anotherSignData = getWallSignData(signBlockEntities[i])
            if(anotherSignData == null)return ShopResult.notAShop()
            if(anotherSignData != firstSignData)return ShopResult.notAShop()
        }

//        val optGP = minecraftServer.userCache.findByName(firstSignData.vendorName)
//        val vendor: ServerPlayerEntity? = world.server?.playerManager?.getPlayer(firstSignData.vendorName)
        val state: PlayerState = getPlayerState(firstSignData.vendorName)

        if(state == PlayerState.NOT_EXIST)return ShopResult.notAShop()


        if(T::class.java == UseBlockCallback::class.java){
            if(buyer.name.string == firstSignData.vendorName){
                buyer.sendMessage(Text.of("You can't buy from yourself!"), false)
                return ShopResult(isShop = true, cancel = false, vendorName = firstSignData.vendorName)
            }else{
                if(signFirstClicked){
                    // TODO Process transaction
                    println("Process transaction")
                }
            }
        }else if(T::class.java == AttackBlockCallback::class.java){
            if(buyer.name.string != firstSignData.vendorName){
                return ShopResult(isShop = true, cancel = true, vendorName = firstSignData.vendorName)
            }
        }

        return ShopResult(isShop = true, cancel = false, vendorName = firstSignData.vendorName)
    }

    data class SignData(val vendorName: String, val itemAmount: Int, val price: Float)

    private fun getWallSignData(sign: SignBlockEntity) : SignData? {
        val args = sign.getTextOnRow(2, false).string.split(" ")
        if(args.count() != 3) return null
        return try {
            val itemAmount = args[0].toInt()
            val price = args[2].toFloat()
            val vendorName = sign.getTextOnRow(0, false).string
            SignData(vendorName, itemAmount, price)
        }catch (e: java.lang.Exception){
            null
        }
    }

    private inline fun <reified T> getBlockNear(blockPos: BlockPos, world: World): BlockEntity? {
        for (entity in ArrayList(
            listOf(
                world.getBlockEntity(BlockPos(blockPos.x, blockPos.y, blockPos.z + 1)),
                world.getBlockEntity(BlockPos(blockPos.x, blockPos.y, blockPos.z - 1)),
                world.getBlockEntity(BlockPos(blockPos.x - 1, blockPos.y, blockPos.z)),
                world.getBlockEntity(BlockPos(blockPos.x + 1, blockPos.y, blockPos.z))
            )
        )) if (entity != null && entity.javaClass == T::class.java) return entity
        return null
    }

    /**
     * Returns the blockEntity behind the block corresponding to the "blockPos" parameter. If no blockEntity is found the returned value will be null
     */
    private inline fun <reified T> getBlockBehind(blockPos: BlockPos, world: World, direction: Direction): BlockEntity? {
        val pos: BlockPos? = when (direction) {
            Direction.NORTH -> BlockPos(blockPos.x, blockPos.y, blockPos.z + 1)
            Direction.SOUTH -> BlockPos(blockPos.x, blockPos.y, blockPos.z - 1)
            Direction.EAST -> BlockPos(blockPos.x - 1, blockPos.y, blockPos.z)
            Direction.WEST -> BlockPos(blockPos.x + 1, blockPos.y, blockPos.z)
            else -> {
                null
            }
        }
        val blockEntity = world.getBlockEntity(pos)
        return if (pos != null && blockEntity != null && blockEntity.javaClass == T::class.java) blockEntity
        else null
    }

    private inline fun <reified T : BlockEntity> getBlocksAround(blockPos: BlockPos, world: World): MutableList<T> {
        val list = mutableListOf<T>()
        listOf(
            BlockPos(blockPos.x, blockPos.y, blockPos.z + 1),
            BlockPos(blockPos.x, blockPos.y, blockPos.z - 1),
            BlockPos(blockPos.x - 1, blockPos.y, blockPos.z),
            BlockPos(blockPos.x + 1, blockPos.y, blockPos.z),
        ).forEach { world.getBlockEntity(it)?.let { blockEntity ->  if (blockEntity == T::class.java) list.add(blockEntity as T) } }
        return list
    }

    private fun getPlayerState(name: String): PlayerState {
        databaseManager.db.players.find { it.name like name }.let { if(it == null)return PlayerState.NOT_EXIST }
        minecraftServer.playerManager.getPlayer(name) ?: return PlayerState.OFFLINE
        return PlayerState.ONLINE
    }

}
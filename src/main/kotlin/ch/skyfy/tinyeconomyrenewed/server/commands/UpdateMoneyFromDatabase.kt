package ch.skyfy.tinyeconomyrenewed.server.commands


import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.LOGGER
import ch.skyfy.tinyeconomyrenewed.server.TinyEconomyRenewedInitializer.Companion.LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE
import ch.skyfy.tinyeconomyrenewed.server.logic.Game
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.launch
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull

/**
 * Allow an Administrator to modify the money of a player from the database, the update it to this mod
 */
class UpdateMoneyFromDatabase(private val optGameRef: AtomicReference<Optional<Game>>) : Command<ServerCommandSource> {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val command = literal("updateMoneyFromDatabase").requires { it.hasPermissionLevel(4) }.executes(UpdateMoneyFromDatabase(optGameRef))
        dispatcher.register(command)
    }

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val game = optGameRef.get().getOrNull() ?: return Command.SINGLE_SUCCESS

        var spe: ServerPlayerEntity? = null
        if (context.source.entity is ServerPlayerEntity) {
            spe = context.source.entity as ServerPlayerEntity
//            if (!spe.hasPermissionLevel(4)) {
//                spe.sendMessage(Text.literal("Only administrator can use this command").setStyle(Style.EMPTY.withColor(Formatting.RED)))
//                return Command.SINGLE_SUCCESS
//            }
        }

//        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            game.databaseManager.modifyPlayers {cachePlayers ->
                cachePlayers.clear()
                cachePlayers.addAll(game.databaseManager.getAllPlayersAsMutableList())
                if(spe != null) game.updateScoreBoard(spe.uuidAsString)
                LOGGER.info("Command: updateMoneyFromDatabase executed, players data from database has been update to the code")
            }
//        }

        return Command.SINGLE_SUCCESS
    }
}
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

/**
 * Allow an Administrator to modify the money of a player from the database, the update it to this mod
 */
class UpdateMoneyFromDatabase(private val optGameRef: AtomicReference<Optional<Game>>) : Command<ServerCommandSource> {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val command = literal("updateMoneyFromDatabase").executes(UpdateMoneyFromDatabase(optGameRef))
        dispatcher.register(command)
    }

    override fun run(context: CommandContext<ServerCommandSource>): Int {
        if (optGameRef.get().isEmpty) return Command.SINGLE_SUCCESS

        var player: ServerPlayerEntity? = null
        if (context.source.entity is ServerPlayerEntity) {
            player = context.source.entity as ServerPlayerEntity
            if (!player.hasPermissionLevel(4)) {
                player.sendMessage(Text.literal("Only administrator can use this command").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                return Command.SINGLE_SUCCESS
            }
        }

        val game = optGameRef.get().get()
        LEAVE_THE_MINECRAFT_THREAD_ALONE_SCOPE.launch {
            game.databaseManager.modifyPlayers {
                game.databaseManager.cachePlayers.clear()
                game.databaseManager.cachePlayers.addAll(game.databaseManager.getAllPlayersAsMutableList())
                if(player != null) game.scoreboardManager.updatePlayerMoney(player.uuidAsString)
                LOGGER.info("Command: updateMoneyFromDatabase executed, players data from database has been update to the code")
            }
        }

        return Command.SINGLE_SUCCESS
    }
}
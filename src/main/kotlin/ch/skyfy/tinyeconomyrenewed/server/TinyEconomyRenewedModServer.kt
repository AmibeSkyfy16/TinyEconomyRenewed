package ch.skyfy.tinyeconomyrenewed.server

import ch.skyfy.tinyeconomyrenewed.server.commands.UpdateMoneyFromDatabase
import ch.skyfy.tinyeconomyrenewed.server.logic.Game
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class TinyEconomyRenewedModServer : DedicatedServerModInitializer {

    /**
     * This will be use in future for registered command because command must be
     * registered in [TinyEconomyRenewedModServer.onInitializeServer] so we can pass this ref to every command object
     */
    private val optGameRef: AtomicReference<Optional<Game>> = AtomicReference(Optional.empty())

    init { TinyEconomyRenewedInitializer(optGameRef) }

    override fun onInitializeServer() {registerCommands()}

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            UpdateMoneyFromDatabase(optGameRef).register(dispatcher)
        }
    }

}
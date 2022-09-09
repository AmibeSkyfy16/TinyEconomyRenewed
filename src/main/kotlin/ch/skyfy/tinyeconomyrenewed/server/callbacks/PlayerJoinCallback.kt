package ch.skyfy.tinyeconomyrenewed.server.callbacks

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.ClientConnection
import net.minecraft.server.network.ServerPlayerEntity

fun interface PlayerJoinCallback {
    companion object {
        @JvmField
        val EVENT: Event<PlayerJoinCallback> = EventFactory.createArrayBacked(PlayerJoinCallback::class.java){ listeners ->
            PlayerJoinCallback{ connection, player ->
                for(listener in listeners) listener.joinServer(connection, player)
            }
        }
    }

    fun joinServer(connection: ClientConnection, player: ServerPlayerEntity)
}
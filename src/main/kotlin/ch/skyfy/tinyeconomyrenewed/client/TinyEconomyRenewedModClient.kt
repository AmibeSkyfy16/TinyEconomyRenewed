package ch.skyfy.tinyeconomyrenewed.client

import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.CLIENT_HAS_THE_MOD
import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerJoinCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

@Suppress("unused")
class TinyEconomyRenewedModClient : ClientModInitializer {

    override fun onInitializeClient() {

        ClientPlayConnectionEvents.JOIN.register{handler, sender, client ->
            // If player join a dedicated server
            if(client.server == null){
                ClientPlayNetworking.send(CLIENT_HAS_THE_MOD, PacketByteBufs.empty())
            }
        }


    }

}
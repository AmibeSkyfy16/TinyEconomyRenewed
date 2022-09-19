package ch.skyfy.tinyeconomyrenewed.client

import ch.skyfy.tinyeconomyrenewed.server.utils.setupConfigDirectory
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
class TinyEconomyRenewedModClient : ClientModInitializer {

    init { setupConfigDirectory() }

    override fun onInitializeClient() {}

}
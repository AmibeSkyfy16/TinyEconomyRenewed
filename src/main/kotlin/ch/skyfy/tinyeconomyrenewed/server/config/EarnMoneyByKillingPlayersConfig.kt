package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EarnMoneyByKillingPlayersConfig(
    val amount: Double = 50.0,
    val shouldKilledPlayerLostMoneyToo: Boolean = true
) : Validatable
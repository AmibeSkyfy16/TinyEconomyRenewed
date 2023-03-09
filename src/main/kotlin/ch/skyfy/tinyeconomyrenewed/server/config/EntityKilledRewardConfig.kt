package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EntityKilledRewardConfig(
    val list: MutableList<EntityKilledReward> = mutableListOf()
) : Validatable

@Serializable
data class EntityKilledReward(
    val translationKey: String,
    var currentPrice: Double,
    val maximumEntityKilledPerMinute: Double,
    var cryptoCurrencyName: String,
    var lastCryptoPrice: Double
) : Validatable
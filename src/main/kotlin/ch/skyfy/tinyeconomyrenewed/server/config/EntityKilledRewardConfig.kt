package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EntityKilledRewardConfig(
//    val map: MutableMap<String, Double>
    val list: MutableList<EntityKilledReward> = mutableListOf()
) : Validatable

@Serializable
data class EntityKilledReward(
    val translationKey: String,
    val maximumEntityKilledPerMinute: Double,
    var currentPrice: Double,
    var basedCryptoCurrencyName: String,
    var lastCryptoPrice: Double
) : Validatable

//class DefaultEntityKilledRewardConfig : Defaultable<EntityKilledRewardConfig>{
//    override fun getDefault() = EntityKilledRewardConfig(mutableMapOf())
//}
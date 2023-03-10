package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EntityKilledRewardConfig(
    val list: MutableList<EntityKilledRewardData> = mutableListOf()
) : Validatable

//@Serializable
//data class EntityKilledReward(
//    val translationKey: String,
//    var currentPrice: Double,
//    val maximumEntityKilledPerMinute: Double,
//    var cryptoCurrencyName: String,
//    var lastCryptoPrice: Double
//) : Validatable

@Serializable
data class EntityKilledRewardData(
    override val translationKey: String,
    override var currentPrice: Double,
    override val maximumPerMinute: Double,
    override var cryptoCurrencyName: String,
    override var lastCryptoPrice: Double
) : CryptoBasedPriceReward(), Validatable

package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class MinedBlockRewardConfig(
//    val map: MutableMap<String, Float>
    val list: MutableList<MinedBlockRewardData>
) : Validatable

//@Serializable
//data class MinedBlockReward(
//    val translationKey: String,
//    var currentPrice: Double,
//    val maximumMinedBlockPerMinute: Double,
//    var cryptoCurrencyName: String,
//    var lastCryptoPrice: Double
//) : Validatable

@Serializable
data class MinedBlockRewardData(
    override val translationKey: String,
    override var currentPrice: Double,
    override val maximumPerMinute: Double,
    override var cryptoCurrencyName: String,
    override var lastCryptoPrice: Double
) : CryptoBasedPriceReward(), Validatable

abstract class CryptoBasedPriceReward {
    abstract val translationKey: String
    abstract var currentPrice: Double
    abstract val maximumPerMinute: Double
    abstract var cryptoCurrencyName: String
    abstract var lastCryptoPrice: Double
}

class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig> {
    //    override fun getDefault() = MinedBlockRewardConfig(mutableMapOf())
    override fun getDefault() = MinedBlockRewardConfig(mutableListOf())
}
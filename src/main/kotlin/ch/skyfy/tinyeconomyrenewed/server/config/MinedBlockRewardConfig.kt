package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class MinedBlockRewardConfig(
//    val map: MutableMap<String, Float>
    val list: MutableList<MinedBlockReward>
) : Validatable

@Serializable
data class MinedBlockReward(
    val translationKey: String,
    val maximumNumberPerMinute: Double,
    var currentPrice: Double,
    var basedCryptoCurrencyName: String,
    var lastCryptoPrice: Double
) : Validatable



class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig>{
//    override fun getDefault() = MinedBlockRewardConfig(mutableMapOf())
    override fun getDefault() = MinedBlockRewardConfig(mutableListOf())
}
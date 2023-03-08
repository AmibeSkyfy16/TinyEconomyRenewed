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
    val average: Average,
    var currentPrice: Double,
    var basedCryptoCurrencyName: String,
    var lastCryptoPrice: Double,
    val percentUp: Int,
    val percentDown: Int,
) : Validatable

@Serializable
data class Average(
    val defaultPrice: Double,
    val numberPerMinute: Double,
) : Validatable

class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig>{
//    override fun getDefault() = MinedBlockRewardConfig(mutableMapOf())
    override fun getDefault() = MinedBlockRewardConfig(mutableListOf())
}
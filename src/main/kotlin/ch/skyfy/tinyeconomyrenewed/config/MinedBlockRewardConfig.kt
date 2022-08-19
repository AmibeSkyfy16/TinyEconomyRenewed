package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
data class MinedBlockRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig>{
    override fun getDefault() = MinedBlockRewardConfig(mutableMapOf())
}
package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable

@kotlinx.serialization.Serializable
data class MinedBlockRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig>{
    override fun getDefault() = MinedBlockRewardConfig(mutableMapOf())
}
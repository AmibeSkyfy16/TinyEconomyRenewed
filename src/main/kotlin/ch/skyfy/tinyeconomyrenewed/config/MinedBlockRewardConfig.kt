package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.Defaultable
import ch.skyfy.jsonconfig.Validatable
import ch.skyfy.tinyeconomyrenewed.DataRetriever

@kotlinx.serialization.Serializable
data class MinedBlockRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultMinedBlockRewardConfig : Defaultable<MinedBlockRewardConfig>{
    override fun getDefault(): MinedBlockRewardConfig {
        val map: MutableMap<String, Float> = mutableMapOf()
        DataRetriever.blocks.forEach { map[it] = 1f }
        return MinedBlockRewardConfig(map)
    }
}
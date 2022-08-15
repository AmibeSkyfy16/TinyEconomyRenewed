package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.Defaultable
import ch.skyfy.jsonconfig.Validatable
import ch.skyfy.tinyeconomyrenewed.DataRetriever

@kotlinx.serialization.Serializable
data class AdvancementRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultAdvancementRewardConfig : Defaultable<AdvancementRewardConfig>{
    override fun getDefault(): AdvancementRewardConfig {
        val map: MutableMap<String, Float> = mutableMapOf()
        DataRetriever.advancements.forEach { map[it.advancementId] = 1f }
        return AdvancementRewardConfig(map)
    }
}
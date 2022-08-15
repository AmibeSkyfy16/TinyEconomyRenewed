package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.Defaultable
import ch.skyfy.jsonconfig.Validatable
import ch.skyfy.tinyeconomyrenewed.DataRetriever

@kotlinx.serialization.Serializable
data class EntityKilledRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultEntityKilledRewardConfig : Defaultable<EntityKilledRewardConfig>{
    override fun getDefault(): EntityKilledRewardConfig {
        val map: MutableMap<String, Float> = mutableMapOf()
        DataRetriever.entities.forEach { map[it] = 1f }
        return EntityKilledRewardConfig(map)
    }
}
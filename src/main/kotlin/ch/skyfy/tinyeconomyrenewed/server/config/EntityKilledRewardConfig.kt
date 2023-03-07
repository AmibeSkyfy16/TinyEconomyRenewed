package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable

@kotlinx.serialization.Serializable
data class EntityKilledRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultEntityKilledRewardConfig : Defaultable<EntityKilledRewardConfig>{
    override fun getDefault() = EntityKilledRewardConfig(mutableMapOf())
}
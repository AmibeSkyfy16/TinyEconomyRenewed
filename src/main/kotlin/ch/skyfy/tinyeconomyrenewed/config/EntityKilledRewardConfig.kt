package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
data class EntityKilledRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultEntityKilledRewardConfig : Defaultable<EntityKilledRewardConfig>{
    override fun getDefault() = EntityKilledRewardConfig(mutableMapOf())
}
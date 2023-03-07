package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable

@kotlinx.serialization.Serializable
data class AdvancementRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultAdvancementRewardConfig : Defaultable<AdvancementRewardConfig> {
    override fun getDefault() = AdvancementRewardConfig(mutableMapOf())
}
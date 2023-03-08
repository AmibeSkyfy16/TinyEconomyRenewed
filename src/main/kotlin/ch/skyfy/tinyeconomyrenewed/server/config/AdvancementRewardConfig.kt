package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
data class AdvancementRewardConfig(
    val map: MutableMap<String, Double>
) : Validatable

class DefaultAdvancementRewardConfig : Defaultable<AdvancementRewardConfig> {
    override fun getDefault() = AdvancementRewardConfig(mutableMapOf())
}
package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
data class AdvancementRewardConfig(
    val map: MutableMap<String, Float>
) : Validatable

class DefaultAdvancementRewardConfig : Defaultable<AdvancementRewardConfig>{
    override fun getDefault() = AdvancementRewardConfig(mutableMapOf())
}
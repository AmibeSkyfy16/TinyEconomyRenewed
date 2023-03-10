package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
data class AdvancementRewardConfig(
    val map: MutableMap<String, Double> = mutableMapOf()
) : Validatable
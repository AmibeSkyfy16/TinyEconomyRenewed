package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
@JvmRecord
data class VillagerTradeCostsMoneyConfig(
    @JvmField
    val enabled: Boolean = true,
    val price: Double = 20.0,
    val amount: Int = 2
): Validatable
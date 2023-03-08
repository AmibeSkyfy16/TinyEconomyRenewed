package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
@JvmRecord
data class VillagerTradeCostsMoneyConfig(
    @JvmField
    val enabled: Boolean,
    val price: Double,
    val amount: Int
): Validatable

class DefaultVillagerTradeCostsMoneyConfig : Defaultable<VillagerTradeCostsMoneyConfig>{
    override fun getDefault() = VillagerTradeCostsMoneyConfig(false, 0.5, 2)
}

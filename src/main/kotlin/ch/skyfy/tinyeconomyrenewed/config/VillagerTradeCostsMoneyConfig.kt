package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable

@kotlinx.serialization.Serializable
@JvmRecord
data class VillagerTradeCostsMoneyConfig(
    @JvmField
    val enabled: Boolean,
    val price: Float,
    val amount: Int
): Validatable

class DefaultVillagerTradeCostsMoneyConfig : Defaultable<VillagerTradeCostsMoneyConfig>{
    override fun getDefault() = VillagerTradeCostsMoneyConfig(false, 0.5f, 2)
}

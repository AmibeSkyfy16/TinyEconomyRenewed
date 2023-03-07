package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable

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

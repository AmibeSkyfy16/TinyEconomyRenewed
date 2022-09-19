package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class LossMoneyDyingConfig(val amount: Float) : Validatable

class DefaultLossMoneyDyingConfig : Defaultable<LossMoneyDyingConfig> {
    override fun getDefault() = LossMoneyDyingConfig(50f)
}

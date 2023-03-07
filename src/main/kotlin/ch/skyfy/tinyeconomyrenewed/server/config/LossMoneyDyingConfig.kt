package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class LossMoneyDyingConfig(val amount: Float) : Validatable

class DefaultLossMoneyDyingConfig : Defaultable<LossMoneyDyingConfig> {
    override fun getDefault() = LossMoneyDyingConfig(50f)
}

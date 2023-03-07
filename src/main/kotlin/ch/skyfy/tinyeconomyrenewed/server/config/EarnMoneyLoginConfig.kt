package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EarnMoneyLoginConfig(val amount: Float) : Validatable

class DefaultEarnMoneyLoginConfig : Defaultable<EarnMoneyLoginConfig> {
    override fun getDefault() = EarnMoneyLoginConfig(10f)
}
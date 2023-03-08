package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EarnMoneyLoginConfig(val amount: Double) : Validatable

class DefaultEarnMoneyLoginConfig : Defaultable<EarnMoneyLoginConfig> {
    override fun getDefault() = EarnMoneyLoginConfig(10.0)
}
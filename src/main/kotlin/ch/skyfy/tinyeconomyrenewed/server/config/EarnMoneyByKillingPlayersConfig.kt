package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Defaultable
import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EarnMoneyByKillingPlayersConfig(val amount: Float, val shouldKilledPlayerLostMoney: Boolean) : Validatable

class DefaultEarnMoneyByKillingPlayersConfig: Defaultable<EarnMoneyByKillingPlayersConfig> {
    override fun getDefault() = EarnMoneyByKillingPlayersConfig(10f, true)
}

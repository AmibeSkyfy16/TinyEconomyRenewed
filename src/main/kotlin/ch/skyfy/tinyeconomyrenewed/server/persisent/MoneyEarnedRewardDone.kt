package ch.skyfy.tinyeconomyrenewed.server.persisent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class MoneyEarnedRewardDone(
    val earnedRewardDone: MutableMap<String, MutableList<Float>>,
    val earnedRewardDoneAndReceived: MutableMap<String, MutableList<Float>>
) : Validatable

class DefaultMoneyEarnedRewardDone : Defaultable<MoneyEarnedRewardDone> {
    override fun getDefault() = MoneyEarnedRewardDone(mutableMapOf(), mutableMapOf())
}
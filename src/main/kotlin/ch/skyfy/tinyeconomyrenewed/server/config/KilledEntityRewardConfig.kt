package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class KilledEntityRewardConfig(
    val list: MutableList<KilledEntityRewardData> = mutableListOf()
) : Validatable

@Serializable
data class KilledEntityRewardData(
    override val translationKey: String,
    override var currentPrice: Double,
    override val maximumPerMinute: Double,
    override var cryptoCurrencyName: String,
    override var lastCryptoPrice: Double
) : CryptoBasedPriceReward(), Validatable

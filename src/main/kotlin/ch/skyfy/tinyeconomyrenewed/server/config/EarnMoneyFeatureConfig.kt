package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class EarnMoneyFeatureConfig(
    val minedBlockRewardNerfer: MinedBlockRewardNerfer = MinedBlockRewardNerfer(),
    val entityKilledRewardNerfer: EntityKilledRewardNerfer = EntityKilledRewardNerfer(),
    val priceToEarnForFirstLoggingOfTheDay: Double = 500.0
) : Validatable

/**
 * Contains configurable data to nerf the money won when a player mines blocks.
 * Example: if a player mines at a speed of more than 100 blocks/minute and almost
 * does not move (he stays in an area of 50 blocks²) and this since 5mn (300 seconds).
 * He is detected as afk and will not receive any more money
 *
 */
@Serializable
data class MinedBlockRewardNerfer(
    val lastXXXSeconds: Double = 300.0,
    val surfaceThatShouldNotBeExceeded: Double = 50.0,
    val maximumBlockPerMinute: Double = 100.0
) : Validatable

@Serializable
data class EntityKilledRewardNerfer(
    val lastXXXSeconds: Double = 300.0,
    val surfaceThatShouldNotBeExceeded: Double = 50.0,
    val maximumEntityKilledPerMinute: Double = 50.0
) : Validatable
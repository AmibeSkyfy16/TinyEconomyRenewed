package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class MoneyEarnedRewardConfig(
    val step: MutableMap<Double, MoneyEarnReward> =  mutableMapOf(
        1000.0 to MoneyEarnReward(32, mapOf("block.minecraft.acacia_wood" to 2).toMap()),
        5000.0 to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
        10_000.0 to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
        15_000.0 to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
        20_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        25_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        30_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        35_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        40_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        45_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        50_000.0 to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
        100_000.0 to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
        200_000.0 to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
        300_000.0 to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
        400_000.0 to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
        500_000.0 to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
        1_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        2_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        3_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        4_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        5_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        6_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        7_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        8_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        9_000_000.0 to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
        10_000_000.0 to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
        20_000_000.0 to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
        30_000_000.0 to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
        40_000_000.0 to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
        50_000_000.0 to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
        100_000_000.0 to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
        200_000_000.0 to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
        300_000_000.0 to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
        400_000_000.0 to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
        500_000_000.0 to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
        1_000_000_000.0 to MoneyEarnReward(4096, mapOf("item.minecraft.diamond" to 1000))
    )
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        var previous: Map.Entry<Double, MoneyEarnReward>? = null
        step.forEach { entry ->
            if(previous != null)
                if(previous!!.key > entry.key) errors.add("Wrong configuration for MoneyEarnedRewardConfig. The list is not ordered !!!")
            previous = entry
        }
    }
}

@Serializable
data class MoneyEarnReward(
    val xpAmount: Int,
    val earnedItems: Map<String, Int>
): Validatable
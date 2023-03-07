package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class MoneyEarnedRewardConfig(
    val step: Map<Float, MoneyEarnReward>
) : Validatable {
    override fun validateImpl(errors: MutableList<String>) {
        var previous: Map.Entry<Float, MoneyEarnReward>? = null
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
)

class DefaultMoneyEarnedRewardConfig : Defaultable<MoneyEarnedRewardConfig> {
    override fun getDefault() : MoneyEarnedRewardConfig {
        return MoneyEarnedRewardConfig(
            mutableMapOf(
                1000f to MoneyEarnReward(32, mapOf("block.minecraft.acacia_wood" to 2)),
                5000f to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
                10_000f to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
                15_000f to MoneyEarnReward(64, mapOf("block.minecraft.acacia_wood" to 2)),
                20_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                25_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                30_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                35_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                40_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                45_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                50_000f to MoneyEarnReward(128, mapOf("block.minecraft.acacia_wood" to 2)),
                100_000f to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
                200_000f to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
                300_000f to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
                400_000f to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
                500_000f to MoneyEarnReward(256, mapOf("block.minecraft.acacia_wood" to 2)),
                1_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                2_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                3_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                4_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                5_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                6_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                7_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                8_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                9_000_000f to MoneyEarnReward(512, mapOf("block.minecraft.acacia_wood" to 2)),
                10_000_000f to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
                20_000_000f to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
                30_000_000f to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
                40_000_000f to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
                50_000_000f to MoneyEarnReward(1024, mapOf("block.minecraft.acacia_wood" to 2)),
                100_000_000f to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
                200_000_000f to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
                300_000_000f to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
                400_000_000f to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
                500_000_000f to MoneyEarnReward(2048, mapOf("block.minecraft.acacia_wood" to 2)),
                1_000_000_000f to MoneyEarnReward(4096, mapOf("item.minecraft.diamond" to 1000))
            )
        )
//        return MoneyEarnedRewardConfig(mutableMapOf(1000f to MoneyEarnReward(32, mapOf("block.minecraft.acacia_wood" to 2))))
    }
}
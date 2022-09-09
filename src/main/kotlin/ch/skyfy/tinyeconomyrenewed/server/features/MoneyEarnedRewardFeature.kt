package ch.skyfy.tinyeconomyrenewed.server.features

import ch.skyfy.jsonconfiglib.ConfigManager
import ch.skyfy.tinyeconomyrenewed.both.CustomSounds
import ch.skyfy.tinyeconomyrenewed.server.config.Configs
import ch.skyfy.tinyeconomyrenewed.server.config.MoneyEarnReward
import ch.skyfy.tinyeconomyrenewed.server.persisent.Persistents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry

class MoneyEarnedRewardFeature {

    private val persistentData= Persistents.MONEY_EARNED_REWARD_DONE.`data`

    init {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->

            val player = handler.player

            // If the player received some money when he wasn't connected. We will have to get it back to earned rewards now !
            Persistents.MONEY_EARNED_REWARD_DONE.`data`.earnedRewardDone[player.uuidAsString]?.let { list ->

                if (list.size >= 1)
                    player.world.playSound(null, player.blockPos, CustomSounds.DOGECOIN_EVENT, SoundCategory.MASTER, 1f, 1f)

                list.forEach { step ->
                    Configs.MONEY_EARNED_REWARD_CONFIG.`data`.step[step]?.let { moneyEarnReward ->

                        player.sendMessage(Text.literal(""))

                        player.sendMessage(
                            Text.literal("You received money when you wasn't connected on the server !")
                                .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                        )
                        player.sendMessage(
                            Text.literal("Congratulations ! Your money amount reach $step !")
                                .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                        )

                        player.sendMessage(
                            Text.literal("You earn ${moneyEarnReward.xpAmount} of experience")
                                .setStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        )

                        player.addExperience(moneyEarnReward.xpAmount)
                        dropItem(player, moneyEarnReward)

                        player.sendMessage(Text.literal(""))

                        persistentData.earnedRewardDone.remove(player.uuidAsString)
                        persistentData.earnedRewardDoneAndReceived[player.uuidAsString] = list.toMutableList()
                        ConfigManager.save(Persistents.MONEY_EARNED_REWARD_DONE)
                    }
                }
            }

        }
    }

    fun rewardPlayer(player: ServerPlayerEntity?, uuid: String, amount: Float) {
        fun computeAndSave(map: MutableMap<String, MutableList<Float>>, correctStep: Map.Entry<Float, MoneyEarnReward>) {
            map.compute(uuid) { _, listOfStepDone ->
                if (listOfStepDone == null) return@compute mutableListOf(correctStep.key)
                else if (!listOfStepDone.contains(correctStep.key)) listOfStepDone.add(correctStep.key)
                return@compute listOfStepDone
            }
            ConfigManager.save(Persistents.MONEY_EARNED_REWARD_DONE)
        }

        // Every time a player earn money, we will check if he reaches a specific step (1000$, 10000$, etc.) and then give them some reward
        // But if the player is not connected, reward will be give to them the next time he connects

        val correctStep = Configs.MONEY_EARNED_REWARD_CONFIG.`data`.step
            .toList().sortedBy { (key, _) -> key }.reversed().toMap()
            .firstNotNullOfOrNull { if (it.key <= amount) it else null }

        if (correctStep != null) {
            if (player != null) {
                // Check if this player has already received a reward for the specific step
                val result1 = persistentData.earnedRewardDoneAndReceived[uuid]?.none { stepp -> stepp == correctStep.key }
                val result2 = persistentData.earnedRewardDone[uuid]?.none { stepp -> stepp == correctStep.key }

                if (result1 == null && result2 != null && !result2) return
                if (result2 == null && result1 != null && !result1) return

                player.world.playSound(null, player.blockPos, CustomSounds.DOGECOIN_EVENT, SoundCategory.MASTER, 1f, 1f)

                player.sendMessage(Text.literal(""))

                player.sendMessage(
                    Text.literal("Congratulations ! Your money amount reach ${correctStep.key} !")
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                )

                player.sendMessage(
                    Text.literal("You earn ${correctStep.value.xpAmount} of experience")
                        .setStyle(Style.EMPTY.withColor(Formatting.GOLD))
                )

                player.addExperience(correctStep.value.xpAmount)
                dropItem(player, correctStep.value)

                player.sendMessage(Text.literal(""))

                computeAndSave(persistentData.earnedRewardDoneAndReceived, correctStep)
            } else computeAndSave(persistentData.earnedRewardDone, correctStep)
        }

    }

    private fun dropItem(player: ServerPlayerEntity, moneyEarnReward: MoneyEarnReward) {
        player.server.execute {
            player.sendMessage(Text.literal("You earned the following item (dropping on the ground now)").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
            moneyEarnReward.earnedItems.forEach { earnedItem ->
                val item = Registry.ITEM.find { it.translationKey == earnedItem.key } ?: return@execute
                player.sendMessage(Text.literal("    - ${item.name.string} ${earnedItem.value}x").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                for (i in 0..earnedItem.value) player.dropItem(ItemStack(item, 1), true, false)
            }
        }
    }

}
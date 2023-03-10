package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.ConfigData
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY

object Configs {
    val DB_CONFIG = ConfigData.invokeSpecial<DatabaseConfig>(CONFIG_DIRECTORY.resolve("database-config.json"), true)
    val SHOP_CONFIG = ConfigData.invokeSpecial<ShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json"), true)
    val VILLAGER_TRADE_COSTS_MONEY_CONFIG = ConfigData.invokeSpecial<VillagerTradeCostsMoneyConfig>(CONFIG_DIRECTORY.resolve("villager-trade-costs-money-config.json"), true)
    val MINED_BLOCK_REWARD_CONFIG = ConfigData.invokeSpecial<MinedBlockRewardConfig>(CONFIG_DIRECTORY.resolve("mined-block-reward-config.json"), true)
    val KILLED_ENTITY_REWARD_CONFIG = ConfigData.invokeSpecial<KilledEntityRewardConfig>(CONFIG_DIRECTORY.resolve("killed-entity-reward-config.json"), true)
    val ADVANCEMENT_REWARD_CONFIG = ConfigData.invokeSpecial<AdvancementRewardConfig>(CONFIG_DIRECTORY.resolve("advancement-reward-config.json"), true)

    val MONEY_EARNED_REWARD_CONFIG = ConfigData.invokeSpecial<MoneyEarnedRewardConfig>(CONFIG_DIRECTORY.resolve("money-earned-reward-config.json"), true)
    val LOSS_MONEY_DYING_CONFIG = ConfigData.invokeSpecial<LossMoneyDyingConfig>(CONFIG_DIRECTORY.resolve("loss-money-dying-config.json"), true)
    val EARN_MONEY_BY_KILLING_PLAYERS_CONFIG = ConfigData.invokeSpecial<EarnMoneyByKillingPlayersConfig>(CONFIG_DIRECTORY.resolve("earn-money-by-killing-players-config.json"), true)

    val EARN_MONEY_FEATURE_CONFIG = ConfigData.invokeSpecial<EarnMoneyFeatureConfig>(CONFIG_DIRECTORY.resolve("earn-money-feature-config.json"), true)
}
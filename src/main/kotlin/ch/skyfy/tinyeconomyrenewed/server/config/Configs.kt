package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.ConfigData
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY

object Configs {
    val DB_CONFIG = ConfigData<DatabaseConfig, DefaultDataConfig>(CONFIG_DIRECTORY.resolve("database-config.json"), true)
    val SHOP_CONFIG = ConfigData<ShopConfig, DefaultShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json"), true)
    val VILLAGER_TRADE_COSTS_MONEY_CONFIG = ConfigData<VillagerTradeCostsMoneyConfig, DefaultVillagerTradeCostsMoneyConfig>(CONFIG_DIRECTORY.resolve("villager-trade-costs-money-config.json"), true)

    val MINED_BLOCK_REWARD_CONFIG = ConfigData<MinedBlockRewardConfig, DefaultMinedBlockRewardConfig>(CONFIG_DIRECTORY.resolve("mined-block-reward-config.json"), true)
    val ENTITY_KILLED_REWARD_CONFIG = ConfigData<EntityKilledRewardConfig, DefaultEntityKilledRewardConfig>(CONFIG_DIRECTORY.resolve("entity-killed-reward-config.json"), true)
    val ADVANCEMENT_REWARD_CONFIG = ConfigData<AdvancementRewardConfig, DefaultAdvancementRewardConfig>(CONFIG_DIRECTORY.resolve("advancement-reward-config.json"), true)

    val MONEY_EARNED_REWARD_CONFIG = ConfigData<MoneyEarnedRewardConfig, DefaultMoneyEarnedRewardConfig>(CONFIG_DIRECTORY.resolve("money-earned-reward-config.json"), true)
    val EARN_MONEY_LOGIN_CONFIG = ConfigData<EarnMoneyLoginConfig, DefaultEarnMoneyLoginConfig>(CONFIG_DIRECTORY.resolve("earn-money-login-config.json"), true)
    val LOSS_MONEY_DYING_CONFIG = ConfigData<LossMoneyDyingConfig, DefaultLossMoneyDyingConfig>(CONFIG_DIRECTORY.resolve("loss-money-dying-config.json"), true)
    val EARN_MONEY_BY_KILLING_PLAYERS_CONFIG = ConfigData<EarnMoneyByKillingPlayersConfig, DefaultEarnMoneyByKillingPlayersConfig>(CONFIG_DIRECTORY.resolve("earn-money-by-killing-players-config.json"), true)
}
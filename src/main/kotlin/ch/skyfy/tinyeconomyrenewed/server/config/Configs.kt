package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.ConfigData
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY
import io.github.xn32.json5k.Json5

object Configs {
    val DB_CONFIG = ConfigData<DatabaseConfig, DefaultDataConfig>(CONFIG_DIRECTORY.resolve("database-config.json5"), true)
    val SHOP_CONFIG = ConfigData<ShopConfig, DefaultShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json5"), true)
    val VILLAGER_TRADE_COSTS_MONEY_CONFIG = ConfigData<VillagerTradeCostsMoneyConfig, DefaultVillagerTradeCostsMoneyConfig>(CONFIG_DIRECTORY.resolve("villager-trade-costs-money-config.json5"), true)
    val MINED_BLOCK_REWARD_CONFIG = ConfigData<MinedBlockRewardConfig, DefaultMinedBlockRewardConfig>(CONFIG_DIRECTORY.resolve("mined-block-reward-config.json5"), true)
    val ENTITY_KILLED_REWARD_CONFIG = ConfigData<EntityKilledRewardConfig, DefaultEntityKilledRewardConfig>(CONFIG_DIRECTORY.resolve("entity-killed-reward-config.json5"), true)
    val ADVANCEMENT_REWARD_CONFIG = ConfigData<AdvancementRewardConfig, DefaultAdvancementRewardConfig>(CONFIG_DIRECTORY.resolve("advancement-reward-config.json5"), true)

    val MONEY_EARNED_REWARD_CONFIG = ConfigData<MoneyEarnedRewardConfig, DefaultMoneyEarnedRewardConfig>(CONFIG_DIRECTORY.resolve("money-earned-reward-config.json5"), true)
    val EARN_MONEY_LOGIN_CONFIG = ConfigData<EarnMoneyLoginConfig, DefaultEarnMoneyLoginConfig>(CONFIG_DIRECTORY.resolve("earn-money-login-config.json5"), true)
    val LOSS_MONEY_DYING_CONFIG = ConfigData<LossMoneyDyingConfig, DefaultLossMoneyDyingConfig>(CONFIG_DIRECTORY.resolve("loss-money-dying-config.json5"), true)
    val EARN_MONEY_BY_KILLING_PLAYERS_CONFIG = ConfigData<EarnMoneyByKillingPlayersConfig, DefaultEarnMoneyByKillingPlayersConfig>(CONFIG_DIRECTORY.resolve("earn-money-by-killing-players-config.json5"), true)
}
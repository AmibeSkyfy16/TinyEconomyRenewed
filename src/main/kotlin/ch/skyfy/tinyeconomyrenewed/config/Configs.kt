package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfiglib.ConfigData
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY

object Configs {
    val DB_CONFIG = ConfigData<DatabaseConfig, DefaultDataConfig>(CONFIG_DIRECTORY.resolve("database-config.json"))
    val SHOP_CONFIG = ConfigData<ShopConfig, DefaultShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json"))
    val VILLAGER_TRADE_COSTS_MONEY_CONFIG = ConfigData<VillagerTradeCostsMoneyConfig, DefaultVillagerTradeCostsMoneyConfig>(CONFIG_DIRECTORY.resolve("villager-trade-costs-money-config.json"))

    val MINED_BLOCK_REWARD_CONFIG = ConfigData<MinedBlockRewardConfig, DefaultMinedBlockRewardConfig>(CONFIG_DIRECTORY.resolve("mined-block-reward-config.json"))
    val ENTITY_KILLED_REWARD_CONFIG = ConfigData<EntityKilledRewardConfig, DefaultEntityKilledRewardConfig>(CONFIG_DIRECTORY.resolve("entity-killed-reward-config.json"))
    val ADVANCEMENT_REWARD_CONFIG = ConfigData<AdvancementRewardConfig, DefaultAdvancementRewardConfig>(CONFIG_DIRECTORY.resolve("advancement-reward-config.json"))
}
package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.JsonData
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY

object Configs {
    val DB_CONFIG = JsonData<DatabaseConfig, DefaultDataConfig>(CONFIG_DIRECTORY.resolve("database-config.json"))
    val SHOP_CONFIG = JsonData<ShopConfig, DefaultShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json"))

    val MINED_BLOCK_REWARD_CONFIG = JsonData<MinedBlockRewardConfig, DefaultMinedBlockRewardConfig>(CONFIG_DIRECTORY.resolve("mined-block-reward-config.json"))
    val ENTITY_KILLED_REWARD_CONFIG = JsonData<EntityKilledRewardConfig, DefaultEntityKilledRewardConfig>(CONFIG_DIRECTORY.resolve("entity-killed-reward-config.json"))
    val ADVANCEMENT_REWARD_CONFIG = JsonData<AdvancementRewardConfig, DefaultAdvancementRewardConfig>(CONFIG_DIRECTORY.resolve("advancement-reward-config.json"))
}
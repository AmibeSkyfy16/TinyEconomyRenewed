package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.JsonData
import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY

object Configs {
    val DB_CONFIG = JsonData<DatabaseConfig, DefaultDataConfig>(CONFIG_DIRECTORY.resolve("database-config.json"))
    val SHOP_CONFIG = JsonData<ShopConfig, DefaultShopConfig>(CONFIG_DIRECTORY.resolve("shop-config.json"))
}
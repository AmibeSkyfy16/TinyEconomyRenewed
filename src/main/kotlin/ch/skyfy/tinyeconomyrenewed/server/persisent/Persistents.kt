package ch.skyfy.tinyeconomyrenewed.server.persisent

import ch.skyfy.jsonconfiglib.ConfigData
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod

object Persistents {
    val MONEY_EARNED_REWARD_DONE = ConfigData<MoneyEarnedRewardDone, DefaultMoneyEarnedRewardDone>(TinyEconomyRenewedMod.PERSISTENT_DIRECTORY.resolve("money-earned-reward-done.json5"), true)
    val PLAYER_INFOS = ConfigData<PlayerInfosPersistent, DefaultPlayerInfosPersistent>(TinyEconomyRenewedMod.PERSISTENT_DIRECTORY.resolve("player-infos.json5"), true)
}
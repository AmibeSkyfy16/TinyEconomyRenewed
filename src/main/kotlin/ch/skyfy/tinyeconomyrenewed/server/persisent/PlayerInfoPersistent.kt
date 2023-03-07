package ch.skyfy.tinyeconomyrenewed.server.persisent

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfosPersistent(val infos: MutableList<PlayerInfo>) : Validatable

@Serializable
data class PlayerInfo(val uuid: String, var lastLoginDate: String) : Validatable

class DefaultPlayerInfosPersistent : Defaultable<PlayerInfosPersistent> {
    override fun getDefault() = PlayerInfosPersistent(mutableListOf())
}
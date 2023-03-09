package ch.skyfy.tinyeconomyrenewed.server.config.data

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class BlockPosition(
    val x: Int,
    val y: Int,
    val z: Int
) : Validatable

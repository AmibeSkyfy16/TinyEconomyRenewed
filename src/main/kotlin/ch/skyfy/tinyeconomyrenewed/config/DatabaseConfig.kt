package ch.skyfy.tinyeconomyrenewed.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String
)

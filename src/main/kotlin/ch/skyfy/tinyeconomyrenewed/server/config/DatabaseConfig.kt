package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.jsonconfiglib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val url: String = "jdbc:mariadb://localhost:3307",
    val user: String = "root",
    val password: String = "Pa\$\$w0rd"
) :  Validatable
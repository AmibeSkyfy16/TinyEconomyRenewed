package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String
) :  Validatable

class DefaultDataConfig : Defaultable<DatabaseConfig>{
    override fun getDefault() = DatabaseConfig("jdbc:mariadb://localhost:3307", "root", "Pa\$\$w0rd")
}
package ch.skyfy.tinyeconomyrenewed.config

import ch.skyfy.jsonconfig.Defaultable
import ch.skyfy.jsonconfig.Validatable
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
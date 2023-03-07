@file:Suppress("unused")

package ch.skyfy.tinyeconomyrenewed.server.config

import ch.skyfy.json5configlib.Defaultable
import ch.skyfy.json5configlib.Validatable

@kotlinx.serialization.Serializable
data class ShopConfig(
    val allowShopsToBeDestroyedByAnExplosion: MutableMap<ExplosionType, Boolean>,
    val shopsCannotBeDestroyedByAnyExplosion: Boolean
) : Validatable

@kotlinx.serialization.Serializable
enum class ExplosionType(val id: String) {
    WITHER("entity.minecraft.wither_skull"),
    END_CRYSTAL("entity.minecraft.end_crystal"),
    CREEPER("entity.minecraft.creeper"),
    FIREBALL("entity.minecraft.fireball"),
    TNT("entity.minecraft.tnt"),
    BAD_RESPAWN_POINT("badRespawnPoint")
}

class DefaultShopConfig : Defaultable<ShopConfig> {
    override fun getDefault(): ShopConfig {

        val map = mutableMapOf<ExplosionType?, Boolean?>()
//        map.mapValues { it.value ?: false }

        return ShopConfig(
            mutableMapOf(
                ExplosionType.WITHER to false,
                ExplosionType.END_CRYSTAL to false,
                ExplosionType.CREEPER to false,
                ExplosionType.FIREBALL to false,
                ExplosionType.TNT to false,
                ExplosionType.BAD_RESPAWN_POINT to false
            ),
            false
        )
    }

}
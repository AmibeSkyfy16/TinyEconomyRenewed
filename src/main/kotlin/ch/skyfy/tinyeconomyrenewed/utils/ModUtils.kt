package ch.skyfy.tinyeconomyrenewed.utils

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import org.ktorm.database.Database
import org.ktorm.support.mysql.insertOrUpdate

class ModUtils {

    companion object{
        fun populateDatabase(database: Database){

            TinyEconomyRenewedMod.LOGGER.info("Populating database")

            val blocks= ReflectionUtils.getListOfTranslationKey(Blocks::class.java, Block::class.java)
            val items = ReflectionUtils.getListOfTranslationKey(Items::class.java, Item::class.java)
            val entities = ReflectionUtils.getListOfTranslationKey(EntityType::class.java, EntityType::class.java)

            for (item in items) {
                database.insertOrUpdate(ch.skyfy.tinyeconomyrenewed.db.Item){
                    set(it.translationKey, item)
                    onDuplicateKey {
                        set(it.translationKey, item)
                    }
                }
            }

            for (entity in entities) {
                database.insertOrUpdate(ch.skyfy.tinyeconomyrenewed.db.Entity){
                    set(it.translationKey, entity)
                    onDuplicateKey {
                        set(it.translationKey, entity)
                    }
                }
            }

        }
    }

}
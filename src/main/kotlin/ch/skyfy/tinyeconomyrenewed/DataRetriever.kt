package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCreatedCallback
import ch.skyfy.tinyeconomyrenewed.utils.ReflectionUtils
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items

object DataRetriever {

    val advancements: MutableList<Advancement> = mutableListOf()
    val items: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(Items::class.java, Item::class.java)
    val blocks: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(Blocks::class.java, Block::class.java)
    val entities: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(EntityType::class.java, EntityType::class.java)

    init {
        items.sortWith(compareBy { it })
        blocks.sortWith(compareBy { it })
        entities.sortWith(compareBy { it })

        AdvancementCreatedCallback.EVENT.register { id, display ->
            advancements.add(
                Advancement(
                    id.toString(),
                    display.frame.toString(),
                    display.title.string,
                    display.description.string
                )
            )
        }
    }

    data class Advancement(
        val advancementId: String,
        val advancementFrame: String,
        val advancementTitle: String,
        val advancementDescription: String
    )

}
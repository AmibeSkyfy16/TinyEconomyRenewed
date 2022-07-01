package ch.skyfy.tinyeconomyrenewed

import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCreatedCallback
import ch.skyfy.tinyeconomyrenewed.utils.ReflectionUtils
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import org.apache.commons.lang3.StringUtils

object DataRetriever {

    val advancements: HashSet<Advancement> = HashSet()
    val items: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(Items::class.java, Item::class.java)
    val blocks: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(Blocks::class.java, Block::class.java)
    val entities: ArrayList<String> = ReflectionUtils.getListOfTranslationKey(EntityType::class.java, EntityType::class.java)

    init {
        AdvancementCreatedCallback.EVENT.register { id, display ->
            advancements.add(
                Advancement(
                    id.toString(),
                    display.frame.toString(),
                    StringUtils.substringBetween(display.title.toString(), "'", "'"),
                    StringUtils.substringBetween(display.description.toString(), "'", "'")
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
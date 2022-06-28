package ch.skyfy.tinyeconomyrenewed.utils

import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item

@Suppress("unused")
class ReflectionUtils {

    companion object {
        fun <T, TYPE> getListOfTranslationKey(tClass: Class<T>, typeClass: Class<TYPE>): ArrayList<String> {
            val list = ArrayList<String>()

            val map = HashMap<Class<*>, String>()
            map[Block::class.java] = "method_9539"
            map[Item::class.java] = "method_7876"
            map[EntityType::class.java] = "method_5882"

            val yarnMethodName = map[typeClass] ?: return list

            for (field in tClass.declaredFields) {
                if (field.type == typeClass) {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val `object`: TYPE = field.get(null) as TYPE
                        list.add(typeClass.getMethod(yarnMethodName).invoke(`object`) as String)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return list
        }
    }

}


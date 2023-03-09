@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package ch.skyfy.tinyeconomyrenewed.server.db

import org.ktorm.schema.*

interface Player : org.ktorm.entity.Entity<Player> {
    companion object : org.ktorm.entity.Entity.Factory<Player>()

    val id: Int
    var uuid: String
    var name: String
    var money: Double
}

interface Item : org.ktorm.entity.Entity<Item> {
    companion object : org.ktorm.entity.Entity.Factory<Item>()

    val id: Int
    var translationKey: String
}

interface Block : org.ktorm.entity.Entity<Block> {
    companion object : org.ktorm.entity.Entity.Factory<Block>()

    val id: Int
    var translationKey: String
}

interface Entity : org.ktorm.entity.Entity<Entity> {
    companion object : org.ktorm.entity.Entity.Factory<Entity>()

    val id: Int
    var translationKey: String
}

interface Advancement : org.ktorm.entity.Entity<Advancement> {
    companion object : org.ktorm.entity.Entity.Factory<Advancement>()

    val id: Int
    var identifier: String
    var frame: String
    var title: String
    var description: String
}

interface MinedBlockReward : org.ktorm.entity.Entity<MinedBlockReward> {
    companion object : org.ktorm.entity.Entity.Factory<MinedBlockReward>()

    val id: Int
    var currentPrice: Double
    var maximumMinedBlockPerMinute: Double
    var cryptoCurrencyName: String
    var lastCryptoPrice: Double
    var block: Block
}

interface EntityKilledReward : org.ktorm.entity.Entity<EntityKilledReward> {
    companion object : org.ktorm.entity.Entity.Factory<EntityKilledReward>()

    val id: Int
    var currentPrice: Double
    var maximumEntityKilledPerMinute: Double
    var cryptoCurrencyName: String
    var lastCryptoPrice: Double
    var entity: Entity
}

interface AdvancementReward : org.ktorm.entity.Entity<AdvancementReward> {
    companion object : org.ktorm.entity.Entity.Factory<AdvancementReward>()

    val id: Int
    var amount: Double
    var advancement: Advancement
}

interface BlackListedPlacedBlock : org.ktorm.entity.Entity<BlackListedPlacedBlock> {
    companion object : org.ktorm.entity.Entity.Factory<BlackListedPlacedBlock>()

    val id: Int
    var x: Int
    var y: Int
    var z: Int
}

open class Players(alias: String?) : Table<Player>("player", alias) {
    companion object : Players(null)
    override fun aliased(alias: String) = Players(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val uuid = varchar("uuid").bindTo { it.uuid }
    val name = varchar("name").bindTo { it.name }
    val money = double("money").bindTo { it.money }
}
open class Items(alias: String?) : Table<Item>("item", alias) {
    companion object : Items(null)

    override fun aliased(alias: String) = Items(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}
open class Blocks(alias: String?) : Table<Block>("block", alias) {
    companion object : Blocks(null)

    override fun aliased(alias: String) = Blocks(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}
open class Entities(alias: String?) : Table<Entity>("entity", alias) {
    companion object : Entities(null)
    override fun aliased(alias: String) = Entities(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val translationKey = varchar("translation_key").bindTo { it.translationKey }
}
open class Advancements(alias: String?) : Table<Advancement>("advancement", alias) {
    companion object : Advancements(null)
    override fun aliased(alias: String) = Advancements(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val identifier = varchar("identifier").bindTo { it.identifier }
    val frame = varchar("frame").bindTo { it.frame }
    val title = varchar("title").bindTo { it.title }
    val description = varchar("description").bindTo { it.description }
}


open class MinedBlockRewards(alias: String?) : Table<MinedBlockReward>("mined_block_reward", alias) {
    companion object : MinedBlockRewards(null)

    override fun aliased(alias: String) = MinedBlockRewards(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val currentPrice = double("current_price").bindTo { it.currentPrice }
    val maximumMinedBlockPerMinute = double("maximum_mined_block_per_minute").bindTo { it.maximumMinedBlockPerMinute }
    val cryptoCurrencyName = varchar("crypto_currency_name").bindTo { it.cryptoCurrencyName }
    val lastCryptoPrice = double("last_crypto_price").bindTo { it.lastCryptoPrice }
    val blockId = int("block_id").references(Blocks) { it.block }
    val block get() = blockId.referenceTable as Blocks
}
open class EntityKilledRewards(alias: String?) : Table<EntityKilledReward>("entity_killed_reward", alias) {
    companion object : EntityKilledRewards(null)

    override fun aliased(alias: String) = EntityKilledRewards(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val currentPrice = double("current_price").bindTo { it.currentPrice }
    val maximumMinedBlockPerMinute = double("maximum_entity_killed_per_minute").bindTo { it.maximumEntityKilledPerMinute }
    val cryptoCurrencyName = varchar("crypto_currency_name").bindTo { it.cryptoCurrencyName }
    val lastCryptoPrice = double("last_crypto_price").bindTo { it.lastCryptoPrice }
    val entityId = int("entity_id").references(Entities) { it.entity }
    val entity get() = entityId.referenceTable as Entities
}
open class AdvancementRewards(alias: String?) : Table<AdvancementReward>("advancement_reward", alias) {
    companion object : AdvancementRewards(null)

    override fun aliased(alias: String) = AdvancementRewards(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val amount = double("amount").bindTo { it.amount }
    val advancementId = int("advancement_id").references(Advancements) { it.advancement }
    val advancement get() = advancementId.referenceTable as Advancements
}

open class BlackListedPlacedBlocks(alias: String?) : Table<BlackListedPlacedBlock>("blacklisted_placed_block", alias) {
    companion object : BlackListedPlacedBlocks(null)

    override fun aliased(alias: String) = BlackListedPlacedBlocks(alias)

    val id = int("id").primaryKey().bindTo { it.id }
    val x = int("x").bindTo { it.x }
    val y = int("y").bindTo { it.y }
    val z = int("z").bindTo { it.z }

}


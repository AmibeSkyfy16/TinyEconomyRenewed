package ch.skyfy.tinyeconomyrenewed.api

import ch.skyfy.tinyeconomyrenewed.server.Economy
import ch.skyfy.tinyeconomyrenewed.server.db.DatabaseManager
import ch.skyfy.tinyeconomyrenewed.server.db.Player
import java.util.*
import kotlin.properties.Delegates

object TinyEconomyRenewedAPI {


    var databaseManagerOptional: Optional<DatabaseManager> by Delegates.observable(Optional.empty()) { property, oldValue, newValue ->
        // If TinyEconomyRenewed has been initialized, all queries made before will be executed
        if (newValue.isPresent) {
            onCompletedList1.forEach { it.invoke(newValue.get()) }
            onCompletedList1.clear()
//            list.forEach { l -> l.block.invoke(newValue.get()) }
        }
    }

    var economyOptional: Optional<Economy> by Delegates.observable(Optional.empty()) { property, oldValue, newValue ->
        // If TinyEconomyRenewed has been initialized, all queries made before will be executed
        if (newValue.isPresent) {
            onCompletedList2.forEach { it.invoke(newValue.get()) }
            onCompletedList2.clear()
//            list.forEach { l -> l.block.invoke() }
        }
    }

    private val onCompletedList1: MutableList<(DatabaseManager) -> Unit> = mutableListOf()
    private val onCompletedList2: MutableList<(Economy) -> Unit> = mutableListOf()

//    val list = mutableListOf<DelayedTask<*>>()

    fun getPlayers(): DelayedTask<DatabaseManager, List<Player>> {
        val b: (DatabaseManager) -> List<Player> = { dbManager ->
            dbManager.cachePlayers
        }
        return DelayedTask(
            block = b,
            rootOnCompletedList = onCompletedList1,
            optional = databaseManagerOptional
        )
    }

    fun getEconomy(): DelayedTask<Economy, Economy> {
        val b: (Economy) -> Economy = { it }
        return DelayedTask(b, onCompletedList2, economyOptional)
    }

    data class DelayedTask<T : Any, T2 : Any>(
        val block: (T) -> T2,
        val rootOnCompletedList: MutableList<(T) -> Unit>,
        val optional: Optional<T>,
        var isCompleted: Boolean = false
    ) {

        private val onCompletedList: MutableList<(T2) -> Unit> = mutableListOf()

        lateinit var players: T2

        init {
            if (optional.isEmpty) {
                val block: (T) -> Unit = { dbm ->
                    players = this.block.invoke(dbm)
                    isCompleted = true
                    onCompletedList.forEach { it.invoke(players) }
                    onCompletedList.clear()
                }
                rootOnCompletedList.add(block)
//                onCompletedList1.add(block)
            } else {
                players = block.invoke(optional.get())
                isCompleted = true
            }
        }

        fun ifIsDone(block: (T2) -> Unit): DelayedTask<T, T2> {
            if (isCompleted) {
                block.invoke(players)
            }
            return this
        }

        fun ifIsDoneNowOrOnCompleted(block: (T2, Boolean) -> Unit): DelayedTask<T, T2> {
            if (isCompleted) {
                block.invoke(players, false)
            } else {
                onCompleted { block.invoke(it, true) }
            }
            return this
        }

        fun onCompleted(block: (T2) -> Unit): DelayedTask<T, T2> {
            onCompletedList.add(block)
            return this
        }

    }

}
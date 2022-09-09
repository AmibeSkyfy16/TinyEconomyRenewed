package ch.skyfy.tinyeconomyrenewed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import kotlin.concurrent.fixedRateTimer
import kotlin.test.Test

class Tests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testWithMultipleDelays() = runTest {

        fixedRateTimer("", true, 0, 1000) {
            println("hey")
        }
        withContext(Dispatchers.IO) {
            sleep(100000)
        }
        println("hey")
//        launch {
//            delay(1_000)
//            println("1. $currentTime") // 1000
//            delay(200)
//            println("2. $currentTime") // 1200
//            delay(2_000)
//            println("4. $currentTime") // 3200
//        }
//        val deferred = async {
//            delay(3_000)
//            println("3. $currentTime") // 3000
//            delay(500)
//            println("5. $currentTime") // 3500
//        }
//        deferred.await()
    }

    @Test
    fun test2() {

//        val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        val f = DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm:ss");
        val d = Instant.now().atZone(ZoneId.systemDefault()).dayOfYear


        val givenMap = hashMapOf<String, Int>()
        givenMap["one"] = 1000
        givenMap["two"] = 5000
        givenMap["three"] = 9000
        givenMap["four"] = 400000
        givenMap["five"] = 5000000
        givenMap["six"] = 600000000

        val p2 = givenMap.toList().sortedBy { (_, value) -> value }.reversed().toMap()
        val p = p2.firstNotNullOfOrNull { if (it.value <= 9001) it else null }

        println()
    }
}
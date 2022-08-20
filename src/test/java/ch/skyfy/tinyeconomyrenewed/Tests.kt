package ch.skyfy.tinyeconomyrenewed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep
import kotlin.concurrent.fixedRateTimer
import kotlin.test.Test

class Tests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testWithMultipleDelays() = runTest {

        fixedRateTimer("", true, 0, 1000){
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
}
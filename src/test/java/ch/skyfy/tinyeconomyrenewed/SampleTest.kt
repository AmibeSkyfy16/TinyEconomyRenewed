package ch.skyfy.tinyeconomyrenewed

import org.apache.logging.log4j.LogManager
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SampleTest {
    private val testSample: Sample = Sample()

    @Test
    fun testSum() {
        val logger = LogManager.getLogger(this::class)
        val expected = 42

        println("a simple println")
        logger.info("hey logger ")

        assertEquals(expected, testSample.sum(40, 2))
    }
}
package com.example

import com.example.audio.VoiceBiometricsManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testWakeWordMatchingPatterns() {
        val testPhrases = listOf(
            "ultron wake up",
            "hey ultron wake up please",
            "wake up ultron",
            "ultron are you there"
        )
        val wakeKeywords = listOf("wake up", "wake", "ultron", "start")

        testPhrases.forEach { phrase ->
            val hasWakeKeyword = wakeKeywords.any { phrase.contains(it) }
            assertTrue("Phrase '$phrase' should match wake keywords", hasWakeKeyword)
        }
    }
}

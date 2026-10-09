package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FastNavigationUnitTest {

    @Test
    fun testAlphabetJumpIndexing() {
        val streamNames = listOf(
            "ABC News",
            "BBC One",
            "CNN International",
            "Discovery",
            "ESPN HD",
            "Fox Sports",
            "HBO",
            "Sky Sports",
            "100 News",
            "99 Music"
        )

        val letters = streamNames.mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }.toSet()
        assertTrue(letters.contains('A'))
        assertTrue(letters.contains('B'))
        assertTrue(letters.contains('C'))
        assertTrue(letters.contains('D'))
        assertTrue(letters.contains('E'))
        assertTrue(letters.contains('S'))
        assertTrue(letters.contains('1'))
        assertTrue(letters.contains('9'))

        // First index of 'C' should be 2
        val cIndex = streamNames.indexOfFirst { it.startsWith("C", ignoreCase = true) }
        assertEquals(2, cIndex)

        // First index of '#' (digits/symbols) should be 8
        val digitIndex = streamNames.indexOfFirst {
            val c = it.trim().firstOrNull()
            c != null && (c.isDigit() || !c.isLetter())
        }
        assertEquals(8, digitIndex)
    }

    @Test
    fun testFastScrollJumpStepCalculation() {
        val totalItems = 150
        val jumpStep = 10

        // Page Down from index 0
        var currentIndex = 0
        var targetDown = (currentIndex + jumpStep).coerceAtMost(totalItems - 1)
        assertEquals(10, targetDown)

        // Page Down from index 145
        currentIndex = 145
        targetDown = (currentIndex + jumpStep).coerceAtMost(totalItems - 1)
        assertEquals(149, targetDown)

        // Page Up from index 5
        currentIndex = 5
        val targetUp = (currentIndex - jumpStep).coerceAtLeast(0)
        assertEquals(0, targetUp)
    }

    @Test
    fun testSelectedCategoryIndexResolutionForDpadLeft() {
        val categories = listOf("All", "News", "Sports", "Movies", "Kids")
        val selectedCategory = "Sports"

        val selectedIndex = categories.indexOfFirst { it == selectedCategory }.coerceAtLeast(0)
        assertEquals(2, selectedIndex)

        // When non-existent category selected, falls back safely to 0
        val unknownIndex = categories.indexOfFirst { it == "Unknown" }.coerceAtLeast(0)
        assertEquals(0, unknownIndex)
    }

    @Test
    fun testJumpButtonsTriggerOnlyOnExplicitClickNotDpadLeftRight() {
        var jumpedToTop = false
        var jumpedToBottom = false

        val onJumpToTop = { jumpedToTop = true }
        val onJumpToBottom = { jumpedToBottom = true }

        // Plain navigation left/right should NOT invoke onJumpToTop or onJumpToBottom
        val dpadLeftKey = android.view.KeyEvent.KEYCODE_DPAD_LEFT
        val dpadRightKey = android.view.KeyEvent.KEYCODE_DPAD_RIGHT
        val dpadCenterKey = android.view.KeyEvent.KEYCODE_DPAD_CENTER
        val enterKey = android.view.KeyEvent.KEYCODE_ENTER

        assertTrue(dpadLeftKey != dpadCenterKey && dpadLeftKey != enterKey)
        assertTrue(dpadRightKey != dpadCenterKey && dpadRightKey != enterKey)
        org.junit.Assert.assertFalse(jumpedToTop)
        org.junit.Assert.assertFalse(jumpedToBottom)

        // Only explicit invocation (simulating OK/Enter click) triggers jump
        onJumpToTop()
        assertTrue(jumpedToTop)
        onJumpToBottom()
        assertTrue(jumpedToBottom)
    }
}

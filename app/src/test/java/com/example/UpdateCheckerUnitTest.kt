package com.example

import com.example.data.update.UpdateManager
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UpdateCheckerUnitTest {

    @Test
    fun testParseBuildNumberStandard() {
        val body = "Automated build 12"
        val buildNumber = UpdateManager.parseBuildNumber(body)
        assertEquals(12, buildNumber)
    }

    @Test
    fun testParseBuildNumberWithExtraWhitespace() {
        val body = "Automated build   42"
        val buildNumber = UpdateManager.parseBuildNumber(body)
        assertEquals(42, buildNumber)
    }

    @Test
    fun testParseBuildNumberWithPrefixColonOrHash() {
        assertEquals(12, UpdateManager.parseBuildNumber("Automated build: 12"))
        assertEquals(99, UpdateManager.parseBuildNumber("Automated build #99"))
    }

    @Test
    fun testParseBuildNumberMultilineChangelog() {
        val body = """
            # MaZze Player Release
            Automated build 15
            - Fixed playback buffer
            - Added TV remote fast navigation
        """.trimIndent()
        val buildNumber = UpdateManager.parseBuildNumber(body)
        assertEquals(15, buildNumber)
    }

    @Test
    fun testParseBuildNumberInvalidOrMissing() {
        assertNull(UpdateManager.parseBuildNumber("Manual release without tag"))
        assertNull(UpdateManager.parseBuildNumber(""))
        assertNull(UpdateManager.parseBuildNumber(null))
        assertNull(UpdateManager.parseBuildNumber("Automated build no-number"))
    }

    @Test
    fun testIsUpdateAvailable() {
        // Newer build available
        assertTrue(UpdateManager.isUpdateAvailable(remoteBuild = 12, currentBuild = 1))
        assertTrue(UpdateManager.isUpdateAvailable(remoteBuild = 15, currentBuild = 14))

        // Same build - up to date
        assertFalse(UpdateManager.isUpdateAvailable(remoteBuild = 1, currentBuild = 1))
        assertFalse(UpdateManager.isUpdateAvailable(remoteBuild = 12, currentBuild = 12))

        // Older remote build (should not downgrade)
        assertFalse(UpdateManager.isUpdateAvailable(remoteBuild = 10, currentBuild = 12))
    }

    @Test
    fun testFormatInstalledVersionRequirement() {
        // Requirement: "Show the installed version in Settings as 'Version 1.0.<versionCode> (build <versionCode>)'."
        assertEquals("Version 1.0.1 (build 1)", UpdateManager.formatInstalledVersion(1))
        assertEquals("Version 1.0.12 (build 12)", UpdateManager.formatInstalledVersion(12))
        assertEquals("Version 1.0.100 (build 100)", UpdateManager.formatInstalledVersion(100))
    }

    @Test
    fun testSimulatedGitHubReleasePayload() {
        val jsonPayload = """
            {
                "tag_name": "MaZze-latest",
                "name": "MaZze Latest Release",
                "body": "Automated build 25\nNew IPTV features and bug fixes",
                "assets": [
                    {
                        "name": "MaZze.apk",
                        "browser_download_url": "https://github.com/Blkmaze/Mazze-Player/releases/download/MaZze-latest/MaZze.apk"
                    }
                ]
            }
        """.trimIndent()

        val json = JSONObject(jsonPayload)
        val body = json.optString("body")
        val parsedBuild = UpdateManager.parseBuildNumber(body)

        assertEquals(25, parsedBuild)
        assertTrue(UpdateManager.isUpdateAvailable(remoteBuild = parsedBuild!!, currentBuild = 1))
    }
}

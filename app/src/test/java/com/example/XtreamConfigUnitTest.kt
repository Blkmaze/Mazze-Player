package com.example

import com.example.data.model.InternalConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XtreamConfigUnitTest {

    @Test
    fun testInternalConfigUrlFormatting() {
        val config = InternalConfig(
            serverUrl = "http://iptv.example.com:8080/",
            username = "testuser",
            password = "testpassword",
            streamFormat = "m3u8"
        )

        assertEquals("http://iptv.example.com:8080", config.formattedBaseUrl)
        assertTrue(config.isValid)

        val streamUrl = config.buildLiveStreamUrl(12345)
        assertEquals("http://iptv.example.com:8080/live/testuser/testpassword/12345.m3u8", streamUrl)

        val vodUrl = config.buildVodStreamUrl(6789, "mp4")
        assertEquals("http://iptv.example.com:8080/movie/testuser/testpassword/6789.mp4", vodUrl)
    }

    @Test
    fun testUrlPrefixCorrection() {
        val config = InternalConfig(
            serverUrl = "server.domain.com:25461",
            username = "demo",
            password = "pwd"
        )
        assertEquals("http://server.domain.com:25461", config.formattedBaseUrl)
    }
}

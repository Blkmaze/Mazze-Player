package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Xtream Codes Authentication & Server Handshake Response
 */
@JsonClass(generateAdapter = true)
data class XtreamAuthResponse(
    @Json(name = "user_info") val userInfo: XtreamUserInfo? = null,
    @Json(name = "server_info") val serverInfo: XtreamServerInfo? = null
)

@JsonClass(generateAdapter = true)
data class XtreamUserInfo(
    @Json(name = "username") val username: String? = null,
    @Json(name = "password") val password: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "auth") val auth: Any? = null, // Can be 1 or "1"
    @Json(name = "status") val status: String? = null,
    @Json(name = "exp_date") val expDate: String? = null,
    @Json(name = "is_trial") val isTrial: Any? = null,
    @Json(name = "active_cons") val activeConnections: Any? = null,
    @Json(name = "max_connections") val maxConnections: Any? = null
) {
    val isAuthorized: Boolean
        get() = auth == 1 || auth == "1" || status.equals("Active", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
data class XtreamServerInfo(
    @Json(name = "url") val url: String? = null,
    @Json(name = "port") val port: String? = null,
    @Json(name = "https_port") val httpsPort: String? = null,
    @Json(name = "server_protocol") val serverProtocol: String? = null,
    @Json(name = "rtmp_port") val rtmpPort: String? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "time_now") val timeNow: String? = null
)

/**
 * Live Category from Xtream Codes:
 * action=get_live_categories
 */
@JsonClass(generateAdapter = true)
data class LiveCategory(
    @Json(name = "category_id") val categoryId: String = "",
    @Json(name = "category_name") val categoryName: String = "",
    @Json(name = "parent_id") val parentId: Any? = null
)

/**
 * Live Stream from Xtream Codes:
 * action=get_live_streams
 */
@JsonClass(generateAdapter = true)
data class LiveStream(
    @Json(name = "num") val num: Any? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "stream_type") val streamType: String? = "live",
    @Json(name = "stream_id") val streamId: Any = 0,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    @Json(name = "epg_channel_id") val epgChannelId: String? = null,
    @Json(name = "category_id") val categoryId: String? = null
) {
    val streamIdInt: Int
        get() = when (streamId) {
            is Number -> streamId.toInt()
            is String -> streamId.toIntOrNull() ?: 0
            else -> 0
        }
}

/**
 * VOD (Movie) Category from Xtream Codes:
 * action=get_vod_categories
 */
@JsonClass(generateAdapter = true)
data class VodCategory(
    @Json(name = "category_id") val categoryId: String = "",
    @Json(name = "category_name") val categoryName: String = "",
    @Json(name = "parent_id") val parentId: Any? = null
)

/**
 * VOD Stream (Movie) from Xtream Codes:
 * action=get_vod_streams
 */
@JsonClass(generateAdapter = true)
data class VodStream(
    @Json(name = "num") val num: Any? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "stream_type") val streamType: String? = "movie",
    @Json(name = "stream_id") val streamId: Any = 0,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    @Json(name = "rating") val rating: Any? = null,
    @Json(name = "container_extension") val containerExtension: String? = "mp4",
    @Json(name = "category_id") val categoryId: String? = null
) {
    val streamIdInt: Int
        get() = when (streamId) {
            is Number -> streamId.toInt()
            is String -> streamId.toIntOrNull() ?: 0
            else -> 0
        }
}

/**
 * Series Category from Xtream Codes:
 * action=get_series_categories
 */
@JsonClass(generateAdapter = true)
data class SeriesCategory(
    @Json(name = "category_id") val categoryId: String = "",
    @Json(name = "category_name") val categoryName: String = "",
    @Json(name = "parent_id") val parentId: Any? = null
)

/**
 * Series Item from Xtream Codes:
 * action=get_series
 */
@JsonClass(generateAdapter = true)
data class SeriesItem(
    @Json(name = "num") val num: Any? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "series_id") val seriesId: Any = 0,
    @Json(name = "cover") val cover: String? = null,
    @Json(name = "plot") val plot: String? = null,
    @Json(name = "cast") val cast: String? = null,
    @Json(name = "director") val director: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "releaseDate") val releaseDate: String? = null,
    @Json(name = "rating") val rating: Any? = null,
    @Json(name = "category_id") val categoryId: String? = null
) {
    val seriesIdInt: Int
        get() = when (seriesId) {
            is Number -> seriesId.toInt()
            is String -> seriesId.toIntOrNull() ?: 0
            else -> 0
        }
}

/**
 * Series Season details
 */
data class SeriesSeason(
    val id: Int = 1,
    val seasonNumber: Int = 1,
    val name: String = "Season 1",
    val episodeCount: Int = 0,
    val overview: String? = null,
    val cover: String? = null
)

/**
 * Series Episode details from action=get_series_info
 */
data class SeriesEpisode(
    val id: String = "",
    val episodeNum: Int = 1,
    val title: String = "",
    val containerExtension: String = "mp4",
    val plot: String? = null,
    val duration: String? = null,
    val rating: String? = null,
    val season: Int = 1,
    val seriesName: String = ""
)

/**
 * Full Series Info and Episode breakdown
 */
data class SeriesDetail(
    val info: SeriesItem,
    val seasons: List<SeriesSeason>,
    val episodesBySeason: Map<Int, List<SeriesEpisode>>
)

/**
 * Internal Configuration Model for MaZze
 */
data class InternalConfig(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val profileName: String = "Default Profile",
    val streamFormat: String = "m3u8", // m3u8 or ts
    val autoConnect: Boolean = true,
    val hardwareAcceleration: Boolean = true
) {
    val isValid: Boolean
        get() = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()

    val formattedBaseUrl: String
        get() {
            var url = serverUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            return if (url.endsWith("/")) url.dropLast(1) else url
        }

    fun buildLiveStreamUrl(streamId: Int): String {
        return "$formattedBaseUrl/live/$username/$password/$streamId.$streamFormat"
    }

    fun buildVodStreamUrl(streamId: Int, extension: String = "mp4"): String {
        val ext = if (extension.isNotBlank()) extension else "mp4"
        return "$formattedBaseUrl/movie/$username/$password/$streamId.$ext"
    }

    fun buildSeriesStreamUrl(episodeId: String, extension: String = "mp4"): String {
        val ext = if (extension.isNotBlank()) extension else "mp4"
        return "$formattedBaseUrl/series/$username/$password/$episodeId.$ext"
    }
}

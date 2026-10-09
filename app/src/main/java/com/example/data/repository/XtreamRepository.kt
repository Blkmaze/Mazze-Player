package com.example.data.repository

import com.example.data.local.FavoriteChannelEntity
import com.example.data.local.MaZzeDao
import com.example.data.local.RecentStreamEntity
import com.example.data.local.XtreamProfileEntity
import com.example.data.model.InternalConfig
import com.example.data.model.LiveCategory
import com.example.data.model.LiveStream
import com.example.data.model.SeriesCategory
import com.example.data.model.SeriesDetail
import com.example.data.model.SeriesEpisode
import com.example.data.model.SeriesItem
import com.example.data.model.SeriesSeason
import com.example.data.model.VodCategory
import com.example.data.model.VodStream
import com.example.data.model.XtreamAuthResponse
import com.example.data.model.XtreamServerInfo
import com.example.data.model.XtreamUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtreamRepository(
    private val dao: MaZzeDao
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // --- Profile Management ---
    val allProfiles: Flow<List<XtreamProfileEntity>> = dao.getAllProfiles()
    val activeProfileFlow: Flow<XtreamProfileEntity?> = dao.getActiveProfileFlow()
    val favorites: Flow<List<FavoriteChannelEntity>> = dao.getFavorites()
    val recents: Flow<List<RecentStreamEntity>> = dao.getRecentStreams()

    suspend fun saveOrUpdateProfile(
        id: Long = 0,
        profileName: String,
        serverUrl: String,
        username: String,
        password: String,
        streamFormat: String = "m3u8",
        makeActive: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        if (makeActive) {
            dao.deactivateAllProfiles()
        }
        val entity = XtreamProfileEntity(
            id = id,
            profileName = profileName.ifBlank { "Xtream Profile" },
            serverUrl = serverUrl.trim(),
            username = username.trim(),
            password = password.trim(),
            streamFormat = streamFormat,
            isActive = makeActive,
            lastConnected = System.currentTimeMillis()
        )
        if (id == 0L) {
            dao.insertProfile(entity)
        } else {
            dao.updateProfile(entity)
            id
        }
    }

    suspend fun setActiveProfile(profileId: Long) = withContext(Dispatchers.IO) {
        dao.deactivateAllProfiles()
        dao.setActiveProfile(profileId)
    }

    suspend fun deleteProfile(profile: XtreamProfileEntity) = withContext(Dispatchers.IO) {
        dao.deleteProfile(profile)
    }

    suspend fun getActiveConfig(): InternalConfig? = withContext(Dispatchers.IO) {
        val active = dao.getActiveProfile() ?: return@withContext null
        InternalConfig(
            serverUrl = active.serverUrl,
            username = active.username,
            password = active.password,
            profileName = active.profileName,
            streamFormat = active.streamFormat
        )
    }

    // --- Xtream Codes Network API Calls ---

    /**
     * Authenticates with Xtream Codes panel.
     * URL: {server}/player_api.php?username={user}&password={pass}
     */
    suspend fun authenticate(config: InternalConfig): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "MaZze-IPTV/1.0")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
                    }
                    val body = response.body?.string() ?: ""
                    if (body.isBlank()) {
                        return@withContext Result.failure(Exception("Empty response received from Xtream server."))
                    }

                    val json = JSONObject(body)
                    val userInfoObj = json.optJSONObject("user_info")
                    val serverInfoObj = json.optJSONObject("server_info")

                    val userInfo = if (userInfoObj != null) {
                        XtreamUserInfo(
                            username = userInfoObj.optString("username"),
                            password = userInfoObj.optString("password"),
                            message = userInfoObj.optString("message"),
                            auth = userInfoObj.opt("auth"),
                            status = userInfoObj.optString("status"),
                            expDate = userInfoObj.optString("exp_date"),
                            isTrial = userInfoObj.opt("is_trial"),
                            activeConnections = userInfoObj.opt("active_cons"),
                            maxConnections = userInfoObj.opt("max_connections")
                        )
                    } else null

                    val serverInfo = if (serverInfoObj != null) {
                        XtreamServerInfo(
                            url = serverInfoObj.optString("url"),
                            port = serverInfoObj.optString("port"),
                            httpsPort = serverInfoObj.optString("https_port"),
                            serverProtocol = serverInfoObj.optString("server_protocol"),
                            rtmpPort = serverInfoObj.optString("rtmp_port"),
                            timezone = serverInfoObj.optString("timezone"),
                            timeNow = serverInfoObj.optString("time_now")
                        )
                    } else null

                    val authResponse = XtreamAuthResponse(userInfo = userInfo, serverInfo = serverInfo)

                    if (userInfo?.isAuthorized == true) {
                        Result.success(authResponse)
                    } else {
                        val msg = userInfo?.message?.ifBlank { "Authorization failed. Check credentials or account status: ${userInfo.status}" }
                            ?: "Invalid credentials or unauthorized Xtream account."
                        Result.failure(Exception(msg))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch Live Categories
     */
    suspend fun getLiveCategories(config: InternalConfig): Result<List<LiveCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_live_categories"
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<LiveCategory>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            LiveCategory(
                                categoryId = obj.optString("category_id"),
                                categoryName = obj.optString("category_name"),
                                parentId = obj.opt("parent_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch Live Streams
     */
    suspend fun getLiveStreams(config: InternalConfig, categoryId: String? = null): Result<List<LiveStream>> =
        withContext(Dispatchers.IO) {
            try {
                var url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_live_streams"
                if (!categoryId.isNullOrBlank() && categoryId != "all") {
                    url += "&category_id=$categoryId"
                }
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<LiveStream>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            LiveStream(
                                num = obj.opt("num"),
                                name = obj.optString("name"),
                                streamType = obj.optString("stream_type", "live"),
                                streamId = obj.opt("stream_id"),
                                streamIcon = obj.optString("stream_icon").takeIf { it.isNotBlank() },
                                epgChannelId = obj.optString("epg_channel_id"),
                                categoryId = obj.optString("category_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch VOD (Movie) Categories
     */
    suspend fun getVodCategories(config: InternalConfig): Result<List<VodCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_vod_categories"
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<VodCategory>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            VodCategory(
                                categoryId = obj.optString("category_id"),
                                categoryName = obj.optString("category_name"),
                                parentId = obj.opt("parent_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch VOD Streams
     */
    suspend fun getVodStreams(config: InternalConfig, categoryId: String? = null): Result<List<VodStream>> =
        withContext(Dispatchers.IO) {
            try {
                var url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_vod_streams"
                if (!categoryId.isNullOrBlank() && categoryId != "all") {
                    url += "&category_id=$categoryId"
                }
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<VodStream>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            VodStream(
                                num = obj.opt("num"),
                                name = obj.optString("name"),
                                streamType = obj.optString("stream_type", "movie"),
                                streamId = obj.opt("stream_id"),
                                streamIcon = obj.optString("stream_icon").takeIf { it.isNotBlank() },
                                rating = obj.opt("rating"),
                                containerExtension = obj.optString("container_extension", "mp4"),
                                categoryId = obj.optString("category_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // --- Series API ---

    /**
     * Fetch Series Categories: action=get_series_categories
     */
    suspend fun getSeriesCategories(config: InternalConfig): Result<List<SeriesCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_series_categories"
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<SeriesCategory>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            SeriesCategory(
                                categoryId = obj.optString("category_id"),
                                categoryName = obj.optString("category_name"),
                                parentId = obj.opt("parent_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch Series List: action=get_series
     */
    suspend fun getSeriesList(config: InternalConfig, categoryId: String? = null): Result<List<SeriesItem>> =
        withContext(Dispatchers.IO) {
            try {
                var url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_series"
                if (!categoryId.isNullOrBlank() && categoryId != "all") {
                    url += "&category_id=$categoryId"
                }
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<SeriesItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.optJSONObject(i) ?: continue
                        list.add(
                            SeriesItem(
                                num = obj.opt("num"),
                                name = obj.optString("name"),
                                seriesId = obj.opt("series_id"),
                                cover = obj.optString("cover").takeIf { it.isNotBlank() },
                                plot = obj.optString("plot"),
                                cast = obj.optString("cast"),
                                director = obj.optString("director"),
                                genre = obj.optString("genre"),
                                releaseDate = obj.optString("releaseDate"),
                                rating = obj.opt("rating"),
                                categoryId = obj.optString("category_id")
                            )
                        )
                    }
                    Result.success(list)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Fetch Series Info with Seasons & Episodes: action=get_series_info&series_id=...
     */
    suspend fun getSeriesInfo(config: InternalConfig, seriesItem: SeriesItem): Result<SeriesDetail> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${config.formattedBaseUrl}/player_api.php?username=${config.username}&password=${config.password}&action=get_series_info&series_id=${seriesItem.seriesIdInt}"
                val request = Request.Builder().url(url).header("User-Agent", "MaZze-IPTV/1.0").build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
                    val body = response.body?.string() ?: "{}"
                    val json = JSONObject(body)

                    // Seasons
                    val seasonsList = mutableListOf<SeriesSeason>()
                    val seasonsArr = json.optJSONArray("seasons")
                    if (seasonsArr != null) {
                        for (i in 0 until seasonsArr.length()) {
                            val sObj = seasonsArr.optJSONObject(i) ?: continue
                            seasonsList.add(
                                SeriesSeason(
                                    id = sObj.optInt("id", i + 1),
                                    seasonNumber = sObj.optInt("season_number", i + 1),
                                    name = sObj.optString("name", "Season ${i + 1}"),
                                    episodeCount = sObj.optInt("episode_count", 0),
                                    overview = sObj.optString("overview"),
                                    cover = sObj.optString("cover")
                                )
                            )
                        }
                    }

                    // Episodes map
                    val episodesMap = mutableMapOf<Int, MutableList<SeriesEpisode>>()
                    val episodesObj = json.optJSONObject("episodes")
                    if (episodesObj != null) {
                        val seasonKeys = episodesObj.keys()
                        while (seasonKeys.hasNext()) {
                            val key = seasonKeys.next()
                            val seasonNumber = key.toIntOrNull() ?: 1
                            val epsArr = episodesObj.optJSONArray(key) ?: continue
                            val epsList = mutableListOf<SeriesEpisode>()
                            for (e in 0 until epsArr.length()) {
                                val epObj = epsArr.optJSONObject(e) ?: continue
                                val epInfo = epObj.optJSONObject("info")
                                epsList.add(
                                    SeriesEpisode(
                                        id = epObj.optString("id"),
                                        episodeNum = epObj.optInt("episode_num", e + 1),
                                        title = epObj.optString("title", "Episode ${e + 1}"),
                                        containerExtension = epObj.optString("container_extension", "mp4"),
                                        plot = epInfo?.optString("plot"),
                                        duration = epInfo?.optString("duration"),
                                        rating = epInfo?.optString("rating"),
                                        season = seasonNumber,
                                        seriesName = seriesItem.name
                                    )
                                )
                            }
                            episodesMap[seasonNumber] = epsList
                        }
                    }

                    // If seasons was empty but episodes had seasons, populate seasons
                    if (seasonsList.isEmpty() && episodesMap.isNotEmpty()) {
                        episodesMap.keys.sorted().forEach { sNum ->
                            seasonsList.add(
                                SeriesSeason(
                                    id = sNum,
                                    seasonNumber = sNum,
                                    name = "Season $sNum",
                                    episodeCount = episodesMap[sNum]?.size ?: 0
                                )
                            )
                        }
                    }

                    Result.success(
                        SeriesDetail(
                            info = seriesItem,
                            seasons = seasonsList.sortedBy { it.seasonNumber },
                            episodesBySeason = episodesMap
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // --- Favorites & Recents ---
    suspend fun toggleFavorite(stream: LiveStream, isFav: Boolean) = withContext(Dispatchers.IO) {
        if (isFav) {
            dao.removeFavorite(stream.streamIdInt)
        } else {
            dao.addFavorite(
                FavoriteChannelEntity(
                    streamId = stream.streamIdInt,
                    streamName = stream.name,
                    streamIcon = stream.streamIcon,
                    categoryId = stream.categoryId,
                    streamType = "live"
                )
            )
        }
    }

    fun isFavorite(streamId: Int): Flow<Boolean> = dao.isFavorite(streamId)

    suspend fun recordPlayed(streamId: Int, name: String, icon: String?, type: String = "live") =
        withContext(Dispatchers.IO) {
            dao.recordRecentStream(
                RecentStreamEntity(
                    streamId = streamId,
                    streamName = name,
                    streamIcon = icon,
                    streamType = type
                )
            )
        }

    // --- Demo / Offline Preview Channels for testing player & UI ---
    fun getDemoCategories(): List<LiveCategory> = listOf(
        LiveCategory(categoryId = "all", categoryName = "All Channels"),
        LiveCategory(categoryId = "demo_news", categoryName = "News & Space"),
        LiveCategory(categoryId = "demo_cinema", categoryName = "Cinema & Movies"),
        LiveCategory(categoryId = "demo_sports", categoryName = "Sports & Live Events"),
        LiveCategory(categoryId = "demo_music", categoryName = "Music & Entertainment")
    )

    fun getDemoStreams(categoryId: String? = null): List<LiveStream> {
        val all = listOf(
            LiveStream(
                num = 1,
                name = "NASA TV Live (HLS HD)",
                streamType = "live",
                streamId = 1001,
                streamIcon = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300",
                categoryId = "demo_news"
            ),
            LiveStream(
                num = 2,
                name = "Big Buck Bunny Cinema (1080p)",
                streamType = "live",
                streamId = 1002,
                streamIcon = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300",
                categoryId = "demo_cinema"
            ),
            LiveStream(
                num = 3,
                name = "Tears of Steel Sci-Fi Stream",
                streamType = "live",
                streamId = 1003,
                streamIcon = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300",
                categoryId = "demo_cinema"
            ),
            LiveStream(
                num = 4,
                name = "Action Sports & Outdoor Extreme",
                streamType = "live",
                streamId = 1004,
                streamIcon = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=300",
                categoryId = "demo_sports"
            ),
            LiveStream(
                num = 5,
                name = "Sintel Fantasy Master Stream",
                streamType = "live",
                streamId = 1005,
                streamIcon = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=300",
                categoryId = "demo_cinema"
            ),
            LiveStream(
                num = 6,
                name = "Lo-Fi Beats & Ambient Live",
                streamType = "live",
                streamId = 1006,
                streamIcon = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300",
                categoryId = "demo_music"
            )
        )
        return if (categoryId.isNullOrBlank() || categoryId == "all") {
            all
        } else {
            all.filter { it.categoryId == categoryId }
        }
    }

    // Demo Series Data
    fun getDemoSeriesCategories(): List<SeriesCategory> = listOf(
        SeriesCategory(categoryId = "all", categoryName = "All Series"),
        SeriesCategory(categoryId = "demo_scifi", categoryName = "Sci-Fi & Action"),
        SeriesCategory(categoryId = "demo_nature", categoryName = "Documentary & Nature"),
        SeriesCategory(categoryId = "demo_fantasy", categoryName = "Animation & Fantasy")
    )

    fun getDemoSeriesList(categoryId: String? = null): List<SeriesItem> {
        val all = listOf(
            SeriesItem(
                num = 1,
                name = "Tears of Steel: Resistance",
                seriesId = 2001,
                cover = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                plot = "Set in a dystopian future where humanity faces robotic overlords, a rogue team of scientists attempts to alter history.",
                genre = "Sci-Fi, Cyberpunk",
                releaseDate = "2024",
                rating = "8.9",
                categoryId = "demo_scifi"
            ),
            SeriesItem(
                num = 2,
                name = "Cosmos & Ocean Odyssey",
                seriesId = 2002,
                cover = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400",
                plot = "An awe-inspiring voyage spanning distant galaxies, nebula nurseries, and uncharted deep ocean thermal trenches.",
                genre = "Documentary, Nature",
                releaseDate = "2023",
                rating = "9.5",
                categoryId = "demo_nature"
            ),
            SeriesItem(
                num = 3,
                name = "Sintel: Dragon Chronicle",
                seriesId = 2003,
                cover = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=400",
                plot = "A fierce wanderer traverses snowbound peaks and desert wastelands to reunite with her companion dragon.",
                genre = "Fantasy, Adventure",
                releaseDate = "2024",
                rating = "9.2",
                categoryId = "demo_fantasy"
            )
        )
        return if (categoryId.isNullOrBlank() || categoryId == "all") {
            all
        } else {
            all.filter { it.categoryId == categoryId }
        }
    }

    fun getDemoSeriesDetail(series: SeriesItem): SeriesDetail {
        val seasons = listOf(
            SeriesSeason(
                id = 1,
                seasonNumber = 1,
                name = "Season 1",
                episodeCount = 3,
                overview = "The introductory season exploring the origins and first critical missions."
            ),
            SeriesSeason(
                id = 2,
                seasonNumber = 2,
                name = "Season 2",
                episodeCount = 2,
                overview = "The high-stakes continuation with unexpected allies and greater perils."
            )
        )

        val s1Episodes = listOf(
            SeriesEpisode(
                id = "${series.seriesIdInt}_101",
                episodeNum = 1,
                title = "Episode 1: The Genesis",
                containerExtension = "mp4",
                plot = "The journey begins as unexpected anomalies surface across the grid.",
                duration = "45m",
                rating = "9.0",
                season = 1,
                seriesName = series.name
            ),
            SeriesEpisode(
                id = "${series.seriesIdInt}_102",
                episodeNum = 2,
                title = "Episode 2: Echoes in the Void",
                containerExtension = "mp4",
                plot = "Pushing deeper into uncharted territory, revelations alter their trajectory.",
                duration = "48m",
                rating = "8.8",
                season = 1,
                seriesName = series.name
            ),
            SeriesEpisode(
                id = "${series.seriesIdInt}_103",
                episodeNum = 3,
                title = "Episode 3: Turning the Tide",
                containerExtension = "mp4",
                plot = "A high-intensity confrontation that changes the fate of the team.",
                duration = "52m",
                rating = "9.4",
                season = 1,
                seriesName = series.name
            )
        )

        val s2Episodes = listOf(
            SeriesEpisode(
                id = "${series.seriesIdInt}_201",
                episodeNum = 1,
                title = "Episode 1: New Horizons",
                containerExtension = "mp4",
                plot = "Rebuilding from the aftermath and setting out toward new objectives.",
                duration = "50m",
                rating = "9.1",
                season = 2,
                seriesName = series.name
            ),
            SeriesEpisode(
                id = "${series.seriesIdInt}_202",
                episodeNum = 2,
                title = "Episode 2: The Final Gateway",
                containerExtension = "mp4",
                plot = "The ultimate test where all skills and courage are pushed to the limit.",
                duration = "55m",
                rating = "9.6",
                season = 2,
                seriesName = series.name
            )
        )

        return SeriesDetail(
            info = series,
            seasons = seasons,
            episodesBySeason = mapOf(1 to s1Episodes, 2 to s2Episodes)
        )
    }

    fun getDemoStreamPlaybackUrl(streamId: Int): String {
        return when (streamId) {
            1001 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            1002 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            1003 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            1004 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            1005 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
            1006 -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4"
            else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }
    }
}

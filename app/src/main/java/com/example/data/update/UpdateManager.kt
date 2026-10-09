package com.example.data.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val latestBuildNumber: Int,
    val currentBuildNumber: Int,
    val releaseBody: String,
    val downloadUrl: String = "https://github.com/Blkmaze/Mazze-Player/releases/download/MaZze-latest/MaZze.apk"
)

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(
        val percent: Int?,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadState()
    data class NeedsPermission(
        val apkFile: File,
        val apkUri: Uri
    ) : DownloadState()
    data class ReadyToInstall(
        val apkFile: File,
        val apkUri: Uri
    ) : DownloadState()
    data class Failed(val error: String) : DownloadState()
}

object UpdateManager {

    private const val GITHUB_RELEASE_URL =
        "https://api.github.com/repos/Blkmaze/Mazze-Player/releases/tags/MaZze-latest"
    const val DEFAULT_DOWNLOAD_URL =
        "https://github.com/Blkmaze/Mazze-Player/releases/download/MaZze-latest/MaZze.apk"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Parses the automated build number from release body text.
     * The release body typically looks like 'Automated build 12'.
     */
    fun parseBuildNumber(body: String?): Int? {
        if (body.isNullOrBlank()) return null
        val regex = Regex("""Automated build\s*[:#]?\s*(\d+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(body)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Checks if remote build is strictly greater than current installed build.
     */
    fun isUpdateAvailable(remoteBuild: Int, currentBuild: Int): Boolean {
        return remoteBuild > currentBuild
    }

    /**
     * Formats the installed version string according to the spec:
     * "Version 1.0.<versionCode> (build <versionCode>)"
     */
    fun formatInstalledVersion(versionCode: Int = BuildConfig.VERSION_CODE): String {
        return "Version 1.0.$versionCode (build $versionCode)"
    }

    /**
     * Queries the GitHub releases API for latest release info.
     * Fails silently on network errors or malformed payloads without throwing exceptions.
     */
    suspend fun checkForUpdate(
        currentBuild: Int = BuildConfig.VERSION_CODE,
        apiUrl: String = GITHUB_RELEASE_URL
    ): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "MaZze-Player-Android")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP error code ${response.code}"))
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(IOException("Empty response body"))

                val json = JSONObject(responseBody)
                val bodyText = json.optString("body", "")
                val releaseName = json.optString("name", "")
                val tagName = json.optString("tag_name", "")

                // Extract build number from body, fallback to release name or tag
                val buildNumber = parseBuildNumber(bodyText)
                    ?: parseBuildNumber(releaseName)
                    ?: parseBuildNumber(tagName)
                    ?: return@withContext Result.failure(
                        IllegalStateException("Could not parse build number from release")
                    )

                // Optional: find custom APK download URL from assets if present
                var apkUrl = DEFAULT_DOWNLOAD_URL
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i)
                        val name = asset?.optString("name", "") ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            val assetUrl = asset.optString("browser_download_url", "")
                            if (assetUrl.isNotBlank()) {
                                apkUrl = assetUrl
                                break
                            }
                        }
                    }
                }

                if (isUpdateAvailable(buildNumber, currentBuild)) {
                    Result.success(
                        UpdateInfo(
                            latestBuildNumber = buildNumber,
                            currentBuildNumber = currentBuild,
                            releaseBody = bodyText,
                            downloadUrl = apkUrl
                        )
                    )
                } else {
                    Result.success(null) // App is up to date
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Checks if the app is granted permission to install unknown apps (Android O+).
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens the system settings screen for Unknown App Sources specifically for this app.
     */
    fun openUnknownAppSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general security settings
                try {
                    val fallback = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Launches the Android package installer for the given APK URI using FileProvider.
     */
    fun launchPackageInstaller(context: Context, apkUri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            // Log or ignore gracefully
        }
    }

    /**
     * Downloads the APK using Android's DownloadManager, reports progress,
     * and triggers the installer once completed.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        updateInfo: UpdateInfo,
        onStateChanged: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            onStateChanged(DownloadState.Failed("DownloadManager unavailable"))
            return@withContext
        }

        val apkFileName = "MaZze-${updateInfo.latestBuildNumber}.apk"
        val destinationDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val destinationFile = File(destinationDir, apkFileName)

        // Delete old file if present
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = try {
            DownloadManager.Request(Uri.parse(updateInfo.downloadUrl)).apply {
                setTitle("MaZze Player Update")
                setDescription("Downloading build ${updateInfo.latestBuildNumber}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, apkFileName)
                setMimeType("application/vnd.android.package-archive")
            }
        } catch (e: Exception) {
            onStateChanged(DownloadState.Failed("Could not start download: ${e.message}"))
            return@withContext
        }

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            onStateChanged(DownloadState.Failed("Could not enqueue download: ${e.message}"))
            return@withContext
        }

        onStateChanged(DownloadState.Downloading(0, 0L, 0L))

        var isDownloading = true
        while (isDownloading && isActive) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            var cursor: Cursor? = null
            try {
                cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val totalBytes = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )

                    val percent = if (totalBytes > 0) {
                        ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                    } else {
                        null
                    }

                    onStateChanged(DownloadState.Downloading(percent, bytesDownloaded, totalBytes))

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDownloading = false
                            // Build content URI using FileProvider
                            val apkUri = try {
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    destinationFile
                                )
                            } catch (e: Exception) {
                                onStateChanged(DownloadState.Failed("File provider error: ${e.message}"))
                                return@withContext
                            }

                            if (!canRequestPackageInstalls(context)) {
                                onStateChanged(DownloadState.NeedsPermission(destinationFile, apkUri))
                                withContext(Dispatchers.Main) {
                                    openUnknownAppSourcesSettings(context)
                                }
                            } else {
                                onStateChanged(DownloadState.ReadyToInstall(destinationFile, apkUri))
                                withContext(Dispatchers.Main) {
                                    launchPackageInstaller(context, apkUri)
                                }
                            }
                        }
                        DownloadManager.STATUS_FAILED -> {
                            isDownloading = false
                            val reason = try {
                                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            } catch (_: Exception) { -1 }
                            onStateChanged(DownloadState.Failed("Download failed with code $reason"))
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Non-fatal query error, retry next loop
            } finally {
                cursor?.close()
            }

            if (isDownloading) {
                delay(350)
            }
        }
    }
}

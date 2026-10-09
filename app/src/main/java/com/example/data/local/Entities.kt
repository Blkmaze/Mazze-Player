package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xtream_profiles")
data class XtreamProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileName: String,
    val serverUrl: String,
    val username: String,
    val password: String,
    val streamFormat: String = "m3u8",
    val isActive: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_channels")
data class FavoriteChannelEntity(
    @PrimaryKey
    val streamId: Int,
    val streamName: String,
    val streamIcon: String?,
    val categoryId: String?,
    val streamType: String = "live",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_streams")
data class RecentStreamEntity(
    @PrimaryKey
    val streamId: Int,
    val streamName: String,
    val streamIcon: String?,
    val streamType: String = "live",
    val playedAt: Long = System.currentTimeMillis()
)

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaZzeDao {

    // --- Profiles ---
    @Query("SELECT * FROM xtream_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<XtreamProfileEntity>>

    @Query("SELECT * FROM xtream_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(): XtreamProfileEntity?

    @Query("SELECT * FROM xtream_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<XtreamProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: XtreamProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: XtreamProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: XtreamProfileEntity)

    @Query("UPDATE xtream_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE xtream_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun setActiveProfile(profileId: Long)

    // --- Favorites ---
    @Query("SELECT * FROM favorite_channels ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteChannelEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_channels WHERE streamId = :streamId)")
    fun isFavorite(streamId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteChannelEntity)

    @Query("DELETE FROM favorite_channels WHERE streamId = :streamId")
    suspend fun removeFavorite(streamId: Int)

    // --- Recent Streams ---
    @Query("SELECT * FROM recent_streams ORDER BY playedAt DESC LIMIT 20")
    fun getRecentStreams(): Flow<List<RecentStreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordRecentStream(recent: RecentStreamEntity)

    @Query("DELETE FROM recent_streams")
    suspend fun clearRecents()
}

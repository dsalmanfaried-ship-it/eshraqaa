package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int

    @Query("SELECT MAX(sortOrder) FROM videos")
    suspend fun maxSortOrder(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(videos: List<VideoEntity>)

    @Query("UPDATE videos SET isSaved = :saved WHERE key = :key")
    suspend fun setSaved(key: String, saved: Boolean)

    @Query("UPDATE videos SET isWatched = :watched WHERE key = :key")
    suspend fun setWatched(key: String, watched: Boolean)

    @Query("UPDATE videos SET isWatched = 0, isSaved = 0")
    suspend fun clearAllProgress()

    @Query("SELECT DISTINCT scholar FROM videos ORDER BY scholarIndex ASC, sortOrder ASC")
    fun observeScholars(): Flow<List<String>>
}

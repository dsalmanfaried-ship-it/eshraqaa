package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY favoritedAt DESC")
    fun observeAllFavorites(): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT videoKey FROM favorites")
    fun observeFavoriteKeys(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE videoKey = :videoKey)")
    suspend fun isFavorite(videoKey: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteVideoEntity)

    @Query("DELETE FROM favorites WHERE videoKey = :videoKey")
    suspend fun deleteFavorite(videoKey: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}

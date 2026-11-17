package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.VideoItem
import com.example.model.VideoKind

@Entity(tableName = "favorites")
data class FavoriteVideoEntity(
    @PrimaryKey val videoKey: String,
    val youtubeId: String,
    val kindCode: String,
    val title: String,
    val scholar: String,
    val scholarIndex: Int,
    val topic: String,
    val category: String,
    val isShort: Boolean,
    val favoritedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(isWatched: Boolean = false): VideoItem = VideoItem(
        key = videoKey,
        kind = VideoKind.fromCode(kindCode),
        youtubeId = youtubeId,
        title = title,
        scholar = scholar,
        scholarIndex = scholarIndex,
        topic = topic,
        category = category,
        isShort = isShort,
        isCustom = false,
        isWatched = isWatched,
        isSaved = true,
        sortOrder = 0
    )

    companion object {
        fun fromDomain(video: VideoItem, favoritedAt: Long = System.currentTimeMillis()): FavoriteVideoEntity =
            FavoriteVideoEntity(
                videoKey = video.key,
                youtubeId = video.youtubeId,
                kindCode = video.kind.code,
                title = video.title,
                scholar = video.scholar,
                scholarIndex = video.scholarIndex,
                topic = video.topic,
                category = video.category,
                isShort = video.isShort,
                favoritedAt = favoritedAt
            )
    }
}

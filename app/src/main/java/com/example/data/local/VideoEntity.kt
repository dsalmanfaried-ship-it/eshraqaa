package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.VideoItem
import com.example.model.VideoKind

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val key: String,
    val kindCode: String,
    val youtubeId: String,
    val title: String,
    val scholar: String,
    val scholarIndex: Int,
    val topic: String,
    val category: String,
    val isShort: Boolean,
    val isCustom: Boolean,
    val isWatched: Boolean = false,
    val isSaved: Boolean = false,
    val sortOrder: Int
) {
    fun toDomain(): VideoItem = VideoItem(
        key = key,
        kind = VideoKind.fromCode(kindCode),
        youtubeId = youtubeId,
        title = title,
        scholar = scholar,
        scholarIndex = scholarIndex,
        topic = topic,
        category = category,
        isShort = isShort,
        isCustom = isCustom,
        isWatched = isWatched,
        isSaved = isSaved,
        sortOrder = sortOrder
    )
}

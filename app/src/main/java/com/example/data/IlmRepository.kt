package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.model.CategoryMapper
import com.example.model.VideoItem
import com.example.model.YouTubeUrlParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class IlmRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val favoriteDao: FavoriteDao,
    private val metadataService: YouTubeMetadataService
) {
    val videosFlow: Flow<List<VideoItem>> = videoDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoritesFlow: Flow<List<FavoriteVideoEntity>> = favoriteDao.observeAllFavorites()

    val favoriteKeysFlow: Flow<Set<String>> = favoriteDao.observeFavoriteKeys().map { it.toSet() }

    val darkThemeFlow: Flow<Boolean> = context.ilmSettingsDataStore.data.map { prefs ->
        prefs[IlmPrefsKeys.DARK_THEME] ?: true
    }

    val userNameFlow: Flow<String> = context.ilmSettingsDataStore.data.map { prefs ->
        prefs[IlmPrefsKeys.USER_NAME].orEmpty()
    }

    val reminderSettingsFlow: Flow<ReminderSettings> = context.ilmSettingsDataStore.data.map { prefs ->
        ReminderSettings(
            enabled = prefs[IlmPrefsKeys.REMINDER_ENABLED] ?: false,
            hour = (prefs[IlmPrefsKeys.REMINDER_HOUR] ?: 20).coerceIn(0, 23),
            minute = (prefs[IlmPrefsKeys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59)
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.ilmSettingsDataStore.edit { prefs ->
            prefs[IlmPrefsKeys.DARK_THEME] = isDark
        }
    }

    suspend fun setUserName(name: String) {
        context.ilmSettingsDataStore.edit { prefs ->
            prefs[IlmPrefsKeys.USER_NAME] = name.trim()
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.ilmSettingsDataStore.edit { prefs ->
            prefs[IlmPrefsKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        val clampedHour = hour.coerceIn(0, 23)
        val clampedMinute = minute.coerceIn(0, 59)
        context.ilmSettingsDataStore.edit { prefs ->
            prefs[IlmPrefsKeys.REMINDER_HOUR] = clampedHour
            prefs[IlmPrefsKeys.REMINDER_MINUTE] = clampedMinute
            prefs[IlmPrefsKeys.REMINDER_ENABLED] = true
        }
    }

    suspend fun ensureSeeded() {
        if (videoDao.count() == 0) {
            videoDao.insertMissing(CatalogSeed.buildEntities())
        }
    }

    suspend fun toggleFavorite(video: VideoItem): Boolean {
        val currentlyFavorite = favoriteDao.isFavorite(video.key)
        val nextFavorite = !currentlyFavorite
        if (nextFavorite) {
            favoriteDao.insertFavorite(FavoriteVideoEntity.fromDomain(video))
        } else {
            favoriteDao.deleteFavorite(video.key)
        }
        videoDao.setSaved(video.key, nextFavorite)
        return nextFavorite
    }

    suspend fun setWatched(key: String, watched: Boolean) {
        videoDao.setWatched(key, watched)
    }

    suspend fun clearAllProgress() {
        favoriteDao.clearAll()
        videoDao.clearAllProgress()
    }

    suspend fun addSingleVideo(scholar: String, section: String, title: String, rawUrl: String): Result<Unit> {
        val parsed = YouTubeUrlParser.parse(rawUrl)
            ?: return Result.failure(IllegalArgumentException("الرابط غير مفهوم — يرجى إدخال رابط يوتيوب صحيح"))
        val maxOrder = (videoDao.maxSortOrder() ?: 0) + 1
        val scholarIndex = CatalogSeed.defaultScholars.indexOf(scholar).let { if (it >= 0) it else 0 }
        val entity = VideoEntity(
            key = parsed.key,
            kindCode = parsed.kind.code,
            youtubeId = parsed.id,
            title = title.trim(),
            scholar = scholar.trim(),
            scholarIndex = scholarIndex,
            topic = section.trim(),
            category = CategoryMapper.categoryOf(section.trim()),
            isShort = rawUrl.contains("/shorts/"),
            isCustom = true,
            isWatched = false,
            isSaved = false,
            sortOrder = maxOrder
        )
        videoDao.upsert(listOf(entity))
        return Result.success(Unit)
    }

    suspend fun importFromYouTube(
        scholar: String,
        section: String,
        rawLines: String,
        existingKeys: Set<String>,
        onProgress: (String) -> Unit
    ): Result<Int> {
        val lines = rawLines.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال رابط واحد على الأقل"))
        }

        data class PendingItem(val id: String, var title: String, val url: String, val isPlaylist: Boolean)
        val pending = mutableListOf<PendingItem>()

        for (line in lines) {
            val pipeIdx = line.indexOf('|')
            val customTitle = if (pipeIdx > -1) line.substring(0, pipeIdx).trim() else ""
            val urlPart = if (pipeIdx > -1) line.substring(pipeIdx + 1).trim() else line
            val parsed = YouTubeUrlParser.parse(urlPart) ?: continue
            if (parsed.kind.code == "p") {
                onProgress("جاري تحميل قائمة التشغيل…")
                val idsResult = metadataService.fetchPlaylistVideoIds(parsed.id)
                if (idsResult.isFailure) {
                    return Result.failure(idsResult.exceptionOrNull() ?: IllegalStateException("تعذّر تحميل القائمة"))
                }
                idsResult.getOrDefault(emptyList()).forEach { vid ->
                    pending.add(PendingItem(vid, "", "https://www.youtube.com/watch?v=$vid", false))
                }
            } else {
                pending.add(PendingItem(parsed.id, customTitle, urlPart, false))
            }
        }

        val seen = HashSet<String>()
        val uniqueNew = pending.filter { item ->
            val key = "v:${item.id}"
            seen.add(item.id) && !existingKeys.contains(key)
        }

        if (uniqueNew.isEmpty()) {
            return Result.success(0)
        }

        uniqueNew.forEachIndexed { idx, item ->
            if (item.title.isEmpty()) {
                item.title = metadataService.fetchVideoTitle(item.id) ?: "فيديو ${idx + 1}"
            }
            onProgress("جاري جلب العناوين… ${idx + 1} / ${uniqueNew.size}")
        }

        var nextOrder = (videoDao.maxSortOrder() ?: 0) + 1
        val scholarIndex = CatalogSeed.defaultScholars.indexOf(scholar).let { if (it >= 0) it else 0 }
        val entities = uniqueNew.map { item ->
            VideoEntity(
                key = "v:${item.id}",
                kindCode = "v",
                youtubeId = item.id,
                title = item.title,
                scholar = scholar.trim(),
                scholarIndex = scholarIndex,
                topic = section.trim(),
                category = CategoryMapper.categoryOf(section.trim()),
                isShort = item.url.contains("/shorts/"),
                isCustom = true,
                isWatched = false,
                isSaved = false,
                sortOrder = nextOrder++
            )
        }
        videoDao.insertMissing(entities)
        return Result.success(entities.size)
    }

    suspend fun syncFromWebsite(): Result<Int> {
        val currentCount = videoDao.count()
        val maxOrder = (videoDao.maxSortOrder() ?: 0) + 1
        val result = metadataService.fetchRemoteIlmCatalog(maxOrder)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull() ?: IllegalStateException("تعذّر التحديث"))
        }
        val fetched = result.getOrDefault(emptyList())
        if (fetched.isNotEmpty()) {
            videoDao.insertMissing(fetched)
        }
        val newCount = videoDao.count()
        return Result.success((newCount - currentCount).coerceAtLeast(0))
    }

    fun buildExportDataJs(videos: List<VideoItem>): String {
        val scholarsArray = JSONArray()
        val byScholar = LinkedHashMap<String, MutableList<VideoItem>>()
        for (scholar in CatalogSeed.defaultScholars) {
            byScholar[scholar] = mutableListOf()
        }
        for (v in videos) {
            byScholar.getOrPut(v.scholar) { mutableListOf() }.add(v)
        }

        for ((scholarName, scholarVideos) in byScholar) {
            val sectionsObj = JSONArray()
            val byTopic = LinkedHashMap<String, MutableList<VideoItem>>()
            for (v in scholarVideos) {
                byTopic.getOrPut(v.topic) { mutableListOf() }.add(v)
            }
            for ((topic, topicVideos) in byTopic) {
                val vidsArray = JSONArray()
                for (v in topicVideos) {
                    val obj = JSONObject().apply {
                        put("title", v.title)
                        put("url", v.youtubeUrl)
                        if (v.isShort) put("short", true)
                    }
                    vidsArray.put(obj)
                }
                sectionsObj.put(
                    JSONObject().apply {
                        put("title", topic)
                        put("videos", vidsArray)
                    }
                )
            }
            scholarsArray.put(
                JSONObject().apply {
                    put("name", scholarName)
                    put("sections", sectionsObj)
                }
            )
        }

        val root = JSONObject().apply { put("scholars", scholarsArray) }
        return "const DATA = ${root.toString(1)};"
    }
}

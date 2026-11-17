package com.example.data

import com.example.data.local.VideoEntity
import com.example.model.CategoryMapper
import com.example.model.YouTubeUrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class YouTubeMetadataService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun fetchVideoTitle(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode("https://www.youtube.com/watch?v=$videoId", "UTF-8")
            val req = Request.Builder()
                .url("https://www.youtube.com/oembed?format=json&url=$encoded")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val title = JSONObject(body).optString("title", "").trim()
                title.ifEmpty { null }
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchPlaylistVideoIds(playlistId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://www.youtube.com/playlist?list=$playlistId")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept-Language", "ar,en;q=0.9")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("تعذّر الوصول إلى قائمة التشغيل (HTTP ${resp.code})"))
                }
                val html = resp.body?.string().orEmpty()
                val regex = Regex(""""videoId"\s*:\s*"([\w-]{11})"""")
                val ids = LinkedHashSet<String>()
                for (match in regex.findAll(html)) {
                    ids.add(match.groupValues[1])
                    if (ids.size >= 80) break
                }
                if (ids.isEmpty()) {
                    Result.failure(IllegalStateException("القائمة خاصة أو فارغة أو غير متاحة حالياً"))
                } else {
                    Result.success(ids.toList())
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "تعذّر تحميل القائمة"))
        }
    }

    suspend fun fetchRemoteIlmCatalog(startingSortOrder: Int): Result<List<VideoEntity>> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("https://dsalmanfaried-ship-it.github.io/ilm/data.js")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException("تعذّر الاتصال بموقع العلم"))
                }
                val raw = resp.body?.string().orEmpty()
                val start = raw.indexOf('{')
                val end = raw.lastIndexOf('}')
                if (start < 0 || end <= start) {
                    return@withContext Result.failure(IllegalStateException("تنسيق البيانات غير صالح"))
                }
                val root = JSONObject(raw.substring(start, end + 1))
                val scholars = root.optJSONArray("scholars")
                    ?: return@withContext Result.success(emptyList())
                val out = mutableListOf<VideoEntity>()
                var order = startingSortOrder
                for (si in 0 until scholars.length()) {
                    val sch = scholars.getJSONObject(si)
                    val scholarName = sch.optString("name", "").trim()
                    val sections = sch.optJSONArray("sections") ?: continue
                    for (seci in 0 until sections.length()) {
                        val sec = sections.getJSONObject(seci)
                        val topic = sec.optString("title", "").trim()
                        val videos = sec.optJSONArray("videos") ?: continue
                        for (vi in 0 until videos.length()) {
                            val v = videos.getJSONObject(vi)
                            val title = v.optString("title", "").trim()
                            val url = v.optString("url", "").trim()
                            val isShort = v.optBoolean("short", false)
                            val parsed = YouTubeUrlParser.parse(url) ?: continue
                            out.add(
                                VideoEntity(
                                    key = parsed.key,
                                    kindCode = parsed.kind.code,
                                    youtubeId = parsed.id,
                                    title = title.ifEmpty { "فيديو" },
                                    scholar = scholarName,
                                    scholarIndex = si,
                                    topic = topic,
                                    category = CategoryMapper.categoryOf(topic),
                                    isShort = isShort,
                                    isCustom = false,
                                    isWatched = false,
                                    isSaved = false,
                                    sortOrder = order++
                                )
                            )
                        }
                    }
                }
                Result.success(out)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "تعذّر تحديث المكتبة"))
        }
    }
}

package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Diversity1
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

enum class VideoKind(val code: String) {
    VIDEO("v"),
    PLAYLIST("p");

    companion object {
        fun fromCode(code: String): VideoKind = if (code == "p") PLAYLIST else VIDEO
    }
}

data class ParsedYouTubeLink(
    val kind: VideoKind,
    val id: String,
    val key: String
)

object YouTubeUrlParser {
    private val videoRegex = Regex("""(?:v=|youtu\.be/|embed/|shorts/|live/)([\w-]{11})""")
    private val playlistRegex = Regex("""[?&]list=([\w-]+)""")

    fun parse(rawUrl: String?): ParsedYouTubeLink? {
        val url = rawUrl?.trim().orEmpty()
        if (url.isEmpty()) return null
        val videoMatch = videoRegex.find(url)
        if (videoMatch != null && !url.contains("playlist?")) {
            val id = videoMatch.groupValues[1]
            return ParsedYouTubeLink(VideoKind.VIDEO, id, "v:$id")
        }
        val listMatch = playlistRegex.find(url)
        if (listMatch != null) {
            val id = listMatch.groupValues[1]
            return ParsedYouTubeLink(VideoKind.PLAYLIST, id, "p:$id")
        }
        return null
    }
}

object CategoryMapper {
    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("قوائم") to "قوائم تشغيل",
        Regex("أسرة|زواج|علاقات") to "الأسرة والزواج",
        Regex("قصص|قصة|حكاية|سير") to "قصص وحكايات",
        Regex("قرآن") to "القرآن",
        Regex("صلاة|الصلاة|خشوع") to "الصلاة",
        Regex("دعاء|أدعية|مناجاة") to "الأدعية",
        Regex("توب|رجوع|هداية") to "التوبة والرجوع",
        Regex("موت|آخرة|دنيا") to "الموت والآخرة",
        Regex("تحفيز|نجاح|حياة|سلوك|بداية") to "تحفيز وحياة",
        Regex("عقيدة") to "عقيدة وإيمان"
    )

    fun categoryOf(sectionTitle: String): String {
        for ((regex, category) in rules) {
            if (regex.containsMatchIn(sectionTitle)) return category
        }
        return "تزكية ورقائق"
    }

    fun iconFor(category: String): ImageVector = when (category) {
        "قوائم تشغيل" -> Icons.Rounded.PlaylistPlay
        "الأسرة والزواج" -> Icons.Rounded.Diversity1
        "قصص وحكايات" -> Icons.Rounded.AutoStories
        "القرآن" -> Icons.Rounded.MenuBook
        "الصلاة" -> Icons.Rounded.Mosque
        "الأدعية" -> Icons.Rounded.VolunteerActivism
        "التوبة والرجوع" -> Icons.Rounded.Spa
        "الموت والآخرة" -> Icons.Rounded.HourglassBottom
        "تحفيز وحياة" -> Icons.Rounded.AutoAwesome
        "عقيدة وإيمان" -> Icons.Rounded.NightsStay
        else -> Icons.Rounded.Favorite
    }
}

data class VideoItem(
    val key: String,
    val kind: VideoKind,
    val youtubeId: String,
    val title: String,
    val scholar: String,
    val scholarIndex: Int,
    val topic: String,
    val category: String,
    val isShort: Boolean,
    val isCustom: Boolean,
    val isWatched: Boolean,
    val isSaved: Boolean,
    val sortOrder: Int
) {
    val hqThumbnailUrl: String
        get() = if (kind == VideoKind.VIDEO) "https://i.ytimg.com/vi/$youtubeId/hqdefault.jpg" else ""

    val maxResThumbnailUrl: String
        get() = if (kind == VideoKind.VIDEO) "https://i.ytimg.com/vi/$youtubeId/maxresdefault.jpg" else ""

    val youtubeUrl: String
        get() = if (kind == VideoKind.VIDEO) {
            "https://www.youtube.com/watch?v=$youtubeId"
        } else {
            "https://www.youtube.com/playlist?list=$youtubeId"
        }

    val shortScholarName: String
        get() = scholar.removePrefix("دكتور ").removePrefix("الشيخ ").trim()

    val scholarInitial: String
        get() = shortScholarName.firstOrNull()?.toString() ?: "ع"

    val categoryIcon: ImageVector
        get() = CategoryMapper.iconFor(category)
}

object CatalogInterleaver {
    fun interleave(list: List<VideoItem>): List<VideoItem> {
        if (list.isEmpty()) return emptyList()
        val grouped = LinkedHashMap<String, MutableList<VideoItem>>()
        for (item in list) {
            grouped.getOrPut(item.scholar) { mutableListOf() }.add(item)
        }
        val result = ArrayList<VideoItem>(list.size)
        val keys = grouped.keys.toList()
        var index = 0
        while (true) {
            var addedAny = false
            for (k in keys) {
                val items = grouped[k] ?: continue
                if (index < items.size) {
                    result.add(items[index])
                    addedAny = true
                }
            }
            if (!addedAny) break
            index++
        }
        return result
    }
}

fun filterLessonsByQuery(lessons: List<VideoItem>, rawQuery: String): List<VideoItem> {
    val q = rawQuery.trim()
    if (q.isEmpty()) return lessons
    return lessons.filter {
        it.title.contains(q, ignoreCase = true) ||
            it.scholar.contains(q, ignoreCase = true)
    }
}


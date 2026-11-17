package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogSeed
import com.example.data.IlmRepository
import com.example.data.ReminderSettings
import com.example.data.YouTubeMetadataService
import com.example.data.local.IlmDatabase
import com.example.model.CatalogInterleaver
import com.example.model.MotivationalProvider
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.model.filterLessonsByQuery
import com.example.notifications.DailyReminderNotificationHelper
import com.example.notifications.DailyReminderScheduler
import com.example.notifications.SpiritualReminders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class IlmTab {
    HOME,
    EXPLORE,
    SAVED,
    MORE
}

enum class MoreSubPage {
    ADD_VIDEO,
    IMPORT_YOUTUBE,
    EXPORT_DATA
}

data class ScholarStory(
    val name: String,
    val shortName: String,
    val initial: String,
    val index: Int,
    val hasUnwatched: Boolean
)

data class CategoryShelf(
    val category: String,
    val videos: List<VideoItem>
)

data class PlayerState(
    val isOpen: Boolean = false,
    val currentKey: String? = null,
    val queueKeys: List<String> = emptyList()
)

data class IlmUiState(
    val isLoading: Boolean = true,
    val allVideos: List<VideoItem> = emptyList(),
    val watchedCount: Int = 0,
    val savedCount: Int = 0,
    val selectedTab: IlmTab = IlmTab.HOME,
    val moreSubPage: MoreSubPage? = null,
    val selectedScholar: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val userName: String = "",
    val motivationalMessages: List<String> = emptyList(),
    val isDarkTheme: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val scholars: List<ScholarStory> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val heroVideos: List<VideoItem> = emptyList(),
    val categoryShelves: List<CategoryShelf> = emptyList(),
    val homeFeed: List<VideoItem> = emptyList(),
    val exploreResults: List<VideoItem> = emptyList(),
    val savedVideos: List<VideoItem> = emptyList(),
    val player: PlayerState = PlayerState(),
    val isImporting: Boolean = false,
    val importStatus: String = "",
    val isSyncing: Boolean = false,
    val feedbackMessage: String? = null
)

private data class NavFilterState(
    val selectedTab: IlmTab = IlmTab.HOME,
    val moreSubPage: MoreSubPage? = null,
    val selectedScholar: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

private data class AsyncStatusState(
    val player: PlayerState = PlayerState(),
    val isImporting: Boolean = false,
    val importStatus: String = "",
    val isSyncing: Boolean = false,
    val feedbackMessage: String? = null
)

class IlmViewModel(application: Application) : AndroidViewModel(application) {
    private val database = IlmDatabase.getInstance(application)
    private val repository = IlmRepository(
        context = application,
        videoDao = database.videoDao(),
        favoriteDao = database.favoriteDao(),
        metadataService = YouTubeMetadataService()
    )

    private val navState = MutableStateFlow(NavFilterState())
    private val asyncState = MutableStateFlow(AsyncStatusState())

    private data class SettingsState(
        val isDarkTheme: Boolean = true,
        val userName: String = "",
        val reminder: ReminderSettings = ReminderSettings()
    )

    private val settingsFlow = combine(
        repository.darkThemeFlow,
        repository.userNameFlow,
        repository.reminderSettingsFlow
    ) { isDark, userName, reminder -> SettingsState(isDark, userName, reminder) }

    val uiState: StateFlow<IlmUiState> = combine(
        repository.videosFlow,
        repository.favoritesFlow,
        settingsFlow,
        navState,
        asyncState
    ) { rawVideos, favoriteEntities, settings, nav, async ->
        val isDark = settings.isDarkTheme
        val userName = settings.userName
        val reminder = settings.reminder
        val favoriteKeys = favoriteEntities.map { it.videoKey }.toSet()
        val videos = rawVideos.map { video ->
            if (video.isSaved != favoriteKeys.contains(video.key)) {
                video.copy(isSaved = favoriteKeys.contains(video.key))
            } else {
                video
            }
        }
        val watchedByKey = videos.associate { it.key to it.isWatched }
        val watchedCount = videos.count { it.isWatched }
        val savedVideos = favoriteEntities.map { fav ->
            fav.toDomain(isWatched = watchedByKey[fav.videoKey] ?: false)
        }

        val scholarNames = (CatalogSeed.defaultScholars + videos.map { it.scholar }).distinct()
        val stories = scholarNames.mapIndexed { idx, name ->
            val scholarVids = videos.filter { it.scholar == name }
            val hasUnwatched = scholarVids.any { !it.isWatched }
            val short = name.removePrefix("دكتور ").removePrefix("الشيخ ").trim()
            ScholarStory(
                name = name,
                shortName = short,
                initial = short.firstOrNull()?.toString() ?: "ع",
                index = idx,
                hasUnwatched = hasUnwatched
            )
        }

        val q = nav.searchQuery.trim()
        val scholarFiltered = videos.filter {
            nav.selectedScholar == null || it.scholar == nav.selectedScholar
        }
        val availableCategories = scholarFiltered.map { it.category }.distinct()

        val categoryFiltered = scholarFiltered.filter {
            nav.selectedCategory == null || it.category == nav.selectedCategory
        }
        val filtered = filterLessonsByQuery(categoryFiltered, q)
        val homeFeed = if (nav.selectedScholar != null) {
            filtered
        } else {
            CatalogInterleaver.interleave(filtered)
        }

        val showRows = nav.selectedScholar == null && nav.selectedCategory == null && q.isEmpty()
        val heroVideos = if (showRows) {
            CatalogInterleaver.interleave(
                videos.filter { it.kind == VideoKind.VIDEO && !it.isWatched }
            ).take(6)
        } else {
            emptyList()
        }

        val categoryShelves = if (showRows) {
            videos.groupBy { it.category }
                .filter { it.value.size >= 3 }
                .entries
                .sortedByDescending { it.value.size }
                .take(6)
                .map { (cat, catVideos) ->
                    CategoryShelf(
                        category = cat,
                        videos = CatalogInterleaver.interleave(catVideos).take(10)
                    )
                }
        } else {
            emptyList()
        }

        val exploreResults = filterLessonsByQuery(videos, q)

        IlmUiState(
            isLoading = videos.isEmpty(),
            allVideos = videos,
            watchedCount = watchedCount,
            savedCount = savedVideos.size,
            selectedTab = nav.selectedTab,
            moreSubPage = nav.moreSubPage,
            selectedScholar = nav.selectedScholar,
            selectedCategory = nav.selectedCategory,
            searchQuery = nav.searchQuery,
            userName = userName,
            motivationalMessages = MotivationalProvider.buildMessages(
                rawUserName = userName,
                watchedCount = watchedCount,
                totalCount = videos.size,
                savedCount = savedVideos.size
            ),
            isDarkTheme = isDark,
            reminderEnabled = reminder.enabled,
            reminderHour = reminder.hour,
            reminderMinute = reminder.minute,
            scholars = stories,
            availableCategories = availableCategories,
            heroVideos = heroVideos,
            categoryShelves = categoryShelves,
            homeFeed = homeFeed,
            exploreResults = exploreResults,
            savedVideos = savedVideos,
            player = async.player,
            isImporting = async.isImporting,
            importStatus = async.importStatus,
            isSyncing = async.isSyncing,
            feedbackMessage = async.feedbackMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IlmUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun selectTab(tab: IlmTab) {
        navState.update { it.copy(selectedTab = tab, moreSubPage = null) }
    }

    fun openMoreSubPage(subPage: MoreSubPage?) {
        navState.update { it.copy(moreSubPage = subPage) }
    }

    fun selectScholar(scholarName: String?) {
        navState.update { it.copy(selectedScholar = scholarName, selectedCategory = null) }
    }

    fun selectCategory(category: String?) {
        navState.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        navState.update { it.copy(searchQuery = query) }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            repository.setDarkTheme(!uiState.value.isDarkTheme)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.setUserName(name)
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                showFeedback("أهلاً بك يا $trimmed 🌿")
            }
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val state = uiState.value
            val app = getApplication<Application>()
            if (enabled) {
                repository.setReminderEnabled(true)
                DailyReminderScheduler.scheduleDailyReminder(
                    context = app,
                    hour = state.reminderHour,
                    minute = state.reminderMinute
                )
                val formatted = SpiritualReminders.formatTimeArabic(state.reminderHour, state.reminderMinute)
                showFeedback("تم تفعيل التذكير اليومي الساعة $formatted 🌙")
            } else {
                repository.setReminderEnabled(false)
                DailyReminderScheduler.cancelDailyReminder(app)
                showFeedback("تم إيقاف التذكير اليومي")
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            repository.setReminderTime(hour, minute)
            DailyReminderScheduler.scheduleDailyReminder(
                context = app,
                hour = hour,
                minute = minute
            )
            val formatted = SpiritualReminders.formatTimeArabic(hour, minute)
            showFeedback("تم ضبط التذكير اليومي على $formatted ✓")
        }
    }

    fun sendTestReminderNotification() {
        val app = getApplication<Application>()
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val posted = DailyReminderNotificationHelper.showReminderNotification(
            context = app,
            rawUserName = uiState.value.userName,
            dayIndex = dayOfYear,
            notificationId = DailyReminderNotificationHelper.DAILY_REMINDER_NOTIFICATION_ID + 1
        )
        if (posted) {
            showFeedback("تم إرسال التذكير الآن 🌙")
        } else {
            showFeedback("يرجى السماح بالإشعارات أولاً")
        }
    }

    fun toggleSaved(video: VideoItem) {
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(video)
            showFeedback(MotivationalProvider.personalizedFavoriteToast(uiState.value.userName, isNowFavorite))
        }
    }

    fun toggleWatched(video: VideoItem) {
        viewModelScope.launch {
            val next = !video.isWatched
            repository.setWatched(video.key, next)
            showFeedback(
                if (next) MotivationalProvider.personalizedWatchedToast(uiState.value.userName)
                else "اتلغى تسجيل المشاهدة"
            )
        }
    }

    fun markWatchedSilently(key: String) {
        viewModelScope.launch {
            val item = uiState.value.allVideos.find { it.key == key }
            if (item != null && !item.isWatched) {
                repository.setWatched(key, true)
                showFeedback(MotivationalProvider.personalizedWatchedToast(uiState.value.userName))
            }
        }
    }

    fun openPlayer(key: String, queue: List<VideoItem>) {
        val keys = queue.map { it.key }.ifEmpty { listOf(key) }
        asyncState.update {
            it.copy(player = PlayerState(isOpen = true, currentKey = key, queueKeys = keys))
        }
    }

    fun switchPlayerVideo(key: String) {
        asyncState.update {
            it.copy(player = it.player.copy(isOpen = true, currentKey = key))
        }
    }

    fun playNextInQueue() {
        val player = asyncState.value.player
        val currentKey = player.currentKey ?: return
        val q = player.queueKeys
        val idx = q.indexOf(currentKey)
        if (idx < 0 || idx + 1 >= q.size) return
        val videosByKey = uiState.value.allVideos.associateBy { it.key }
        val nextUnwatched = q.drop(idx + 1).firstOrNull { key -> videosByKey[key]?.isWatched == false }
        val target = nextUnwatched ?: q.getOrNull(idx + 1) ?: return
        switchPlayerVideo(target)
    }

    fun closePlayer() {
        asyncState.update {
            it.copy(player = PlayerState(isOpen = false, currentKey = null, queueKeys = emptyList()))
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.clearAllProgress()
            showFeedback("تم مسح المشاهدات والمحفوظات")
        }
    }

    fun addSingleVideo(scholar: String, section: String, title: String, url: String) {
        viewModelScope.launch {
            val res = repository.addSingleVideo(scholar, section, title, url)
            if (res.isSuccess) {
                showFeedback("تمت إضافة الفيديو ✓")
                navState.update { it.copy(selectedTab = IlmTab.HOME, moreSubPage = null) }
            } else {
                showFeedback(res.exceptionOrNull()?.message ?: "تعذّرت الإضافة")
            }
        }
    }

    fun importFromYouTube(scholar: String, section: String, rawLines: String) {
        if (asyncState.value.isImporting) return
        viewModelScope.launch {
            asyncState.update { it.copy(isImporting = true, importStatus = "جاري التحضير…") }
            val existingKeys = uiState.value.allVideos.map { it.key }.toSet()
            val res = repository.importFromYouTube(
                scholar = scholar,
                section = section,
                rawLines = rawLines,
                existingKeys = existingKeys
            ) { status ->
                asyncState.update { it.copy(importStatus = status) }
            }
            if (res.isSuccess) {
                val count = res.getOrDefault(0)
                if (count > 0) {
                    asyncState.update { it.copy(isImporting = false, importStatus = "") }
                    showFeedback("تم استيراد $count فيديو ✓")
                    navState.update { it.copy(selectedTab = IlmTab.HOME, moreSubPage = null) }
                } else {
                    asyncState.update {
                        it.copy(isImporting = false, importStatus = "لا توجد فيديوهات جديدة لإضافتها.")
                    }
                }
            } else {
                asyncState.update {
                    it.copy(
                        isImporting = false,
                        importStatus = "⚠ ${res.exceptionOrNull()?.message ?: "حدث خطأ أثناء الاستيراد"}"
                    )
                }
            }
        }
    }

    fun syncCatalogFromWebsite() {
        if (asyncState.value.isSyncing) return
        viewModelScope.launch {
            asyncState.update { it.copy(isSyncing = true) }
            val res = repository.syncFromWebsite()
            asyncState.update { it.copy(isSyncing = false) }
            if (res.isSuccess) {
                val added = res.getOrDefault(0)
                showFeedback(if (added > 0) "تم تحديث المكتبة وإضافة $added درس جديد ✓" else "المكتبة محدّثة بالفعل ✓")
            } else {
                showFeedback(res.exceptionOrNull()?.message ?: "تعذّر الاتصال بموقع العلم")
            }
        }
    }

    fun buildExportJson(): String = repository.buildExportDataJs(uiState.value.allVideos)

    fun showFeedback(message: String) {
        asyncState.update { it.copy(feedbackMessage = message) }
    }

    fun consumeFeedback() {
        asyncState.update { it.copy(feedbackMessage = null) }
    }
}

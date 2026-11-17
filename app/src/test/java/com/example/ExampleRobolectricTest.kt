package com.example

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.data.CatalogSeed
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.IlmDatabase
import com.example.model.CatalogInterleaver
import com.example.model.CategoryMapper
import com.example.model.VideoKind
import com.example.model.YouTubeUrlParser
import com.example.notifications.DailyReminderNotificationHelper
import com.example.notifications.DailyReminderScheduler
import com.example.notifications.SpiritualReminders
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("العلم", appName)
    }

    @Test
    fun `catalog seed parses all curated videos and playlists`() {
        val entities = CatalogSeed.buildEntities()
        assertEquals(107, entities.size)
        assertTrue(entities.any { it.kindCode == VideoKind.PLAYLIST.code })
        assertTrue(entities.any { it.kindCode == VideoKind.VIDEO.code })
    }

    @Test
    fun `youtube url parser handles watch shorts and playlists`() {
        val watch = YouTubeUrlParser.parse("https://www.youtube.com/watch?v=ZWraKKwNfpE")
        assertNotNull(watch)
        assertEquals(VideoKind.VIDEO, watch?.kind)
        assertEquals("ZWraKKwNfpE", watch?.id)

        val shortUrl = YouTubeUrlParser.parse("https://youtube.com/shorts/J71rHj7gyCU")
        assertNotNull(shortUrl)
        assertEquals(VideoKind.VIDEO, shortUrl?.kind)
        assertEquals("J71rHj7gyCU", shortUrl?.id)

        val playlist = YouTubeUrlParser.parse("https://www.youtube.com/playlist?list=PLVPp4tYPys9O-Vlcp8dnmaShe6eZ2Y3Uq")
        assertNotNull(playlist)
        assertEquals(VideoKind.PLAYLIST, playlist?.kind)
        assertEquals("PLVPp4tYPys9O-Vlcp8dnmaShe6eZ2Y3Uq", playlist?.id)
    }

    @Test
    fun `category mapper and interleaver balance scholars`() {
        assertEquals("القرآن", CategoryMapper.categoryOf("القرآن الكريم"))
        assertEquals("الصلاة", CategoryMapper.categoryOf("الصلاة ورمضان"))
        assertEquals("تزكية ورقائق", CategoryMapper.categoryOf("واهدِ قلبي — تزكية"))

        val items = CatalogSeed.buildEntities().map { it.toDomain() }
        val interleaved = CatalogInterleaver.interleave(items)
        assertEquals(items.size, interleaved.size)
        val firstEightScholars = interleaved.take(8).map { it.scholar }.distinct()
        assertEquals(8, firstEightScholars.size)
    }

    @Test
    fun `favorite dao persists liked videos in favorites table ordered by newest first`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, IlmDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val favoriteDao = db.favoriteDao()
            val sampleVideos = CatalogSeed.buildEntities().take(2).map { it.toDomain() }

            favoriteDao.insertFavorite(FavoriteVideoEntity.fromDomain(sampleVideos[0], favoritedAt = 1000L))
            favoriteDao.insertFavorite(FavoriteVideoEntity.fromDomain(sampleVideos[1], favoritedAt = 2000L))

            assertTrue(favoriteDao.isFavorite(sampleVideos[0].key))
            assertTrue(favoriteDao.isFavorite(sampleVideos[1].key))

            val favorites = favoriteDao.observeAllFavorites().first()
            assertEquals(2, favorites.size)
            assertEquals(sampleVideos[1].key, favorites[0].videoKey)
            assertEquals(sampleVideos[0].key, favorites[1].videoKey)

            favoriteDao.deleteFavorite(sampleVideos[0].key)
            assertFalse(favoriteDao.isFavorite(sampleVideos[0].key))
            assertEquals(1, favoriteDao.observeFavoriteKeys().first().size)

            favoriteDao.clearAll()
            assertTrue(favoriteDao.observeAllFavorites().first().isEmpty())
        } finally {
            db.close()
        }
    }

    @Test
    fun `filterLessonsByQuery filters lessons by title and speaker name`() {
        val lessons = CatalogSeed.buildEntities().map { it.toDomain() }

        val bySpeaker = com.example.model.filterLessonsByQuery(lessons, "أحمد العربي")
        assertTrue(bySpeaker.isNotEmpty())
        assertTrue(bySpeaker.all { it.scholar.contains("أحمد العربي") })

        val byTitle = com.example.model.filterLessonsByQuery(lessons, "القلب السليم")
        assertTrue(byTitle.isNotEmpty())
        assertTrue(byTitle.all { it.title.contains("القلب السليم") || it.scholar.contains("القلب السليم") })

        val blankQuery = com.example.model.filterLessonsByQuery(lessons, "   ")
        assertEquals(lessons.size, blankQuery.size)
    }

    @Test
    fun `motivational provider personalizes messages and toasts with user name`() {
        val messages = com.example.model.MotivationalProvider.buildMessages(
            rawUserName = "سلمان",
            watchedCount = 4,
            totalCount = 107,
            savedCount = 2
        )
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.all { it.contains("سلمان") })

        val toast = com.example.model.MotivationalProvider.personalizedWatchedToast("سلمان")
        assertTrue(toast.contains("سلمان"))

        val favToast = com.example.model.MotivationalProvider.personalizedFavoriteToast("سلمان", true)
        assertTrue(favToast.contains("سلمان"))
    }

    @Test
    fun `spiritual reminders rotate verses personalize title and format arabic time`() {
        val first = SpiritualReminders.reminderForDay(0, "سلمان")
        val next = SpiritualReminders.reminderForDay(1, "")
        assertTrue(first.title.contains("سلمان"))
        assertTrue(first.verseOrQuote.isNotBlank())
        assertFalse(first.verseOrQuote == next.verseOrQuote)

        assertEquals("12:00 ص", SpiritualReminders.formatTimeArabic(0, 0))
        assertEquals("08:05 ص", SpiritualReminders.formatTimeArabic(8, 5))
        assertEquals("12:30 م", SpiritualReminders.formatTimeArabic(12, 30))
        assertEquals("08:15 م", SpiritualReminders.formatTimeArabic(20, 15))
    }

    @Test
    fun `scheduler computes exact initial delay for same day and next day`() {
        val utc = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance(utc).apply {
            set(2026, Calendar.MAY, 10, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        // 16:30 same day -> 2 hours 30 minutes
        val sameDayDelay = DailyReminderScheduler.computeInitialDelayMillis(
            hour = 16,
            minute = 30,
            nowMillis = now,
            timeZone = utc
        )
        assertEquals(150L * 60L * 1000L, sameDayDelay)

        // 08:00 already passed today -> tomorrow 08:00 (18 hours)
        val nextDayDelay = DailyReminderScheduler.computeInitialDelayMillis(
            hour = 8,
            minute = 0,
            nowMillis = now,
            timeZone = utc
        )
        assertEquals(18L * 60L * 60L * 1000L, nextDayDelay)
    }

    @Test
    fun `workmanager schedules and cancels unique daily reminder work`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        DailyReminderScheduler.scheduleDailyReminder(context, hour = 20, minute = 15)

        val workManager = WorkManager.getInstance(context)
        val enqueuedInfos = workManager.getWorkInfosForUniqueWork(DailyReminderScheduler.UNIQUE_WORK_NAME).get()
        assertEquals(1, enqueuedInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, enqueuedInfos[0].state)
        assertTrue(enqueuedInfos[0].tags.contains(DailyReminderScheduler.WORK_TAG))

        DailyReminderScheduler.cancelDailyReminder(context)
        val cancelledInfos = workManager.getWorkInfosForUniqueWork(DailyReminderScheduler.UNIQUE_WORK_NAME).get()
        assertEquals(WorkInfo.State.CANCELLED, cancelledInfos[0].state)
    }

    @Test
    fun `notification helper creates channel and posts styled spiritual notification when permitted`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as android.app.Application
        Shadows.shadowOf(app).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        val posted = DailyReminderNotificationHelper.showReminderNotification(
            context = context,
            rawUserName = "سلمان",
            dayIndex = 0
        )
        assertTrue(posted)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowManager = Shadows.shadowOf(manager)
        assertNotNull(manager.getNotificationChannel(DailyReminderNotificationHelper.CHANNEL_ID))
        assertEquals(1, shadowManager.allNotifications.size)
        val extras = shadowManager.allNotifications.first().extras
        val title = extras.getString(android.app.Notification.EXTRA_TITLE).orEmpty()
        assertTrue(title.contains("سلمان"))
    }
}

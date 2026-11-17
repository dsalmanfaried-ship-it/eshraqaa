package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VideoEntity::class, FavoriteVideoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class IlmDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `favorites` (
                        `videoKey` TEXT NOT NULL,
                        `youtubeId` TEXT NOT NULL,
                        `kindCode` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `scholar` TEXT NOT NULL,
                        `scholarIndex` INTEGER NOT NULL,
                        `topic` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `isShort` INTEGER NOT NULL,
                        `favoritedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`videoKey`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `favorites` (
                        `videoKey`, `youtubeId`, `kindCode`, `title`, `scholar`,
                        `scholarIndex`, `topic`, `category`, `isShort`, `favoritedAt`
                    )
                    SELECT `key`, `youtubeId`, `kindCode`, `title`, `scholar`,
                           `scholarIndex`, `topic`, `category`, `isShort`, 0
                    FROM `videos` WHERE `isSaved` = 1
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: IlmDatabase? = null

        fun getInstance(context: Context): IlmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    IlmDatabase::class.java,
                    "ilm_library.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

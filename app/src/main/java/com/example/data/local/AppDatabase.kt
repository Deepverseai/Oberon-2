package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BookmarkEntity::class, HistoryEntity::class, QuickLinkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun quickLinkDao(): QuickLinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oberon_browser.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedDefaultQuickLinks(database.quickLinkDao())
                }
            }
        }

        private suspend fun seedDefaultQuickLinks(dao: QuickLinkDao) {
            val defaults = listOf(
                QuickLinkEntity(
                    title = "Google",
                    url = "https://www.google.com",
                    iconName = "google",
                    position = 0
                ),
                QuickLinkEntity(
                    title = "AI Studio",
                    url = "https://aistudio.google.com",
                    iconName = "ai",
                    position = 1
                ),
                QuickLinkEntity(
                    title = "GitHub",
                    url = "https://github.com",
                    iconName = "github",
                    position = 2
                ),
                QuickLinkEntity(
                    title = "Wikipedia",
                    url = "https://www.wikipedia.org",
                    iconName = "wikipedia",
                    position = 3
                ),
                QuickLinkEntity(
                    title = "YouTube",
                    url = "https://www.youtube.com",
                    iconName = "youtube",
                    position = 4
                ),
                QuickLinkEntity(
                    title = "Reddit",
                    url = "https://www.reddit.com",
                    iconName = "reddit",
                    position = 5
                ),
                QuickLinkEntity(
                    title = "Hacker News",
                    url = "https://news.ycombinator.com",
                    iconName = "news",
                    position = 6
                ),
                QuickLinkEntity(
                    title = "DuckDuckGo",
                    url = "https://duckduckgo.com",
                    iconName = "duckduckgo",
                    position = 7
                )
            )
            dao.insertAll(defaults)
        }
    }
}

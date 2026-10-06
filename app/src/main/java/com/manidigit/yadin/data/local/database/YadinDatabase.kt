package com.manidigit.yadin.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.ImportReviewDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.dao.SettingsDao
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ConceptTagEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.ImportReviewItemEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.data.local.entity.TagEntity

@Database(
    entities = [
        ConceptEntity::class,
        ContentEntity::class,
        CategoryEntity::class,
        ConceptCategoryEntity::class,
        TagEntity::class,
        ConceptTagEntity::class,
        LearningStateEntity::class,
        DifficultyStateEntity::class,
        ReviewSessionEntity::class,
        ReviewSessionItemEntity::class,
        ReviewHistoryEntity::class,
        SettingEntity::class,
        AchievementEntity::class,
        ImportReviewItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class YadinDatabase : RoomDatabase() {

    abstract fun conceptDao(): ConceptDao
    abstract fun learningDao(): LearningDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun importReviewDao(): ImportReviewDao

    companion object {
        @Volatile
        private var INSTANCE: YadinDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE contents DROP COLUMN pronunciation")
                } catch (_: Exception) {
                }
            }
        }

        fun getInstance(context: Context): YadinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YadinDatabase::class.java,
                    "yadin_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

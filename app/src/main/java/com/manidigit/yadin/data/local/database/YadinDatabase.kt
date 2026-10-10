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
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.dao.SettingsDao
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.data.local.entity.SettingEntity

@Database(
    entities = [
        ConceptEntity::class,
        ContentEntity::class,
        CategoryEntity::class,
        ConceptCategoryEntity::class,
        LearningStateEntity::class,
        DifficultyStateEntity::class,
        ReviewSessionEntity::class,
        ReviewSessionItemEntity::class,
        ReviewHistoryEntity::class,
        SettingEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class YadinDatabase : RoomDatabase() {

    abstract fun conceptDao(): ConceptDao
    abstract fun learningDao(): LearningDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: YadinDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `contents_new` (
                        `id` TEXT NOT NULL,
                        `conceptId` TEXT NOT NULL,
                        `languageCode` TEXT NOT NULL,
                        `text` TEXT NOT NULL,
                        `canonicalKey` TEXT NOT NULL,
                        `note` TEXT,
                        `translationIndex` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `contents_new` (`id`, `conceptId`, `languageCode`, `text`, `canonicalKey`, `note`, `translationIndex`)
                    SELECT `id`, `conceptId`, `languageCode`, `text`, `canonicalKey`, `note`, `translationIndex` FROM `contents`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `contents`")
                db.execSQL("ALTER TABLE `contents_new` RENAME TO `contents`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_conceptId` ON `contents` (`conceptId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_languageCode` ON `contents` (`languageCode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_canonicalKey` ON `contents` (`canonicalKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_languageCode_canonicalKey` ON `contents` (`languageCode`, `canonicalKey`)")
            }
        }

        val MIGRATION_2_1 = object : Migration(2, 1) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // حل نقص ISS-40: مهاجرت معکوس امن بدون پاکسازی مخرب پایگاه داده
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // حذف کامل جداول متروکه تگ و بررسی ایمپورت طبق دستور کارفرما (ISS-98 / ISS-119)
                db.execSQL("DROP TABLE IF EXISTS `tags`")
                db.execSQL("DROP TABLE IF EXISTS `concept_tags`")
                db.execSQL("DROP TABLE IF EXISTS `import_review_items`")
            }
        }

        fun getInstance(context: Context): YadinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YadinDatabase::class.java,
                    "yadin_database.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_1, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

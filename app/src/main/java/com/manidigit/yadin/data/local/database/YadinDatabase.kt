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
    version = 8,
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

        private fun migrateContentsTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `contents_new` ")
            db.execSQL(
                """
                CREATE TABLE `contents_new` (
                    `id` TEXT NOT NULL,
                    `conceptId` TEXT NOT NULL,
                    `languageCode` TEXT NOT NULL,
                    `text` TEXT NOT NULL,
                    `canonicalKey` TEXT NOT NULL,
                    `note` TEXT,
                    `translationIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `contents_new` (`id`, `conceptId`, `languageCode`, `text`, `canonicalKey`, `note`, `translationIndex`)
                SELECT `id`, `conceptId`, `languageCode`, `text`, `canonicalKey`, `note`, `translationIndex`
                FROM `contents`
                WHERE `conceptId` IN (SELECT `id` FROM `concepts`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `contents` ")
            db.execSQL("ALTER TABLE `contents_new` RENAME TO `contents` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_conceptId` ON `contents` (`conceptId`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_languageCode` ON `contents` (`languageCode`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_canonicalKey` ON `contents` (`canonicalKey`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_contents_languageCode_canonicalKey` ON `contents` (`languageCode`, `canonicalKey`) ")
        }

        private fun migrateConceptCategoriesTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `concept_categories_new` ")
            db.execSQL(
                """
                CREATE TABLE `concept_categories_new` (
                    `conceptId` TEXT NOT NULL,
                    `categoryId` TEXT NOT NULL,
                    PRIMARY KEY(`conceptId`, `categoryId`),
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `concept_categories_new` (`conceptId`, `categoryId`)
                SELECT `conceptId`, `categoryId` FROM `concept_categories`
                WHERE `conceptId` IN (SELECT `id` FROM `concepts`)
                  AND `categoryId` IN (SELECT `id` FROM `categories`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `concept_categories` ")
            db.execSQL("ALTER TABLE `concept_categories_new` RENAME TO `concept_categories` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_concept_categories_conceptId` ON `concept_categories` (`conceptId`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_concept_categories_categoryId` ON `concept_categories` (`categoryId`) ")
        }

        private fun migrateLearningStatesTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `learning_states_new` ")
            db.execSQL(
                """
                CREATE TABLE `learning_states_new` (
                    `id` TEXT NOT NULL,
                    `conceptId` TEXT NOT NULL,
                    `direction` TEXT NOT NULL,
                    `stage` TEXT NOT NULL,
                    `nextReviewDay` TEXT,
                    `lastReviewedDay` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `learning_states_new` (`id`, `conceptId`, `direction`, `stage`, `nextReviewDay`, `lastReviewedDay`, `createdAt`, `updatedAt`)
                SELECT `id`, `conceptId`, `direction`, `stage`, `nextReviewDay`, `lastReviewedDay`, `createdAt`, `updatedAt`
                FROM `learning_states`
                WHERE `conceptId` IN (SELECT `id` FROM `concepts`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `learning_states` ")
            db.execSQL("ALTER TABLE `learning_states_new` RENAME TO `learning_states` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_learning_states_conceptId` ON `learning_states` (`conceptId`) ")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_learning_states_conceptId_direction` ON `learning_states` (`conceptId`, `direction`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_learning_states_stage` ON `learning_states` (`stage`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_learning_states_nextReviewDay` ON `learning_states` (`nextReviewDay`) ")
        }

        private fun migrateDifficultyStatesTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `difficulty_states_new` ")
            db.execSQL(
                """
                CREATE TABLE `difficulty_states_new` (
                    `id` TEXT NOT NULL,
                    `conceptId` TEXT NOT NULL,
                    `direction` TEXT NOT NULL,
                    `current` TEXT NOT NULL,
                    `consecutiveCorrect` INTEGER NOT NULL,
                    `consecutiveWrong` INTEGER NOT NULL,
                    `hasReachedVeryHard` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `difficulty_states_new` (`id`, `conceptId`, `direction`, `current`, `consecutiveCorrect`, `consecutiveWrong`, `hasReachedVeryHard`)
                SELECT `id`, `conceptId`, `direction`, `current`, `consecutiveCorrect`, `consecutiveWrong`, `hasReachedVeryHard`
                FROM `difficulty_states`
                WHERE `conceptId` IN (SELECT `id` FROM `concepts`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `difficulty_states` ")
            db.execSQL("ALTER TABLE `difficulty_states_new` RENAME TO `difficulty_states` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_difficulty_states_conceptId` ON `difficulty_states` (`conceptId`) ")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_difficulty_states_conceptId_direction` ON `difficulty_states` (`conceptId`, `direction`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_difficulty_states_current` ON `difficulty_states` (`current`) ")
        }

        private fun migrateReviewSessionItemsTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `review_session_items_new` ")
            db.execSQL(
                """
                CREATE TABLE `review_session_items_new` (
                    `id` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `position` INTEGER NOT NULL,
                    `conceptId` TEXT NOT NULL,
                    `direction` TEXT NOT NULL,
                    `state` TEXT NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`sessionId`) REFERENCES `review_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `review_session_items_new` (`id`, `sessionId`, `position`, `conceptId`, `direction`, `state`)
                SELECT `id`, `sessionId`, `position`, `conceptId`, `direction`, `state`
                FROM `review_session_items`
                WHERE `sessionId` IN (SELECT `id` FROM `review_sessions`)
                  AND `conceptId` IN (SELECT `id` FROM `concepts`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `review_session_items` ")
            db.execSQL("ALTER TABLE `review_session_items_new` RENAME TO `review_session_items` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_session_items_sessionId` ON `review_session_items` (`sessionId`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_session_items_conceptId` ON `review_session_items` (`conceptId`) ")
        }

        private fun migrateReviewHistoryTableWithForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `review_history_new` ")
            db.execSQL(
                """
                CREATE TABLE `review_history_new` (
                    `id` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `reviewAttemptId` TEXT NOT NULL,
                    `conceptId` TEXT NOT NULL,
                    `direction` TEXT NOT NULL,
                    `reviewedAt` INTEGER NOT NULL,
                    `reviewedDay` TEXT NOT NULL,
                    `isCorrect` INTEGER NOT NULL,
                    `reviewType` TEXT NOT NULL,
                    `mode` TEXT NOT NULL,
                    `stageBefore` TEXT NOT NULL,
                    `quizLevel` TEXT,
                    `optionsJson` TEXT,
                    `selectedIndex` INTEGER,
                    `correctIndex` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`sessionId`) REFERENCES `review_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `review_history_new` (`id`, `sessionId`, `reviewAttemptId`, `conceptId`, `direction`, `reviewedAt`, `reviewedDay`, `isCorrect`, `reviewType`, `mode`, `stageBefore`, `quizLevel`, `optionsJson`, `selectedIndex`, `correctIndex`)
                SELECT `id`, `sessionId`, `reviewAttemptId`, `conceptId`, `direction`, `reviewedAt`, `reviewedDay`, `isCorrect`, `reviewType`, `mode`, `stageBefore`, `quizLevel`, `optionsJson`, `selectedIndex`, `correctIndex`
                FROM `review_history`
                WHERE `sessionId` IN (SELECT `id` FROM `review_sessions`)
                  AND `conceptId` IN (SELECT `id` FROM `concepts`)
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `review_history` ")
            db.execSQL("ALTER TABLE `review_history_new` RENAME TO `review_history` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_history_sessionId` ON `review_history` (`sessionId`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_history_conceptId` ON `review_history` (`conceptId`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_history_reviewedDay` ON `review_history` (`reviewedDay`) ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_history_reviewedAt` ON `review_history` (`reviewedAt`) ")
        }

        private fun reconcileAllTablesFully(db: SupportSQLiteDatabase) {
            migrateContentsTableWithForeignKey(db)
            migrateConceptCategoriesTableWithForeignKey(db)
            migrateLearningStatesTableWithForeignKey(db)
            migrateDifficultyStatesTableWithForeignKey(db)
            migrateReviewSessionItemsTableWithForeignKey(db)
            migrateReviewHistoryTableWithForeignKey(db)
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_2_1 = object : Migration(2, 1) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // MIGRATION_2_1
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `tags` ")
                db.execSQL("DROP TABLE IF EXISTS `concept_tags` ")
                db.execSQL("DROP TABLE IF EXISTS `import_review_items` ")
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_6_8 = object : Migration(6, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                reconcileAllTablesFully(db)
            }
        }

        fun getInstance(context: Context): YadinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YadinDatabase::class.java,
                    "yadin_database.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_1,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_6_8,
                        MIGRATION_7_8
                    )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

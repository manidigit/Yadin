package com.manidigit.yadin.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class MigrationTest {

    @Test
    fun testMigrationVersions() {
        assertEquals(1, YadinDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, YadinDatabase.MIGRATION_1_2.endVersion)

        assertEquals(2, YadinDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, YadinDatabase.MIGRATION_2_3.endVersion)

        assertEquals(3, YadinDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, YadinDatabase.MIGRATION_3_4.endVersion)

        assertEquals(5, YadinDatabase.MIGRATION_5_6.startVersion)
        assertEquals(6, YadinDatabase.MIGRATION_5_6.endVersion)

        assertEquals(6, YadinDatabase.MIGRATION_6_7.startVersion)
        assertEquals(7, YadinDatabase.MIGRATION_6_7.endVersion)

        assertEquals(6, YadinDatabase.MIGRATION_6_8.startVersion)
        assertEquals(8, YadinDatabase.MIGRATION_6_8.endVersion)

        assertEquals(7, YadinDatabase.MIGRATION_7_8.startVersion)
        assertEquals(8, YadinDatabase.MIGRATION_7_8.endVersion)
    }

    @Test
    fun testMigrationExecutesExpectedSql() {
        val executedSql = mutableListOf<String>()
        val dummyDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL") {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        YadinDatabase.MIGRATION_6_7.migrate(dummyDb)

        assert(executedSql.any { it.contains("CREATE TABLE `contents_new`") })
        assert(executedSql.any { it.contains("CREATE TABLE `concept_categories_new`") })
        assert(executedSql.any { it.contains("FOREIGN KEY(`conceptId`) REFERENCES `concepts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE") })
        assert(executedSql.any { it.contains("ALTER TABLE `contents_new` RENAME TO `contents`") })
        assert(executedSql.any { it.contains("ALTER TABLE `concept_categories_new` RENAME TO `concept_categories`") })
    }
}

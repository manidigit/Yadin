package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

data class ConceptWithContents(
    val id: String,
    val entryType: String,
    val categoryId: String?,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val sourceText: String?,
    val targetTranslations: String?
)

@Dao
interface ConceptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: ConceptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<ConceptEntity>)

    @Update
    suspend fun updateConcept(concept: ConceptEntity)

    @Query("SELECT * FROM concepts WHERE id = :id LIMIT 1")
    suspend fun getConceptById(id: String): ConceptEntity?

    @Query("SELECT COUNT(*) FROM concepts WHERE active = 1")
    suspend fun getActiveConceptCount(): Int

    @Query("SELECT COUNT(*) FROM concepts")
    suspend fun getTotalConceptCount(): Int

    @Query("SELECT * FROM concepts WHERE active = 1 ORDER BY createdAt DESC")
    fun getAllActiveConceptsFlow(): Flow<List<ConceptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(contents: List<ContentEntity>)

    @Update
    suspend fun updateContent(content: ContentEntity)

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId ORDER BY translationIndex ASC")
    suspend fun getContentsForConcept(conceptId: String): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE conceptId IN (:conceptIds) ORDER BY translationIndex ASC")
    suspend fun getContentsForConcepts(conceptIds: List<String>): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId ORDER BY translationIndex ASC")
    fun getContentsForConceptFlow(conceptId: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM contents WHERE languageCode = :languageCode AND canonicalKey = :canonicalKey LIMIT 1")
    suspend fun findContentByCanonicalKey(languageCode: String, canonicalKey: String): ContentEntity?

    @Query("SELECT * FROM contents WHERE languageCode = :languageCode AND canonicalKey IN (:canonicalKeys)")
    suspend fun findContentsByCanonicalKeys(languageCode: String, canonicalKeys: List<String>): List<ContentEntity>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConceptCategory(item: ConceptCategoryEntity)

    @Query("UPDATE concepts SET active = 0, updatedAt = :timestamp WHERE id = :conceptId")
    suspend fun softDeleteConcept(conceptId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE concepts SET active = 1, updatedAt = :timestamp WHERE id = :conceptId")
    suspend fun reactivateConcept(conceptId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteContentsForConcept(conceptId: String)

    @Query("DELETE FROM concepts WHERE id = :conceptId")
    suspend fun deleteConceptPermanently(conceptId: String)

    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        INNER JOIN contents cnt ON c.id = cnt.conceptId
        WHERE c.active = 1
        AND (cnt.text LIKE '%' || :query || '%' OR cnt.canonicalKey LIKE '%' || :query || '%' OR cnt.note LIKE '%' || :query || '%')
        ORDER BY c.updatedAt DESC
        LIMIT :limit
    """)
    suspend fun searchConcepts(query: String, limit: Int = 100): List<ConceptEntity>

    @Query("""
        SELECT c.* FROM concepts c
        WHERE c.active = 1 AND c.categoryId = :categoryId
        ORDER BY c.updatedAt DESC
    """)
    fun getConceptsByCategoryFlow(categoryId: String): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM contents WHERE languageCode = :languageCode ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomContents(languageCode: String, limit: Int): List<ContentEntity>

    @Query("SELECT * FROM contents")
    suspend fun getAllContents(): List<ContentEntity>

    @Query("DELETE FROM contents")
    suspend fun clearContents()

    @Query("DELETE FROM concepts")
    suspend fun clearConcepts()

    @Query("DELETE FROM categories WHERE isDefault = 0")
    suspend fun clearCustomCategories()
}

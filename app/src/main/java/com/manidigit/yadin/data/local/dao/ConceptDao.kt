package com.manidigit.yadin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.domain.model.CardDirection
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

data class ConceptCategoryJoin(
    val conceptId: String,
    val id: String,
    val name: String,
    val sortOrder: Int,
    val isDefault: Boolean
)

data class CategoryMasteryRaw(
    val categoryId: String,
    val categoryName: String,
    val totalWords: Int,
    val learnedWords: Int
)

@Dao
interface ConceptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: ConceptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<ConceptEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConceptsIgnore(concepts: List<ConceptEntity>)

    @Update
    suspend fun updateConcept(concept: ConceptEntity)

    @Update
    suspend fun updateConcepts(concepts: List<ConceptEntity>)

    @Query("SELECT * FROM concepts WHERE id = :id LIMIT 1")
    suspend fun getConceptById(id: String): ConceptEntity?

    @Query("SELECT COUNT(*) FROM concepts WHERE active = 1")
    suspend fun getActiveConceptCount(): Int

    @Query("SELECT COUNT(*) FROM concepts")
    suspend fun getTotalConceptCount(): Int

    @Query("SELECT * FROM concepts")
    suspend fun getAllConcepts(): List<ConceptEntity>

    @Query("SELECT * FROM concepts WHERE id IN (:conceptIds)")
    suspend fun getConceptsByIds(conceptIds: List<String>): List<ConceptEntity>

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

    @Query("""
        SELECT cnt.* FROM contents cnt
        INNER JOIN concepts c ON cnt.conceptId = c.id
        WHERE cnt.languageCode = :languageCode 
        AND cnt.canonicalKey = :canonicalKey 
        AND c.active = 1
        LIMIT 1
    """)
    suspend fun findActiveContentByCanonicalKey(languageCode: String, canonicalKey: String): ContentEntity?

    @Query("SELECT * FROM concepts WHERE active = 0")
    suspend fun getInactiveConcepts(): List<ConceptEntity>

    @Query("DELETE FROM concepts WHERE id IN (:conceptIds)")
    suspend fun deleteConceptsPermanently(conceptIds: List<String>)

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

    @Query("UPDATE categories SET name = :newName WHERE id = :id")
    suspend fun updateCategoryName(id: String, newName: String)

    @Query("DELETE FROM categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteCategory(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConceptCategory(item: ConceptCategoryEntity)

    @Query("""
        SELECT cat.* FROM categories cat
        INNER JOIN concept_categories cc ON cat.id = cc.categoryId
        WHERE cc.conceptId = :conceptId
        ORDER BY cat.sortOrder ASC, cat.name ASC
    """)
    suspend fun getCategoriesForConcept(conceptId: String): List<CategoryEntity>

    @Query("""
        SELECT cc.conceptId, cat.id, cat.name, cat.sortOrder, cat.isDefault 
        FROM categories cat
        INNER JOIN concept_categories cc ON cat.id = cc.categoryId
        WHERE cc.conceptId IN (:conceptIds)
        ORDER BY cat.sortOrder ASC, cat.name ASC
    """)
    suspend fun getCategoriesForConcepts(conceptIds: List<String>): List<ConceptCategoryJoin>

    @Query("UPDATE concepts SET active = 0, updatedAt = :timestamp WHERE id = :conceptId")
    suspend fun softDeleteConcept(conceptId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE concepts SET active = 1, updatedAt = :timestamp WHERE id = :conceptId")
    suspend fun reactivateConcept(conceptId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteContentsForConcept(conceptId: String)

    @Query("DELETE FROM contents WHERE conceptId IN (:conceptIds)")
    suspend fun deleteContentsForConcepts(conceptIds: List<String>)

    @Query("DELETE FROM contents WHERE id IN (:contentIds)")
    suspend fun deleteContents(contentIds: List<String>)

    @Query("DELETE FROM concept_categories WHERE conceptId = :conceptId")
    suspend fun deleteConceptCategories(conceptId: String)

    @Query("DELETE FROM concept_categories WHERE conceptId IN (:conceptIds)")
    suspend fun deleteConceptCategoriesForConcepts(conceptIds: List<String>)

    @Query("DELETE FROM concepts WHERE id = :conceptId")
    suspend fun deleteConceptPermanently(conceptId: String)

    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        INNER JOIN contents cnt ON c.id = cnt.conceptId
        LEFT JOIN learning_states ls ON (c.id = ls.conceptId AND ls.direction = :direction)
        LEFT JOIN difficulty_states ds ON (c.id = ds.conceptId AND ds.direction = :direction)
        WHERE ((:onlyInactive = 0 AND c.active = 1) OR (:onlyInactive = 1 AND c.active = 0))
        AND (:query = '' 
             OR cnt.text LIKE '%' || :query || '%' 
             OR cnt.canonicalKey LIKE '%' || :query || '%' 
             OR cnt.note LIKE '%' || :query || '%'
             OR (:searchKey != '' AND (
                 cnt.canonicalKey LIKE '%' || :searchKey || '%'
                 OR cnt.text LIKE '%' || :searchKey || '%'
                 OR REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                    LOWER(cnt.canonicalKey),
                    'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ü', 'u'), 'ñ', 'n'),
                    'ي', 'ی'), 'ك', 'ک'), 'آ', 'ا'), 'أ', 'ا'), 'إ', 'ا'), '${'\u200C'}', '')
                    LIKE '%' || :searchKey || '%'
             ))
        )
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        AND (:stage IS NULL OR ls.stage = :stage)
        AND (:difficulty IS NULL OR COALESCE(ds.current, 'EASY') = :difficulty)
        ORDER BY c.updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchConceptsFiltered(
        query: String,
        searchKey: String = "",
        categoryId: String?,
        stage: String?,
        direction: CardDirection,
        difficulty: String? = null,
        onlyInactive: Boolean = false,
        limit: Int = 50,
        offset: Int = 0
    ): List<ConceptEntity>

    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        INNER JOIN contents cnt ON c.id = cnt.conceptId
        WHERE c.active = 1
        AND (:query = '' 
             OR cnt.text LIKE '%' || :query || '%' 
             OR cnt.canonicalKey LIKE '%' || :query || '%' 
             OR cnt.note LIKE '%' || :query || '%'
             OR (:searchKey != '' AND (
                 cnt.canonicalKey LIKE '%' || :searchKey || '%'
                 OR cnt.text LIKE '%' || :searchKey || '%'
                 OR REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                    LOWER(cnt.canonicalKey),
                    'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ü', 'u'), 'ñ', 'n'),
                    'ي', 'ی'), 'ك', 'ک'), 'آ', 'ا'), 'أ', 'ا'), 'إ', 'ا'), '${'\u200C'}', '')
                    LIKE '%' || :searchKey || '%'
             ))
        )
        ORDER BY c.updatedAt DESC
        LIMIT :limit
    """)
    suspend fun searchConcepts(
        query: String,
        searchKey: String = "",
        limit: Int = 100
    ): List<ConceptEntity>

    @Query("""
        SELECT c.* FROM concepts c
        WHERE c.active = 1 AND c.categoryId = :categoryId
        ORDER BY c.updatedAt DESC
    """)
    fun getConceptsByCategoryFlow(categoryId: String): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM contents WHERE languageCode = :languageCode ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomContents(languageCode: String, limit: Int): List<ContentEntity>

    @Query("""
        SELECT cnt.* FROM contents cnt
        INNER JOIN concepts c ON cnt.conceptId = c.id
        WHERE cnt.languageCode = :languageCode
          AND c.active = 1
          AND c.categoryId IN (:categoryIds)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getContentsByCategories(
        languageCode: String,
        categoryIds: List<String>,
        limit: Int = 600
    ): List<ContentEntity>

    @Query("SELECT * FROM contents")
    suspend fun getAllContents(): List<ContentEntity>

    @Query("DELETE FROM contents")
    suspend fun clearContents()

    @Query("DELETE FROM concepts")
    suspend fun clearConcepts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConceptCategories(items: List<ConceptCategoryEntity>)

    @Query("SELECT * FROM concept_categories")
    suspend fun getAllConceptCategories(): List<ConceptCategoryEntity>

    @Query("DELETE FROM concept_categories")
    suspend fun clearConceptCategories()

    @Query("DELETE FROM categories WHERE isDefault = 0")
    suspend fun clearCustomCategories()

    @Query("""
        SELECT 
            cat.id AS categoryId,
            cat.name AS categoryName,
            COUNT(DISTINCT c.id) AS totalWords,
            COUNT(DISTINCT CASE WHEN ls.stage = 'LEARNED' THEN c.id END) AS learnedWords
        FROM categories cat
        INNER JOIN concept_categories cc ON cc.categoryId = cat.id
        INNER JOIN concepts c ON cc.conceptId = c.id
        LEFT JOIN learning_states ls ON (ls.conceptId = c.id AND ls.direction = :direction)
        WHERE c.active = 1
        GROUP BY cat.id
        ORDER BY totalWords DESC
    """)
    suspend fun getCategoryMasteryRaw(direction: CardDirection): List<CategoryMasteryRaw>
}

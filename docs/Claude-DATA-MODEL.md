# Data Model و Relationships - FlashLearn v6.84

این سند شامل Entity Relationship Diagram، Constraints، Indexes، و Query Examples است.

---

## بخش اول: Entity Relationship Diagram (ERD)

### نمایشِ ساختاری:

```
┌─────────────────────────┐
│      Concepts           │
│                         │
│ conceptId (PK)         │
│ entryType             │
│ categoryId (FK)       │────┐
│ active                │    │
│ createdAt            │    │
│ updatedAt            │    │
└─────────────────────────┘   │
         │                    │
         │ 1:N               │ N:1
         │ CASCADE           │
         │                   │
┌─────────────────────────┐  │
│      Contents           │  │
│                         │  │
│ contentId (PK)         │  │
│ conceptId (FK)         │──┤
│ languageCode          │  │
│ text                  │  │
│ canonicalKey          │  │
│ translationIndex      │  │
│ notes                 │  │
│ createdAt            │  │
│ updatedAt            │  │
└─────────────────────────┘  │
                            │
    ┌───────────────────────┤
    │                      │
    │                      │
    ▼                      ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│    LearningState        │  │   Categories            │
│                         │  │                         │
│ learningStateId (PK)   │  │ categoryId (PK)        │
│ conceptId (FK)◄────────┼──┤ name                   │
│ stage                 │  │ createdAt              │
│ nextReviewAt          │  │                         │
│ lastReviewedAt        │  └─────────────────────────┘
│ successCount          │
│ failureCount          │
│ totalCorrect          │
│ totalWrong            │
│ monthlyWrongCount     │
│ hasPathFailure        │
│ createdAt            │
│ updatedAt            │
└─────────────────────────┘

┌─────────────────────────┐
│   DifficultyState       │
│                         │
│ difficultyStateId (PK) │
│ conceptId (FK)         │
│ current                │
│ consecutiveCorrect     │
│ consecutiveWrong       │
│ hasReachedVeryHard     │
│ createdAt             │
│ updatedAt             │
└─────────────────────────┘

┌─────────────────────────┐
│  ReviewSession          │
│                         │
│ sessionId (PK)         │
│ startedAt              │
│ endedAt                │
│ reviewType             │
│ totalReviewed          │
│ totalCorrect           │
│ totalWrong             │
│ createdAt             │
└─────────────────────────┘
         │ 1:N
         │
         ▼
┌─────────────────────────┐
│   ReviewHistory         │
│                         │
│ reviewHistoryId (PK)   │
│ sessionId (FK)         │
│ conceptId (FK)         │
│ reviewedAt             │
│ isCorrect              │
│ reviewType             │
│ createdAt             │
└─────────────────────────┘

┌─────────────────────────────────────┐
│        VocabularyRelations          │
│                                     │
│ id (PK)                            │
│ sourceConceptId (FK) ────┐          │
│ targetConceptId (FK) ────┼──────┐  │
│ relationType             │      │  │
│ unresolvedText           │      │  │
│ createdAt               │      │  │
└─────────────────────────────────────┘
                                  │
                                  │ (Self-referential)
                                  │
                           Concepts (conceptId)

┌─────────────────────────┐
│  VocabularyVariants     │
│                         │
│ id (PK)                │
│ conceptId (FK)         │
│ text                   │
│ variantType            │
│ createdAt             │
└─────────────────────────┘

┌─────────────────────────┐
│   ParserMetadata        │
│                         │
│ conceptId (FK/PK)      │
│ breakdownJson          │
│ relationshipsJson      │
│ variantsJson           │
│ confidence             │
│ createdAt             │
└─────────────────────────┘

┌─────────────────────────┐
│    ReviewQueue          │
│                         │
│ id (PK)                │
│ conceptId (FK)         │
│ sourceText             │
│ targetText             │
│ confidence             │
│ possibleCorrection     │
│ status                 │
│ lineNumber             │
│ warning                │
│ createdAt             │
└─────────────────────────┘

┌─────────────────────────┐
│    Achievements         │
│                         │
│ achievementId (PK)     │
│ unlocked               │
│ createdAt             │
└─────────────────────────┘

┌─────────────────────────┐
│      Languages          │
│                         │
│ code (PK)              │
│ name                   │
│ active                 │
└─────────────────────────┘

┌────────────────────────────┐
│    LanguagePairs           │
│                            │
│ sourceLanguageCode (PK)   │
│ targetLanguageCode (PK)   │
│ active                     │
└────────────────────────────┘

┌─────────────────────────┐
│      Streak             │
│                         │
│ streakId (PK)          │
│ currentStreak          │
│ bestStreak             │
│ lastActiveDate         │
│ updatedAt             │
└─────────────────────────┘

┌─────────────────────────┐
│      Settings           │
│                         │
│ settingKey (PK)        │
│ settingValue           │
│ updatedAt             │
└─────────────────────────┘

┌─────────────────────────┐
│    SchemaVersion        │
│                         │
│ schemaVersion (PK)     │
│ lastMigratedAt        │
└─────────────────────────┘
```

---

## بخش دوم: Constraints و Foreign Keys

### Concepts جدول
```sql
PRIMARY KEY: conceptId
UNIQUE: None
FOREIGN KEYS:
  categoryId → Categories.categoryId (ON DELETE SET NULL)

INDEXES:
  - INDEX idx_active_category (active, categoryId)
  - INDEX idx_active_favorite (active) -- اگر favorite اضافه شود
  - INDEX idx_entryType (entryType)
```

### Contents جدول
```sql
PRIMARY KEY: contentId

UNIQUE CONSTRAINTS:
  - UNIQUE(conceptId, languageCode, canonicalKey)
    Reason: تکراری نباید باشد

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)
    Reason: اگر Concept حذف شود، Contents هم حذف شود

INDEXES:
  - INDEX idx_concept_language_index (conceptId, languageCode, translationIndex)
  - INDEX idx_language_canonical (languageCode, canonicalKey)
  - INDEX idx_search (text, canonicalKey)
```

### LearningState جدول
```sql
PRIMARY KEY: learningStateId

UNIQUE CONSTRAINTS:
  - UNIQUE(conceptId)
    Reason: هر Concept یک LearningState

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_stage_nextReview (stage, nextReviewAt)
    Reason: سریع‌تر برای getEligibleConcepts
  - INDEX idx_lastReviewed (lastReviewedAt DESC)
    Reason: سریع‌تر برای sorting
```

### DifficultyState جدول
```sql
PRIMARY KEY: difficultyStateId

UNIQUE CONSTRAINTS:
  - UNIQUE(conceptId)

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_current_difficulty (current)
```

### ReviewSession جدول
```sql
PRIMARY KEY: sessionId

FOREIGN KEYS: None

INDEXES:
  - INDEX idx_startedAt (startedAt DESC)
  - INDEX idx_reviewType (reviewType)
```

### ReviewHistory جدول
```sql
PRIMARY KEY: reviewHistoryId

UNIQUE CONSTRAINTS: None (یک conceptId می‌تواند چند بار مرور شود)

FOREIGN KEYS:
  - sessionId → ReviewSession.sessionId (ON DELETE CASCADE)
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_session_concept (sessionId, conceptId)
    Reason: جریان جلسه
  - INDEX idx_concept_reviewedAt (conceptId, reviewedAt DESC)
    Reason: سریع‌تر برای isReviewedToday
  - INDEX idx_reviewedAt_DESC (reviewedAt DESC)
    Reason: آخرین مروری‌ها
```

### VocabularyRelations جدول
```sql
PRIMARY KEY: id

UNIQUE CONSTRAINTS:
  - UNIQUE(sourceConceptId, targetConceptId, relationType)
    Reason: جفت رابطه یکی است

FOREIGN KEYS:
  - sourceConceptId → Concepts.conceptId (ON DELETE CASCADE)
  - targetConceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_source_type (sourceConceptId, relationType)
    Reason: برای selectDistractors
  - INDEX idx_target_concept (targetConceptId)
```

### VocabularyVariants جدول
```sql
PRIMARY KEY: id

UNIQUE CONSTRAINTS:
  - UNIQUE(conceptId, text, variantType)

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_concept_text (conceptId, text)
```

### ParserMetadata جدول
```sql
PRIMARY KEY: conceptId (FK/PK)

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE CASCADE)

INDEXES: None (کلید اصلی کافی)
```

### ReviewQueue جدول
```sql
PRIMARY KEY: id

FOREIGN KEYS:
  - conceptId → Concepts.conceptId (ON DELETE SET NULL)

INDEXES:
  - INDEX idx_status (status)
  - INDEX idx_status_confidence (status, confidence DESC)
```

### Achievements جدول
```sql
PRIMARY KEY: achievementId

FOREIGN KEYS: None

INDEXES: None
```

### Languages جدول
```sql
PRIMARY KEY: code

INDEXES:
  - INDEX idx_active (active)
```

### LanguagePairs جدول
```sql
PRIMARY KEY: (sourceLanguageCode, targetLanguageCode)

FOREIGN KEYS:
  - sourceLanguageCode → Languages.code (ON DELETE CASCADE)
  - targetLanguageCode → Languages.code (ON DELETE CASCADE)

INDEXES:
  - INDEX idx_active_pair (active, sourceLanguageCode, targetLanguageCode)
```

### Streak جدول
```sql
PRIMARY KEY: streakId

INDEXES:
  - INDEX idx_lastActiveDate (lastActiveDate DESC)
```

### Settings جدول
```sql
PRIMARY KEY: settingKey

INDEXES: None (کلیدی‌ای کافی)
```

---

## بخش سوم: Query Examples

### Query ۱: کلمات آماده برای امروز (Flashcard)

```sql
SELECT c.*, ls.stage, ds.current
FROM concepts c
JOIN learning_states ls ON ls.conceptId = c.id
JOIN difficulty_states ds ON ds.conceptId = c.id
WHERE c.active = 1
  AND ls.stage = 'DAILY'
  AND ls.nextReviewAt <= datetime('now')
  AND NOT EXISTS (
    SELECT 1 FROM review_history rh
    WHERE rh.conceptId = c.id
      AND rh.reviewType = 'FLASHCARD'
      AND DATE(rh.reviewedAt) = DATE('now')
  )
ORDER BY ls.lastReviewedAt ASC, c.id ASC
LIMIT 50;
```

**Indexes مورد استفاده:**
- idx_active
- idx_stage_nextReview
- idx_concept_reviewedAt

---

### Query ۲: کلمات برای کوئیز (بدون محدودیت امروز)

```sql
SELECT c.*, ls.stage, ds.current
FROM concepts c
JOIN learning_states ls ON ls.conceptId = c.id
JOIN difficulty_states ds ON ds.conceptId = c.id
WHERE c.active = 1
  AND ls.stage IN ('DAILY', 'WEEKLY', 'MONTHLY')
  AND ls.nextReviewAt <= datetime('now')
ORDER BY ls.stage ASC, ls.lastReviewedAt ASC
LIMIT 50;
```

---

### Query ۳: Distractor برای سطح MEDIUM

```sql
-- ۱. Synonym/Related
SELECT vr.targetConceptId
FROM vocabulary_relations vr
WHERE vr.sourceConceptId = ?
  AND vr.relationType IN ('SYNONYM', 'RELATED')
LIMIT 1;

-- ۲. دسته‌بندی
SELECT c.id
FROM concepts c
WHERE c.categoryId = (
  SELECT categoryId FROM concepts WHERE id = ?
)
  AND c.id != ?
  AND c.active = 1
LIMIT 1;

-- ۳. رندم
SELECT c.id
FROM concepts c
WHERE c.active = 1
  AND c.id NOT IN (?, ?, ?)
ORDER BY RANDOM()
LIMIT 1;
```

---

### Query ۴: Distractor برای سطح HARD

```sql
SELECT DISTINCT vr.targetConceptId
FROM vocabulary_relations vr
WHERE vr.sourceConceptId = ?
ORDER BY RANDOM()
LIMIT 3;
```

---

### Query ۵: آمار کلی

```sql
SELECT
  (SELECT COUNT(*) FROM concepts WHERE active = 1) as totalConcepts,
  (SELECT COUNT(*) FROM learning_states WHERE stage = 'LEARNED') as learnedConcepts,
  (SELECT SUM(totalCorrect) FROM learning_states) as totalCorrect,
  (SELECT SUM(totalWrong) FROM learning_states) as totalWrong
;
```

---

### Query ۶: توزیع مرحله

```sql
SELECT stage, COUNT(*) as count
FROM learning_states ls
JOIN concepts c ON c.id = ls.conceptId
WHERE c.active = 1
GROUP BY stage;
```

---

### Query ۷: توزیع سطح سختی

```sql
SELECT ds.current, COUNT(*) as count
FROM difficulty_states ds
JOIN concepts c ON c.id = ds.conceptId
WHERE c.active = 1
GROUP BY ds.current;
```

---

### Query ۸: کلمات امروز مرور شده

```sql
SELECT DISTINCT rh.conceptId
FROM review_history rh
WHERE rh.reviewType = 'FLASHCARD'
  AND DATE(rh.reviewedAt) = DATE('now')
;
```

---

### Query ۹: جستجو در کتابخانه

```sql
SELECT c.*, cnt.text, ls.stage, ds.current
FROM concepts c
JOIN contents cnt ON cnt.conceptId = c.id
JOIN learning_states ls ON ls.conceptId = c.id
JOIN difficulty_states ds ON ds.conceptId = c.id
WHERE c.active = 1
  AND (cnt.text LIKE ? OR cnt.canonicalKey LIKE ?)
  AND cnt.languageCode = 'es'
  AND (? IS NULL OR c.categoryId = ?)
  AND (? IS NULL OR ds.current = ?)
ORDER BY c.createdAt DESC;
```

---

### Query ۱۰: روند اخیر (۳۰ روز)

```sql
SELECT 
  DATE(rh.reviewedAt) as date,
  COUNT(*) as totalReviews,
  SUM(CASE WHEN rh.isCorrect = 1 THEN 1 ELSE 0 END) as correctCount
FROM review_history rh
WHERE rh.reviewedAt >= datetime('now', '-30 days')
GROUP BY DATE(rh.reviewedAt)
ORDER BY DATE(rh.reviewedAt) ASC;
```

---

## بخش چهارم: Performance Considerations

### ۱. Connection Pool
```
Min connections: 1
Max connections: 5
Idle timeout: 5 minutes
Max lifetime: 30 minutes
```

### ۲. Query Optimization

#### الف. Batch Operations
```kotlin
// غلط:
FOR concept IN concepts {
  DB.insert(concept)  // N queries
}

// صحیح:
DB.insertAll(concepts)  // 1 query
```

#### ب. Select فیلدهای مورد نیاز
```kotlin
// غلط:
SELECT * FROM concepts  // همهٔ فیلدها

// صحیح:
SELECT id, text, categoryId FROM concepts
```

#### ج. Pagination
```kotlin
// غلط:
SELECT * FROM review_history  // میلیون‌ها rows!

// صحیح:
SELECT * FROM review_history
ORDER BY reviewedAt DESC
LIMIT 100 OFFSET 0;
```

### ۳. Index Usage

| Query | Best Index |
|-------|-----------|
| getEligibleConcepts | idx_stage_nextReview |
| isReviewedToday | idx_concept_reviewedAt |
| searchLibrary | idx_language_canonical |
| selectDistractors | idx_source_type |

### ۴. Caching Strategy

```
Cached Data:
  - Settings: 5 minutes
  - Theme: session lifetime
  - Statistics: 10 minutes
  - Eligible concepts: realtime
```

### ۵. Database Transactions

```kotlin
// برای updateStageAndSchedule:
DB.withTransaction {
  updateLearningState(conceptId, wasCorrect)
  insertReviewHistory(sessionId, conceptId, isCorrect)
  updateDifficulty(conceptId, wasCorrect)
}
// Atomicity: هر سه باید موفق باشند یا هیچ
```

---

## بخش پنجم: Migration Path (v1 → v3)

### Migration v1 → v2 (اگر لازم)
```sql
-- ۱. ReviewSession ایجاد کن
CREATE TABLE IF NOT EXISTS review_sessions (
  id TEXT PRIMARY KEY,
  startedAt TIMESTAMP NOT NULL,
  endedAt TIMESTAMP,
  reviewType TEXT NOT NULL,
  totalReviewed INTEGER DEFAULT 0,
  totalCorrect INTEGER DEFAULT 0,
  totalWrong INTEGER DEFAULT 0,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ۲. ReviewHistory ایجاد کن
CREATE TABLE IF NOT EXISTS review_history (
  id TEXT PRIMARY KEY,
  sessionId TEXT NOT NULL,
  conceptId TEXT NOT NULL,
  reviewedAt TIMESTAMP NOT NULL,
  isCorrect BOOLEAN NOT NULL,
  reviewType TEXT DEFAULT 'FLASHCARD',
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sessionId) REFERENCES review_sessions(id),
  FOREIGN KEY (conceptId) REFERENCES concepts(id)
);

-- ۳. مهاجرت داده‌های قدیم (اگر وجود داشت)
-- [عملیات‌های migration]

-- ۴. Schema Version آپدیت
UPDATE schema_version SET schemaVersion = 2, lastMigratedAt = CURRENT_TIMESTAMP;
```

### Migration v2 → v3 (جداول جدید)
```sql
-- ۱. Languages جدول
CREATE TABLE IF NOT EXISTS languages (
  code TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  active BOOLEAN DEFAULT 1
);

INSERT OR IGNORE INTO languages VALUES ('es', 'اسپانیایی', 1);
INSERT OR IGNORE INTO languages VALUES ('fa', 'فارسی', 1);
INSERT OR IGNORE INTO languages VALUES ('en', 'انگلیسی', 1);

-- ۲. LanguagePairs جدول
CREATE TABLE IF NOT EXISTS language_pairs (
  sourceLanguageCode TEXT NOT NULL,
  targetLanguageCode TEXT NOT NULL,
  active BOOLEAN DEFAULT 1,
  PRIMARY KEY (sourceLanguageCode, targetLanguageCode)
);

INSERT OR IGNORE INTO language_pairs VALUES ('es', 'fa', 1);
INSERT OR IGNORE INTO language_pairs VALUES ('es', 'en', 1);
INSERT OR IGNORE INTO language_pairs VALUES ('fa', 'en', 1);

-- ۳. Streak جدول
CREATE TABLE IF NOT EXISTS streak (
  streakId TEXT PRIMARY KEY,
  currentStreak INTEGER DEFAULT 0,
  bestStreak INTEGER DEFAULT 0,
  lastActiveDate TEXT,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO streak VALUES (
  'default',
  0,
  0,
  NULL,
  CURRENT_TIMESTAMP
);

-- ۴. بقیهٔ جداول...
CREATE TABLE IF NOT EXISTS parser_metadata (...);
CREATE TABLE IF NOT EXISTS vocabulary_variants (...);
CREATE TABLE IF NOT EXISTS vocabulary_relations (...);
CREATE TABLE IF NOT EXISTS review_queue (...);
CREATE TABLE IF NOT EXISTS achievements (...);

-- ۵. Schema Version آپدیت
UPDATE schema_version SET schemaVersion = 3, lastMigratedAt = CURRENT_TIMESTAMP;
```

---

## بخش ششم: Backup و Restore

### Backup Format
```json
{
  "schemaVersion": 3,
  "exportedAt": "2026-10-03T16:30:00Z",
  "format": "json_compressed",
  "tables": {
    "concepts": [...],
    "contents": [...],
    "learningStates": [...],
    "difficultyStates": [...],
    "reviewSessions": [...],
    "reviewHistory": [...],
    "streak": [...],
    "settings": [...],
    "achievements": [...]
  }
}
```

### Restore Process
```kotlin
FUNCTION restore(backupFile: File) {
  
  backup = parseJson(backupFile)
  
  IF backup.schemaVersion < currentSchemaVersion:
    migrate(backup.schemaVersion, currentSchemaVersion)
  
  DB.withTransaction {
    FOR EACH table IN backup.tables:
      clearTable(table)  // اختیاری: پاک‌کردن قدیم
      insertAll(table, backup.tables[table])
  }
  
  return true
}
```

---

**پایان Data Model و Relationships**

تمام جزئیات دیتابیس، Constraints، Indexes، و Queries در این سند آمده است.

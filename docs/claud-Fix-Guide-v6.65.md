# راهنمای اجرایی رفع مشکلات فلش‌لرن (نسخهٔ ۶.۶۵)

این فایل برای دادن به هوش مصنوعی دیگر نوشته شده. هر مورد شمارهٔ همان گزارش ممیزی را دارد. هیچ فایلی از پروژه تغییر نکرده.

## دستور شروع (اول از همه بدهید)

1. زبان کد فقط Kotlin است.
2. هر بار فقط یک مورد را انجام بده. سراغ موارد دیگر نرو.
3. قبل از تغییر فایل دیتابیس یا مدل داده، اول توضیح بده چه چیزی عوض می‌شود و منتظر تأیید بمان.
4. خروجی را به‌صورت فایل کامل بده، نه تکه‌تکه و نه فقط تفاوت‌ها.
5. اگر تابعی به رابط مخزن اضافه می‌کنی، پیاده‌سازی Room و همهٔ Fakeهای تست را هم به‌روز کن (هر ماژول جدا کامپایل می‌شود).
6. الگوریتم‌های اصلی (انتقال مرحله، محاسبهٔ دشواری، زمان‌بندی مرور) باید از هم مستقل بمانند.
7. برای هر مورد یک تست واحد بنویس و فایل پیگیری پیشرفت را به‌روز کن.
8. تا فاز ۶ اسکیمای دیتابیس (نسخهٔ ۷) را تغییر نده.

## ترتیب اجرا و وابستگی‌ها

1. ابتدا پیش‌نیازهای مشترک (بخش پ) را انجام بدهید: تابع لغو‌پذیر، تبدیل امن enum، کوئری‌های جدید.
2. سپس فاز ۱ تا ۷ به ترتیب گزارش.
3. مورد الف-۴ بعد از ب-۱۰ اجرا شود.
4. ب-۴ و ب-۵ بعد از کوئری‌های جدید پ-۳ اجرا شوند.

---

## پ. پیش‌نیازهای مشترک

### پ-۱ · تابع کمکی لغو‌پذیر

**فایل جدید**

`core/src/main/java/com/flashlearn/core/util/Coroutines.kt`

**کد**

```kotlin
package com.flashlearn.core.util

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
```

**کار**

هر جا `runCatching { ... }` یا `catch (e: Exception)` داخل کورتین هست، یکی از این دو را انجام بده:

1. `runCatching` را با `runCatchingCancellable` عوض کن.
2. قبل از `catch (e: Exception)` یک `catch (e: CancellationException) { throw e }` اضافه کن.

### پ-۲ · تبدیل امن enum

**فایل**

`data/src/main/java/com/flashlearn/data/repository/RoomRepositories.kt`

**کد**

```kotlin
private const val TAG = "Mappers"

private inline fun <reified T : Enum<T>> safeEnum(value: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value }
        ?: fallback.also { android.util.Log.w(TAG, "unknown ${T::class.simpleName}=$value") }
```

**کار**

در `Mappers` همهٔ `valueOf(...)` را با `safeEnum(مقدار, پیش‌فرض)` عوض کن:

1. `EntryType` با پیش‌فرض `WORD`
2. `Stage` با پیش‌فرض `DAILY`
3. `ReviewType` با پیش‌فرض `DAILY`
4. `VocabularyDifficulty` با پیش‌فرض همان مقدار میانی
5. `VocabularyRelationType`، `VocabularyVariantType`، `ReviewQueueStatus` با پیش‌فرض اولین مقدار تعریف‌شده

### پ-۳ · کوئری‌های جدید (بدون تغییر ساختار جدول)

**فایل**

`database/src/main/java/com/flashlearn/database/RoomSchema.kt`

**کد در ConceptDao**

```kotlin
@Query(
    "SELECT c.* FROM concepts c JOIN contents x ON x.conceptId = c.id " +
    "WHERE x.languageCode = :lang AND x.canonicalKey = :key AND c.active = 1 " +
    "ORDER BY c.createdAt ASC, c.id ASC LIMIT 1"
)
suspend fun findActiveByKey(lang: String, key: String): ConceptEntity?

@Query(
    "SELECT c.* FROM concepts c JOIN contents x ON x.conceptId = c.id " +
    "WHERE x.languageCode = :lang AND x.canonicalKey = :key AND c.active = 1 AND c.id != :excludeId"
)
suspend fun findActiveByKeyExcluding(lang: String, key: String, excludeId: UUID): List<ConceptEntity>

@Query("SELECT categoryId, COUNT(*) AS count FROM concepts WHERE active = 1 GROUP BY categoryId")
suspend fun countByCategory(): List<CategoryCount>
```

**کد در LearningStateDao**

```kotlin
@Query(
    "SELECT l.stage AS stage, COUNT(*) AS count FROM learning_states l " +
    "JOIN concepts c ON c.id = l.conceptId WHERE c.active = 1 GROUP BY l.stage"
)
suspend fun countByStage(): List<StageCount>

@Query(
    "SELECT COUNT(*) FROM learning_states l JOIN concepts c ON c.id = l.conceptId " +
    "WHERE c.active = 1 AND l.stage = :stage AND l.nextReviewAt IS NOT NULL AND l.nextReviewAt <= :now"
)
suspend fun countDue(stage: String, now: Instant): Int
```

**کد در DifficultyStateDao**

```kotlin
@Query(
    "SELECT d.`current` AS difficulty, COUNT(*) AS count FROM difficulty_states d " +
    "JOIN concepts c ON c.id = d.conceptId WHERE c.active = 1 GROUP BY d.`current`"
)
suspend fun countByDifficulty(): List<DifficultyCount>
```

**کلاس‌های نتیجه (بالای فایل)**

```kotlin
data class CategoryCount(val categoryId: UUID?, val count: Int)
data class StageCount(val stage: String, val count: Int)
data class DifficultyCount(val difficulty: String, val count: Int)
```

**کار**

1. اگر نوع `categoryId` در `ConceptEntity` قابل‌تهی نیست، همان نوع را بگذار.
2. رابط‌های دامنه (`ConceptRepository` و ...) و پیاده‌سازی Room و Fakeها را به‌روز کن.
3. نام ستون‌ها را با `ConceptEntity` و `LearningStateEntity` تطبیق بده.

---

## فاز ۱ · باگ‌های داده

### الف-۱ · ادغام تکراری‌ها وضعیت را پاک می‌کند

**فایل**

`domain/src/main/java/com/flashlearn/domain/usecase/DuplicateConceptUseCases.kt`

**علت**

در `copy(id = survivorLearning.id, ...)` فقط `id` عوض می‌شود. `conceptId` همان واژهٔ تکراری می‌ماند. چون `conceptId` یکتاست، `REPLACE` ردیف واژهٔ تکراری را می‌گیرد و ردیف واژهٔ اصلی حذف می‌شود.

**راه‌حل**

در هر دو `copy` مقدار `conceptId = survivor.id` را اضافه کن.

```kotlin
val merged = preferred.copy(
    id = sl.id,
    conceptId = survivor.id,
    totalCorrect = sl.totalCorrect + dl.totalCorrect,
    totalWrong = sl.totalWrong + dl.totalWrong,
    lastReviewedAt = listOfNotNull(sl.lastReviewedAt, dl.lastReviewedAt).maxOrNull()
)
```

```kotlin
difficultyRepository.upsert(duplicateDifficulty.copy(id = sd.id, conceptId = survivor.id))
```

**تست**

سه واژه بساز. تکراری مرحلهٔ WEEKLY و اصلی DAILY. بعد از ادغام: `learningRepository.get(survivor.id)` باید null نباشد و مرحله WEEKLY باشد. تعداد کل ردیف‌های `learning_states` برای آن واژه دقیقاً یکی باشد.

### الف-۲ · ادغام بیش از دو تکراری

**فایل**

`DuplicateConceptUseCases.kt`

**علت**

`survivorLearning`، `survivorDifficulty` و `survivor` قبل از حلقه یک بار خوانده می‌شوند و در هر دور از همان مقدار قدیمی استفاده می‌شود.

**راه‌حل**

سه متغیر قابل‌تغییر بساز و بعد از هر ادغام به‌روزشان کن.

```kotlin
var currentSurvivor = survivor
var currentLearning = survivorLearning
var currentDifficulty = survivorDifficulty
```

```kotlin
learningRepository.get(duplicate.id)?.let { dl ->
    val sl = currentLearning
    val merged = if (sl == null) {
        dl.copy(id = UUID.randomUUID(), conceptId = survivor.id)
    } else {
        val preferred = if (rank.getValue(dl.stage) > rank.getValue(sl.stage)) dl else sl
        preferred.copy(
            id = sl.id,
            conceptId = survivor.id,
            totalCorrect = sl.totalCorrect + dl.totalCorrect,
            totalWrong = sl.totalWrong + dl.totalWrong,
            lastReviewedAt = listOfNotNull(sl.lastReviewedAt, dl.lastReviewedAt).maxOrNull()
        )
    }
    learningRepository.upsert(merged)
    currentLearning = merged
}
```

```kotlin
difficultyRepository.get(duplicate.id)?.let { dd ->
    val sd = currentDifficulty
    val merged = when {
        sd == null -> dd.copy(id = UUID.randomUUID(), conceptId = survivor.id)
        dd.current.ordinal > sd.current.ordinal -> dd.copy(id = sd.id, conceptId = survivor.id)
        else -> sd
    }
    if (merged !== sd) difficultyRepository.upsert(merged)
    currentDifficulty = merged
}
```

```kotlin
currentSurvivor = currentSurvivor.copy(
    favorite = currentSurvivor.favorite || duplicate.favorite,
    updatedAt = Instant.now()
)
conceptRepository.update(currentSurvivor)
```

**تست**

سه واژهٔ یکسان، شمارنده‌های ۱ و ۲ و ۳. بعد از ادغام، `totalCorrect` باید ۶ باشد. اگر فقط واژهٔ دوم علاقه‌مندی داشته، واژهٔ باقی‌مانده هم علاقه‌مندی باشد.

### الف-۳ · نشانگرهای بدون دو‌نقطه در پارسر

**فایل‌ها**

`domain/.../parser/ParserMarkers.kt`

`domain/.../parser/VocabularyParser.kt`

**علت**

مجموعهٔ `notes` شامل «نکته»، «توضیحات»، «احتمال اشتباه»، «توجه»، «مثال» بدون دو‌نقطه است. ترجمهٔ فارسی که با این کلمه‌ها شروع شود یادداشت حساب می‌شود.

**راه‌حل**

1. از مجموعهٔ `notes` پنج مدخل بدون دو‌نقطه را حذف کن.
2. همین را برای بقیهٔ مجموعه‌ها بررسی کن (مثل تجزیه، مشتق شده از، Breakdown). فقط اگر مشخصات پارسر (الگوریتم ۱) صریحاً اجازه می‌دهد نگه دار.
3. در `explicitMarkerType` بعد از تطبیق ابتدای خط، فقط وقتی مارکر بپذیر که یا خودش با دو‌نقطه تمام شود، یا تمام خط مساوی مارکر باشد.

**تست**

```kotlin
// ورودی: خط اول "hola"، خط دوم "مثال" -> ترجمه، نه یادداشت
// ورودی: خط اول "hola"، خط دوم "مثال: hola amigo" -> یادداشت
```

برای هر پنج کلمه یک تست.

### الف-۴ · دستاوردها هیچ‌وقت باز نمی‌شوند

**فایل‌ها**

`app/.../ui/progress/ProgressViewModel.kt`

`domain/.../gamification/Achievement.kt`

**علت**

ویومدل `AchievementContext` را دستی می‌سازد و چهار فیلد را صفر می‌گذارد. کلاس `CheckAndUnlockAchievements` همین را درست حساب می‌کند ولی هیچ‌جا صدا زده نمی‌شود.

**راه‌حل**

1. اول ب-۱۰ را انجام بده.
2. `CheckAndUnlockAchievements` را در سازندهٔ `ProgressViewModel` تزریق کن.
3. ساخت دستی `AchievementContext` را حذف کن و به‌جایش:

```kotlin
val states = checkAndUnlockAchievements(Instant.now(), ZoneId.systemDefault())
```

4. از `states` همان کاری را بکن که قبلاً با نتیجهٔ ارزیابی می‌کرد.
5. فقط بعد از پایان مرور و ایمپورت این را صدا بزن، نه با هر تغییر بازه.

**تست**

۱۰ واژهٔ تمرین‌شده: دستاورد «اولین ده واژه» باید باز شود.

### الف-۵ · شمارش آمادهٔ مرور با فهرست واقعی نمی‌خواند

**فایل‌ها**

`app/.../ui/review/ReviewViewModel.kt`

`app/.../MainActivity.kt`

`app/.../ui/home/HomeViewModel.kt`

**راه‌حل**

1. در `refreshAvailableReviewCount` همان `languagePair` که در انتخاب واقعی به `ReviewSelectionFilters` داده می‌شود را به شمارش هم بده.
2. `HomeViewModel.refresh` را طوری بنویس که جفت‌زبان را از تنظیمات بخواند. پارامتر پیش‌فرض اسپانیایی-فارسی را حذف کن.
3. سه فراخوانی `homeViewModel.refresh()` در `MainActivity` را حذف کن (جزئیات در ب-۴).

**تست**

دو جفت‌زبان ذخیره کن. شمارش صفحهٔ تنظیم مرور باید با `size` نتیجهٔ انتخاب واقعی برابر باشد.

### الف-۶ · ریستور برچسب هم‌نام

**فایل**

`data/.../backup/RoomBackupRepository.kt`

**راه‌حل**

قبل از درج برچسب‌ها یک نقشهٔ تبدیل شناسه بساز و اتصال‌ها را با آن بنویس.

```kotlin
val existingByName = db.tagDao().getAll().associateBy { it.name }.toMutableMap()
val tagIdMap = HashMap<UUID, UUID>()
for (tag in backupTags) {
    val hit = existingByName[tag.name]
    if (hit != null) {
        tagIdMap[tag.id] = hit.id
    } else {
        db.tagDao().insert(tag)
        existingByName[tag.name] = tag
        tagIdMap[tag.id] = tag.id
    }
}
val links = backupConceptTags.mapNotNull { link ->
    tagIdMap[link.tagId]?.let { link.copy(tagId = it) }
}
db.conceptTagDao().insertAll(links)
```

نام متغیرها را با کد فعلی تطبیق بده. دسته‌بندی‌ها را هم به همین شکل بررسی کن.

**تست**

بک‌آپ با برچسب «سفر» و شناسهٔ A روی دیتابیسی که برچسب «سفر» با شناسهٔ B دارد. ریستور باید موفق شود و اتصال‌ها به B بروند.

### الف-۷ · تنظیمات در ریستور پیشرفت

**فایل**

`data/.../backup/TypedBackupRepository.kt`

**راه‌حل**

1. تصمیم بگیر: اگر تنظیمات باید بازیابی شود، `settingsDao().putAll(...)` را با همان لیست بخوان و بنویس.
2. اگر نه، خواندن آن را حذف کن.
3. `catch (_: Exception)` کلی را دو تکه کن: خطای تجزیهٔ جیسون با پیام «فایل نامعتبر»، خطای بقیه با پیام واقعی و لاگ.

### الف-۸ · حالت رد تکراری

**فایل**

`domain/.../usecase/ImportParsedEntryUseCase.kt`

**راه‌حل**

در ابتدای تابع، بعد از یافتن `existingSource`:

```kotlin
if (existingSource != null && mode == ImportMode.SKIP_DUPLICATE) return
```

نام مقدار enum را از تعریف واقعی `ImportMode` بردار. رفتار را با مشخصات تطبیق بده.

### الف-۹ · نوع رابطه در ریستور

**فایل**

`RoomBackupRepository.kt`

**راه‌حل**

```kotlin
private val validRelationTypes = VocabularyRelationType.values().map { it.name }.toSet()
```

و این را جایگزین `setOf("DERIVED_FROM","USED_IN","SYNONYM")` کن.

### الف-۱۰ · مهاجرت داده‌های راه‌اندازی

**فایل‌ها**

`domain/.../usecase/RefreshDataUseCase.kt`

`app/.../FlashLearnApplication.kt`

**راه‌حل**

1. `require(concept.createdAt <= now)` را حذف کن. به‌جایش اگر `createdAt > now` بود، `Log.w` بزن و ادامه بده.
2. در `FlashLearnApplication` داخل `catch` مهاجرت، حتماً `Log.e` بزن (و لغو را دوباره پرتاب کن).

### الف-۱۱ · واژه‌های غیرفعال در بررسی صحت

**فایل**

`domain/.../usecase/DataIntegrityUseCases.kt`

**راه‌حل**

مجموعهٔ `activeIds` بساز و فقط وضعیت‌هایی را یتیم بشمار که `conceptId` آن‌ها در `getAll()` واژه‌ها اصلاً وجود ندارد (نه صرفاً غیرفعال است).

### الف-۱۲ · فیلدهای مرده و دشواری آسان

1. `monthlyWrongCount` و `hasPathFailure` را یا در الگوریتم انتقال به‌روز کن (طبق مشخصات الگوریتم)، یا از رابط آمار حذف کن. بدون مشخصات تصمیم نگیر.
2. در `QuizUseCases.kt` رفتار EASY و MEDIUM را با مشخصات آزمون تطبیق بده. تصمیم ثبت‌شده: MEDIUM یعنی بدون رتبه‌بندی مجدد.

---

## فاز ۲ · لایهٔ کوئری سریع

### ب-۱ · افزودن تک‌واژه

**فایل**

`domain/.../usecase/UseCases.kt` (تابع `createInTransaction`)

**راه‌حل**

1. پ-۳ را انجام بده.
2. `val activeConcepts = conceptRepository.getAllActive()` را حذف کن.
3. مسیر ادغام:

```kotlin
if (command.mergeExistingSource) {
    val existing = conceptRepository.findActiveByKey(command.sourceLanguage, sourceKey)
    if (existing != null) {
        // همان منطق فعلی ترجمه‌ها
    }
}
```

4. مسیر پشتیبان `contentRepository.getAll().firstOrNull {...}` را کامل حذف کن.
5. هر جای دیگر که از `activeConcepts` برای بررسی تکراری استفاده شده، با همین کوئری عوض کن.

**تست**

۲۰۰۰ واژه بساز. افزودن واژهٔ ۲۰۰۱ نباید `getAll` یا `getAllActive` را صدا بزند (Fake را طوری بنویس که صدا زدنش شکست بدهد).

### ب-۲ · ایمپورت انبوه: رابطه‌ها

**فایل**

`ImportParsedEntryUseCase.kt` (حلقهٔ `entry.relationships`)

**راه‌حل**

```kotlin
val target = conceptRepository.findActiveByKey(sourceLanguage, computeCanonicalKey(relationship.text))
```

و `contentRepository.getAll().firstOrNull {...}` را حذف کن.

### ب-۳ · ایمپورت انبوه: دسته‌ای، پیشرفت، لغو

**فایل**

`app/.../ui/addword/BulkImportViewModel.kt`

**راه‌حل**

```kotlin
private var importJob: Job? = null
private val _progress = MutableStateFlow(0 to 0)
val progress: StateFlow<Pair<Int, Int>> = _progress

fun startImport(entries: List<ParsedEntry>) {
    importJob?.cancel()
    importJob = viewModelScope.launch {
        var done = 0
        entries.chunked(100).forEach { chunk ->
            ensureActive()
            database.withTransaction {
                chunk.forEach { entry ->
                    try {
                        importParsedEntry(entry /* پارامترهای فعلی */)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        failures += entry to (e.message ?: "")
                    }
                }
            }
            done += chunk.size
            _progress.value = done to entries.size
        }
    }
}

fun cancelImport() { importJob?.cancel() }
```

**نکته‌ها**

1. هر خطای یک ردیف نباید کل دسته را شکست بدهد.
2. در صفحه یک نوار پیشرفت و دکمهٔ لغو اضافه کن.
3. `database` باید به ویومدل تزریق شود یا این دسته‌بندی داخل یک use case انجام شود (ترجیحاً use case).

### ب-۴ · صفحهٔ خانه

**فایل‌ها**

`app/.../ui/home/HomeViewModel.kt`

`domain/.../usecase/SettingsDifficultyProgressUseCases.kt`

`app/.../MainActivity.kt`

**راه‌حل**

1. آمار مراحل: از `learningStateDao.countByStage()` بگیر.
2. آمار دشواری: از `countByDifficulty()`.
3. تعداد آمادهٔ مرور روزانه/هفتگی/ماهانه: از `countDue(stage, now)`.
4. در خلاصهٔ پیشرفت، به‌جای `difficultyStateRepository.get(concept.id)` در حلقه:

```kotlin
val difficultyByConcept = difficultyStateRepository.getAll().associateBy { it.conceptId }
```

5. ساخت وضعیت گمشده را از مسیر خواندن بیرون ببر: یک use case جدا به نام `EnsureStatesUseCase` که فقط هنگام شروع برنامه و بعد از ایمپورت و ریستور صدا زده شود.
6. رفع صدا زدن‌های تکراری:

```kotlin
private var refreshJob: Job? = null
fun refresh() {
    refreshJob?.cancel()
    refreshJob = viewModelScope.launch { /* بارگذاری */ }
}
```

7. سه فراخوانی `refresh()` در `MainActivity` را حذف کن. فقط `init` و بعد از عملیاتی که داده را عوض می‌کند (پایان مرور، ایمپورت، افزودن، ریستور) صدا بزن.
8. تاریخچه را فقط یک بار بخوان و به همهٔ محاسبه‌ها بده.

**تست**

با Fake که تعداد صدا زدن `getAll` را می‌شمارد: یک `refresh` بیش از یک خواندن تاریخچه نداشته باشد.

### ب-۵ · انتخاب و شمارش صف مرور

**فایل‌ها**

`domain/.../usecase/ReviewEngineUseCases.kt`

`domain/.../usecase/CountReviewQueueUseCase.kt`

`app/.../ui/review/ReviewViewModel.kt`

**راه‌حل**

1. به‌جای `getAllByStage` + فیلتر حافظه‌ای، از `getDueByStage` و `getAllDueNonLearned` (موجودند) استفاده کن.
2. متن‌ها را فقط برای شناسه‌های نامزد بخوان و دسته‌ای (SQLite حداکثر متغیر دارد):

```kotlin
val contents = candidateIds.chunked(500).flatMap { contentRepository.getForConcepts(it) }
```

3. فیلتر برچسب: `getConceptIdsForTag` برای هر برچسب انتخاب‌شده، یک بار و اشتراک مجموعه‌ها.
4. منطق فیلتر (دشواری، دسته، برچسب، جفت‌زبان) را در یک تابع خصوصی مشترک بگذار و هم انتخاب و هم شمارش صدایش بزنند.
5. در `ReviewViewModel` شمارش را یک بار با مجموعه‌ها بزن و کار قبلی را لغو کن:

```kotlin
private var countJob: Job? = null
fun refreshAvailableReviewCount() {
    countJob?.cancel()
    countJob = viewModelScope.launch {
        delay(150)
        // یک فراخوانی با مجموعهٔ دشواری‌ها و مجموعهٔ دسته‌ها
    }
}
```

**تست**

خروجی `count` باید همیشه برابر `size` نتیجهٔ انتخاب با همان فیلترها باشد. تستی که چندین ترکیب فیلتر را مقایسه کند.

### ب-۷ · صفحهٔ جزئیات واژه

**فایل**

`app/.../ui/library/LibraryDetailScreen.kt` (ویومدل داخل آن)

**راه‌حل**

1. به `ContentRepository` تابع `getAllForConcept(conceptId)` اضافه کن که `contentDao().getAllByConceptId` را صدا بزند. (Fakeها هم.)
2. `contents.getAll().filter { ... }` را با آن عوض کن.
3. `_item.value = null; _message.value = null` را از ابتدای بارگذاری حذف کن. نمایش قبلی بماند تا داده جدید برسد.

### ب-۸ · صفحهٔ پیشرفت

**فایل‌ها**

`app/.../ui/progress/ProgressViewModel.kt`

`domain/.../statistics/Statistics.kt`

`domain/.../progress/Progress.kt`

**راه‌حل**

1. تاریخچه را یک بار بخوان و به همهٔ محاسبه‌گرها بده.
2. برای نمودار فعالیت، یک‌بار گروه‌بندی روزانه:

```kotlin
val byDay = history.groupingBy { it.reviewedAt.atZone(zone).toLocalDate() }.eachCount()
val points = days.map { day -> byDay[day] ?: 0 }
```

3. «آیا حداقل یک مرور دارد» را با `getDistinctConceptIds()` (موجود) بگیر.
4. تغییر `setActivityRange` فقط نمودار را دوباره بسازد، نه کل صفحه را. دادهٔ گروه‌بندی‌شده در ویومدل بماند.
5. ایندکس زمان مرور در فاز ۶.

### ب-۱۰ · دستاوردها (پویش درجه‌دوم)

**فایل**

`domain/.../gamification/Achievement.kt`

**کد**

```kotlin
val learnedIds = learning.asSequence().filter { it.stage == Stage.LEARNED }.map { it.conceptId }.toSet()
val veryHardIds = difficulty.asSequence().filter { it.hasReachedVeryHard }.map { it.conceptId }.toSet()
// در AchievementContext:
veryHardLearnedConcepts = activeIds.count { it in learnedIds && it in veryHardIds }
```

### ب-۱۲ · ذخیرهٔ ویرایش واژه

**فایل**

`UseCases.kt` (ویرایش)

**راه‌حل**

به‌جای حلقهٔ همهٔ واژه‌ها:

```kotlin
val clash = conceptRepository.findActiveByKeyExcluding(sourceLanguage, sourceKey, editingId)
```

و اگر `clash` خالی نبود، ترجمه‌ها را فقط برای همان واژه‌ها مقایسه کن.

### ب-۱۳ · شروع مرور

**فایل**

`ReviewViewModel.kt`

**راه‌حل**

شمارش دسته‌ها را با `conceptDao().countByCategory()` بگیر. بانک آزمون را فقط هنگام شروع جلسهٔ آزمون بساز، نه هنگام ساخت ویومدل.

---

## فاز ۳ · پایداری

### ج-۱ · ردیف خراب

**فایل‌ها**

`RoomRepositories.kt`

`domain/.../model/Models.kt`

**راه‌حل**

1. پ-۲ را انجام بده.
2. در `Mappers.difficulty` قبل از ساخت مدل مقدارها را اصلاح کن:

```kotlin
val correct = e.consecutiveCorrect.coerceAtLeast(0)
val wrong = e.consecutiveWrong.coerceAtLeast(0)
val (c, w) = if (correct > 0 && wrong > 0) correct to 0 else correct to wrong
```

3. در مخزن‌ها به‌جای `map { Mappers.x(it) }` از این استفاده کن تا یک ردیف خراب همه را نشکند:

```kotlin
.mapNotNull { runCatchingCancellable { Mappers.x(it) }.getOrNull() }
```

**تست**

یک ردیف با `stage = "XYZ"` در دیتابیس بگذار. `getAll` باید بدون استثنا برگردد.

### ج-۲ · ویومدل‌های بدون محافظ

**فایل‌ها**

`app/.../ui/review/NeedsReviewViewModel.kt`

`RoomRepositories.kt` (تابع `parserMetadata`)

**راه‌حل**

1. هر `launch` که به مخزن می‌رود را در `runCatchingCancellable` بگذار و شکست را به یک `error` در state بده.
2. در `Mappers.parserMetadata` هر `JSONArray(...)` را جدا با `runCatching` بخوان و در شکست `emptyList()` بده.

### ج-۳ · بک‌آپ بزرگ

**فایل‌ها**

`app/.../ui/backup/BackupScreen.kt`

`core` (تابع کمکی جدید)

**کد**

```kotlin
class LimitedInputStream(private val src: java.io.InputStream, private val max: Long) : java.io.InputStream() {
    private var count = 0L
    override fun read(): Int {
        val b = src.read()
        if (b >= 0 && ++count > max) throw java.io.IOException("فایل بیش از حد بزرگ است")
        return b
    }
    override fun read(buf: ByteArray, off: Int, len: Int): Int {
        val n = src.read(buf, off, len)
        if (n > 0) {
            count += n
            if (count > max) throw java.io.IOException("فایل بیش از حد بزرگ است")
        }
        return n
    }
    override fun close() = src.close()
}
```

**کار**

1. به‌جای اعتماد به `OpenableColumns.SIZE`، خواندن را با `LimitedInputStream` انجام بده.
2. سقف را از ۲۰ مگابایت به ۲۰۰ مگابایت ببر.
3. تشخیص gzip هنگام ریستور:

```kotlin
val input = java.io.BufferedInputStream(stream)
input.mark(2)
val b0 = input.read(); val b1 = input.read()
input.reset()
val source = if (b0 == 0x1f && b1 == 0x8b) java.util.zip.GZIPInputStream(input) else input
```

4. ریستور قدیمی (بدون gzip) باید همچنان کار کند.

### ج-۴ · ساخت بک‌آپ جریانی

**فایل‌ها**

`data/.../backup/RoomBackupRepository.kt`

`BackupScreen.kt`

**تغییر حساس: قبل از اجرا تأیید بگیر**

**راه‌حل**

1. نوشتن با `android.util.JsonWriter` مستقیم روی `OutputStream`:

```kotlin
JsonWriter(java.io.OutputStreamWriter(out, Charsets.UTF_8)).use { w ->
    w.beginObject()
    w.name("reviewHistory").beginArray()
    var offset = 0
    while (true) {
        val page = db.reviewHistoryDao().getPage(1000, offset)
        if (page.isEmpty()) break
        page.forEach { writeHistory(w, it) }
        offset += page.size
    }
    w.endArray()
    w.endObject()
}
```

2. به `ReviewHistoryDao` اضافه کن:

```kotlin
@Query("SELECT * FROM review_history ORDER BY reviewedAt ASC, id ASC LIMIT :limit OFFSET :offset")
suspend fun getPage(limit: Int, offset: Int): List<ReviewHistoryEntity>
```

3. ساختار جیسون خروجی دقیقاً مثل قبل بماند تا ریستور قدیمی کار کند.
4. اول در فایل موقت `cacheDir` بنویس، بعد به Uri مقصد کپی کن.
5. برای فایل پیش از ریستور (`PRE_RESTORE`) هم همین روش.
6. در `BackupScreen` رشتهٔ کامل را در state نگه نداری. فقط مسیر یا Uri.
7. اختیاری: خروجی را با `GZIPOutputStream` فشرده کن.

### ج-۵ · لغو کورتین

**راه‌حل**

پ-۱ را در همهٔ جاهای زیر اعمال کن:

`RoomBackupRepository.kt` (حدود خط ۱۶۵)

`TypedBackupRepository.kt` (دو `catch`)

`BulkImportViewModel.kt`

هر ویومدلی که `runCatching` دارد. برای پیدا کردن: جست‌وجوی `runCatching` و `catch (e: Exception)`.

### ج-۶ · پیام‌های فنی

**فایل‌ها**

`ReviewViewModel.kt`

`UseCases.kt`

**راه‌حل**

1. دو کلاس استثنای مشخص بساز:

```kotlin
class AlreadyReviewedTodayException : IllegalStateException()
class NotDueYetException : IllegalStateException()
```

2. به‌جای `error(...)` با متن انگلیسی، این‌ها را پرتاب کن.
3. در `ReviewViewModel` این دو را بگیر و با رشتهٔ فارسی از منابع نمایش بده. کارت جاری را رد کن تا کاربر گیر نکند.

### ج-۷ · مهاجرت ۶ به ۷

**فایل**

`database/.../DatabaseMigrations.kt`

**تغییر روی مهاجرت: قبل از اجرا تأیید بگیر**

**راه‌حل**

قبل از `CREATE UNIQUE INDEX ... tags(name)` این‌ها را اجرا کن:

```sql
UPDATE OR IGNORE concept_tags SET tagId = (
  SELECT MIN(t2.id) FROM tags t2 WHERE t2.name = (
    SELECT t1.name FROM tags t1 WHERE t1.id = concept_tags.tagId));
DELETE FROM concept_tags WHERE tagId NOT IN (SELECT MIN(id) FROM tags GROUP BY name);
DELETE FROM tags WHERE id NOT IN (SELECT MIN(id) FROM tags GROUP BY name);
```

**تست**

مهاجرت با دیتابیس نسخهٔ ۶ که دو برچسب هم‌نام دارد. مهاجرت باید بدون خطا تمام شود و یک برچسب بماند.

### ج-۸ · زمان‌سنج خودکار آزمون

**فایل**

`ReviewViewModel.kt` (حدود خط ۳۰۰)

**کد**

```kotlin
private var autoAdvanceJob: Job? = null

private fun scheduleAutoAdvance() {
    autoAdvanceJob?.cancel()
    autoAdvanceJob = viewModelScope.launch {
        delay(3_000)
        goNext()
    }
}
```

`autoAdvanceJob?.cancel()` را در `next`، `skip`، `exit` و `reset` هم صدا بزن.

### ج-۹ · مشاهدهٔ state در MainActivity

**فایل**

`app/.../MainActivity.kt`

**راه‌حل**

1. `libraryViewModel.state.value` را با `val libraryState by libraryViewModel.state.collectAsState()` در بالای composable عوض کن.
2. `uiState.operationError` را در یک `Snackbar` یا نوار پیام بگذار و پس از نمایش با یک تابع `clearOperationError()` پاک کن.

### ج-۱۰ · موارد دیگر

1. `DatabaseModule.kt`: برای مهاجرت‌ناموفق (نسخهٔ قدیمی‌تر برنامه)، پیام روشن نشان بده. `fallbackToDestructiveMigration` اضافه نکن.
2. جلسه‌های ناتمام: هنگام شروع برنامه، جلسه‌هایی که `endedAt` ندارند و از آخرین مرورشان بیش از یک روز گذشته را با زمان آخرین مرور ببند.
3. `FlashLearnApplication.kt`: `Log.e` در `catch`.
4. `AppViewModel.kt`: نوع پارامتر `RoomSettingsRepository` را به رابط `SettingsRepository` عوض کن و در ماژول تزریق `@Binds` بگذار.

---

## فاز ۴ · پارسر و آزمون

### ب-۶ · سرعت ساخت سؤال آزمون

**فایل**

`domain/.../usecase/QuizUseCases.kt`

**راه‌حل**

1. الگوهای متنی را بالای فایل ثابت کن:

```kotlin
private val WHITESPACE = Regex("\\s+")
private val DIACRITICS = Regex("\\p{Mn}+")
```

و در `normalizeQuizText` از همین‌ها استفاده کن.

2. در زمان ساخت بانک برای هر مدخل فیلدهای نرمال‌شده را یک بار بساز:

```kotlin
private data class BankEntry(
    val conceptId: UUID,
    val normalized: String,
    val tokens: Set<String>,
    val difficulty: VocabularyDifficulty?,
    val categoryId: UUID?
)
```

3. قبل از هر محاسبهٔ شباهت، نامزدها را کم کن:

```kotlin
val pool = candidates.shuffled(random).take(60)
```

اول از همان دسته و همان دشواری، اگر کم بود از بقیه پر کن.

4. امتیاز را برای هر نامزد یک بار حساب کن و در `sortedByDescending { score }` از فیلد ذخیره‌شده استفاده کن، نه از تابع.
5. بانک را در سطح جلسه یک بار بساز و برای همهٔ سؤال‌های همان جلسه استفاده کن.
6. مرحلهٔ بعد (اختیاری): کلاس `@Singleton` با شمارندهٔ نسخهٔ داده که بعد از ایمپورت، افزودن، ویرایش، حذف و ریستور زیاد شود.

**تست**

۸۰۰۰ واژه، ساخت ۵۰ سؤال پشت‌سرهم زیر یک ثانیه در تست JVM. نتیجه با نسخهٔ قبلی از نظر درستی گزینه‌ها یکسان.

### ب-۱۱ · الگوهای متنی و پارسر

**فایل‌ها**

`UseCases.kt` (تابع `computeCanonicalKey`)

`VocabularyParser.kt`

**کد**

```kotlin
private val WHITESPACE = Regex("\\s+")

fun computeCanonicalKey(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFC)
        .replace(WHITESPACE, " ")
        .lowercase(Locale.ROOT)
```

**کار**

1. در `VocabularyParser` همهٔ `Regex(...)` داخل تابع‌ها (`stripLeadingNumbering`، `isSeparator` و مشابه) را به `private val` بالای فایل ببر.
2. در `explicitMarkerType` مقدار `line.lowercase()` را یک بار بیرون از حلقه حساب کن.
3. خروجی پارسر نباید تغییر کند. تست‌های موجود را اجرا کن.

### ب-۳ (تکمیل)

رابط پیشرفت و لغو در این فاز کامل شود (بالا در فاز ۲ کد آمد).

---

## فاز ۵ · بک‌آپ جریانی

ج-۳ و ج-۴ و الف-۹ (کدها بالا). ترتیب: اول خواندن (gzip و سقف)، بعد نوشتن جریانی. هر مرحله جدا تست و جدا زیپ شود.

---

## فاز ۶ · اسکیمای نسخهٔ ۸ (قبل از هر کاری توضیح کامل بده و تأیید بگیر)

1. ایندکس روی `review_history(reviewedAt)` و `review_history(conceptId)`.
2. ستون `searchKey` در `contents` (متن بدون تلفظ‌نما و با حروف کوچک) به همراه پر کردن در مهاجرت.
3. ادغام برچسب‌های هم‌نام (ج-۷) اگر در نسخهٔ ۷ انجام نشده.
4. بررسی کلید خارجی برای جدول‌های وابسته. اگر بخواهی اضافه شود، مهاجرت باید جدول را بازسازی کند. حتماً تست مهاجرت با داده.
5. پوشهٔ طرح‌واره‌های Room را به مخزن اضافه کن و تست مهاجرت را برای ۷ به ۸ بنویس.

---

## فاز ۷ · رابط، تم، ساخت

### ب-۹ · جست‌وجو و کتابخانه

**راه‌حل بدون تغییر اسکیما**

1. فرار از علامت‌های الگو:

```kotlin
fun escapeLike(q: String) = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
```

و در کوئری `LIKE '%' || :query || '%' ESCAPE '\\'`.

2. تأخیر ۲۵۰ میلی‌ثانیه قبل از جست‌وجو و لغو کار قبلی (مثل `countJob` بالا).
3. حفظ اسکرول: `rememberLazyListState()` را بالاتر از `when` ناوبری بگذار و به صفحه بده.
4. جست‌وجوی بدون تلفظ‌نما بعد از ستون `searchKey` (فاز ۶).

### ب-۱۴ · تم و رابط

1. `remember` برای تایپوگرافی:

```kotlin
val typography = remember(spec) { Typography(/* همان مقدارهای فعلی */) }
```

2. وضعیت تم را از وضعیت ناوبری جدا کن: `themeState: StateFlow` جدا در `AppViewModel` و `distinctUntilChanged()`.
3. `availableThemes()` را در ویومدل و پس‌زمینه بخوان:

```kotlin
val themes = withContext(Dispatchers.IO) { availableThemes() }
```

4. سقف اندازه برای وارد کردن تم (یک مگابایت) با `LimitedInputStream` بالا.
5. در `BulkImportScreen` فیلتر نتایج را بپیچ:

```kotlin
val visibleResults = remember(results, filter) { results.filter { /* شرط فعلی */ } }
```

### د-۱ · ناوبری

فاز جدا و بزرگ. فقط با تأیید. کتابخانهٔ `navigation-compose` را اضافه کن و صفحه‌ها را یکی‌یکی منتقل کن. `collectAsStateWithLifecycle` را با افزودن `lifecycle-runtime-compose` جایگزین `collectAsState` کن.

### د-۲ · بومی‌سازی

1. برچسب‌های ثابت فارسی را از ویومدل‌ها بیرون ببر. ویومدل enum یا شناسه بدهد، صفحه با `stringResource` متن بسازد.
2. دو کپی تابع ارقام فارسی را به یک تابع مشترک در `core` تبدیل کن. اگر زبان رابط فارسی نیست، ارقام لاتین.

### د-۳ · خروجی

1. اول فایل CSV سه بایت BOM بنویس:

```kotlin
out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
```

2. خروجی اکسل را خط‌به‌خط در `BufferedWriter` بنویس، نه `buildString`.

### د-۴ · ساخت

1. `gradle.properties`: `org.gradle.jvmargs=-Xmx3g`
2. فایل نسخه‌ها: از `androidx.compose:compose-bom` استفاده کن و نسخهٔ تک‌تک کتابخانه‌های کامپوز را حذف کن.
3. در سی‌آی `clean` و `--no-daemon` را بردار و یک فراخوانی: `./gradlew assembleDebug testDebugUnitTest lintDebug --build-cache`.
4. Room را از kapt به KSP ببر (اول Room، بعد Hilt در گام جدا). یادآوری: آرگومان‌های `room.*` فقط با KSP کار می‌کنند.
5. پروفایل پایه (Baseline Profile) با ماژول `macrobenchmark` در فاز جدا.
6. `targetSdk` را با الزام روز فروشگاه بررسی کن.

---

## معیار پایان هر مورد

1. کامپایل سی‌آی سبز.
2. تست واحد همان مورد نوشته شده و سبز.
3. تست‌های قبلی خراب نشده.
4. فایل پیگیری پیشرفت به‌روز شده.
5. زیپ کامل تجمعی تحویل شده.

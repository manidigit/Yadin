# مشخصات جامع و نهایی FlashLearn v6.84

**سند مستقل برای ساخت کامل برنامه از صفر**

هر توسعه‌دهنده یا هوش مصنوعی باید بتواند **فقط با خواندن این فایل** کل برنامه را بسازد بدون اینکه چیزی جا بیفتد.

---

## ۱. هدف برنامه

FlashLearn یک برنامه یادگیری واژگان چندزبانه، آفلاین و محلی است.

کاربر کلمات و عبارات را وارد می‌کند. برنامه با روش تکرار فاصله‌دار (Spaced Repetition) آن‌ها را در مراحل روزانه، هفتگی و ماهانه نگه می‌دارد تا به حافظه بلندمدت بروند.

### ویژگی‌های اصلی
- پشتیبانی از چند زبان (حداقل سه، قابل گسترش)
- چند ترجمه همزمان برای یک مفهوم
- دو روش مرور: فلش‌کارت و کوئیز چهارگزینه‌ای
- قابلیت برعکس کردن جهت آزمون
- سطح سختی پویا برای هر کلمه
- سطح سختی کوئیز: مبتدی، متوسط، حرفه‌ای
- ظاهر کاملاً تم‌محور (رنگ، آیکون، چیدمان، فاصله‌ها، شکل کامپوننت‌ها)
- پشتیبان‌گیری و بازگردانی کامل
- نصب و بروزرسانی روی نسخه‌های قبلی بدون از دست رفتن داده
- مهاجرت خودکار دیتابیس

---

## ۲. زبان‌ها و جفت‌های زبانی

برنامه حداقل سه زبان را پشتیبانی می‌کند. معماری باید برای افزودن زبان بیشتر آماده باشد.

| کد | نام | نقش پیش‌فرض |
|----|-----|-------------|
| es | اسپانیایی | زبان مبدأ |
| fa | فارسی | زبان مقصد |
| en | انگلیسی | در دسترس |

### جفت‌های زبانی پشتیبانی‌شده
- اسپانیایی ↔ فارسی
- اسپانیایی ↔ انگلیسی
- فارسی ↔ انگلیسی
- (قابل گسترش)

### قوانین زبان
1. جفت پیش‌فرض: اسپانیایی → فارسی
2. کاربر می‌تواند جهت آزمون را برعکس کند
3. رابط کاربری کاملاً به فارسی و انگلیسی ترجمه شود
4. جهت نوشتار (RTL/LTR) خودکار بر اساس زبان رابط عوض شود
5. زبان رابط برنامه مستقل از زبان‌های یادگیری است

---

## ۳. مدل داده (Database Schema)

همه داده‌ها در دیتابیس محلی پایدار ذخیره می‌شوند. ساختار باید قابل مهاجرت باشد.

### ۳.۱ جدول Concepts (مفاهیم)

هر کلمه یا عبارت یک «مفهوم» است.

| فیلد | نوع | توضیح |
|------|------|--------|
| conceptId | UUID | کلید اصلی |
| entryType | ENUM | WORD, PHRASE, SENTENCE, IDIOM, STRUCTURE, COLLOCATION |
| categoryId | UUID (FK) | دسته‌بندی (اختیاری، NULL مجاز) |
| active | BOOLEAN | فعال بودن (soft-delete، نه حذف واقعی) |
| createdAt | TIMESTAMP | |
| updatedAt | TIMESTAMP | |

**نکات:**
- شناسه‌ها UUID هستند (نه Auto-Increment)
- active=false به‌جای حذف واقعی
- فیلد خالی (empty string) مجاز نیست

---

### ۳.۲ جدول Contents (محتوا/ترجمه‌ها)

هر مفهوم می‌تواند چند محتوا در زبان‌های مختلف و چند ترجمه همزمان داشته باشد.

| فیلد | نوع | توضیح |
|------|------|--------|
| contentId | UUID | کلید اصلی |
| conceptId | UUID (FK) | ارجاع به Concepts (CASCADE DELETE) |
| languageCode | STRING | es, fa, en |
| text | STRING | متن ترجمه (غیرخالی) |
| canonicalKey | STRING | نسخهٔ نرمال‌سازی شدهٔ متن |
| translationIndex | INTEGER | ترتیب: 0 (اولی)، 1 (دوم)، ... |
| notes | STRING | یادداشت (اختیاری) |
| createdAt | TIMESTAMP | |
| updatedAt | TIMESTAMP | |

**Constraints:**
- PRIMARY KEY: contentId
- FOREIGN KEY: conceptId → Concepts (CASCADE DELETE)
- UNIQUE: (conceptId, languageCode, canonicalKey)
- INDEX: (languageCode, canonicalKey) برای تسریع جلوگیری از تکرار

**Canonical Key:**
```
canonicalKey = toLowerCase(normalize(trim(text)))
normalize: حروف خاص حذف، فاصله‌های اضافی حذف، تشدید‌ها حذف
```

مثال:
- "   Apple  " → "apple"
- "Ñoño" → "nono"

**قانون مهم:** کلمه تکراری پذیرفته نمی‌شود. اگر canonicalKey موجود باشد، ورود رد می‌شود.

---

### ۳.۳ جدول LearningState (وضعیت یادگیری)

برای هر مفهوم یک وضعیت یادگیری وجود دارد.

| فیلد | نوع | توضیح |
|------|------|--------|
| learningStateId | UUID | کلید اصلی |
| conceptId | UUID (FK) | یکتا، ارجاع به Concepts |
| stage | ENUM | DAILY, WEEKLY, MONTHLY, LEARNED |
| nextReviewAt | TIMESTAMP | زمان مرور بعدی (NULL برای LEARNED) |
| lastReviewedAt | TIMESTAMP | آخرین بار مرور (NULL اگر هرگز نشده) |
| successCount | INTEGER | تعداد درست متوالی |
| failureCount | INTEGER | تعداد غلط متوالی |
| totalCorrect | INTEGER | کل درست (تمام زمان‌ها) |
| totalWrong | INTEGER | کل غلط |
| monthlyWrongCount | INTEGER | تعداد غلط در مرحلهٔ ماهانه |
| hasPathFailure | BOOLEAN | پرچم شکست مسیر ماهانه |
| createdAt | TIMESTAMP | |
| updatedAt | TIMESTAMP | |

**Constraints:**
- conceptId یکتا است
- INDEX: (stage, nextReviewAt) برای جستجوی سریع

---

### ۳.۴ جدول DifficultyState (وضعیت سختی)

سطح سختی کلمه، مستقل از stage یادگیری.

| فیلد | نوع | توضیح |
|------|------|--------|
| difficultyStateId | UUID | کلید اصلی |
| conceptId | UUID (FK) | یکتا |
| current | ENUM | EASY, MEDIUM, HARD, VERY_HARD |
| consecutiveCorrect | INTEGER | درست متوالی (برای بالا رفتن) |
| consecutiveWrong | INTEGER | غلط متوالی (برای پایین آمدن) |
| hasReachedVeryHard | BOOLEAN | آیا تا به حال VERY_HARD شده |
| createdAt | TIMESTAMP | |
| updatedAt | TIMESTAMP | |

---

### ۳.۵ جدول Categories (دسته‌بندی‌ها)

| فیلد | نوع | توضیح |
|------|------|--------|
| categoryId | UUID | کلید اصلی |
| name | STRING | نام دسته (یکتا، غیرخالی) |
| createdAt | TIMESTAMP | |

**دسته‌های پیش‌فرض (seed):**
1. افعال (Verbs)
2. خوراکی و غذا (Food)
3. خانه و وسایل (House & Objects)
4. خانواده (Family)
5. بدن و پزشکی (Body & Health)
6. حمل‌ونقل (Transportation)
7. سفر و گردشگری (Travel)
8. شغل و کار (Work)
9. اصطلاح (Idiom)
10. مکالمهٔ روزمره (Daily Conversation)

---

### ۳.۶ جدول ReviewSession (جلسات مرور)

| فیلد | نوع | توضیح |
|------|------|--------|
| sessionId | UUID | کلید اصلی |
| startedAt | TIMESTAMP | زمان شروع |
| endedAt | TIMESTAMP | زمان پایان (NULL اگر جاری) |
| reviewType | ENUM | DAILY, WEEKLY, MONTHLY, RANDOM, LEARNED |
| totalReviewed | INTEGER | کل مرور شده |
| totalCorrect | INTEGER | درست |
| totalWrong | INTEGER | غلط |

---

### ۳.۷ جدول ReviewHistory (تاریخچهٔ مرور)

| فیلد | نوع | توضیح |
|------|------|--------|
| reviewHistoryId | UUID | کلید اصلی |
| sessionId | UUID (FK) | ارجاع به جلسه |
| conceptId | UUID (FK) | ارجاع به مفهوم |
| reviewedAt | TIMESTAMP | زمان مرور |
| isCorrect | BOOLEAN | درست/غلط |
| reviewType | ENUM | FLASHCARD, QUIZ |

**Constraints:**
- INDEX: (reviewedAt, conceptId) برای آمار سریع

---

### ۳.۸ جدول Streak (رگبار)

یک سطر واحد برای کل برنامه.

| فیلد | نوع | توضیح |
|------|------|--------|
| streakId | UUID | کلید اصلی |
| currentStreak | INTEGER | روزهای پیاپی فعلی |
| bestStreak | INTEGER | بهترین رگبار |
| lastActiveDate | DATE | آخرین فعالیت (YYYY-MM-DD) |
| updatedAt | TIMESTAMP | |

---

### ۳.۹ جدول Settings (تنظیمات)

| فیلد | نوع | توضیح |
|------|------|--------|
| settingKey | STRING | کلید تنظیم |
| settingValue | STRING | مقدار |
| updatedAt | TIMESTAMP | |

**تنظیمات پیش‌فرض:**
```
themeId = "grok"
isDark = true
uiLanguage = "fa"
difficultyThreshold = 3
defaultQuizDifficulty = "MEDIUM"
activeLanguagePair = "es-fa"
maxQuizPoolSize = 50
```

---

### ۳.۱۰ جدول Languages (زبان‌ها)

| فیلد | نوع | توضیح |
|------|------|--------|
| code | STRING | کد زبان (es, fa, en) - کلید اصلی |
| name | STRING | نام زبان فارسی |
| active | BOOLEAN | فعال بودن |

**داده پیش‌فرض:**
```
es | اسپانیایی | true
fa | فارسی | true
en | انگلیسی | true
```

---

### ۳.۱۱ جدول LanguagePairs (جفت زبانی)

| فیلد | نوع | توضیح |
|------|------|--------|
| sourceLanguageCode | STRING | کد زبان اول |
| targetLanguageCode | STRING | کد زبان دوم |
| active | BOOLEAN | فعال بودن |

**Constraints:**
- PRIMARY KEY: (sourceLanguageCode, targetLanguageCode)
- INDEX: (active, sourceLanguageCode, targetLanguageCode)

**داده پیش‌فرض:**
```
es | fa | true
es | en | true
fa | en | true
```

---

### ۳.۱۲ جدول ParserMetadata (متادیتای تجزیه)

برای تجزیه و تحلیل ساختار کلمات.

| فیلد | نوع | توضیح |
|------|------|--------|
| conceptId | UUID | کلید اصلی (FK) |
| breakdownJson | STRING | تقسیم کلمه (مثلاً: "run+ing") |
| relationshipsJson | STRING | روابط ریخت‌شناسی |
| variantsJson | STRING | تنوع‌های کلمه |
| confidence | DOUBLE | میزان اطمینان (0-1) |

**مثال:**
```json
conceptId: "550e8400-e29b-41d4-a716-446655440000"
breakdownJson: {"morphemes": ["run", "ing"], "type": "verb+participle"}
relationshipsJson: {"morphologicalFamily": ["run", "runs", "running", "ran"]}
variantsJson: ["run", "runs", "running", "ran", "runner"]
confidence: 0.95
```

---

### ۳.۱۳ جدول VocabularyVariants (تنوع‌های واژگان)

تنوع‌های یک کلمه (مفرد، جمع، فعل‌های مختلف، ...)

| فیلد | نوع | توضیح |
|------|------|--------|
| id | UUID | کلید اصلی |
| conceptId | UUID (FK) | ارجاع به Concepts |
| text | STRING | متن تنوع |
| variantType | STRING | SINGULAR, PLURAL, PAST, PRESENT, GERUND, ... |

**Constraints:**
- UNIQUE: (conceptId, text, variantType)

**مثال:**
```
conceptId: "550e8400-e29b-41d4-a716-446655440000"
text: "runs"
variantType: "PRESENT_THIRD_PERSON"

conceptId: "550e8400-e29b-41d4-a716-446655440000"
text: "running"
variantType: "GERUND"
```

---

### ۳.۱۴ جدول VocabularyRelations (روابط واژگان)

روابط بین کلمات: مترادف، نقیض، مرتبط، ...

| فیلد | نوع | توضیح |
|------|------|--------|
| id | UUID | کلید اصلی |
| sourceConceptId | UUID (FK) | کلمهٔ اصلی |
| targetConceptId | UUID (FK) | کلمهٔ مرتبط (اختیاری) |
| relationType | STRING | SYNONYM, ANTONYM, RELATED, RELATED_CATEGORY, MORPHOLOGICAL |
| unresolvedText | STRING | اگر targetConceptId NULL، متن مستقیم نوشته می‌شود |

**Constraints:**
- UNIQUE: (sourceConceptId, targetConceptId, relationType)
- INDEX: (targetConceptId)

**مثال:**
```
sourceConceptId: "550e8400-e29b-41d4-a716-446655440000" (run)
targetConceptId: "660e8400-e29b-41d4-a716-446655440001" (sprint)
relationType: "SYNONYM"

sourceConceptId: "550e8400-e29b-41d4-a716-446655440000" (run)
targetConceptId: "770e8400-e29b-41d4-a716-446655440002" (walk)
relationType: "RELATED"

sourceConceptId: "550e8400-e29b-41d4-a716-446655440000" (run)
targetConceptId: "880e8400-e29b-41d4-a716-446655440003" (sport)
relationType: "RELATED_CATEGORY"
```

---

### ۳.۱۵ جدول ReviewQueue (صف مرور)

صف کلماتی که منتظر بررسی هستند (برای import بصری).

| فیلد | نوع | توضیح |
|------|------|--------|
| id | UUID | کلید اصلی |
| conceptId | UUID (FK) | اختیاری (NULL اگر هنوز تایید نشده) |
| sourceText | STRING | متن مبدأ |
| targetText | STRING | متن مقصد (اختیاری) |
| confidence | DOUBLE | میزان اطمینان تجزیه (0-1) |
| possibleCorrection | STRING | تصحیح پیشنهادی |
| status | ENUM | PENDING, APPROVED, REJECTED, NEEDS_REVIEW |
| lineNumber | INTEGER | شماره خط در فایل import |
| warning | STRING | هشدار (مثلاً: "کلمه تکراری") |

---

### ۳.۱۶ جدول Achievements (دستاوردها)

بج‌ها و نشان‌های کاربر.

| فیلد | نوع | توضیح |
|------|------|--------|
| achievementId | STRING | کلید اصلی (BRONZE_10_STREAK, SILVER_100_WORDS, ...) |
| unlocked | BOOLEAN | آیا باز شده |

**مثال:**
```
BRONZE_10_STREAK | true (۱۰ روز پیاپی)
SILVER_100_WORDS | false (۱۰۰ کلمه یاد گرفتن)
GOLD_1000_REVIEWS | false (۱۰۰۰ بار مرور)
```

---

### ۳.۱۷ نسخهٔ Schema دیتابیس

| فیلد | نوع | توضیح |
|------|------|--------|
| schemaVersion | INTEGER | شماره نسخه (حالا: 3) |
| lastMigratedAt | TIMESTAMP | آخرین مهاجرت |

**نسخه‌ها:**
- v1: جداول پایه (Concepts, Contents, LearningState, DifficultyState, Categories, ReviewSession, ReviewHistory, Streak, Settings)
- v2: (مهاجرت شروع شده)
- v3: جداول اضافی (Languages, LanguagePairs, ParserMetadata, VocabularyVariants, VocabularyRelations, ReviewQueue, Achievements)

---

## ۴. الگوریتم‌های اصلی

### ۴.۱ انتقال مرحلهٔ یادگیری (Stage Transition)

**ورودی:**
- conceptId
- wasCorrect: Boolean
- difficultyThreshold: Integer (پیش‌فرض: 3)

**خروجی:**
- Stage جدید
- nextReviewAt جدید
- successCount / failureCount جدید

**Pseudo-code:**
```
FUNCTION updateStageAndSchedule(conceptId, wasCorrect, threshold = 3):
  learning = DB.get(LearningState, conceptId)
  
  IF learning.stage == LEARNED:
    // کلماتِ یادگرفته ثابت می‌مانند
    RETURN
  
  IF wasCorrect == TRUE:
    learning.successCount += 1
    learning.failureCount = 0
    
    IF learning.stage == DAILY:
      IF learning.successCount >= 1:
        learning.stage = WEEKLY
        learning.nextReviewAt = now + 7_days_midnight
    
    ELSE IF learning.stage == WEEKLY:
      IF learning.successCount >= 3:  // آستانه
        learning.stage = MONTHLY
        learning.nextReviewAt = now + 30_days_midnight
    
    ELSE IF learning.stage == MONTHLY:
      IF learning.successCount >= 3:
        learning.stage = LEARNED
        learning.nextReviewAt = NULL
  
  ELSE:  // wasCorrect == FALSE
    learning.failureCount += 1
    learning.successCount = 0
    
    prev_stage = learning.stage
    learning.stage = DAILY
    learning.nextReviewAt = tomorrow_midnight
    
    // اگر در ماهانه غلط زده:
    IF prev_stage == MONTHLY:
      learning.monthlyWrongCount += 1
      learning.hasPathFailure = TRUE
  
  learning.lastReviewedAt = now
  learning.totalCorrect += (wasCorrect ? 1 : 0)
  learning.totalWrong += (wasCorrect ? 0 : 1)
  learning.updatedAt = now
  
  DB.save(LearningState, learning)
  
  // ثبت در ReviewHistory
  INSERT ReviewHistory {
    reviewHistoryId: UUID(),
    conceptId: conceptId,
    reviewedAt: now,
    isCorrect: wasCorrect,
    reviewType: FLASHCARD,
    sessionId: current_session_id
  }
END
```

**نکات:**
- successCount مقدار درست **متوالی** است
- فشل (غلط) شمارنده را reset می‌کند
- امروز تاریخ محلی دستگاه است

---

### ۴.۲ قانون "امروز مرور شده"

**قانون:** کلمه‌ای که امروز مرور شده، همان روز دوباره در مرور عادی نمی‌آید.

**Pseudo-code:**
```
FUNCTION isReviewedToday(conceptId):
  count = DB.countReviews(
    conceptId = conceptId,
    reviewType = FLASHCARD,
    reviewedAt >= today_midnight AND reviewedAt < tomorrow_midnight
  )
  RETURN count > 0
END

FUNCTION getEligibleConcepts(reviewType, categoryFilter, difficultyFilter, now):
  // quiz نمی‌شود محدود به "امروز مرور شده"
  
  IF reviewType == FLASHCARD:
    candidates = DB.query(
      active = true,
      stage = (DAILY if reviewType==DAILY else WEEKLY if reviewType==WEEKLY else MONTHLY),
      nextReviewAt <= now,
      NOT isReviewedToday(conceptId),  // ← این شرط تنها برای FLASHCARD
      categoryFilter (if set),
      difficultyFilter (if set)
    )
  ELSE:  // QUIZ
    candidates = DB.query(
      active = true,
      stage IN (DAILY, WEEKLY, MONTHLY),  // هر مرحله‌ای
      nextReviewAt <= now,
      // NO "today" check for quiz
      categoryFilter (if set),
      difficultyFilter (if set)
    )
  
  candidates.ORDER BY stage ASC, lastReviewedAt ASC
  RETURN candidates.LIMIT(maxQuizPoolSize)
END
```

---

### ۴.۳ تحدیث سطح سختی (Difficulty Update)

**ورودی:**
- conceptId
- wasCorrect: Boolean
- difficultyThreshold: Integer (۱-۵)

**خروجی:**
- Difficulty جدید (EASY/MEDIUM/HARD/VERY_HARD)
- consecutiveCorrect / consecutiveWrong جدید

**Pseudo-code:**
```
FUNCTION updateDifficulty(conceptId, wasCorrect, threshold):
  difficulty = DB.get(DifficultyState, conceptId)
  
  IF wasCorrect == TRUE:
    difficulty.consecutiveCorrect += 1
    difficulty.consecutiveWrong = 0
    
    IF difficulty.consecutiveCorrect >= threshold:
      IF difficulty.current == EASY:
        difficulty.current = MEDIUM
      ELSE IF difficulty.current == MEDIUM:
        difficulty.current = HARD
      ELSE IF difficulty.current == HARD:
        difficulty.current = VERY_HARD
        difficulty.hasReachedVeryHard = TRUE
      
      difficulty.consecutiveCorrect = 0
  
  ELSE:  // wasCorrect == FALSE
    difficulty.consecutiveWrong += 1
    difficulty.consecutiveCorrect = 0
    
    IF difficulty.consecutiveWrong >= threshold:
      IF difficulty.current == VERY_HARD:
        difficulty.current = HARD
      ELSE IF difficulty.current == HARD:
        difficulty.current = MEDIUM
      ELSE IF difficulty.current == MEDIUM:
        difficulty.current = EASY
      // EASY نمی‌تواند کمتر شود
      
      difficulty.consecutiveWrong = 0
  
  difficulty.updatedAt = now
  DB.save(DifficultyState, difficulty)
END
```

**نکات:**
- Threshold قابل تنظیم است (۱-۵)
- سختی کاهش نمی‌تواند کمتر از EASY باشد
- سختی بالا نمی‌تواند بیشتر از VERY_HARD باشد

---

### ۴.۴ انتخاب Distractor بر اساس سطح کوئیز

**ورودی:**
- correctConceptId: UUID
- quizDifficulty: EASY, MEDIUM, HARD
- maxDistractors: Integer (معمولاً 3)
- categoryId: UUID (اختیاری)

**خروجی:**
- List<UUID> از 3 conceptId برای Distractor

**Pseudo-code:**

```
FUNCTION selectDistractors(correctConceptId, quizDifficulty, maxDistractors = 3):
  
  IF quizDifficulty == EASY:
    // ۱. همهٔ غلط‌ها کاملاً رندم
    candidates = DB.get(Concepts, active=true, NOT correctConceptId)
    SHUFFLE(candidates)
    RETURN candidates.LIMIT(maxDistractors)
  
  ELSE IF quizDifficulty == MEDIUM:
    // ۲. سه نوع Distractor:
    //    - یکی نزدیک (مشابه معنی)
    //    - یکی از دسته‌بندی
    //    - یکی کاملاً رندم
    
    distractors = []
    
    // ۲.۱ یکی نزدیک (مترادف یا مرتبط)
    similar = DB.query(
      VocabularyRelations,
      targetConceptId = correctConceptId,
      relationType IN (SYNONYM, RELATED)
    )
    IF similar.size > 0:
      d1 = RANDOM(similar)
      distractors.ADD(d1.sourceConceptId)
    
    // ۲.۲ یکی از همان دسته‌بندی
    correctConcept = DB.get(Concepts, correctConceptId)
    sameCategory = DB.query(
      Concepts,
      categoryId = correctConcept.categoryId,
      active = true,
      NOT correctConceptId
    )
    IF sameCategory.size > 0:
      d2 = RANDOM(sameCategory)
      distractors.ADD(d2.id)
    
    // ۲.۳ یکی کاملاً رندم
    random_all = DB.get(Concepts, active=true, NOT correctConceptId, NOT IN distractors)
    IF random_all.size > 0:
      d3 = RANDOM(random_all)
      distractors.ADD(d3.id)
    
    // اگر کمتر از 3 پیدا شد، بقیه رندم پر کن
    WHILE distractors.size < maxDistractors:
      random = RANDOM(DB.get(Concepts, active=true, NOT correctConceptId, NOT IN distractors))
      distractors.ADD(random.id)
    
    SHUFFLE(distractors)
    RETURN distractors.LIMIT(maxDistractors)
  
  ELSE IF quizDifficulty == HARD:
    // ۳. هر سه Distractor مرتبط (از VocabularyRelations)
    
    distractors = []
    
    // دریافت تمام روابط این کلمه
    relations = DB.query(
      VocabularyRelations,
      sourceConceptId = correctConceptId,
      relationType IN (SYNONYM, ANTONYM, RELATED, RELATED_CATEGORY, MORPHOLOGICAL)
    )
    
    IF relations.size >= maxDistractors:
      // انتخاب maxDistractors اول
      selected = relations.LIMIT(maxDistractors)
      FOR EACH rel IN selected:
        distractors.ADD(rel.targetConceptId)
    
    ELSE:
      // اگر روابط کافی نیست، رندم پر کن
      FOR EACH rel IN relations:
        distractors.ADD(rel.targetConceptId)
      
      WHILE distractors.size < maxDistractors:
        random = RANDOM(DB.get(Concepts, active=true, NOT correctConceptId, NOT IN distractors))
        distractors.ADD(random.id)
    
    SHUFFLE(distractors)
    RETURN distractors.LIMIT(maxDistractors)
END
```

**مثال‌های عملی:**

```
correctConceptId: "apple" (سیب)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EASY (مبتدی):
  Distractor 1: "car" (کاملاً رندم)
  Distractor 2: "mountain" (کاملاً رندم)
  Distractor 3: "computer" (کاملاً رندم)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
MEDIUM (متوسط):
  Distractor 1: "orange" (نزدیک = میوه/SYNONYM)
  Distractor 2: "table" (دسته‌بندی = خانه)
  Distractor 3: "airplane" (کاملاً رندم)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
HARD (حرفه‌ای):
  Distractor 1: "fruit" (RELATED_CATEGORY)
  Distractor 2: "tree" (RELATED)
  Distractor 3: "orange" (SYNONYM)
```

---

### ۴.۵ آمار و محاسبات

**Statistics ها:**
- تعداد کل کلمات
- تعداد یادگرفته
- تعداد در هر stage (DAILY/WEEKLY/MONTHLY/LEARNED)
- تعداد در هر difficulty (EASY/MEDIUM/HARD/VERY_HARD)
- دقت کلی (totalCorrect / (totalCorrect + totalWrong))
- رگبار فعلی و بهترین
- تعداد مرور آماده امروز

**Pseudo-code:**
```
FUNCTION calculateStatistics():
  stats = {
    totalConcepts: DB.count(Concepts, active=true),
    learnedConcepts: DB.count(LearningState, stage=LEARNED),
    accuracy: totalCorrect / (totalCorrect + totalWrong) if totalCorrect+totalWrong > 0 else 0,
    stageDistribution: {
      DAILY: DB.count(LearningState, stage=DAILY),
      WEEKLY: DB.count(LearningState, stage=WEEKLY),
      MONTHLY: DB.count(LearningState, stage=MONTHLY),
      LEARNED: DB.count(LearningState, stage=LEARNED)
    },
    difficultyDistribution: {
      EASY: DB.count(DifficultyState, current=EASY),
      MEDIUM: DB.count(DifficultyState, current=MEDIUM),
      HARD: DB.count(DifficultyState, current=HARD),
      VERY_HARD: DB.count(DifficultyState, current=VERY_HARD)
    },
    streak: DB.get(Streak).currentStreak,
    bestStreak: DB.get(Streak).bestStreak,
    readyToday: DB.countDue(now)
  }
  RETURN stats
END
```

---

### ۴.۶ به‌روزرسانی Streak

**وقتی جلسهٔ مرور تمام شود:**

```
FUNCTION updateStreak():
  streak = DB.get(Streak)
  today = getCurrentDate()
  yesterday = today - 1 day
  
  IF streak.lastActiveDate == today:
    // امروز قبلاً فعالیت شده، تغییری نیست
    RETURN
  
  ELSE IF streak.lastActiveDate == yesterday:
    // دیروز فعالیت شده و امروز ادامه داده، +1
    streak.currentStreak += 1
    IF streak.currentStreak > streak.bestStreak:
      streak.bestStreak = streak.currentStreak
  
  ELSE:
    // شکاف بیشتر از یک روز
    streak.currentStreak = 1
  
  streak.lastActiveDate = today
  streak.updatedAt = now
  DB.save(Streak, streak)
END
```

---

## ۵. صفحات و رابط کاربری

### ۵.۱ صفحهٔ اصلی (Home)

- نمایش رگبار (روز‌های پیاپی و بهترین)
- ۴ کارت مرور (روزانه، هفتگی، ماهانه، رندم)
- نمایش تعداد آماده‌ای برای هر مرور
- خلاصهٔ پیشرفت (یادگرفته، دقت، دسته‌ها)
- Bottom Navigation (۴ تب)

---

### ۵.۲ صفحهٔ مرور (Review)

**مرحلهٔ ۱: تنظیمات**
- انتخاب نوع مرور (FLASHCARD / QUIZ)
- فیلتر دسته‌بندی (اختیاری)
- فیلتر سطح سختی (اختیاری)
- انتخاب سطح کوئیز (EASY/MEDIUM/HARD) - فقط برای QUIZ
- جهت آزمون (برعکس/معمولی)

**مرحلهٔ ۲: فلش‌کارت یا کوئیز**

#### Flashcard:
- نمایش کلمهٔ مبدأ
- دکمهٔ کشف (Reveal)
- نمایش ترجمه
- دکمه‌های (درست / غلط)
- دکمهٔ تلفظ

#### Quiz:
- سؤال (کلمهٔ مبدأ)
- ۴ گزینه (درست + ۳ Distractor)
- انتخاب گزینه
- بازخورد فوری:
  - ✅ سبز اگر درست
  - ❌ قرمز اگر غلط
- ۳ ثانیه صبر خودکار
- سپس سؤال بعدی

**مرحلهٔ ۳: نتایج**
- درصد دقت
- مجموع درست/غلط
- Streak اپدیت‌شده
- گزینهٔ بازگشت یا تکرار

---

### ۵.۳ صفحهٔ افزودن کلمه (Add Word)

**روش ۱: ورود تکی**
- فیلد: کلمهٔ مبدأ (اجباری)
- فیلد: ترجمه (اجباری)
- Dropdown: نوع (WORD/PHRASE/SENTENCE/IDIOM/STRUCTURE/COLLOCATION)
- Dropdown: دسته‌بندی (اختیاری)
- فیلد: یادداشت (اختیاری)

**روش ۲: ورود دسته‌ای**
- چسباندن چند خط
- رفتار با تکراری:
  - فقط جدید
  - رد
  - ادغام
  - بروزرسانی

---

### ۵.۴ کتابخانه (Library)

- لیست کامل واژگان
- جستجو در متن مبدأ و مقصد
- فیلتر: دسته‌بندی، سطح سختی، مرحلهٔ یادگیری
- مشاهدهٔ جزئیات هر کلمه
- ویرایش و حذف (soft-delete)

---

### ۵.۵ پیشرفت (Progress)

- خلاصه: تعداد، یادگرفته، دقت
- نمودار توزیع Stage
- نمودار توزیع Difficulty
- نمودار روند (نقاط ماهانه)
- رگبار
- تاریخچهٔ جلسات اخیر

---

### ۵.۶ تنظیمات (Settings)

**ظاهر:**
- تم (کل برنامه عوض می‌شود)
- حالت روشن/تاریک

**زبان:**
- زبان رابط
- جفت زبانی فعال

**یادگیری:**
- آستانهٔ تغییر سطح سختی (۱-۵)
- سطح کوئیز پیش‌فرض
- سقف اندازهٔ مخزن کوئیز (maxQuizPoolSize)

**داده:**
- بکاپ کامل
- بازیابی از فایل

**درباره و راهنما**

---

### ۵.۷ پشتیبان‌گیری و بازیابی

انواع خروجی:
- کامل (همه داده‌ها)
- فقط واژگان
- فقط پیشرفت

بازیابی بدون خرابی ساختار، با مهاجرت Schema اگر نیاز باشد.

---

## ۶. سیستم تم (Theme System)

کل شکل ظاهری برنامه با تم تعریف می‌شود.

### ۶.۱ موارد تحت کنترل تم

1. **رنگ‌ها**
   - اصلی، ثانویه، پس‌زمینه، سطح، کارت
   - متن اصلی و کمرنگ
   - موفقیت (سبز)، خطا (قرمز)، هشدار
   - حالت روشن و تاریک جداگانه

2. **تایپوگرافی**
   - خانواده فونت
   - اندازه‌های مختلف
   - وزن‌های مختلف

3. **چیدمان و فاصله‌ها**
   - padding و margin
   - فاصلهٔ بین بخش‌ها
   - حاشیه صفحه

4. **شکل کامپوننت‌ها**
   - شعاع گوشه
   - ضخامت حاشیه
   - سایه

5. **آیکون‌ها**
   - سبک (خطی/پر)
   - اندازه
   - رنگ

6. **ناوبری و هدر**
   - Bottom Navigation
   - هدر
   - دکمه‌های اصلی

7. **حالت‌های تعاملی**
   - دکمهٔ فشرده
   - گزینهٔ انتخاب‌شده
   - صحیح (سبز) | غلط (قرمز)

### ۶.۲ تم‌های پیشنهادی

1. **گروک طلایی** — لوکس و گرم، گوشه‌های گرد، سایه ملایم
2. **کلاد سبز زیتونی** — تحریری و آرام، خطوط تمیز
3. **مدرن مینیمال** — آبی/خاکستری، گوشه‌های کمتر گرد
4. حداقل یک تم دیگر

حالت روشن و تاریک برای همه الزامی.

### ۶.۳ قانون مهم

**هیچ رنگ، شعاع گوشه، فاصله یا آیکون hard-coded نباشد. همه از توکن‌های تم خوانده شود.**

---

## ۷. تلفظ

تلفظ از Text-to-Speech سیستم‌عامل.
دکمهٔ پخش در صفحهٔ مرور و جزئیات کلمه.

---

## ۸. نصب، بروزرسانی و مهاجرت دیتابیس

- برنامه روی نسخه‌های قبلی قابل نصب و بروزرسانی
- داده‌ها پاک نمی‌شوند
- هر تغییر Schema مسیر مهاجرت امن دارد
- هنگام باز شدن، نسخهٔ Schema بررسی و مهاجرت اجرا شود

**مسیر مهاجرت:**
```
v1 → v2 → v3

FUNCTION migrateDatabase():
  current = DB.get(schemaVersion).version
  
  IF current < 2:
    // مهاجرت v1 → v2 (اگر لازم)
    // [توضیح مهاجرت به‌روز‌رسانی شود اگر نسخه‌های قدیم وجود داشتند]
  
  IF current < 3:
    // جداول جدید در v3
    CREATE TABLE IF NOT EXISTS Languages (...)
    CREATE TABLE IF NOT EXISTS LanguagePairs (...)
    CREATE TABLE IF NOT EXISTS ParserMetadata (...)
    CREATE TABLE IF NOT EXISTS VocabularyVariants (...)
    CREATE TABLE IF NOT EXISTS VocabularyRelations (...)
    CREATE TABLE IF NOT EXISTS ReviewQueue (...)
    CREATE TABLE IF NOT EXISTS Achievements (...)
    
    INSERT INTO Languages DEFAULT VALUES (seed data)
    INSERT INTO LanguagePairs DEFAULT VALUES (seed data)
    INSERT INTO Achievements DEFAULT VALUES (seed data)
  
  DB.update(schemaVersion, 3)
END
```

---

## ۹. Validation و Error Handling

### Validation Rules

**ورود کلمه:**
- نه empty string
- نه whitespace فقط
- حداکثر ۲۰۰ کاراکتر
- canonicalKey یکتا در زبان

**ترجمه:**
- حداقل یکی برای هر conceptId
- حداکثر ۱۰ ترجمهٔ همزمان
- یادداشت: اختیاری، حداکثر ۵۰۰ کاراکتر

**دسته‌بندی:**
- می‌تواند NULL باشد
- نه empty

**Timeline:**
- nextReviewAt باید آینده‌ای باشد (یا NULL)

### Edge Cases

**تکرار کلمه:**
```
IF EXISTS(Content WHERE canonicalKey="apple" AND languageCode="es"):
  ERROR: "این کلمه قبلاً اضافه شده"
  OFFER: "می‌خواهید ترجمهٔ دیگری اضافه کنید؟"
```

**هیچ کلمه برای مرور:**
```
IF eligible_concepts.isEmpty():
  MESSAGE: "هیچ کلمهٔ جدیدی برای امروز نیست ✓"
  OFFER: "مرور تصادفی" یا "افزودن کلمه"
```

**خطای دیتابیس:**
```
LOG(error_details)
SHOW: "خطای پایگاه داده. لطفاً دوباره شروع کنید."
```

**Distractor ناکافی:**
```
IF distractor_count < 3:
  fill_randomly_from_all_concepts()
```

**بارگذاری کوئیز طول می‌کشد:**
```
// v6.84: maxQuizPoolSize = 50
IF eligible_concepts.size > maxQuizPoolSize:
  eligible_concepts = eligible_concepts.LIMIT(maxQuizPoolSize)
```

---

## ۱۰. نمونهٔ داده

### نمونهٔ Concept
```
conceptId: "550e8400-e29b-41d4-a716-446655440000"
entryType: WORD
categoryId: "9c1d0f4a-b2c3-4d5e-8f9a-0b1c2d3e4f5a"
active: true
createdAt: "2026-10-01T10:00:00Z"
updatedAt: "2026-10-03T14:30:00Z"
```

### نمونهٔ Content
```
contentId: "660e8400-e29b-41d4-a716-446655440001"
conceptId: "550e8400-e29b-41d4-a716-446655440000"
languageCode: "es"
text: "manzana"
canonicalKey: "manzana"
translationIndex: 0
notes: "میوه‌ای قرمز و شیرین"
createdAt: "2026-10-01T10:00:00Z"
```

### نمونهٔ LearningState
```
learningStateId: "770e8400-e29b-41d4-a716-446655440002"
conceptId: "550e8400-e29b-41d4-a716-446655440000"
stage: WEEKLY
nextReviewAt: "2026-10-10T00:00:00Z"
lastReviewedAt: "2026-10-03T10:15:00Z"
successCount: 2
failureCount: 0
totalCorrect: 5
totalWrong: 1
monthlyWrongCount: 0
hasPathFailure: false
```

### نمونهٔ DifficultyState
```
difficultyStateId: "880e8400-e29b-41d4-a716-446655440003"
conceptId: "550e8400-e29b-41d4-a716-446655440000"
current: MEDIUM
consecutiveCorrect: 2
consecutiveWrong: 0
hasReachedVeryHard: false
```

### نمونهٔ VocabularyRelations
```
id: "990e8400-e29b-41d4-a716-446655440004"
sourceConceptId: "550e8400-e29b-41d4-a716-446655440000" (run)
targetConceptId: "aa0e8400-e29b-41d4-a716-446655440005" (sprint)
relationType: "SYNONYM"
```

### نمونهٔ VocabularyVariants
```
id: "bb0e8400-e29b-41d4-a716-446655440006"
conceptId: "550e8400-e29b-41d4-a716-446655440000" (run)
text: "running"
variantType: "GERUND"
```

---

## ۱۱. Performance Requirements

- `getEligibleConcepts()`: < ۱۰۰ms
- `selectDistractors()`: < ۵۰۰ms (v6.84 بهینه شده)
- `calculateStatistics()`: < ۲۰۰ms
- کوئیز بارگذاری: < ۲ ثانیه
- صفحهٔ اصلی: < ۵۰۰ms

**v6.84 بهینه‌سازی:**
- `maxQuizPoolSize = 50`: تعداد کلمات برای ساخت Distractor محدود
- تنها شمارنده‌های مورد نیاز ذخیره شوند
- از eager-loading خودداری کنید، lazy-load استفاده کنید

---

## ۱۲. قوانین کسب‌وکار که نباید جا بیفتند

1. کلمهٔ تکراری پذیرفته نمی‌شود.
2. یک مفهوم می‌تواند چند ترجمهٔ همزمان داشته باشد.
3. کلمه‌ای که امروز مرور شده، همان روز دوباره در مرور عادی فلش‌کارت نمی‌آید.
4. کلمه فقط وقتی در هفتگی/ماهانه ظاهر می‌شود که زمانش رسیده باشد.
5. پاسخ غلط همیشه کلمه را به روزانه برمی‌گرداند.
6. سطح سختی (vocabulary difficulty) مستقل از Stage است.
7. آستانهٔ تغییر سطح سختی قابل تنظیم است (۱-۵).
8. جهت کوئیز قابل برعکس شدن است.
9. ظاهر برنامه (رنگ + آیکون + چیدمان + فاصله + شکل) با تم کنترل می‌شود.
10. پشتیبان‌گیری و بازیابی کامل.
11. حداقل سه‌زبانه، قابل گسترش.
12. تلفظ از سیستم.
13. داده‌ها هنگام بروزرسانی حفظ می‌شوند.
14. مخزن مرور با `maxQuizPoolSize` محدود شود.
15. **در کوئیز: سبز/قرمز فوری + ۳ ثانیه صبر + خودکار سؤال بعدی** (v6.84).
16. **سطح کوئیز (EASY/MEDIUM/HARD) با VocabularyRelations برای Distractor محدود می‌شود.**

---

## ۱۳. جریان کار نمونهٔ کاربر

1. نصب/بروزرسانی (داده‌ها حفظ می‌شوند)
2. انتخاب تم و زبان
3. افزودن کلمات
4. شروع مرور روزانی
5. تنظیم فیلترها و سطح کوئیز (برای کوئیز)
6. مرور با فلش‌کارت یا کوئیز
7. در کوئیز: بازخورد فوری (سبز/قرمز) + ۳ ثانیه + خودکار
8. جهت برعکس (اختیاری)
9. نتایج و Streak
10. مشاهدهٔ پیشرفت
11. بکاپ

---

## ۱۴. نکات پیاده‌سازی

- این **Specification** است، نه کد.
- **همه** بخش‌های بالا باید پیاده‌سازی شوند.
- الگوریتم‌ها (بخش ۴) دقیقاً رعایت شوند.
- Schema (بخش ۳) کامل پوشش داده شود.
- ظاهر کاملاً تم‌محور باشد.
- مهاجرت دیتابیس الزامی است.
- کوئیز: سبز/قرمز + ۳ ثانیه + خودکار.
- سطح کوئیز دقیقاً با تعریف‌شده استفاده شود.
- هیچ‌یک از ۱۶ قانون حذف نشود.
- فیلدهای اضافی (favorite, example, grammaticalNote) حذف شده‌اند و نباید اضافه شوند.

---

**پایان مشخصات جامع و نهایی FlashLearn v6.84**

این سند برای ساخت کامل برنامه از صفر نوشته شده است.
تمام بخش‌های ضروری پوشش داده شده‌اند.

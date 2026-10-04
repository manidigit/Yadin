# الگوریتم‌ها و Unit Tests - FlashLearn v6.84

این سند شامل 6 الگوریتم اصلی برنامه با Unit Tests جزئی است.

---

## الگوریتم ۱: Stage Transition (انتقال مرحلهٔ یادگیری)

### توضیح
هر بار کاربر پاسخی می‌دهد (درست یا غلط)، مرحلهٔ یادگیری کلمه تغییر می‌کند.

**مراحل:**
- DAILY → WEEKLY (۱ درست متوالی)
- WEEKLY → MONTHLY (۳ درست متوالی)
- MONTHLY → LEARNED (۳ درست متوالی)
- هر غلط → برگشت به DAILY + reset شمارنده

### Pseudo-code

```kotlin
FUNCTION updateStageAndSchedule(
  conceptId: UUID,
  wasCorrect: Boolean,
  threshold: Int = 3
): Unit {
  
  learning = DB.get(LearningState, conceptId)
  
  IF learning.stage == LEARNED:
    RETURN  // کلماتِ یادگرفته ثابت می‌مانند
  
  IF wasCorrect == TRUE:
    learning.successCount += 1
    learning.failureCount = 0
    
    WHEN learning.stage:
      DAILY:
        IF learning.successCount >= 1:
          learning.stage = WEEKLY
          learning.nextReviewAt = tomorrow_midnight + 6_days  // 7 روز
      
      WEEKLY:
        IF learning.successCount >= threshold:
          learning.stage = MONTHLY
          learning.nextReviewAt = tomorrow_midnight + 29_days  // 30 روز
      
      MONTHLY:
        IF learning.successCount >= threshold:
          learning.stage = LEARNED
          learning.nextReviewAt = NULL
  
  ELSE:  // wasCorrect == FALSE
    learning.failureCount += 1
    learning.successCount = 0
    
    prev_stage = learning.stage
    learning.stage = DAILY
    learning.nextReviewAt = tomorrow_midnight
    
    IF prev_stage == MONTHLY:
      learning.monthlyWrongCount += 1
      learning.hasPathFailure = TRUE
  
  learning.lastReviewedAt = now
  learning.totalCorrect += (wasCorrect ? 1 : 0)
  learning.totalWrong += (wasCorrect ? 0 : 1)
  learning.updatedAt = now
  
  DB.save(LearningState, learning)
  
  INSERT INTO ReviewHistory {
    reviewHistoryId: UUID(),
    conceptId: conceptId,
    sessionId: current_session_id,
    reviewedAt: now,
    isCorrect: wasCorrect,
    reviewType: FLASHCARD
  }
}
```

### Unit Tests

#### Test 1.1: DAILY → WEEKLY (۱ درست)
```
Input:
  - stage: DAILY
  - successCount: 0
  - wasCorrect: TRUE

Expected Output:
  - stage: WEEKLY
  - successCount: 1
  - failureCount: 0
  - nextReviewAt: now + 7 days
  - totalCorrect: 1
```

#### Test 1.2: WEEKLY → MONTHLY (۳ درست متوالی)
```
Input:
  - stage: WEEKLY
  - successCount: 2
  - wasCorrect: TRUE

Expected Output:
  - stage: MONTHLY
  - successCount: 3
  - nextReviewAt: now + 30 days
```

#### Test 1.3: MONTHLY → LEARNED (۳ درست متوالی)
```
Input:
  - stage: MONTHLY
  - successCount: 2
  - wasCorrect: TRUE

Expected Output:
  - stage: LEARNED
  - successCount: 3
  - nextReviewAt: NULL
```

#### Test 1.4: DAILY + غلط → برگشت DAILY
```
Input:
  - stage: DAILY
  - successCount: 0
  - wasCorrect: FALSE

Expected Output:
  - stage: DAILY
  - successCount: 0
  - failureCount: 1
  - totalWrong: 1
```

#### Test 1.5: WEEKLY + غلط → برگشت DAILY
```
Input:
  - stage: WEEKLY
  - successCount: 2
  - wasCorrect: FALSE

Expected Output:
  - stage: DAILY
  - successCount: 0
  - failureCount: 1
```

#### Test 1.6: MONTHLY + غلط → برگشت DAILY + Flag
```
Input:
  - stage: MONTHLY
  - successCount: 2
  - wasCorrect: FALSE
  - monthlyWrongCount: 0
  - hasPathFailure: false

Expected Output:
  - stage: DAILY
  - successCount: 0
  - monthlyWrongCount: 1
  - hasPathFailure: true
```

#### Test 1.7: LEARNED ثابت می‌ماند
```
Input:
  - stage: LEARNED
  - wasCorrect: FALSE

Expected Output:
  - stage: LEARNED (تغییر نمی‌کند)
  - nextReviewAt: NULL (تغییر نمی‌کند)
```

#### Test 1.8: Reset شمارنده‌های درست/غلط
```
Input:
  - stage: WEEKLY
  - successCount: 2
  - failureCount: 0
  - wasCorrect: FALSE

Expected Output:
  - successCount: 0 (reset)
  - failureCount: 1
```

#### Test 1.9: ترکیب: درست → غلط → درست
```
Sequence:
  1. wasCorrect: TRUE  → successCount: 1, failureCount: 0
  2. wasCorrect: FALSE → successCount: 0, failureCount: 1
  3. wasCorrect: TRUE  → successCount: 1, failureCount: 0
```

---

## الگوریتم ۲: قانون "امروز مرور شده"

### توضیح
اگر کاربر کلمه‌ای را امروز در مود فلش‌کارت مرور کرده، همان روز دوباره نمی‌آید.
(برای کوئیز این محدودیت نیست)

### Pseudo-code

```kotlin
FUNCTION isReviewedToday(conceptId: UUID): Boolean {
  today_start = getCurrentDateAtMidnight()
  today_end = today_start + 24_hours
  
  count = DB.count(
    ReviewHistory,
    conceptId = conceptId,
    reviewType = FLASHCARD,
    reviewedAt >= today_start AND reviewedAt < today_end
  )
  
  RETURN count > 0
}

FUNCTION getEligibleConcepts(
  reviewType: Enum,  // DAILY, WEEKLY, MONTHLY, QUIZ
  categoryFilter: UUID? = null,
  difficultyFilter: Enum? = null,
  now: Instant = Instant.now()
): List<UUID> {
  
  IF reviewType == FLASHCARD:
    // برای فلش‌کارت، فقط کلمات روزانه، هفتگی، ماهانه
    // و محدود به "امروز مرور نشده"
    
    stage_filter = WHEN reviewType:
      DAILY -> DAILY
      WEEKLY -> WEEKLY
      MONTHLY -> MONTHLY
      else -> null
    
    candidates = DB.query(
      Concepts.active = true,
      LearningState.stage = stage_filter,
      LearningState.nextReviewAt <= now,
      NOT isReviewedToday(conceptId),  // ← کلیدی
      IF categoryFilter: Concepts.categoryId = categoryFilter,
      IF difficultyFilter: DifficultyState.current = difficultyFilter
    )
  
  ELSE IF reviewType == QUIZ:
    // برای کوئیز، "امروز مرور شده" فیلتر نمی‌شود
    candidates = DB.query(
      Concepts.active = true,
      LearningState.stage IN (DAILY, WEEKLY, MONTHLY),
      LearningState.nextReviewAt <= now,
      IF categoryFilter: Concepts.categoryId = categoryFilter,
      IF difficultyFilter: DifficultyState.current = difficultyFilter
    )
  
  ELSE IF reviewType == RANDOM:
    // RANDOM: همهٔ کلمات فعال
    candidates = DB.query(
      Concepts.active = true,
      LearningState.stage IN (DAILY, WEEKLY, MONTHLY),
      IF categoryFilter: Concepts.categoryId = categoryFilter,
      IF difficultyFilter: DifficultyState.current = difficultyFilter
    )
  
  // ترتیب
  candidates.ORDER BY LearningState.stage ASC, LearningState.lastReviewedAt ASC
  
  // محدود کردن
  IF candidates.size > maxQuizPoolSize:
    candidates = candidates.LIMIT(maxQuizPoolSize)
  
  RETURN candidates
}
```

### Unit Tests

#### Test 2.1: فلش‌کارت - امروز مرور شده فیلتر شود
```
Input:
  - reviewType: FLASHCARD
  - stage: DAILY
  - concept A: مرور شده امروز
  - concept B: مرور نشده امروز

Expected Output:
  - [concept B]  // concept A فیلتر شده
```

#### Test 2.2: کوئیز - امروز مرور شده فیلتر نشود
```
Input:
  - reviewType: QUIZ
  - stage: DAILY
  - concept A: مرور شده امروز
  - concept B: مرور نشده امروز

Expected Output:
  - [concept A, concept B]  // هردو شامل
```

#### Test 2.3: فقط nextReviewAt <= now
```
Input:
  - reviewType: FLASHCARD
  - concept A: nextReviewAt = now + 1 day (آینده)
  - concept B: nextReviewAt = now (آماده)

Expected Output:
  - [concept B]
```

#### Test 2.4: فیلتر دسته‌بندی
```
Input:
  - reviewType: FLASHCARD
  - categoryFilter: "Verbs"
  - concept A: category=Verbs, active=true, ready
  - concept B: category=Food, active=true, ready

Expected Output:
  - [concept A]
```

#### Test 2.5: فیلتر سطح سختی
```
Input:
  - reviewType: FLASHCARD
  - difficultyFilter: HARD
  - concept A: difficulty=HARD, ready
  - concept B: difficulty=EASY, ready

Expected Output:
  - [concept A]
```

#### Test 2.6: maxQuizPoolSize محدود کردن
```
Input:
  - reviewType: QUIZ
  - maxQuizPoolSize: 50
  - eligible_count: 200

Expected Output:
  - size: 50 (محدود شده)
```

---

## الگوریتم ۳: Difficulty Update (تحدیث سطح سختی)

### توضیح
سطح سختی کلمه (EASY/MEDIUM/HARD/VERY_HARD) بر اساس عملکرد تغییر می‌کند.
مستقل از stage یادگیری است.

### Pseudo-code

```kotlin
FUNCTION updateDifficulty(
  conceptId: UUID,
  wasCorrect: Boolean,
  threshold: Int = 3  // قابل تنظیم ۱-۵
): Unit {
  
  difficulty = DB.get(DifficultyState, conceptId)
  
  IF wasCorrect == TRUE:
    difficulty.consecutiveCorrect += 1
    difficulty.consecutiveWrong = 0
    
    IF difficulty.consecutiveCorrect >= threshold:
      // بالا رفتن در سختی
      WHEN difficulty.current:
        EASY:
          difficulty.current = MEDIUM
        MEDIUM:
          difficulty.current = HARD
        HARD:
          difficulty.current = VERY_HARD
          difficulty.hasReachedVeryHard = TRUE
        VERY_HARD:
          // ثابت می‌ماند
          pass
      
      difficulty.consecutiveCorrect = 0
  
  ELSE:  // wasCorrect == FALSE
    difficulty.consecutiveWrong += 1
    difficulty.consecutiveCorrect = 0
    
    IF difficulty.consecutiveWrong >= threshold:
      // پایین آمدن در سختی
      WHEN difficulty.current:
        VERY_HARD:
          difficulty.current = HARD
        HARD:
          difficulty.current = MEDIUM
        MEDIUM:
          difficulty.current = EASY
        EASY:
          // نمی‌تواند کمتر شود
          pass
      
      difficulty.consecutiveWrong = 0
  
  difficulty.updatedAt = now
  DB.save(DifficultyState, difficulty)
}
```

### Unit Tests

#### Test 3.1: EASY → MEDIUM (۳ درست متوالی)
```
Input:
  - current: EASY
  - consecutiveCorrect: 2
  - threshold: 3
  - wasCorrect: TRUE

Expected Output:
  - current: MEDIUM
  - consecutiveCorrect: 0 (reset)
```

#### Test 3.2: MEDIUM → HARD (۳ درست متوالی)
```
Input:
  - current: MEDIUM
  - consecutiveCorrect: 2
  - wasCorrect: TRUE

Expected Output:
  - current: HARD
  - consecutiveCorrect: 0
```

#### Test 3.3: HARD → VERY_HARD (۳ درست متوالی)
```
Input:
  - current: HARD
  - consecutiveCorrect: 2
  - hasReachedVeryHard: false
  - wasCorrect: TRUE

Expected Output:
  - current: VERY_HARD
  - hasReachedVeryHard: true
  - consecutiveCorrect: 0
```

#### Test 3.4: VERY_HARD ثابت (درست بیشتر)
```
Input:
  - current: VERY_HARD
  - wasCorrect: TRUE

Expected Output:
  - current: VERY_HARD (تغییر نمی‌کند)
```

#### Test 3.5: VERY_HARD → HARD (۳ غلط متوالی)
```
Input:
  - current: VERY_HARD
  - consecutiveWrong: 2
  - wasCorrect: FALSE

Expected Output:
  - current: HARD
  - consecutiveWrong: 0
```

#### Test 3.6: HARD → MEDIUM (۳ غلط متوالی)
```
Input:
  - current: HARD
  - consecutiveWrong: 2
  - wasCorrect: FALSE

Expected Output:
  - current: MEDIUM
  - consecutiveWrong: 0
```

#### Test 3.7: EASY نمی‌تواند کمتر شود
```
Input:
  - current: EASY
  - consecutiveWrong: 3
  - wasCorrect: FALSE

Expected Output:
  - current: EASY (تغییر نمی‌کند)
```

#### Test 3.8: Reset درست وقتی غلط زده
```
Input:
  - consecutiveCorrect: 2
  - wasCorrect: FALSE

Expected Output:
  - consecutiveCorrect: 0 (reset)
  - consecutiveWrong: 1
```

#### Test 3.9: Threshold قابل تنظیم
```
Input (threshold=1):
  - current: EASY
  - consecutiveCorrect: 0
  - wasCorrect: TRUE

Expected Output:
  - current: MEDIUM (تنها ۱ درست لازم)
  - consecutiveCorrect: 0
```

---

## الگوریتم ۴: Distractor Selection (انتخاب گزینه‌های غلط)

### توضیح
بر اساس سطح کوئیز (EASY/MEDIUM/HARD)، 3 گزینهٔ غلط برای کوئیز انتخاب می‌شود.

- **EASY:** 3 گزینهٔ کاملاً رندم
- **MEDIUM:** 1 نزدیک + 1 دسته + 1 رندم
- **HARD:** 3 گزینهٔ مرتبط (از VocabularyRelations)

### Pseudo-code

```kotlin
FUNCTION selectDistractors(
  correctConceptId: UUID,
  quizDifficulty: Enum,  // EASY, MEDIUM, HARD
  maxDistractors: Int = 3
): List<UUID> {
  
  IF quizDifficulty == EASY:
    // ۱. همهٔ غلط‌ها کاملاً رندم
    candidates = DB.query(
      Concepts,
      active = true,
      id NOT IN (correctConceptId)
    )
    
    candidates.SHUFFLE()
    RETURN candidates.LIMIT(maxDistractors)
  
  ELSE IF quizDifficulty == MEDIUM:
    // ۲. سه نوع متفاوت
    distractors = []
    
    // ۲.۱ یکی نزدیک (مترادف یا مرتبط)
    similar = DB.query(
      VocabularyRelations,
      sourceConceptId = correctConceptId,
      relationType IN (SYNONYM, RELATED)
    )
    
    IF similar.size > 0:
      d1 = RANDOM(similar).targetConceptId
      distractors.ADD(d1)
    ELSE:
      // Fallback: رندم
      random = DB.randomConcept(active=true, NOT correctConceptId)
      distractors.ADD(random.id)
    
    // ۲.۲ یکی از همان دسته‌بندی
    correctConcept = DB.get(Concepts, correctConceptId)
    IF correctConcept.categoryId NOT NULL:
      sameCategory = DB.query(
        Concepts,
        categoryId = correctConcept.categoryId,
        active = true,
        id NOT IN (correctConceptId, distractors)
      )
      
      IF sameCategory.size > 0:
        d2 = RANDOM(sameCategory).id
        distractors.ADD(d2)
      ELSE:
        // Fallback: رندم
        random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
        distractors.ADD(random.id)
    ELSE:
      // No category, fallback رندم
      random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
      distractors.ADD(random.id)
    
    // ۲.۳ یکی کاملاً رندم
    random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
    distractors.ADD(random.id)
    
    // اگر کمتر از ۳ پیدا شد، بقیه رندم
    WHILE distractors.size < maxDistractors:
      random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
      distractors.ADD(random.id)
    
    distractors.SHUFFLE()
    RETURN distractors.LIMIT(maxDistractors)
  
  ELSE IF quizDifficulty == HARD:
    // ۳. هر سه مرتبط (از VocabularyRelations)
    distractors = []
    
    relations = DB.query(
      VocabularyRelations,
      sourceConceptId = correctConceptId
      // NOT relationType = MORPHOLOGICAL (اختیاری)
    )
    
    IF relations.size >= maxDistractors:
      // انتخاب ۳ تا
      selected = RANDOM(relations, count=maxDistractors)
      FOR EACH rel IN selected:
        distractors.ADD(rel.targetConceptId)
    
    ELSE IF relations.size > 0:
      // تمام روابط + بقیه رندم
      FOR EACH rel IN relations:
        distractors.ADD(rel.targetConceptId)
      
      WHILE distractors.size < maxDistractors:
        random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
        distractors.ADD(random.id)
    
    ELSE:
      // هیچ رابطه نیست، رندم
      WHILE distractors.size < maxDistractors:
        random = DB.randomConcept(active=true, NOT IN (correctConceptId, distractors))
        distractors.ADD(random.id)
    
    distractors.SHUFFLE()
    RETURN distractors.LIMIT(maxDistractors)
}
```

### Unit Tests

#### Test 4.1: EASY - رندم کامل
```
Input:
  - quizDifficulty: EASY
  - correctConceptId: "apple"
  - Available: [car, house, mountain, tree, ...]

Expected Output:
  - 3 رندم دلخواه (نه "apple")
  
Verification:
  - نباید شامل "apple" باشد
  - نباید الگو یا منطق خاصی داشته باشد
```

#### Test 4.2: MEDIUM - نزدیک + دسته + رندم
```
Input:
  - quizDifficulty: MEDIUM
  - correctConceptId: "apple" (category: Food)
  - VocabularyRelations: apple ↔ orange (SYNONYM)
  - Category concepts: [banana, grape, orange, ...]

Expected Output:
  - Distractor 1: "orange" (نزدیک/synonym)
  - Distractor 2: "banana" (دسته Food)
  - Distractor 3: random (رندم)
```

#### Test 4.3: MEDIUM - Fallback اگر synonym نیست
```
Input:
  - quizDifficulty: MEDIUM
  - correctConceptId: "apple" (بدون synonym)

Expected Output:
  - Distractor 1: random (fallback)
  - Distractor 2: category
  - Distractor 3: random
```

#### Test 4.4: HARD - تمام مرتبط
```
Input:
  - quizDifficulty: HARD
  - correctConceptId: "run"
  - VocabularyRelations:
    - run ↔ sprint (SYNONYM)
    - run ↔ walk (RELATED)
    - run ↔ sport (RELATED_CATEGORY)

Expected Output:
  - [sprint, walk, sport]
```

#### Test 4.5: HARD - Fallback اگر رابطه کم
```
Input:
  - quizDifficulty: HARD
  - correctConceptId: "apple"
  - VocabularyRelations: apple ↔ fruit (۱ تا)

Expected Output:
  - [fruit, random1, random2]
```

#### Test 4.6: کمتر از ۳ relation در HARD
```
Input:
  - quizDifficulty: HARD
  - correctConceptId: "xyz"
  - VocabularyRelations: خالی

Expected Output:
  - [random1, random2, random3]
```

#### Test 4.7: بدون تکرار
```
Input:
  - correctConceptId: "apple"
  - هر distractor

Expected Output:
  - هیچ distractor = "apple"
  - هیچ distractor تکراری
```

---

## الگوریتم ۵: Statistics (محاسبهٔ آمار)

### توضیح
محاسبهٔ آمار کلی برنامه برای نمایش در صفحهٔ Progress.

### Pseudo-code

```kotlin
FUNCTION calculateStatistics(): Statistics {
  
  totalConcepts = DB.count(Concepts, active=true)
  
  learnedConcepts = DB.count(
    LearningState,
    stage = LEARNED,
    conceptId IN (active concepts)
  )
  
  stageDistribution = [
    DAILY: DB.count(LearningState, stage=DAILY),
    WEEKLY: DB.count(LearningState, stage=WEEKLY),
    MONTHLY: DB.count(LearningState, stage=MONTHLY),
    LEARNED: learnedConcepts
  ]
  
  difficultyDistribution = [
    EASY: DB.count(DifficultyState, current=EASY),
    MEDIUM: DB.count(DifficultyState, current=MEDIUM),
    HARD: DB.count(DifficultyState, current=HARD),
    VERY_HARD: DB.count(DifficultyState, current=VERY_HARD)
  ]
  
  totalCorrect = DB.sum(
    LearningState,
    totalCorrect
  )
  
  totalWrong = DB.sum(
    LearningState,
    totalWrong
  )
  
  accuracy = IF (totalCorrect + totalWrong > 0):
    totalCorrect / (totalCorrect + totalWrong)
  ELSE:
    0.0
  
  streak = DB.get(Streak)
  
  readyToday = DB.count(
    LearningState,
    nextReviewAt <= now,
    NOT isReviewedToday(conceptId)
  )
  
  RETURN Statistics(
    totalConcepts: totalConcepts,
    learnedConcepts: learnedConcepts,
    learnedPercentage: (learnedConcepts / totalConcepts * 100) if totalConcepts > 0 else 0,
    stageDistribution: stageDistribution,
    difficultyDistribution: difficultyDistribution,
    accuracy: accuracy,
    currentStreak: streak.currentStreak,
    bestStreak: streak.bestStreak,
    readyToday: readyToday
  )
}
```

### Unit Tests

#### Test 5.1: آمار پایه
```
Input:
  - totalConcepts: 100
  - learnedConcepts: 30
  - totalCorrect: 500
  - totalWrong: 100

Expected Output:
  - learnedPercentage: 30%
  - accuracy: 500 / (500+100) = 83.3%
```

#### Test 5.2: بدون مرور
```
Input:
  - totalConcepts: 50
  - totalCorrect: 0
  - totalWrong: 0

Expected Output:
  - accuracy: 0%
```

#### Test 5.3: توزیع مرحله
```
Input:
  - DAILY: 40
  - WEEKLY: 30
  - MONTHLY: 20
  - LEARNED: 10

Expected Output:
  - stageDistribution: {DAILY: 40, WEEKLY: 30, MONTHLY: 20, LEARNED: 10}
  - sum: 100
```

#### Test 5.4: توزیع سطح سختی
```
Input:
  - EASY: 30
  - MEDIUM: 40
  - HARD: 20
  - VERY_HARD: 10

Expected Output:
  - difficultyDistribution: {EASY: 30, MEDIUM: 40, HARD: 20, VERY_HARD: 10}
```

#### Test 5.5: امروز آماده
```
Input:
  - concept A: nextReviewAt <= now, NOT reviewed today
  - concept B: nextReviewAt > now
  - concept C: reviewed today

Expected Output:
  - readyToday: 1 (فقط A)
```

---

## الگوریتم ۶: Streak Update (به‌روزرسانی رگبار)

### توضیح
در پایان هر جلسهٔ مرور، رگبار به‌روزرسانی می‌شود.
اگر کاربر دیروز و امروز مرور کرده، رگبار +۱ می‌شود.

### Pseudo-code

```kotlin
FUNCTION updateStreak(): Unit {
  
  streak = DB.get(Streak)
  today = getCurrentDateLocal()  // YYYY-MM-DD
  yesterday = today - 1 day
  
  IF streak.lastActiveDate == today:
    // امروز قبلاً فعالیت شده
    RETURN  // تغییری نیست
  
  ELSE IF streak.lastActiveDate == yesterday:
    // دیروز فعالیت شده و امروز ادامه
    streak.currentStreak += 1
    
    IF streak.currentStreak > streak.bestStreak:
      streak.bestStreak = streak.currentStreak
  
  ELSE:
    // شکاف بیشتر از یک روز یا اولین بار
    streak.currentStreak = 1
    
    // bestStreak نمی‌تواند کمتر شود
  
  streak.lastActiveDate = today
  streak.updatedAt = now
  DB.save(Streak, streak)
}
```

### Unit Tests

#### Test 6.1: اولین فعالیت
```
Input:
  - lastActiveDate: null
  - currentStreak: 0

Expected Output:
  - currentStreak: 1
  - bestStreak: 1
  - lastActiveDate: today
```

#### Test 6.2: ادامهٔ رگبار (دیروز + امروز)
```
Input:
  - lastActiveDate: yesterday
  - currentStreak: 5
  - bestStreak: 5

Expected Output:
  - currentStreak: 6
  - bestStreak: 6
  - lastActiveDate: today
```

#### Test 6.3: شکست رگبار (شکاف)
```
Input:
  - lastActiveDate: 3 days ago
  - currentStreak: 10
  - bestStreak: 10

Expected Output:
  - currentStreak: 1 (reset)
  - bestStreak: 10 (ثابت)
  - lastActiveDate: today
```

#### Test 6.4: امروز دوبار فعالیت
```
Input (فراخوانیِ دوم امروز):
  - lastActiveDate: today
  - currentStreak: 5

Expected Output:
  - currentStreak: 5 (تغییر نمی‌کند)
```

#### Test 6.5: Record شکست
```
Input:
  - lastActiveDate: yesterday
  - currentStreak: 5
  - bestStreak: 3

Expected Output:
  - currentStreak: 6
  - bestStreak: 6 (آپدیت شد)
```

---

## خلاصهٔ تمام Unit Tests

| الگوریتم | تعداد Tests |
|----------|------------|
| Stage Transition | 9 |
| Eligible Concepts | 6 |
| Difficulty Update | 9 |
| Distractor Selection | 7 |
| Statistics | 5 |
| Streak Update | 5 |
| **کل** | **41 Test** |

---

## نکات برای Kotlin Implementation

### ۱. Database Transactions
```kotlin
DB.withTransaction {
  updateStageAndSchedule(conceptId, wasCorrect)
  updateDifficulty(conceptId, wasCorrect)
  updateStreak()
}
```

### ۲. Null Safety
```kotlin
val difficulty: DifficultyState? = DB.get(...)
difficulty?.let {
  // update
}
```

### ۳. Coroutines برای queries بزرگ
```kotlin
suspend fun calculateStatistics(): Statistics {
  val stats = withContext(Dispatchers.IO) {
    // database queries
  }
  return stats
}
```

### ۴. Logging برای Debugging
```kotlin
Log.d("Algorithm", "updateStage: $conceptId -> $oldStage to $newStage")
```

---

**پایان الگوریتم‌ها و Unit Tests**

تمام الگوریتم‌ها پیاده‌سازی شده‌اند و۴۱ Unit Test برای تست‌کردن کامل استفاده می‌شوند.

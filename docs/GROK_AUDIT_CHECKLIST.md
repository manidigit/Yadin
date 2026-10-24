# گزارش بررسی دقیق و چک‌لیست رفع باگ‌های استخراج‌شده (Grok Code Audit Verification)

این سند شامل بررسی فنی خط‌به‌خط گزارش تحلیل کد (Grok) در مقایسه با معماری و سند مشخصات پروژه **یادین (Yadin)** و فهرست گام‌به‌گام اصلاحات انجام‌شده است.

---

## چک‌لیست اولویت‌بندی‌شده باگ‌های تاییدشده و وضعیت رفع آن‌ها

### ۱. آمار و پیشرفت (`VocabularyRepository.getRetentionMetrics` و `LearningDao`) — **[تایید شد و رفع گردید ✅]**
- **مشکل ۱-الف (عدم فیلتر جهت در `totalActive`):**
  - در متد `getRetentionMetrics(direction)`، مقدار `totalActive` از `conceptDao.getActiveConceptCount()` بدون فیلتر `direction` خوانده می‌شد، در حالی که تمام شمارنده‌های مراحل (`learned`, `monthly`, `weekly`, `daily`, `practiced`) بر اساس `direction` فیلتر می‌شدند.
- **مشکل ۱-ب (محاسبه نادرست `daily` و فرمول وزن‌دار `weightedScore`):**
  - متغیر `daily` با `learningDao.getCountByStage(direction, Stage.DAILY)` محاسبه می‌شد که شامل **کلمات مرورنشده (`lastReviewedDay IS NULL`)** نیز هست (چون کلمات جدید در بدو ورود در مرحله `DAILY` با `lastReviewedDay = null` ثبت می‌شوند).
  - در نتیجه در `RetentionGaugeCard.kt`، ردیف «مرورهای جاری و روزانه» هم کلمات مرورنشده را نشان می‌داد و هم ردیف «کلمات جدید (هنوز مرور نشده)» دوباره آن‌ها را نمایش می‌داد (دوباره‌شماری).
  - همچنین فرمول امتیاز وزن‌دار به جای استفاده از تعداد واقعی کلمات تمرین‌شده در مرحله روزانه، از تفاضل شکننده `(practiced - learned - monthly - weekly)` استفاده می‌کرد.
- **اصلاح انجام‌شده:**
  - افزودن کوئری‌های دقیق `getActiveConceptCountForDirection(direction)` و `getPracticedCountByStage(direction, stage)` در `LearningDao.kt`.
  - اصلاح `getRetentionMetrics(direction)` در `VocabularyRepository.kt` به گونه‌ای که `practicedDaily` صرفاً کلمات تمرین‌شده (`lastReviewedDay IS NOT NULL`) در مرحله `DAILY` را بشمارد، `practiced = learned + monthly + weekly + practicedDaily` و `unreviewed = (totalActive - practiced).coerceAtLeast(0)` باشد، و فرمول وزن‌دار مستقیماً از `practicedDaily * 35.0` استفاده کند.

---

### ۲ و ۸. متن‌های دوزبانه خارج از فایل‌های `strings.xml` — **[تایید شد و رفع گردید ✅]**
- **مشکل ۲-الف (`Enums.kt`):**
  - توابع `QuizLevel.toPersianLabel()` و `QuizLevel.toEnglishLabel()` به صورت Hardcode تعریف شده و بلااستفاده بودند (چون `QuizLevel.toStringRes()` وجود دارد).
  - تابع `RelationType.toPersianLabel()` متن فارسی را Hardcode کرده بود و معادل `@StringRes fun RelationType.toStringRes(): Int` نداشت.
- **مشکل ۲-ب (کامپوننت‌های UI):**
  - `RetentionGaugeCard.kt`: عبارت `"${metrics.practicedWords.formatDigits()} از ${metrics.totalWords.formatDigits()} کلمه فعال"` به صورت Hardcode فارسی نوشته شده بود.
  - `StudyHabitsCard.kt`: عبارات `"مرور"` و `"دقت"` به صورت Hardcode فارسی نوشته شده بودند.
  - `UpcomingForecastCard.kt`: عبارات `"کلمه سررسید"` و `"مرتب است"` به صورت Hardcode فارسی نوشته شده بودند.
  - `ImportScreen.kt`: دکمه `Text("Paste")` به صورت Hardcode انگلیسی و الگوی هشدار `"• خط ..."` به صورت Hardcode فارسی نوشته شده بودند.
- **مشکل ۲-ج (`BackupRepository.kt` و `DomainModels.kt`):**
  - در `BackupRepository.kt`، هدرها و برچسب‌های خروجی اکسل (CSV) به صورت Hardcode فارسی نوشته شده بودند و با تغییر زبان برنامه به انگلیسی تغییر نمی‌کردند.
  - همچنین کلیدهای `retention_gauge_title` تا `category_mastery_fmt` در `values-en/strings.xml` جا افتاده بودند.
- **اصلاح انجام‌شده:**
  - انتقال تمامی رشته‌های فوق به هر سه فایل `values/strings.xml`، `values-fa/strings.xml` و `values-en/strings.xml`.
  - حذف توابع مرده `QuizLevel.toPersianLabel()` / `toEnglishLabel()` و افزودن `RelationType.toStringRes()` در `Enums.kt`.
  - جایگزینی تمامی موارد در `RetentionGaugeCard.kt`، `StudyHabitsCard.kt`، `UpcomingForecastCard.kt`، `ImportScreen.kt`، `DomainModels.kt` و `BackupRepository.kt` با منابع رشته‌ای دوزبانه.

---

### ۳. الگوریتم دشواری کلمات (`DifficultyCalculator` و رفتار گزینه `HARD`) — **[تایید شد و رفع گردید ✅]**
- **مشکل ۳-الف (حفظ پرچم `hasReachedVeryHard`):**
  - در `DifficultyCalculator.updateDifficulty`، اگر کلمه‌ای از ابتدا (مثلاً از فایل پشتیبان یا سید اولیه) دارای وضعیت `current == VERY_HARD` بود اما پرچم `hasReachedVeryHard` آن `false` بود، با اولین پاسخ صحیح به `HARD` تنزل می‌یافت بدون اینکه `hasReachedVeryHard = true` تثبیت شود.
- **مشکل ۳-ب (هماهنگی گزینه `HARD` / «سخت» بین الگوریتم مرور و سطح دشواری کلمه):**
  - در `Sm2Algorithm` و `FsrsAlgorithm`، گزینه `HARD` («سخت») به معنای **«یادآوری موفق اما همراه با دشواری»** (`isCorrect = true`) است، در حالی که گزینه `AGAIN` («فراموش کردم») به معنای **«عدم یادآوری / پاسخ غلط»** (`isCorrect = false`) است.
  - اما در `submitAnswer`، گزینه `HARD` دقیقاً مشابه `AGAIN` به عنوان پاسخ کاملاً غلط (`isCorrectForDifficulty = false`) به `DifficultyCalculator` داده می‌شد؛ در نتیجه زدن دکمه «سخت» روی یک کلمه `HARD` آن را مثل پاسخ غلط به `VERY_HARD` («خیلی سخت») می‌فرستاد.
- **اصلاح انجام‌شده:**
  - تثبیت `val baseReached = hasReachedVeryHard || (current == VocabularyDifficulty.VERY_HARD)` در `DifficultyCalculator.kt`.
  - پیاده‌سازی متد اختصاصی `DifficultyCalculator.updateDifficultyForHardRating(...)` و فراخوانی آن در `ReviewRepository.submitAnswer` هنگام انتخاب `Sm2Quality.HARD`:
    - کلمات `EASY` و `MEDIUM` در صورت رسیدن به آستانه به سمت `HARD` حرکت می‌کنند (`EASY -> MEDIUM -> HARD`) اما هرگز به `VERY_HARD` نمی‌روند.
    - کلمات `HARD` در همان سطح `HARD` تثبیت می‌شوند.
    - کلمات `VERY_HARD` به دلیل یادآوری موفق، در صورت رسیدن به آستانه یک پله به `HARD` تعدیل می‌شوند (با حفظ `hasReachedVeryHard = true`).

---

### ۴. موتور کوییز و انتخاب گزینه‌های انحرافی (`ReviewRepository.generateQuizQuestions`) — **[تایید شد و رفع گردید ✅]**
- **مشکل ۴-الف (محدودیت استخراج تصادفی ۵۰۰تایی و برش ۴۰تایی پیش از امتیازدهی):**
  - در `generateQuizQuestions`، ابتدا فقط ۵۰۰ رکورد تصادفی از جدول `contents` گرفته می‌شد و سپس قبل از اجرای `calculateConfusability`، استخر کاندیدها به حداکثر **۴۰ کاندید تصادفی** (`take(40)`) محدود می‌شد. در نتیجه در سطح `PROFESSIONAL` گزینه‌های هم‌دسته و واقعاً چالش‌برانگیز ممکن بود وارد مرحله امتیازدهی نشوند.
- **مشکل ۴-ب (`distinctBy { it.semantic.compact }` پیش از امتیازدهی):**
  - حذف کاندیدها با `distinctBy { it.semantic.compact }` قبل از اولویت‌دهی به دسته‌بندی‌های نشست می‌توانست کاندید هم‌دسته را به نفع کاندید غیردسته حذف کند.
- **اصلاح انجام‌شده:**
  - افزودن کوئری `getContentsByCategories(languageCode, categoryIds, limit = 600)` در `ConceptDao.kt` و ترکیب آن با نمونه تصادفی ۸۰۰تایی و کارت‌های نشست در `ReviewRepository.generateQuizQuestions`.
  - مرتب‌سازی اولیه کاندیدها بر اساس تعلق به دسته‌بندی‌های نشست پیش از `distinctBy { it.semantic.compact }`.
  - افزایش سقف کاندیدهای امتیازدهی از ۴۰ به ۲۴۰ کاندید هدفمند (شامل تا ۱۰۰ کاندید هم‌دسته و هم‌نوع) تا سطح `PROFESSIONAL` همواره نزدیک‌ترین گزینه‌های انحرافی را انتخاب کند.

---

### ۵. الگوریتم SuperMemo 2 (`Sm2Algorithm.kt`) — **[بررسی و اصلاح شد ✅]**
- **بررسی فنی و اصلاح انجام‌شده:**
  - در فرمول استاندارد SM-2 (Wozniak 1990)، در مرور اول (`reps == 0`) فاصله $I(1) = 1$ روز و در مرور دوم (`reps == 1`) فاصله $I(2) = 6$ روز است (مطابق با تست‌های استاندارد `Sm2AlgorithmTest`).
  - تاریخ ثابت `"2026-01-01"` در `Sm2Algorithm.previewIntervals` با `ClockAndDayMath.todayDayString()` جایگزین شد و متد `Sm2Algorithm.resolveEffectiveState` برای حفظ پیوستگی وضعیت هنگام تغییر الگوریتم اضافه گردید.

---

### ۶. الگوریتم FSRS (`FsrsAlgorithm.kt` و مهاجرت بین الگوریتم‌ها) — **[تایید شد و رفع گردید ✅]**
- **مشکل ۶-الف (شرط تکراری در خط ۲۱۷):**
  - عبارت `max(if (isFirstReview) 2 else 2, computed)` اصلاح شد.
- **مشکل ۶-ب (عدم تطابق `previewIntervals` با `calculateNextReview`):**
  - منطق یکنواختی فواصل (`again <= hard <= good < easy`) مستقیماً به درون `FsrsAlgorithm.calculateNextReview` منتقل شد تا اعداد پیش‌نمایش روی دکمه‌های پشت فلش‌کارت (`previewIntervals`) با فاصله واقعی ثبت‌شده پس از انتخاب دکمه ۱۰۰٪ یکسان باشند.
- **مشکل ۶-ج (تغییر الگوریتم بین YADIN / SM2 / FSRS برای کارت‌های دارای سابقه مرور):**
  - متدهای `FsrsAlgorithm.resolveEffectiveState(...)` و `Sm2Algorithm.resolveEffectiveState(...)` پیاده‌سازی و در `ReviewRepository.submitAnswer` و `FlashcardScreen.kt` فراخوانی شدند تا در صورت تغییر الگوریتم توسط کاربر، کارت‌هایی که قبلاً مرور شده‌اند (`lastReviewedDay != null`) پایداری (`Stability`) و فاصله متناسب با مرحله یا سابقه قبلی خود را حفظ کنند.

---

### ۷. الگوریتم لایتنر یادین (`LearningTransition`) — **[طراحی استاندارد طبق سند مشخصات]**
- طبق بخش ۶.۲ سند مشخصات یادین، الگوریتم کلاسیک یادین یک جعبه لایتنر ۴ مرحله‌ای قطعی (`DAILY -> 7d WEEKLY -> 30d MONTHLY -> LEARNED`) با ۲ دکمه («فراموش کردم» / «بلد بودم») است و دکمه‌های ۴گانه (`AGAIN`, `HARD`, `GOOD`, `EASY`) مختص حالت‌های `SM-2` و `FSRS` هستند.

---

### ۹. کوئری `getMasteredHardWordsCount` در `LearningDao` — **[تایید شد و رفع گردید ✅]**
- **مشکل:**
  - شرط قبلی `(ds.hasReachedVeryHard = 1 OR ds.current = 'VERY_HARD' OR ds.current = 'HARD') AND (ds.current = 'EASY' OR ds.current = 'MEDIUM' OR ls.stage = 'LEARNED')` باعث می‌شد کلمه‌ای که در مرحله `LEARNED` است اما سطح دشواری فعلی آن هنوز `HARD` یا `VERY_HARD` است نیز به عنوان «واژه سختِ تسلط‌یافته» شمارش شود.
- **اصلاح انجام‌شده:**
  - کوئری‌های `getMasteredHardWordsCount` در `LearningDao.kt` به `ds.hasReachedVeryHard = 1 AND (ds.current = 'EASY' OR ds.current = 'MEDIUM')` اصلاح شدند تا صرفاً واژگانی که قبلاً به سطح `VERY_HARD` رسیده‌اند و اکنون به سطح `EASY` یا `MEDIUM` مسلط شده‌اند شمارش شوند.

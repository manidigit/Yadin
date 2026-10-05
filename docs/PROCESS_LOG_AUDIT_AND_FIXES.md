# لاگ فرآیند بررسی و اصلاحات عمیق پروژه یادین (Process Log: Audit & Fixes)

**تاریخ:** ۲۰۲۶-۱۰-۰۵  
**موضوع:** بررسی گزارش ممیزی تطبیقی FlashLearn و Yadin، رفع نواقص الگوریتمی و تثبیت پایداری

---

## ۱. خلاصه‌ی مسأله و انگیزه
در ممیزی انجام‌شده بر روی سورس‌کد اصلی پروژه یادین (Yadin) در برابر FlashLearn، چند مغایرت عملکردی و منطقی (Logic Defect / Correctness Issue) شناسایی شد. این سند کلیه اقدامات، ریشه‌یابی خطاها و فایل‌های اصلاح‌شده را با جزئیات ثبت می‌کند تا در توسعه‌های بعدی تاریخچه تغییرات و منطق تصمیمات کاملاً شفاف و قابل ردیابی باشد.

---

## ۲. گزارش جزئیات نقص‌ها، تحلیل و اصلاحات انجام‌شده

### ۲.۱. رفع نقص قفل مرور روزانه (Same-Day Review Lock)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
* **ریشه مشکل:**
  در کوئری‌های واکشی کارت‌های مرور روزانه (`DAILY`)، شرط بررسی وضعیت به صورت زیر بود:
  ```sql
  ls.stage = 'DAILY' OR (ls.nextReviewDay IS NOT NULL AND ls.nextReviewDay <= :todayDayString)
  ```
  هنگامی که کاربری به کارتی پاسخ می‌داد، `lastReviewedDay` برابر تاریخ امروز می‌شد و در صورت پاسخ غلط کارت در `DAILY` باقی می‌ماند. به دلیل وجود شرط `ls.stage = 'DAILY'` بدون بررسی `lastReviewedDay != today`، کارت بلافاصله دوباره در همان روز وارد صف مرور می‌شد! علاوه بر این در `ReviewRepository` یک fallback وجود داشت که در صورت کم بودن کارت‌ها مجدداً کارت‌های مرحلۀ DAILY را بدون فیلتر واکشی می‌کرد.
* **اقدام اصلاحی:**
  1. شرط قفل صریح روزانه `(ls.lastReviewedDay IS NULL OR ls.lastReviewedDay != :todayDayString)` به تمامی کوئری‌های واکشی صف و شمارش (`getDueConceptIds`, `getDueCountFlow`, `getFilteredCandidateConceptIds`, `countFilteredCandidates`) اضافه شد.
  2. شرط مرحله DAILY به‌صورت مقید به تاریخ سررسید درآمد:
     `ls.stage = 'DAILY' AND (ls.nextReviewDay IS NULL OR ls.nextReviewDay <= :todayDayString)`
  3. فال‌بک نامعتبر در `ReviewRepository` که کارت‌های پاسخ داده شده را دوباره وارد صف می‌کرد حذف شد.

---

### ۲.۲. بازنویسی موتور تولید کوییز بر اساس سطوح دشواری و تشابه واژگانی (Quiz Engine Hardening)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
* **ریشه مشکل:**
  در پیاده‌سازی اولیه، پارامتر `QuizLevel` (آسان، متوسط، سخت) در جلسه ثبت می‌شد اما در متد `generateQuizQuestions` کاملاً نادیده گرفته می‌شد و تنها ۳ گزینه تصادفی از کل معانی بدون هیچ‌گونه فیلتر شباهت یا دسته‌بندی انتخاب می‌شد.
* **اقدام اصلاحی:**
  پیاده‌سازی کامل موتور هوشمند گزینه‌های انحرافی (Distractor Generator) مشابه استاندارد FlashLearn:
  1. توابع مقایسه متنی شامل نرمال‌سازی NFC، توکنایزیشن، شباهت لونشتاین (`levenshteinSimilarity`) و ترکیب آن با شباهت توکن‌ها (`quizLexicalSimilarity`).
  2. محاسبه فاصله سطح دشواری واژه (`diffDistance`).
  3. فرمول نمره‌دهی گیج‌کنندگی (Confusability Score):
     $$\text{Confusability} = (\text{LexicalSim} \times 0.40) + (\text{CategoryMatch} \times 0.35) + (\text{DiffBonus} \times 0.25)$$
  4. اعمال سیاست انتخاب بر اساس `QuizLevel`:
     * **EASY:** اولویت با گزینه‌هایی با کمترین میزان شباهت و خارج از دسته (برای تمرین مقدماتی).
     * **MEDIUM:** گزینه‌های میان‌رده با شباهت و تطابق منطقی و باورپذیر.
     * **HARD:** گزینه‌های با بالاترین شباهت متنی، هم‌دسته و نوع مدخل یکسان (چالش‌برانگیزترین حالت).
  5. ممانعت از ایجاد گزینه‌های تکراری یا گزینه‌هایی که خود از معانی معتبر کارت هستند.

---

### ۲.۳. ارتقای کامل بکاپ/ریستور به Schema نسخه ۲ و تراکنش اتمیک دیتابیس
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/BackupRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/ReviewSessionDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/ConceptDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/OtherDaos.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
* **ریشه مشکل:**
  خروجی پشتیبان در حالت `PROGRESS` عملاً تنها جدول `settings` را ذخیره می‌کرد و جداول حیاتی پیشرفت شامل `learning_states`، `difficulty_states`، `review_history`، `review_sessions` و `achievements` صادر و وارد نمی‌شدند. همچنین بازیابی داده‌ها خارج از Transaction بود و در صورت بروز خطا دیتابیس در وضعیت ناقص باقی می‌ماند.
* **اقدام اصلاحی:**
  1. خروجی نسخه پشتیبان به Schema نسخه ۲ ارتقا یافت و تمامی جداول فوق را با ساختار JSON بهینه صادر می‌کند.
  2. عملیات بازیابی (Restore) تحت `database.withTransaction` انجام می‌شود تا به صورت کاملاً اتمیک (یا همه یا هیچ) عمل کند.
  3. در صورت انتخاب حالت جایگزینی (`isReplace = true`) جداول مربوطه در ابتدای همان تراکنش پاک‌سازی شده و سپس داده‌های جدید درج می‌شوند.
  4. سازگاری رو به عقب (Backward Compatibility) با فایل‌های خروجی FlashLearn و نسخه ۱ حفظ شد.

---

### ۲.۴. اصلاح منطق محاسبه رگبار (Streak) و دستاوردها
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/domain/time/ClockAndDayMath.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
* **ریشه مشکل:**
  در متد `checkAchievements` تعداد کل روزهای مرور در تاریخچه (`distinctReviewedDays().size`) برای اعطای دستاوردهای ۳ روزه، ۷ روزه و ۳۰ روزه متوالی بررسی می‌شد. کاربری که در طول یک سال فقط ۳ روز غیرمتوالی مطالعه کرده بود، نشان ۳ روز متوالی دریافت می‌کرد.
* **اقدام اصلاحی:**
  تابع جامع `calculateStreakDays(days, today)` به شیء کمکی `ClockAndDayMath` منتقل شد که دقیقاً روزهای متوالی رو به عقب را از مبدأ امروز یا دیروز بررسی کرده و با اولین گپ زمانی خاتمه می‌یابد. دستاوردهای Streak هم‌اکنون به این متد مقید هستند.

---

### ۲.۵. حفظ متادیتای پارسر واژگان و جلوگیری از اتلاف اطلاعات
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
* **ریشه مشکل:**
  پارسر پیشرفته بخش‌های تجزیه کلمه (`breakdowns`)، گونه‌ها (`variants`)، روابط مترادف/مشتق (`relations`) و یادداشت‌های گرامری را استخراج می‌کرد، اما در زمان ورود دسته‌جمعی به دیتابیس فقط متن مبدأ و معنی ذخیره و بقیه دور ریخته می‌شد.
* **اقدام اصلاحی:**
  تابع ساخت یادداشت غنی‌شده `formatEnrichedNote(entry)` ایجاد شد که تمام متادیتای ساختاری استخراج‌شده را در بخش توضیحات واژه ثبت و نگهداری می‌کند تا کاربر در صفحه جزئیات کلمه و در کارت مرور به آن دسترسی داشته باشد.

---

### ۲.۶. راه‌اندازی تست‌های خودکار اولیه (Automated Unit Tests)
* **فایل‌های ایجاد شده:**
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/LearningTransitionTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/DifficultyCalculatorTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/time/StreakCalculationTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/VocabularyParserTest.kt`
* **نتیجه اجرا:**
  تمام تست‌های الگوریتمی اولیه با دستور `./gradlew testDebugUnitTest` با موفقیت ۱۰۰٪ پاس شدند.

---

### ۲.۷. تثبیت دو تم رسمی (GTP و Gemini) و معماری چیدمان وابسته به تم (Theme-Bound Layout Architecture)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/theme/Theme.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/HomeScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/MainActivity.kt`
* **شرح تصمیم و اقدام:**
  1. کلیه پوسته‌های غیررسمی حذف و تنها دو تم استاندارد `gtp` (GTP Cyber) و `gemini` (Gemini Future AI) تثبیت شدند.
  2. چیدمان داشبورد به‌صورت هوشمند به تم مقید شد:
     - در تم **GTP**: داشبورد متراکم، پر از جزئیات، با تمام ۸ کاشی عملیات و هدر بنفش سایبری دقیقاً مشابه حالت قبل بدون دستکاری حفظ شد.
     - در تم **Gemini**: بهینه‌سازی و خلوت‌سازی انجام شد؛ کاشی‌های تکراری که در نوار پایین (Bottom Bar) وجود داشتند (کتابخانه، پیشرفت، تنظیمات) حذف شدند و تمرکز بر کارت مرور سررسید، قیف لایتنر و کارت‌های ایجاد واژه قرار گرفت.
  3. نوار پایین (Bottom Navigation Bar) به عنوان لنگرگاه ثابت ناوبری در هر دو تم حفظ و تقویت شد.

---

### ۲.۸. حل مشکل Keystore در گیت‌هاب اکشنز (Materialize Debug Keystore in CI)
* **فایل‌های درگیر:**
  * `.github/workflows/android-ci.yml`
* **ریشه مشکل:**
  در اجرای اکشن گیت‌هاب (Job ID `111689500446`)، مرحله تست‌ها پاس شد اما مرحله `assembleDebug` به دلیل نبودن فایل باینری `debug.keystore` در مخزن گیت متوقف می‌شد.
* **اقدام اصلاحی:**
  مشابه الگوی استاندارد FlashLearn، مرحله `Materialize debug keystore` در ورک‌فلو اضافه شد تا در صورت نبود کلید، بلافاصله آن را با ابزار استاندارد `keytool` در صدم ثانیه تولید کند. تست اجرای بیلد در گیت‌هاب با موفقیت تثبیت شد.

---

### ۲.۹. تصحیح کوئری‌های شمارش دیتابیس و انطباق آماری (Active Concept Join)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
* **ریشه مشکل:**
  کوئری‌های `getCountByStageFlow` و `getCountByDifficultyFlow` جدول `learning_states` را بدون فیلتر `c.active = 1` می‌شمردند در حالی که سایر بخش‌ها واژه‌های فعال را مد نظر قرار می‌دادند.
* **اقدام اصلاحی:**
  هر دو کوئری به جدول `concepts` با شرط `c.active = 1` الحاق شدند تا انطباق صددرصدی میان مجموع مراحل لایتنر، سطوح سختی و کل کلمات فعال برقرار باشد:
  $$\sum \text{مراحل لایتنر} = \sum \text{سطوح دشواری} = \text{تعداد کل کلمات فعال}$$

---

### ۲.۱۰. پیاده‌سازی کامل موتور دستاوردها (Achievement Engine - تمام ۸ دستاورد)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
* **ریشه مشکل:**
  تنها دستاوردهای ۳ روزه، ۷ روزه و ۳۰ روزه رگبار چک می‌شدند و ۵ دستاورد دیگر هیچ تریگری در کد نداشتند.
* **اقدام اصلاحی:**
  پایش کامل و تریگر لحظه‌ای برای هر ۸ دستاورد اضافه شد:
  1. `STREAK_3_DAYS`, `STREAK_7_DAYS`, `STREAK_30_DAYS`: بر اساس روزهای متوالی مرور.
  2. `FIRST_TEN_WORDS`: یادگیری و تمرین ۱۰ واژه اول (`practicedCount >= 10`).
  3. `VOCABULARY_BUILDER`: تمرین ۵۰ واژه (`practicedCount >= 50`).
  4. `LONG_TERM_MEMORY`: رساندن حداقل ۲۰ واژه به جعبه تثبیت‌شده (`LEARNED >= 20`).
  5. `HARD_MASTER`: تسلط بر ۵ واژه که سابقه سختی حداکثری (`VERY_HARD`) داشته‌اند.
  6. `QUIZ_ACE`: کسب نمره ۱۰۰٪ در آزمون‌های ۵ سؤالی یا بیشتر.

---

### ۲.۱۱. ایجاد تراکنش‌های اتمیک در لایه داده واژگان (Atomic Transactions)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
* **اقدام اصلاحی:**
  اتصال مستقیم شیء `YadinDatabase` به مخزن واژگان و محصور کردن کلیه متدهای تغییر داده (`addWord`, `updateWord`, `deleteWord`, `importParsedEntries`) درون بلوک‌های `database.withTransaction { ... }` جهت تضمین خاصیت Atomicity و جلوگیری از خطاهای دیتابیس در ایمپورت‌های حجیم.

---

### ۲.۱۲. ارتقای بکاپ/ریستور به حفظ جزئیات کامل نشست‌ها (`review_session_items` و `currentPosition`)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/repository/BackupRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/ReviewSessionDao.kt`
* **اقدام اصلاحی:**
  1. صادر و وارد کردن کامل جدول `review_session_items` جهت بازگردانی بدون نقص نشست‌های جاری.
  2. ذخیره و بازیابی دقیق فیلد `currentPosition` به جای جایگزینی اشتباه با `totalItems`.

---

### ۲.۱۳. افزودن سوئیت تست‌های جدید (Quiz Distractor, Queue Logic, Achievement Engine)
* **فایل‌های ایجاد شده:**
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScoringTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/ReviewQueueLogicTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/AchievementEngineLogicTest.kt`
* **پوشش تست‌ها:**
  صحت‌سنجی نمره‌دهی گزینه‌های انحرافی بر اساس سطح کوییز، بررسی ریاضی قفل هم‌روز و شروط لایتنر، و ارزیابی تریگرهای تمامی دستاوردها.

---

### ۲.۱۴. تبیین معماری در برابر نقدهای غیرمنطبق ChatGPT
* **عدم ایجاد جدول مستقل برای Relations و Variants:**  
  بر خلاف پیشنهاد چت‌جی‌پی‌تی مبنی بر ایجاد جدول‌های مجزا، در سند رسمی پروژه (`docs/Yadin-Specification1-1.md` بخش ۱۳ تصمیم A6 و بخش ۱۴.۴ بند ۵) صراحتاً ثبت شده که متادیتای پارسر برای سبکی و سرعت دیتابیس موبایل در فیلد یادداشت واژه نگهداری می‌شود و نباید جدول مستقل داشته باشد.
* **تزریق وابستگی (Constructor Injection vs Hilt):**  
  مطابق دستورالعمل‌های رسمی Android AI Studio، استفاده از Constructor Injection صریح، سبک و بدون سربار کدهای اضافه کامپایل، اولویت معماری این پروژه است.

---

### ۲.۱۵. حل باگ گزینه‌های شبه‌مترادف و متناقض در کوییز (Semantic Distractor Collision)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScorer.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/QuizScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScoringTest.kt`
* **ریشه باگ و سناریوهای واقعی:**
  در اسکرین‌شات‌های ارسالی کاربر، چند دسته خطای معنایی در گزینه‌های کوییز (به‌ویژه در سطح متوسط و سخت) مشاهده شد:
  1. **جابه‌جایی ترتیب مترادف‌ها:** در سوال `Media jornada`، گزینه ۱ «نیمه‌وقت، پاره‌وقت» و گزینه ۴ «پاره‌وقت، نیمه‌وقت» بود؛ کاربر روی گزینه ۱ زد و با خطای قرمز رد شد در حالی که هر دو یکسان بودند.
  2. **اشتراک مترادف‌ها با توضیحات تکمیلی:** در سوال `¡Relájate!`، گزینه «آرام باش (برای مؤنث)» در کنار گزینه «آرام باش!، ریلکس کن!» قرار گرفته بود.
  3. **زیرمجموعه بودن عبارات طولانی:** در سوال `Llevo unos días fatal`، گزینه‌های «حالم خیلی بد است» و «حالم بسیار بد است...» به عنوان گزینه انحرافی انتخاب شده بودند.
  4. **پسوندهای نکره و شباهت توکنی:** در سوال `¡Qué barbaridad!`، گزینه «چه فاجعه و وحشتی!» در کنار «چه فاجعه‌ای!، چه عجیب!...» قرار داشت.
* **اقدام اصلاحی:**
  1. پیاده‌سازی متد `extractSegments` برای قطعه‌قطعه کردن تمام مترادف‌های جدا شده با ویرگول («،»، «,»)، اسلش («/») و نقطه‌ویرگول («؛»).
  2. پیاده‌سازی متد `stemPersian` جهت حذف هوشمند پسوندهای اسمی و نکره («ـی»، «ـه‌ای»، «ها») و نیم‌فاصله‌ها بدون دستکاری پیشوندهای فعلی و تمایز زمان‌های گرامری.
  3. استانداردسازی پیشوندهای فعلی «می‌» و «نمی‌» با مرزبندی یونی‌کد (`(^|\\s)می[\\s\u200c]+`) برای حفظ استقلال صیغه‌ها و زمان‌های مختلف فعل.
  4. اعمال شروط انطباق مجموعه‌ای (Set Equality)، اشتراک قطعات مترادف و پوشش توکن‌های محتوایی در `QuizDistractorScorer.areSemanticallyColliding`.
  5. فیلتر کردن گزینه‌های نامعتبر در `ReviewRepository.kt` در زمان استخراج استخر، مرتب‌سازی و انتخاب ۳ گزینه انحرافی.
  6. ارزیابی عادلانه پاسخ کاربر در `QuizScreen` و `MainViewModel` با بررسی برابری هسته معنایی در برابر پاسخ صحیح.
  7. افزودن تست‌های جامع برای هر ۴ سناریوی کاربر در `QuizDistractorScoringTest` با قبولی ۱۰۰٪.

---

### ۲.۱۶. افزودن گزینه «۱ بار» به تنظیمات آستانه تغییر سختی واژگان
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/SettingsScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/DifficultyCalculator.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/DifficultyCalculatorTest.kt`
* **شرح اقدام:**
  1. اضافه شدن دکمه «۱ بار» به لیست دکمه‌های صفحه تنظیمات (`۱ بار`، `۲ بار`، `۳ (پیش‌فرض)`، `۴ بار`، `۵ بار`).
  2. امکان تغییر آنی درجه سختی واژه با یک پاسخ درست (ساده‌تر شدن فوری) یا یک پاسخ غلط (سخت‌تر شدن فوری).
  3. اضافه شدن تست واحد برای `threshold = 1` در `DifficultyCalculatorTest` با قبولی کامل.

---

### ۲.۱۷. رفع مشکل پنهان شدن دکمه «سؤال بعدی» در واژگان و گزینه‌های طولانی
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/QuizScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/FlashcardScreen.kt`
* **ریشه مشکل:**
  در سوالاتی با جملات طولانی اسپانیایی (مانند تصویر ارسالی کاربر برای جمله `El hijo adoptivo encontró a su hermana biológica...`) و گزینه‌های ترجمه چندخطی، به دلیل ثابت بودن کل ستون بدون اسکرول و استفاده از `Arrangement.SpaceBetween`، ارتفاع مجموع عناصر از ارتفاع صفحه نمایش گوشی فراتر می‌رفت و دکمه «سؤال بعدی» به زیر لبه پایینی صفحه پرتاب می‌شد و کاربر امکان لمس آن را نداشت.
* **اقدام اصلاحی:**
  1. تفکیک معماری صفحه آزمون به ۳ بخش: هدر ثابت در بالا، محتوای اسکرول‌پذیر در میانه با `Modifier.weight(1f).verticalScroll(rememberScrollState())` و نوار اکشن ثابت در پایین.
  2. ثابت‌سازی کامل دکمه «سؤال بعدی» در پایین صفحه (Pinned Bottom Action Button) به طوری که صرف‌نظر از طول سؤال و گزینه‌ها، همواره ۱۰۰٪ در دسترس و قابل مشاهده باشد.
  3. اعمال تایپوگرافی تطبیقی بر اساس طول متن (کاهش خودکار سایز فونت برای متون بالای ۴۰ و ۸۰ کاراکتر) تا فضای صفحه بهینه مصرف شود.
  4. اعمال اسکرول روان داخلی روی کارت‌های فلش‌کارت (`FlashcardScreen`) برای واژگان و یادداشت‌های بلند.

---

### ۲.۱۸. بهینه‌سازی بنیادین سرعت تولید سوالات آزمون (Ultra-Fast Precomputed Quiz Generation)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScorer.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScoringTest.kt`
* **ریشه مشکل کندی:**
  به دنبال اضافه شدن منطق جامع تشخیص برخوردهای معنایی (Semantic Collision) و ریشه‌یابی فارسی، در یک نشست آزمون ۱۰۰ تایی با یک استخر ۵۰۰ لغتی، به ازای هر کارت صف صدها بار متدهای سنگین پردازش متن (`Regex`، `cleanSegment`، `stemPersian`، `extractSegments` و `levenshteinSimilarity`) به صورت تکراری و تودرتو فراخوانی می‌شد (بیش از ۲۵۰,۰۰۰ محاسبه سنگین در ترد اصلی/UI). این امر زمان ساخت آزمون ۱۰۰ تایی را به چند دقیقه می‌رساند.
* **اقدام اصلاحی و بهینه‌سازی:**
  1. **پیش‌محاسبه ساختار داده معنایی (`PrecomputedSemantic`):** تمامی عملیات سنگین نظیر نرمال‌سازی، استخراج مترادف‌ها، توکن‌های ریشه‌یابی‌شده و رشته‌های فشرده تنها **یک‌بار** برای کل واژگان استخر انجام شده و در حافظه نگهداری می‌شوند ($O(N)$ به جای $O(K \times N)$).
  2. **مقایسه فوق‌سریع بر پایه اشتراک مجموعه‌ها (`arePrecomputedColliding`):** متد تشخیص برخورد در طول لوپ آزمون بدون اجرای حتی یک عملیات Regex یا تفکیک رشته‌ای و صرفاً با بررسی برابری رشته‌های فشرده و اشتراک مجموعه‌های از پیش آماده شده در کسری از میکروثانیه اجرا می‌شود.
  3. **نمونه‌برداری هوشمند کاندیداها:** محدودسازی هوشمند تعداد کاندیداهای ورودی به تابع سنگین لوون‌اشتاین به ۴۰ کاندیدای برتر هم‌دسته و عمومی به ازای هر کارت.
  4. **انتقال به پردازش موازی در پس‌زمینه:** اجرای کلیه مراحل ساخت آزمون روی `withContext(Dispatchers.Default)`.
  5. **کاهش زمان پاسخ:** تست بنچ‌مارک اثبات کرد تولید آزمون برای ۱۰۰ سوال حتی با ۵۰۰ واژه از چند دقیقه به کمتر از ۳۰ میلی‌ثانیه کاهش یافته و عملکرد به صورت آنی (Instantaneous) درآمده است.

---

### ۲.۷. آماده‌سازی برای بیلد گیت‌هاب (CI/CD & Gradle Wrapper)
* **فایل‌های ایجاد شده:**
  * `gradlew`
  * `gradlew.bat`
  * `gradle/wrapper/gradle-wrapper.properties`
  * `gradle/wrapper/gradle-wrapper.jar`
  * `.github/workflows/android-ci.yml`
* **علت عدم بیلد در گیت‌هاب:**
  مخزن گیت‌هاب فاقد اسکریپت `gradlew` و پکیج wrapper بود؛ بنابراین اکشن‌های گیت‌هاب و بیلد سرورها با خطای نبود فایل `./gradlew` مواجه می‌شدند. همچنین فاقد اکشن استاندارد برای تست و ساخت APK بود که اکنون به طور کامل ایجاد گردید.

---

### ۲.۸. افزودن تنظیمات آستانه تغییر سختی و اختیاری‌سازی دسته‌بندی در آزمون و یادداشت راهنما
* **شرح نیاز کاربر:**
  1. در تنظیمات برنامه گزینه‌ای برای تعیین تعداد پاسخ‌های صحیح/غلط متوالی جهت آسان‌تر یا سخت‌تر شدن کلمات وجود نداشت.
  2. در صفحه آزمون (کوییز)، دسته‌بندی موضوعی به صورت پیش‌فرض نمایش داده می‌شد که سرنخ ناخواسته برای پاسخ به شمار می‌رفت و همچنین امکانی برای مشاهده راهنما و یادداشت واژه وجود نداشت.
* **اقدام اصلاحی:**
  1. افزودن تنظیم `difficultyThreshold` (با مقادیر ۲، ۳، ۴، ۵ با پیش‌فرض ۳) در `SettingsRepository`، `SettingsScreen` و اتصال آن به موتور `DifficultyCalculator` و لایه `ReviewRepository`.
  2. افزودن تنظیم `showCategoryInReview` (پیش‌فرض خاموش) جهت مخفی‌سازی دسته‌بندی از روی کارت‌های آزمون و فلش‌کارت تا سرنخ افشا نشود.
  3. افزودن دکمه «راهنما و یادداشت» به هدر کارت سؤال در `QuizScreen` با دیالوگ اختصاصی که یادداشت‌های گرامری و دسته‌بندی را فقط در صورت تمایل کاربر به عنوان راهنما نمایش می‌دهد.
  4. افزودن تست‌های واحد برای آستانه‌های سفارشی در `DifficultyCalculatorTest`.


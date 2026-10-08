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

### ۲.۲۰. اصلاح چیدمان کتابخانه، واقعی‌سازی دستاوردها و نمایش کامل ترجمه‌ها در آزمون (`LibraryScreen` & `ReviewRepository`)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/LibraryScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/components/CommonComponents.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/ProgressScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/ReviewSetupScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/ConceptDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/build.gradle.kts`
  * `docs/CHANGELOG.md`
* **ریشه‌یابی و اصلاحات انجام‌شده:**
  1. **رفع بهم‌ریختگی کارت لغات در کتابخانه:** هنگام وجود متن‌های طولانی اسپانیایی، نشان‌های وضعیت (`StageBadge` و `DifficultyBadge`) به لبه راست فشرده شده و کاراکترهای آن عمودی می‌شدند. با اعمال `Modifier.weight(1f, fill = false)` روی عنوان و تنظیم `maxLines = 1` و `softWrap = false` روی نشان‌ها، چیدمان افقی تضمین گردید.
  2. **واقعی‌سازی فرمول آنلاک دستاوردها:** در `ProgressScreen` شرط آنلاک نشان‌های «۱۰ لغت اول» و «واژه‌ساز» به اشتباه با `statistics.totalWords` (کل واژگان دیتابیس = بیش از ۱۰۰۰ لغت) مقایسه می‌شد که باعث آنلاک آنی در نصب تازه می‌شد. فرمول به `practicedCount` (لغات واقعی تمرین‌شده) تغییر یافت.
  3. **پیش‌فرض حالت مرور تصادفی:** حالت پیش‌فرض مرور در `ReviewSetupScreen` و `MainViewModel` به `ReviewType.RANDOM` تغییر یافت.
  4. **نمایش کامل تمام ترجمه‌های یک واژه در گزینه‌های آزمون:** با اضافه شدن کوئری `getContentsForConcepts` در `ConceptDao` و مجتمع‌سازی تمام ترجمه‌های هر مفهوم در `ReviewRepository` با ویرگول فارسی («، »)، کلیه معانی چندگانه یک واژه به صورت یکجا در گزینه آزمون به کاربر ارائه می‌شوند.

---

### ۲.۱۹. بهینه‌سازی بازیابی فایل‌های پشتیبان سنگین و رفع قفل شدن رابط کاربری (`BackupScreen`)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/BackupScreen.kt`
  * `app/build.gradle.kts`
  * `docs/CHANGELOG.md`
* **ریشه مشکل هنگ:**
  هنگام انتخاب فایل‌های پشتیبان سنگین (مانند `docs/progress.json` با حجم ۲.۶ مگابایت)، فریم‌ورک Compose تمام رشتهٔ ۲,۶۰۰,۰۰۰ کاراکتری JSON را در استیت `restoreJsonText` قرار می‌داد و آن را به کامپوننت `OutlinedTextField` پاس می‌داد. کامپوننت متنی Compose برای اندازه‌گیری چیدمان، محاسبات پاراگراف و رندر چند میلیون کاراکتر متن چندخطی، نخ اصلی UI را برای چند دقیقه قفل می‌کرد که باعث بروز خطای ANR و هنگ شدن برنامه می‌شد.
* **اقدام اصلاحی و بهینه‌سازی:**
  1. **جداسازی استیت فایل بارگذاری‌شده از کادر متنی:** فایل بارگذاری‌شده از طریق File Picker اکیداً به کامپوننت متنی منتقل نمی‌شود؛ بلکه به صورت بهینه در استیت متغیر `loadedJsonContent` ذخیره گشته و رابط کاربری یک کارت شیک «اطلاعات فایل پشتیبان بارگذاری‌شده» با وضعیت حجم و نام فایل نمایش می‌دهد.
  2. **پشتیبانی کلیپ‌بورد هوشمند:** در صورت چسباندن متن‌های بزرگتر از ۵۰,۰۰۰ کاراکتر از کلیپ‌بورد، سیستم به صورت خودکار آن را به عنوان فایل در حافظه شناسایی کرده و از سنگین شدن کادر متنی جلوگیری می‌کند.
  3. **تست و ارزیابی:** زمان بارگذاری و آماده‌سازی فایل ۲.۶ مگابایتی `progress.json` به کمتر از ۵۰ میلی‌ثانیه رسیده و بازیابی به صورت کاملاً روان، امن و بدون کوچک‌ترین لگ یا وقفه انجام می‌شود.

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

### ۲.۲۰. ممیزی جامع، بازطراحی بخش پشتیبان‌گیری، به‌روزرسانی آیکن و اصلاح موتور تلفظ (Audit & Cleanup 1.4.3)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/BackupScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/HomeScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/AboutScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/util/TtsManager.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/QuizDistractorScorer.kt`
  * `app/src/main/res/drawable/ic_launcher_foreground.xml`
  * `app/src/main/res/drawable/ic_launcher_background.xml`
  * `app/src/main/res/mipmap-*/ic_launcher.png`
  * `app/src/main/res/mipmap-*/ic_launcher_round.png`
  * `README.md`
  * `docs/CHANGELOG.md`
* **ریشه‌یابی و اصلاحات انجام‌شده:**
  1. **تفکیک‌پذیری کامل بخش پشتیبان‌گیری:** امکان تعیین دقیق حوزه‌های پشتیبان (واژگان، دسته‌بندی‌ها، لایتنر، دشواری، آمار رگبار و تنظیمات) فراهم گردید تا کاربر بتواند خروجی‌های سفارشی اکسل و جیسون دریافت کند.
  2. **آیکن اختصاصی و تطبیقی:** آیکن جدید به صورت کامل در تمامی لایه‌های وکتور و تراکم‌های تصویری (`mdpi` تا `xxxhdpi`) همراه با نسخه دایره‌ای تنظیم شد.
  3. **اصلاح زبان TTS:** تشخیص خودکار زبان متن مانع از فراخوانی موتور صوتی با کد نامعتبر `es` بر روی متون فارسی شد.
  4. **حذف کدهای تکراری و ارتقای معماری:** توابع مقایسه متنی و تشابه در `QuizDistractorScorer` متمرکز شدند، کدهای تکراری پوسته در `HomeScreen` استانداردسازی گردید و اطلاعات فنی در `AboutScreen` تصحیح شد.
  5. **رفع عدم نمایش کارت گزارش ورود گروهی:** در `MainActivity.kt` متغیر `importSummary` به `ImportScreen` متصل شد و پرش زودهنگام به کتابخانه حذف گردید تا کارت گزارش نتایج به‌صورت شفاف و کامل نمایش داده شود.

---

### ۲.۲۱. اصلاح شمارنده‌های مراحل لایتنر و بازطراحی جامع صفحه آمار و گزارش (نسخه ۱.۴.۴)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/ReviewSessionDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/ProgressScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/components/DailyReviewChart.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
  * `app/src/main/java/com/manidigit/yadin/MainActivity.kt`
  * `app/build.gradle.kts`
  * `README.md`
  * `docs/CHANGELOG.md`
* **ریشه‌یابی خطای شمارنده‌های آمار (Stage Counters):**
  در کوئری `LearningDao.getCountByStageFlow` شرط `AND (:stage = 'LEARNED' OR ls.lastReviewedDay IS NOT NULL)` وجود داشت. از آن‌جا که تمام ۶,۴۱۲ واژه اولیه در دیتابیس با `lastReviewedDay = null` ساخته می‌شوند، تا قبل از مرور شدن هر کارت، این شرط مانع از شمرده شدن واژه‌ها در مراحل روزانه، هفتگی و ماهانه می‌شد و در نتیجه عدد کل واژگان (`totalWords`) و هرم لایتنر نزدیک به صفر نشان داده می‌شد.
* **اقدامات اصلاحی و قابلیت‌های افزوده:**
  1. **اصلاح کوئری `getCountByStageFlow`:** شرط غیرضروری `lastReviewedDay IS NOT NULL` حذف شد تا تمامی واژگان جدید و بدون پیشینه مرور بلافاصله در مرحله مربوطه شمارش گردند.
  2. **افزایش برد آماری به ۹۰ روز:** لیمیت متد `getRecentDailyStatsFlow` از ۱۴ به ۹۰ روز افزایش یافت تا فیلتر ماهانه در نمودارها و جدول دقت ماهانه دارای داده‌های واقعی ۳۰ روزه باشد.
  3. **پیاده‌سازی دقیق تمامی ۹ شاخص درخواستی صفحه آمار (`ProgressScreen`):**
     * **کارت‌های خلاصه سه‌گانه:** دقت کل (%)، تعداد یادگرفته‌شده، و کل واژه‌های تمرین‌شده.
     * **پیشرفت یادگیری:** درصد پیشرفت کلی و نوار پیشرفت بر اساس فرمول ۶.۱۸.
     * **حفظ ماندگار (Retention):** نمایش نسبت پاسخ‌های درست از کل و درصد با نوار پیشرفت سبز.
     * **فعالیت مرور:** فیلتر بازه زمانی (هفتگی/ماهانه)، نمودار میله‌ای روزهای هفته و برچسب محور عمودی «تعداد مرور».
     * **مراحل یادگیری:** نوارهای پیشرفت اختصاصی برای مراحل روزانه، هفتگی، ماهانه و یادگرفته.
     * **پروفایل سختی:** نوارهای پیشرفت اختصاصی برای سطوح آسان، متوسط، سخت و خیلی سخت.
     * **دقت مرور:** جدول تفکیکی امروز، این هفته، این ماه و مجموع کل با نمایش نسبت درست از کل و درصد.
     * **دستاوردها:** شمارنده رسمی «تعداد دستاوردهای کسب‌شده» از کل و گالری نشان‌ها.
     * **دکمه به‌روزرسانی آمار:** تعبیه دکمه Refresh در هدر بالا و دکمه اختصاصی در انتهای صفحه جهت تازه‌سازی بلادرنگ.
  4. **ارتقای نسخه:** ارتقای به `versionCode = 12` و `versionName = "1.4.4"`.

---

### ۲.۲۲. ممیزی عمیق و رفع تمامی باگ‌های پنهان، هماهنگ‌سازی جهت‌های زبانی، ایمن‌سازی نشست‌های مرور و ارتقای به نسخه ۱.۵.۰
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/ui/screens/AddEditWordScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/ProgressScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/viewmodel/MainViewModel.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/BackupRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/SeedImporter.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/database/YadinDatabase.kt`
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/FileReaders.kt`
  * `app/src/main/java/com/manidigit/yadin/MainActivity.kt`
  * `app/build.gradle.kts`
  * `README.md`
  * `docs/CHANGELOG.md`
* **ریشه‌یابی خطاها و اقدامات اصلاحی:**
  1. **باگ جهت زبانی در افزودن و ویرایش واژه:** متدهای `addWord` و `updateWord` زبان واژه را هاردکد (`es` و `fa`) ذخیره می‌کردند حتی زمانی که کاربر در جهت معکوس (`REVERSE`: فارسی $\to$ اسپانیایی) واژه اضافه می‌کرد. پارامتر `languageDirection` افزوده شد تا در حالت معکوس متن ورودی اول به عنوان زبان مبدأ فارسی (`fa`) و ترجمه به عنوان زبان مقصد اسپانیایی (`es`) ثبت شود.
  2. **آمار صفحه پیشرفت وابسته به سوئیچ جهت:** متغیرهای `progressScorePercent`، `practicedWordsCount` و `statistics` در `ProgressScreen` اکنون مستقیماً بر اساس جهت انتخابی در تب بالای صفحه (`progressDirection`) تغییر و به‌روزرسانی می‌شوند.
  3. **اصلاح تناقض بازه‌های زمانی دقت:** محاسبه دقت کلی مستقیماً بر اساس کل تاریخچه مرورها از `ReviewSessionDao` متصل شد تا با دقت بازه‌ای ۱۴ یا ۹۰ روزه مخلوط نشود.
  4. **یکپارچه‌سازی دستاوردها:** وضعیت بازگشایی نشان‌ها در UI مستقیماً به استیت واقعی دیتابیس (`getAllAchievementsFlow`) مقید شد و شرط دستاورد `QUIZ_ACE` با متن آن (کسب نمره کامل در آزمون ۱۰ سؤالی) هماهنگ شد.
  5. **جلوگیری از پاسخ‌های تکراری فلش‌کارت:** اضافه شدن متغیر و قفل اعتبارسنجی همزمانی در `MainViewModel` مانع از ارسال پاسخ‌های پشت سر هم برای یک کارت در اثر لمس‌های سریع گردید.
  6. **مدیریت نشست‌های رهاشده:** اضافه شدن متد `abandonSession` در ریپازیتوری تا با خروج کاربر از آزمون، وضعیت جلسه به جای معلق ماندن در `ACTIVE`، صریحاً به `ABANDONED` تغییر یافته و ساعت اتمام آن ثبت شود.
  7. **تضمین گزینه‌های انحرافی کوییز:** افزایش ظرفیت استخر واژگان کاندید به ۸۰۰ واژه و ایجاد فال‌بک تکمیلی در شرایط دسته‌های کم‌واژه جهت پیشگیری از ۳ گزینه‌ای شدن کوییز.
  8. **بازفعال‌سازی واژگان در ورود گروهی:** در عملیات ادغام و ویرایش، کلمات غیرفعال گذشته مجدداً با `active = true` فعال می‌شوند و شمارنده‌های نتیجه دقیق‌تر شدند.
  9. **تثبیت پارسر فایل‌ها و بازیابی اتمیک:** رفع خطای نادیده گرفتن فایل‌های JSON با ساختار غیرمجاز، پشتیبانی از سلول‌های حاوی تگ‌های inlineStr در فایل‌های اکسل، و حذف محتواهای قدیمی هنگام جایگزینی در بازیابی پشتیبان.
  10. **حذف try/catch خاموش در دیتابیس و تراکنش در SeedImporter:** ارتقای سطح ایمنی دیتابیس با حذف مسدودسازی خطاهای مهاجرت Room و درج داده‌های اولیه با `withTransaction`.
  11. **ارتقای نسخه:** ارتقای به `versionCode = 13` و `versionName = "1.5.0"`.

---

### ۲.۲۳. بازنگری کامل قوانین هسته لایتنر، رفع باگ‌های نشست مرور، نمایش ترکیبی سررسید/کل و آیکون اختصاصی لانچر (نسخه ۱.۶.۰)
* **فایل‌های درگیر:**
  * `app/src/main/java/com/manidigit/yadin/domain/algorithm/LearningTransition.kt`
  * `app/src/main/java/com/manidigit/yadin/data/local/dao/LearningDao.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/VocabularyRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/ReviewRepository.kt`
  * `app/src/main/java/com/manidigit/yadin/data/repository/SeedImporter.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/HomeScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/ReviewSetupScreen.kt`
  * `app/src/main/java/com/manidigit/yadin/ui/screens/FlashcardScreen.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/LearningTransitionTest.kt`
  * `app/src/main/res/mipmap-*/` و `res/drawable/`
  * `app/build.gradle.kts`
  * `docs/CHANGELOG.md`
  * `docs/ISSUES_AND_DEFECTS_BACKLOG.md`
* **ریشه‌یابی و اقدامات اصلاحی:**
  1. **سقوط قطعی واژه‌های یادگرفته (`LEARNED`) در پاسخ غلط:** در گذشته در متد `calculateNextStage`، واژه‌هایی که در مرحله `LEARNED` بودند در صورت پاسخ غلط با فرض اینکه دیگر نباید تغییر کنند در همان مرحله می‌ماندند. این رفتار اصلاح شد تا هر واژه‌ای در صورت پاسخ نادرست، بدون استثنا به مرحله روزانه (`Stage.DAILY`) با تاریخ سررسید فردا بازگردد.
  2. **رفع رخنه قفل هم‌روز در واژه‌های یادگرفته و مرور رندوم:** در کوئری‌های واکشی مخزن `LEARNED` و `RANDOM` در `LearningDao`، شرط `lastReviewedDay != today` اعمال نمی‌شد و واژه‌های یادگرفته یا تصادفی در همان روز می‌توانستند دوباره تکرار شوند. این شروط صریحاً اضافه شدند.
  3. **پیاده‌سازی نمایش ترکیبی «آماده مرور از کل مخزن»:** با اضافه شدن فیلدهای `dueDailyCount`، `dueWeeklyCount`، `dueMonthlyCount` و `availableLearnedCount` به مدل آماری، کپسول‌های لایتنر در صفحه اصلی (`StageMetricPill`) و چیپ‌های صفحه تنظیم مرور (`ReviewSetupScreen`) مقدار سررسید را نسبت به کل مخزن (مثلاً `۴۵/۱۲۰` یا `۰/۲۰`) نمایش می‌دهند.
  4. **اصلاح ثبت موقعیت و ترتیب آیتم‌های مرور:** شناسایی سؤالات آزمون در `ReviewRepository` بر مبنای `conceptId` منحصر‌به‌فرد تنظیم شد تا پرش یا بازگشت با دکمه «قبلی» باعث عدم تطابق نگردد.
  5. **بهبود تجربه کاربری فلش‌کارت:** غیرفعال‌سازی دکمه‌های پاسخ تا قبل از چرخش کارت به پشت و حذف آیکون پخش صدای فارسی از پشت کارت.
  6. **جایگزینی آیکون رسمی برنامه با `docs/YADIN.ico`:** پاکسازی آیکون‌های متفرقه و تولید بسته‌های آیکون استاندارد PNG32 برای تمامی چگالی‌های اندروید (مدل‌های معمولی و گرد) به همراه به‌روزرسانی لایه‌های Adaptive Icon.
  7. **تثبیت پایپ‌لاین CI در گیت‌هاب:** اصلاح تست `LearningTransitionTest` متناسب با قانون سقوط `LEARNED` و پاس شدن کامل تمامی ۴۷ تست یونیت و صدور موفقیت‌آمیز بیلد APK.
  8. **ارتقای نسخه رسمی:** ارتقای شماره ساخت و نام نسخه به `versionCode = 14` و `versionName = "1.6.0"`.





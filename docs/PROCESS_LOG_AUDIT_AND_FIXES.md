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
* **ریشه باگ:**
  در سوالاتی مانند `usted durmió`، ترجمه صحیح «شما خوابیدید» است. در بانک داده گزینه‌هایی نظیر «شما (جمع) خوابیدید» (مربوط به `ustedes durmieron`) و «شماها خوابیدید» (مربوط به `vosotros dormisteis`) وجود داشتند. از آنجا که این گزینه‌ها شباهت لغوی، موضوعی و دشواری بسیار بالایی داشتند، موتور کوییز در سطوح MEDIUM و HARD آن‌ها را به عنوان گزینه‌های انحرافی برتر برمی‌گزید. در نتیجه کاربر با چند گزینه عملاً هم‌معنی در زبان فارسی روبرو می‌شد و با انتخاب یکی، سیستم آن را غلط و دیگری را درست نشان می‌داد.
* **اقدام اصلاحی:**
  1. افزودن متد `QuizDistractorScorer.normalizeCore` جهت حذف عبارات توضیحی داخل پرانتز (`(جمع)`، `(مفرد)`، `(مونث)`) و استانداردسازی ضمایر جمع محاوره‌ای («شماها» $\to$ «شما»).
  2. ایجاد فیلتر برخورد معنایی `areSemanticallyColliding` جهت حذف هر گزینه‌ای که هسته معنایی آن با پاسخ صحیح یکی است.
  3. اعمال فیلتر دوطرفه در زمان انتخاب گزینه‌های انحرافی تا هیچ دو گزینه‌ای با هسته معنایی یکسان وارد گزینه‌ها نشوند.
  4. لایه محافظتی در زمان ارزیابی پاسخ کاربر در `MainViewModel` و نمایش بصری در `QuizScreen`.
  5. افزودن تست واحد صریح در `QuizDistractorScoringTest` جهت اثبات شناسایی و حذف این برخوردها.

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


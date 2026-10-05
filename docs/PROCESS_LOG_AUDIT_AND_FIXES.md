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

### ۲.۶. راه‌اندازی تست‌های خودکار (Automated Unit Tests)
* **فایل‌های ایجاد شده:**
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/LearningTransitionTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/DifficultyCalculatorTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/time/StreakCalculationTest.kt`
  * `app/src/test/java/com/manidigit/yadin/domain/algorithm/VocabularyParserTest.kt`
* **نتیجه اجرا:**
  تمام ۲۰ تست الگوریتمی با دستور `./gradlew testDebugUnitTest` با موفقیت ۱۰۰٪ پاس شدند.

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

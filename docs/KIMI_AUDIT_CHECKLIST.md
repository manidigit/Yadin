# گزارش بررسی، پالایش و رفع موارد تحلیل هوش مصنوعی کیمی (Kimi Audit Checklist)

**وضعیت نهایی:** تمامی ۱۳ مورد معتبر تاییدشده در نسخه **`1.67.0` (کد نسخه: `79`)** به طور کامل در کدبیس اعمال، تست و مستندسازی شدند.

این سند نتیجهٔ بررسی دقیق خط‌به‌خط گزارش ۳۲گانه‌ی هوش مصنوعی **کیمی (Kimi)** (بر مبنای کامیت `be27cc6c728acc017a39a994ac6561d63d901ddd`) در برابر کدهای پروژه یادین و سند مشخصات فنی (`docs/Yadin-Specification1-1.md`) است.

تمامی ۳۲ مورد گزارش کیمی به **سه دستهٔ شفاف** تفکیک شده‌اند:
1. **بخش اول: باگ‌ها و مشکلات واقعی و معتبر (۱۳ مورد — همگی در نسخه `1.67.0` رفع شدند)**
2. **بخش دوم: مواردی که قبلاً در نسخه `1.66.0` برطرف شده بودند (۵ مورد)**
3. **بخش سوم: موارد نادرست یا مغایر با معماری و تعاریف اختصاصی پروژه یادین (۱۴ مورد — ردشده)**

---

## بخش اول: باگ‌ها و مشکلات واقعی و معتبر (رفع‌شده در نسخه `1.67.0` - Build `79`)

- [x] **۱. حفظ حضور کارت‌های بالغ SM-2 و FSRS (با فاصله ≥ ۶۰ روز) در صف مرور سررسید و تقویم پیش‌بینی (مورد ۱ کیمی - بحرانی):**
  - **محل اصلاح:** `LearningDao.kt` (`getFilteredCandidateConceptIds`، `countFilteredCandidates` و `getScheduledReviewsByDay`)
  - **شرح اصلاح:** برای انواع مرور `SM2` و `FSRS`، کارت‌هایی که فاصله مرور آن‌ها به ۶۰ روز یا بیشتر رسیده و مرحله نمایشی `LEARNED` دارند اما دارای `nextReviewDay IS NOT NULL AND nextReviewDay <= :todayDayString` هستند، در صف مرور سررسید و تقویم پیش‌بینی ۷روزه حفظ شدند (در حالی که کارت‌های `LEARNED` کلاسیک یادین که `nextReviewDay = NULL` دارند همچنان فقط در بخش اختیاری «یادگرفته‌ها» قرار می‌گیرند).

- [x] **۲. یکتاسازی اتمیک `reviewAttemptId` و بازگشت شمارنده‌های متوالی در مسیر Idempotent (مورد ۳ کیمی - مهم):**
  - **محل اصلاح:** `ReviewRepository.kt` (`submitAnswer`)
  - **شرح اصلاح:** بررسی `reviewSessionDao.getHistoryByAttemptId(reviewAttemptId)` به درون بلوک تراکنش اتمیک `runInTransaction` منتقل شد و مقادیر `consecutiveCorrect` و `consecutiveWrong` از `curDiff` در خروجی `SubmitResult` بازگردانده می‌شوند.

- [x] **۳. بازگردانی خودکار (Rollback) وضعیت انتخاب گزینه در صورت بروز خطای دیتابیس در آزمون (مورد ۴ کیمی - مهم):**
  - **محل اصلاح:** `ReviewViewModel.kt` (`submitQuizAnswer`)
  - **شرح اصلاح:** مقادیر قبلی `_quizSelectedOption` و `_quizUserAnswers` پیش از تغییر خوش‌بینانه ذخیره شده و در صورت وقوع استثنا در بلوک `catch` به حالت قبل بازگردانده می‌شوند تا گزینه قفل نماند.

- [x] **۴. رفع باگ تشخیص `isFirstReview` در الگوریتم FSRS پس از زدن دکمه «فراموش کردم» در کارت جدید (مورد ۶ کیمی - بحرانی):**
  - **محل اصلاح:** `FsrsAlgorithm.kt` (`calculateNextReview`) و تست واحد در `FsrsAlgorithmTest.kt`
  - **شرح اصلاح:** شرط `isFirstReview` به `currentState.stability <= 0.0 || (currentState.state == FsrsCardState.NEW.code && currentState.reps <= 0 && currentState.lapses <= 0)` اصلاح شد تا کارتی که در مرور اول `AGAIN` خورده (`state = LEARNING, lapses = 1, stability = 0.48`) در مرور دوم مجدداً کارت جدید فرض نشود.

- [x] **۵. هماهنگ‌سازی فرمول `initialDifficulty` با ضرایب `W[4]` و `W[5]` در FSRS (مورد ۷ کیمی - بحرانی):**
  - **محل اصلاح:** `FsrsAlgorithm.kt` (`initialDifficulty`) و تست واحد در `FsrsAlgorithmTest.kt`
  - **شرح اصلاح:** فرمول دشواری اولیه به فرمول خطی استاندارد FSRS-4.5 (`W[4] - (rating.grade - 3.0) * W[5]`) منطبق با ضرایب `W[4]=5.00, W[5]=0.85` اصلاح شد تا `GOOD` دقیقاً دارای دشواری اولیه `5.0` باشد، ترتیب اکید `AGAIN (6.70) > HARD (5.85) > GOOD (5.00) > EASY (4.15)` برقرار گردد و بازگشت به میانگین (`Mean Reversion`) به سمت `5.0` عمل کند.

- [x] **۶. جلوگیری از ورود واژه‌های غیرفعال (Soft-Deleted) به استخر گزینه‌های انحرافی آزمون (مورد ۱۳ کیمی - مهم):**
  - **محل اصلاح:** `ConceptDao.kt` (`getRandomContents`)
  - **شرح اصلاح:** کوئری `getRandomContents` با `INNER JOIN concepts c ON cnt.conceptId = c.id WHERE cnt.languageCode = :languageCode AND c.active = 1` ایمن‌سازی شد.

- [x] **۷. اعتبارسنجی وجود هر دو زبان مبدأ و مقصد در انتخاب کارت‌های مرور (مورد ۱۷ کیمی - متوسط):**
  - **محل اصلاح:** `LearningDao.kt` و `ReviewRepository.kt` (`fetchCardsForSession`)
  - **شرح اصلاح:** وجود متن غیرخالی برای هر دو زبان مبدأ (`languageCode != 'fa'`) و مقصد (`languageCode = 'fa'`) در کوئری‌های انتخاب کارت بررسی شده و fallback نادرست `.ifEmpty { sourceVariants }` در `ReviewRepository.kt` حذف گردید.

- [x] **۸. پشتیبانی از جدول واسط `concept_categories` در فیلتر دسته‌بندی جلسات مرور و کتابخانه (مورد ۱۹ کیمی - مهم):**
  - **محل اصلاح:** `LearningDao.kt` و `ConceptDao.kt` (`searchConceptsFiltered`، `getConceptsByCategoryFlow`، `getContentsByCategories`)
  - **شرح اصلاح:** شرط `EXISTS (SELECT 1 FROM concept_categories cc WHERE cc.conceptId = c.id AND ...)` به تمامی کوئری‌های فیلتر دسته‌بندی اضافه شد.

- [x] **۹. شمارش منحصراً جلسات تکمیل‌شده در دستاورد «آزمون‌های بی‌نقص» (مورد ۲۳ کیمی - متوسط):**
  - **محل اصلاح:** `ReviewSessionDao.kt` (`getPerfectQuizSessionsCount`)
  - **شرح اصلاح:** کوئری با `INNER JOIN review_sessions rs ON rh.sessionId = rs.id` و شرط `rs.status = 'COMPLETED' AND COUNT(*) >= rs.totalItems` تکمیل شد تا جلسات رهاشده (`ABANDONED`) شمارش نشوند.

- [x] **۱۰. ذخیره `optionsJson` هنگام ثبت پاسخ آزمون در `ReviewViewModel` (مورد ۲۴ کیمی - متوسط):**
  - **محل اصلاح:** `ReviewViewModel.kt` (`submitQuizAnswer`)
  - **شرح اصلاح:** گزینه‌های سؤال با `JSONArray(q.options.map { it.text }).toString()` سریال‌سازی شده و به `reviewRepo.submitAnswer` ارسال می‌شوند.

- [x] **۱۱. اعمال سرعت تلفظ (`ttsSpeechRate`) در موتور `TtsManager` (مورد ۲۹ کیمی - متوسط):**
  - **محل اصلاح:** `TtsManager.kt` و `MainViewModel.kt`
  - **شرح اصلاح:** متد `setSpeechRate` و فراخوانی `engine.setSpeechRate(...)` به `TtsManager` اضافه شد و در `MainViewModel.initializeDatabase` به صورت واکنشی با `settingsVm.ttsSpeechRate` همگام گردید.

- [x] **۱۲. رهاسازی خودکار جلسات `ACTIVE` قدیمی در زمان راه‌اندازی برنامه (بخشی از مورد ۲۶ کیمی - متوسط):**
  - **محل اصلاح:** `MainViewModel.kt` (`initializeDatabase`)
  - **شرح اصلاح:** متد `reviewRepo.abandonOrphanActiveSessions()` در بلوک راه‌اندازی اولیه برنامه فراخوانی شد.

- [x] **۱۳. محلی‌سازی پیام‌های خطای داخلی در `ReviewViewModel` و `VocabularyRepository` (بخشی از مورد ۳۱ کیمی - جزئی):**
  - **محل اصلاح:** `values/strings.xml`، `values-en/strings.xml`، `ReviewViewModel.kt` و `VocabularyRepository.kt`
  - **شرح اصلاح:** کلیدهای خطای دوزبانه اضافه و در `ReviewViewModel` و `VocabularyRepository` جایگزین رشته‌های ثابت شدند.

---

## بخش دوم: مواردی که قبلاً در نسخه `1.66.0` (Build 78) برطرف شده بودند

1. **مورد ۹ کیمی (عدم تطابق `previewIntervals` و `calculateNextReview` در FSRS):** قبلاً در نسخه `1.66.0` در `FsrsAlgorithm.kt:211-242` برطرف شده بود.
2. **مورد ۱۰ کیمی (ناهماهنگی دکمه «سخت / HARD» بین زمان‌بندی و سطح دشواری):** قبلاً در نسخه `1.66.0` با `DifficultyCalculator.updateDifficultyForHardRating` برطرف شده بود.
3. **مورد ۱۴ کیمی (محدودیت نمونه‌گیری ۵۰۰تایی و سقف ۴۰ کاندید در موتور آزمون):** قبلاً در نسخه `1.66.0` با `getContentsByCategories` و سقف ۲۴۰ کاندید هدفمند برطرف شده بود.
4. **مورد ۲۲ کیمی (شمارش کلمات مرورنشده در `dailyWords` در `getRetentionMetrics`):** قبلاً در نسخه `1.66.0` با `getPracticedCountByStage` برطرف شده بود.
5. **مورد ۳۱ کیمی (کسری کلیدهای انگلیسی در `values-en/strings.xml` و رشته‌های هاردکد در کارت‌های آمار، پشتیبان‌گیری و Enums):** قبلاً در نسخه `1.66.0` برطرف شده بود.

---

## بخش سوم: موارد نادرست یا مغایر با تعاریف و معماری پروژه یادین (ردشده)

1. **مورد ۵ کیمی (امضای بیلد Release با کلید Debug در `app/build.gradle.kts`):** تغییر `signingConfigs` در محیط ابری AI Studio اکیداً ممنوع است و بیلد و شبیه‌ساز را متوقف می‌کند.
2. **مورد ۸ کیمی (تفاوت فرمول SM-2 یادین با مقاله ۱۹۸۷ Wozniak در دکمه‌های HARD و EASY):** در رابط ۴دکمه‌ای مدرن یادین، تولید ۴ فاصله متمایز و صعودی برای ۴ دکمه عمدی است تا بالای دکمه‌ها اعداد تکراری نوشته نشود.
3. **مورد ۲ کیمی (پرتاب خطا در `submitAnswer` برای کارت‌های غیرسررسید):** در یادین حالت‌های «مرور تصادفی»، «مرور بر اساس سطح دشواری» و «مرور یادگرفته‌ها» وجود دارد که کارت در آن‌ها لزوماً سررسید امروز نیست؛ پرتاب خطا باعث از کار افتادن این بخش‌ها می‌شود.
4. **موارد ۱۱ و ۱۲ کیمی (ادعای معیوب بودن نرمال‌سازی و Stemming فارسی در `QuizDistractorScorer`):** این توابع صرفاً برای تشخیص برخورد معنایی بین گزینه‌های آزمون استفاده می‌شوند و متن نمایشی کاربر (`rawText`) دست‌نخورده باقی می‌ماند.
5. **موارد ۱۵، ۱۶، ۱۸، ۲۰، ۲۱، ۲۵، ۲۷، ۲۸، ۳۰ و ۳۲ کیمی:** مغایر با منطق حفظ تاریخچه در ادغام واژه‌ها، نرمال‌سازی کلید واژه برای کیبوردهای بدون آکسان، ویرایش در جهت معکوس و مکانیزم تغییر آنی زبان در Compose بودند.

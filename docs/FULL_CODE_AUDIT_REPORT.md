# گزارش ممیزی خط‌به‌خط مشکلات و باگ‌های فنی برنامه یادین (Full Code Audit Report)

**وضعیت:** ✅ تمامی موارد بخش‌های ۱ تا ۸ به طور کامل در نسخه `1.69.0` (کد نسخه: `82`) برطرف و با تست‌های واحد تایید شدند.

> **دامنه گزارش:** این سند صرفاً شامل باگ‌های واقعی، خطاهای الگوریتمی، دکمه‌ها و تنظیمات غیرفعال/بی‌اثر، دوباره‌کاری‌ها، سوتی‌های برنامه‌نویسی، متن‌های هاردکدشده خارج از فایل‌های دوزبانه و مقادیر طراحی خارج از توکن‌های تم است. هیچ‌گونه توضیح عمومی، تعریف از برنامه یا ایراد سلیقه‌ای در این گزارش درج نشده است.

---

## بخش ۱: دکمه‌های الکی، تنظیمات غیرفعال و سوتی‌های مستقیم رابط کاربری و تعاملات

### ۱.۱. کشیدن (Swipe) فلش‌کارت به چپ یا راست قبل از دیدن جواب، پاسخ را ثبت می‌کند
- **فایل و خط:** `ui/screens/FlashcardScreen.kt:254-273`
- **شرح خطا:** ژست حرکتی `detectHorizontalDragGestures` روی کارت فلش‌کارت هیچ شرطی روی متغیر `isFlipped` ندارد. در حالی که دکمه‌های پایین صفحه قبل از برگرداندن کارت مخفی هستند و فقط دکمه «نمایش پاسخ» دیده می‌شود، اگر کاربر قبل از برگرداندن کارت (`isFlipped == false`) انگشتش را بیش از `150f` به راست یا چپ بکشد، کارت بدون نمایش معنی به عنوان پاسخ «درست / خوب» (`GOOD`) یا «غلط / دوباره» (`AGAIN`) ثبت می‌شود و به کارت بعدی می‌رود.

### ۱.۲. قفل شدن دائمی دکمه «ذخیره تغییرات» در صفحه افزودن/ویرایش واژه در صورت بروز خطا
- **فایل و خط:** `ui/screens/AddEditWordScreen.kt:109, 219-244` و `MainActivity.kt:448-452` و `ui/viewmodel/LibraryViewModel.kt:264-322`
- **شرح خطا:**
  1. در `AddEditWordScreen.kt:231` با زدن دکمه ذخیره، متغیر `isSaving = true` می‌شود و دکمه ذخیره غیرفعال (`enabled = !isSaving`) می‌گردد.
  2. در `MainActivity.kt:449` هنگام فراخوانی `viewModel.saveWord(...)`، هیچ کال‌بکی برای خطا وجود ندارد و فقط در صورت موفقیت (`onSuccess`) صفحه به عقب برمی‌گردد.
  3. اگر `vocabularyRepo.addWord` یا `updateWord` خطا برگرداند (مثلاً خطای تکراری بودن واژه یا خطای اعتبارسنجی)، پیام خطا داخل `LibraryViewModel._errorMessage` ریخته می‌شود که اصلاً در `AddEditWordScreen` مشاهده (observe) نمی‌شود، متغیر محلی `errorMessage` در `AddEditWordScreen` خالی می‌ماند، و `isSaving` برای همیشه `true` باقی می‌ماند؛ در نتیجه دکمه ذخیره تا خروج کامل از صفحه قفل می‌شود و هیچ خطایی هم به کاربر نمایش داده نمی‌شود.

### ۱.۳. جابه‌جا شدن واژه اسپانیایی و ترجمه فارسی در دیتابیس هنگام ویرایش یا افزودن واژه در جهت «فارسی به اسپانیایی» (`REVERSE`)
- **فایل و خط:** `ui/screens/AddEditWordScreen.kt:69-96, 224-239` و `data/repository/VocabularyRepository.kt:437-490, 599-667`
- **شرح خطا:**
  - در `AddEditWordScreen.kt:69-96` وقتی جهت برنامه `CardDirection.REVERSE` (`فارسی ⬅ اسپانیایی`) است، فیلد اول (`sourceText`) با متن فارسی و فیلد دوم (`translationsText`) با متن اسپانیایی پر می‌شود و به `saveWord(conceptId, finalSrc, finalTrans, ...)` فرستاده می‌شود.
  - اما متد `VocabularyRepository.updateWord` (`خط 599-667`) اصلاً پارامتر `direction` دریافت نمی‌کند و بدون توجه به جهت فعلی برنامه، آرگومان اول (`newSourceText`) را با `languageCode = "es"` (اسپانیایی) و آرگومان دوم (`newTranslations`) را با `languageCode = "fa"` (فارسی) در جدول `lexemes` ذخیره می‌کند.
  - در نتیجه، اگر کاربر در حالت «فارسی به اسپانیایی» یک واژه را ویرایش و ذخیره کند، متن فارسی به عنوان واژه اسپانیایی و متن اسپانیایی به عنوان ترجمه فارسی در دیتابیس ذخیره شده و رکورد واژه خراب می‌شود.

### ۱.۴. غیرممکن بودن حذف تمام دسته‌بندی‌های یک واژه در صفحه ویرایش واژه
- **فایل و خط:** `ui/screens/AddEditWordScreen.kt:228-238`، `ui/viewmodel/LibraryViewModel.kt:299-302` و `data/repository/VocabularyRepository.kt:658-665`
- **شرح خطا:** اگر کاربر در صفحه ویرایش واژه تیک همه دسته‌بندی‌ها را بردارد (`selectedCategoryIds` خالی شود)، مقدار `categoryId = null` و `categoryIds = emptyList()` ارسال می‌شود. در `LibraryViewModel.kt:300` شرط `if (effectiveCats.isNotEmpty())` برقرار نمی‌شود و متد `setCategoriesForConcept` صدا زده نمی‌شود؛ در `VocabularyRepository.updateWord:658` نیز شرط `if (categoryId != null)` مانع پاک شدن دسته‌بندی قبلی می‌شود. بنابراین دسته‌بندی‌های قبلی واژه دست‌نخورده باقی می‌مانند.

### ۱.۵. خراب شدن پشته ناوبری (Backstack) هنگام ورود به جزئیات واژه و ویرایش یا حذف آن
- **فایل و خط:** `ui/viewmodel/MainViewModel.kt:448-458` و `MainActivity.kt:436-455`
- **شرح خطا:** متد `selectWordDetail(conceptId)` در `MainViewModel.kt:449` مستقیماً `_currentScreen.value = Screen.WordDetailScreen(conceptId)` را تغییر می‌دهد اما آن را به `_screenStack` اضافه نمی‌کند (برخلاف `navigateTo`). در نتیجه:
  1. وقتی کاربر از «کتابخانه» وارد «جزئیات واژه» می‌شود و سپس دکمه «ویرایش» (`navigateTo(Screen.EditWordScreen)`) را می‌زند، پس از ذخیره یا زدن دکمه بازگشت (`navigateBack()`)، برنامه به‌جای بازگشت به «جزئیات واژه» مستقیماً به «کتابخانه» می‌پرد.
  2. متد `deleteWord` در `MainViewModel.kt:455` نیز مستقیماً `_currentScreen.value = Screen.Library` را ست می‌کند بدون اینکه `_screenStack` را همگام کند.

### ۱.۶. ردیف کلیک‌پذیر صوری و بی‌اثر «انتخاب تم» در صفحه تنظیمات
- **فایل و خط:** `ui/screens/SettingsScreen.kt:122-145`
- **شرح خطا:** کارت انتخاب تم دارای `.clickable { onSelectTheme(currentThemeId.ifBlank { "gemini" }) }` است که با کلیک روی آن، همان مقدار فعلی `"gemini"` دوباره به خودش پاس داده می‌شود و هیچ دیالوگ، پیام یا عملیاتی اجرا نمی‌شود.

### ۱.۷. تنظیمات پیاده‌سازی‌شده در دیتابیس و ViewModel که یا در صفحه تنظیمات وجود ندارند یا در برنامه عمل نمی‌کنند
- **فایل و خط:** `data/repository/SettingsRepository.kt:35-46, 200-221`، `ui/viewmodel/SettingsViewModel.kt:75-115, 198-219`، `ui/viewmodel/MainViewModel.kt:144-149, 306-326` و `ui/components/CommonComponents.kt:476-497`
- **شرح خطا:**
  1. **`ttsEnabled` (فعال بودن تلفظ صوتی):** در `SettingsRepository`، `SettingsViewModel` و `MainViewModel` تعریف شده اما در `SettingsScreen` هیچ سوییچی ندارد و در `SpeakButton` (`CommonComponents.kt:486`) یا `TtsManager` اصلاً بررسی نمی‌شود.
  2. **`ttsAutoPlay` (پخش خودکار تلفظ):** در `SettingsRepository`، `SettingsViewModel` و `MainViewModel` وجود دارد اما نه در `SettingsScreen` قابل تغییر است و نه در `FlashcardScreen` یا `QuizScreen` فراخوانی شده است.
  3. **`ttsSpeechRate` (سرعت تلفظ):** در `SettingsViewModel` و `TtsManager.setSpeechRate` وجود دارد اما هیچ کنترلی در `SettingsScreen` برای تغییر آن توسط کاربر وجود ندارد.
  4. **`quizAutoAdvanceSeconds` (رفتن خودکار به سؤال بعدی آزمون):** در `SettingsRepository`، `SettingsViewModel` و `MainViewModel` تعریف شده اما نه در `SettingsScreen` وجود دارد و نه در `QuizScreen` / `ReviewViewModel` استفاده شده است (در `QuizScreen.kt:238` فقط دکمه دستی «سؤال بعدی» وجود دارد).
  5. **`defaultReviewMode` و `defaultQuizLevel` و `showCategoryInReview`:** متدهای تغییر آن‌ها در `SettingsViewModel` (`خط 198-204, 217`) و `MainViewModel` (`خط 306-309, 326`) نوشته شده‌اند اما هیچ کنترلی در `SettingsScreen` برای آن‌ها قرار داده نشده است.

### ۱.۸. از دست رفتن اطلاعات استخراج‌شده واژه در صفحه «بررسی موارد نیازمند بازبینی» (`ImportReviewScreen`)
- **فایل و خط:** `ui/screens/ImportReviewScreen.kt:123-134, 159-170`
- **شرح خطا:** هنگام تأیید تکی یا تأیید همه موارد در `ImportReviewScreen`، به‌جای استفاده از `item.copy(...)`، یک نمونه جدید `ParsedEntry(sourceText = ..., translations = ..., note = ...)` ساخته می‌شود. این کار باعث می‌شود فیلدهای `entryType` (کلمه/عبارت/جمله)، `confidence`، `lineNumber`، `variants`، `breakdowns`، `relations` و `categoryNames` که توسط `VocabularyParser` استخراج شده بودند کاملاً پاک شوند و همه موارد با `entryType = EntryType.WORD` و بدون دسته‌بندی و اجزای تجزیه وارد دیتابیس شوند.

### ۱.۹. نمایش تعداد سررسید اشتباه روی کارت‌های `SM-2` و `FSRS` در صفحه تنظیمات مرور (`ReviewSetupScreen`)
- **فایل و خط:** `ui/screens/ReviewSetupScreen.kt:275, 315, 326`
- **شرح خطا:** در `ReviewSetupScreen` برای هر دو کارت `ReviewType.SM2` (`خط 315`) و `ReviewType.FSRS` (`خط 326`) مقدار `dueCount = dueRandom` نمایش داده می‌شود که از `stats?.dueTodayCount` (`LearningDao.getDueCountFlow`) می‌آید. این کوئری شرط `stage != 'LEARNED'` دارد، در حالی که صف واقعی `SM2` و `FSRS` (`LearningDao.kt:194, 238`) کارت‌های بالغ سررسیدشده (`stage = 'LEARNED' AND nextReviewDay <= today`) را نیز شامل می‌شود. بنابراین عدد نمایش‌داده‌شده روی کارت‌های SM-2 و FSRS با شمارنده واقعی کاندیداها همخوانی ندارد.

### ۱.۱۰. نمایش اشتباه «حافظه بلندمدت دائمی» برای کارت‌های بالغ SM-2 و FSRS در صفحه جزئیات واژه (`WordDetailScreen`)
- **فایل و خط:** `ui/screens/WordDetailScreen.kt:367-377, 437-447`
- **شرح خطا:** در `WordDetailScreen` هرگاه `stage == Stage.LEARNED` باشد، بدون بررسی اینکه کارت دارای `nextReviewDay` از الگوریتم‌های SM-2 یا FSRS است یا خیر، عبارت «حافظه بلندمدت دائمی» (`word_due_permanent_memory`) نمایش داده می‌شود و تاریخ واقعی مرور بعدی (`nextReviewDay`) مخفی می‌ماند.

---

## بخش ۲: مشکلات الگوریتم درجه سختی کلمات (`DifficultyCalculator`)

### ۲.۱. صفر شدن پیشرفت پاسخ‌های صحیح متوالی هنگام زدن دکمه «سخت» (`HARD`) روی کلمات دارای درجه سختی `HARD`
- **فایل و خط:** `domain/algorithm/DifficultyCalculator.kt:119-125`
- **شرح خطا:** طبق منطق کالیبراسیون دکمه `HARD` (یادآوری موفق با تقلا)، برای کلمات `HARD` و `VERY_HARD` سطح دشواری نباید بدتر شود. اما در `DifficultyCalculator.kt:120-124` برای حالت `VocabularyDifficulty.HARD`، مقدار `consecutiveCorrect = 0` ست می‌شود:
  ```kotlin
  VocabularyDifficulty.HARD -> DifficultyCalculationResult(
      newDifficulty = VocabularyDifficulty.HARD,
      consecutiveCorrect = 0,
      consecutiveWrong = currentWrong
  )
  ```
  اگر کاربر قبلاً روی یک واژه `HARD` یک یا دو بار پاسخ صحیح داده باشد (`consecutiveCorrect = 2` از ۳ پاسخ لازم برای آسان‌تر شدن به `MEDIUM`) و در مرور بعدی دکمه `HARD` (یادآوری موفق) را بزند، تمام پیشرفت `consecutiveCorrect` او صفر می‌شود (در حالی که در همین متد برای `VERY_HARD` در خط ۱۲۹ مقدار `consecutiveCorrect = currentCorrect` حفظ شده است).

---

## بخش ۳: مشکلات موتور تولید کوییز، گزینه‌های غلط و سطوح مبتدی / نرمال / حرفه‌ای

### ۳.۱. نادیده گرفته شدن کامل نوع مدخل (`entryType`: کلمه / عبارت / جمله) در محاسبه شباهت گزینه‌های غلط
- **فایل و خط:** `domain/algorithm/QuizDistractorScorer.kt:289-303` و `data/repository/ReviewRepository.kt:412-417`
- **شرح خطا:**
  - طبق سند مشخصات (`Yadin-Specification1-1.md` بخش ۰.۲ مورد ۷، تصمیم D24 و بخش ۶.۶)، فرمول امتیازدهی گزینه‌های غلط باید بر اساس `entryType` (هم‌نوع بودن تک‌کلمه با تک‌کلمه، عبارت با عبارت، جمله با جمله) و بدون دخالت `VocabularyDifficulty` باشد (`0.55 * lexSim + 0.25 * catShare + 0.20 * typeMatch`).
  - اما در `QuizDistractorScorer.calculateConfusability` (`خط 289-303`) پارامتر `entryType` اصلاً وجود ندارد و به جای آن هنوز از `targetDifficulty == candidateDifficulty` با وزن `0.25` استفاده می‌شود!
  - نتیجه مستقیم: در کوییز، برای یک «تک‌کلمه» ممکن است یک «جمله کامل» به عنوان گزینه غلط انتخاب شود که کاربر به محض دیدن طول گزینه‌ها بدون دانستن معنی می‌تواند گزینه غلط را تشخیص دهد.

### ۳.۲. باگ ریاضی در انتخاب گزینه‌های سطح «عادی» (`QuizLevel.NORMAL`) که باعث انتخاب بی‌ربط‌ترین کلمات هم‌دسته می‌شود
- **فایل و خط:** `data/repository/ReviewRepository.kt:421` و `domain/algorithm/QuizDistractorScorer.kt:289-316`
- **شرح خطا:**
  - در سطح `QuizLevel.NORMAL`، گزینه‌ها بر اساس کمترین فاصله با `0.5` (`abs(it.confusabilityScore - 0.5)`) مرتب می‌شوند.
  - از طرفی در `calculateConfusability`، اگر کاندیدا هم‌دسته (`categoryMatch = 0.35`) و هم‌سختی (`difficultyBonus = 0.25`) باشد، حداقل امتیاز پایه آن بدون هیچ شباهت متنی (`lexicalSim = 0.0`) برابر با `0.35 + 0.25 = 0.60` است.
  - هرچه شباهت ظاهری (`lexicalSim`) بیشتر شود، امتیاز از `0.60` به سمت `1.0` می‌رود و فاصله‌اش از `0.5` بیشتر می‌شود! بنابراین `abs(score - 0.5)` در میان کلمات هم‌دسته و هم‌سختی، **دقیقاً کلماتی را در صدر قرار می‌دهد که شباهت متنی آن‌ها صفر (`lexicalSim = 0.0`) است** و کلمات دارای شباهت متوسط را به انتهای لیست می‌فرستد.

### ۳.۳. نادیده گرفتن جدول دسته‌بندی‌های چندگانه (`concept_categories`) در موتور تولید کوییز
- **فایل و خط:** `data/repository/ReviewRepository.kt:306, 331, 389, 411`
- **شرح خطا:** موتور کوییز فقط فیلد قدیمی `concept.categoryId` را می‌خواند و جدول رابط `concept_categories` را در نظر نمی‌گیرد. در نتیجه اگر مفهومی عضو چند دسته‌بندی باشد، اشتراک دسته‌بندی (`sameCategory`) برای دسته‌های دوم و سوم آن تشخیص داده نمی‌شود.

### ۳.۴. تکرار گزینه‌های غلط یکسان در سؤالات متوالی یک جلسه آزمون (عدم پیاده‌سازی تنوع بین‌سؤالی)
- **فایل و خط:** `data/repository/ReviewRepository.kt:364-465`
- **شرح خطا:** در حلقه تولید سؤالات آزمون (`for (item in sessionItems)`)، هیچ مجموعه‌ای برای ثبت گزینه‌های غلط استفاده‌شده در سؤالات قبلی (`usedDistractorKeys`) نگهداری نمی‌شود. اگر چند واژه از یک دسته‌بندی کوچک در آزمون باشند، دقیقاً همان ۳ گزینه غلط یکسان در چندین سؤال پشت سر هم تکرار می‌شوند.

### ۳.۵. وجود متد مرده و تکراری `selectDistractorTexts` در `QuizDistractorScorer`
- **فایل و خط:** `domain/algorithm/QuizDistractorScorer.kt:307-323` و `data/repository/ReviewRepository.kt:419-423`
- **شرح خطا:** متد `QuizDistractorScorer.selectDistractorTexts` نوشته شده اما در هیچ کجای پروژه فراخوانی نمی‌شود و همان منطق مرتب‌سازی دوباره به صورت دستی داخل `ReviewRepository.kt:419-423` کپی شده است.

---

## بخش ۴: مشکلات الگوریتم‌های مرور (لایتنر یادین، SuperMemo-2 و FSRS)

### ۴.۱. یکسان بودن فاصله زمانی دکمه «سخت» (`HARD`) و «خوب» (`GOOD`) در مرور اول الگوریتم `SM-2`
- **فایل و خط:** `domain/algorithm/Sm2Algorithm.kt:94-110`
- **شرح خطا:** در `Sm2Algorithm.calculateNext` وقتی کارتی برای اولین بار مرور می‌شود (`currentReps == 0`):
  - برای `FlashcardRating.HARD`: `nextInterval = max(1, (0 * 1.2).roundToInt())` ⬅ **`1 روز`**
  - برای `FlashcardRating.GOOD`: `if (currentReps == 0) 1` ⬅ **`1 روز`**
  - در نتیجه در اولین مرور کارت در حالت SM-2، روی هر دو دکمه «سخت» و «خوب» عدد یکسان `1 روز` نمایش داده می‌شود و هر دو فاصله ۱ روزه تولید می‌کنند.

### ۴.۲. امکان تولید فاصله زمانی یکسان برای دکمه «سخت» (`HARD`) و «خوب» (`GOOD`) در الگوریتم `FSRS`
- **فایل و خط:** `domain/algorithm/FsrsAlgorithm.kt:226-237`
- **شرح خطا:** در `FsrsAlgorithm.previewIntervals` و زمان‌بندی کارت‌های در حال مرور:
  - در خط ۲۳۶، برای تضمین بزرگ‌تر بودن فاصله `EASY` از `GOOD` از `max(goodInterval + 1, ...)` استفاده شده است؛
  - اما در خط ۲۳۲ برای تضمین فاصله `GOOD` نسبت به `HARD` از `max(hardInterval, ...)` (بدون `+ 1`) استفاده شده است.
  - در نتیجه برای کارت‌هایی با پایداری پایین (مثلاً `stability` حدود ۱ تا ۲ روز)، هر دو دکمه `HARD` و `GOOD` فاصله یکسان (مثلاً هر دو `2 روز`) نمایش می‌دهند و ثبت می‌کنند.

### ۴.۳. ناهماهنگی بین `intervalDays` محدودشده (Clamped) و `stability` ذخیره‌شده در `FSRS`
- **فایل و خط:** `domain/algorithm/FsrsAlgorithm.kt:215-255`
- **شرح خطا:** در متد `FsrsAlgorithm.review`، پس از محاسبه `nextState` (که حاوی `newStability` است)، مقدار `intervalDays` با استفاده از `hardInterval`، `goodInterval` یا `easyInterval` تغییر می‌کند یا سقف/کف می‌خورد (مثلاً `max(goodInterval + 1, rawEasy)` برای `EASY`)، اما مقدار `nextState.stability` متناسب با فاصله نهایی اصلاح نمی‌شود. در مرور بعدی، الگوریتم FSRS پایداری (`stability`) قدیمی را ملاک قرار می‌دهد، نه فاصله‌ای که واقعاً به کارت داده شده بود.

### ۴.۴. به‌روز نشدن مقادیر `sm2` و `fsrs` روی کارت‌های داخل حافظه جلسه در `ReviewViewModel`
- **فایل و خط:** `ui/viewmodel/ReviewViewModel.kt:258-263` و `data/repository/ReviewRepository.kt:55-64`
- **شرح خطا:** پس از ثبت پاسخ فلش‌کارت (`submitFlashcardAnswer`)، شیء `SubmitResult` فقط `newStage`، `newDifficulty` و شمارنده‌ها را برمی‌گرداند و مقادیر جدید `sm2Repetitions`، `sm2EasinessFactor`، `sm2IntervalDays`، `fsrsStability`، `fsrsDifficulty`، `fsrsReps` و `fsrsLapses` را برنمی‌گرداند و در `_cards` داخل `ReviewViewModel` به‌روزرسانی نمی‌کند. اگر همان کارت در طول جلسه دوباره نمایش داده شود، پیش‌نمایش فاصله دکمه‌های ۴گانه (`previewIntervals`) با مقادیر قدیمی اولیه محاسبه می‌شود.

### ۴.۵. شمارش نشدن کارت‌های بالغ سررسیدشده (`SM-2` و `FSRS`) در شمارنده سررسید امروز صفحه خانه
- **فایل و خط:** `data/local/dao/LearningDao.kt:109, 164` در مقایسه با `194, 238`
- **شرح خطا:** کارت‌هایی که در الگوریتم‌های SM-2 یا FSRS فاصله `>= 60` روز می‌گیرند، دارای `stage = 'LEARNED'` و یک `nextReviewDay` مشخص هستند. وقتی تاریخ `nextReviewDay` آن‌ها فرا می‌رسد، کوئری‌های کاندیدای SM-2 و FSRS (`LearningDao.kt:194, 238`) آن‌ها را واجد مرور می‌دانند، اما کوئری `getDueCountFlow` (`LearningDao.kt:109, 164`) شرط `ls.stage != 'LEARNED'` دارد و آن‌ها را در عدد «کلمات سررسید امروز» در صفحه خانه و آمار کلی لحاظ نمی‌کند.

---

## بخش ۵: مشکلات بخش آمار، پیشرفت و دستاوردها (`Statistics`, `LearningDao`, `ReviewSessionDao`)

### ۵.۱. لحاظ شدن واژگان حذف‌شده (`active = 0`) و نادیده گرفتن جهت زبان (`direction`) در کوئری‌های تاریخچه و نمودار مرور
- **فایل و خط:** `data/local/dao/ReviewSessionDao.kt:77-116, 160-161` و `ui/viewmodel/StatisticsViewModel.kt:85-99`
- **شرح خطا:** کوئری‌های زیر روی جدول `review_history` هیچ `JOIN` با جدول `concepts` برای بررسی `c.active = 1` ندارند و پارامتر `direction` را هم فیلتر نمی‌کنند:
  - `getTotalReviewsCountFlow` (`خط 77`)
  - `getTotalCorrectReviewsCountFlow` (`خط 81`)
  - `getReviewCountForDayFlow` (`خط 85`)
  - `getDistinctReviewedDays` (`خط 95`)
  - `getRecentDailyStats` (`خط 100`)
  - `getRecentDailyStatsFlow` (`خط 109`)
  - `getRecentReviewTimestamps` (`خط 160`)
  در نتیجه:
  1. با تغییر جهت زبان برنامه (`اسپانیایی به فارسی` ⬅ `فارسی به اسپانیایی`)، بخش بالای صفحه پیشرفت (مراحل لایتنر، تسلط موضوعی و...) تغییر می‌کند اما نمودار فعالیت روزانه، عادات مطالعه و کل مرورها تغییر نمی‌کند و آمار هر دو جهت را با هم قاطی می‌کند.
  2. حتی پس از حذف واژگان از کتابخانه (`active = 0`)، تاریخچه مرور آن‌ها همچنان در آمار کل مرورها و نمودار باقی می‌ماند.

### ۵.۲. اجرای ۲۹ کوئری دیتابیس پس از ثبت هر تک‌کارت در `checkAchievements` و وجود کوئری‌های کاملاً زاید
- **فایل و خط:** `data/repository/ReviewRepository.kt:759, 764-879`
- **شرح خطا:**
  1. متد `checkAchievements(direction)` در پایان `submitAnswer` (`خط 759`) برای **هر تک‌کارت** در جلسه مرور یا آزمون فراخوانی می‌شود (به‌جای فراخوانی در پایان جلسه یا به صورت غیرمسدودکننده) و در هر بار فراخوانی تا **۲۹ کوئری SQL** اجرا می‌کند که موجب کندی محسوس بین کارت‌ها می‌شود.
  2. در خطوط `767-796` دو بار کل کوئری‌ها اجرا می‌شوند (یک بار با فیلتر `direction` و یک بار بدون فیلتر `All`) و سپس `maxOf(filtered, unfiltered)` گرفته می‌شود! از نظر ریاضی و منطقی، تعداد کل بدون فیلتر (`All`) همیشه بزرگ‌تر یا مساوی تعداد با فیلتر (`direction`) است؛ بنابراین اجرای ۶ کوئری اول با فیلتر `direction` کاملاً بی‌اثر و اضافه کاری محض است.

### ۵.۳. اختلاف عدد «امتیاز پیشرفت» در صفحه خانه با «نمره ماندگاری» در صفحه آمار به دلیل تفاوت گرد کردن و برش اعشار
- **فایل و خط:** `data/local/dao/LearningDao.kt:289` و `data/repository/VocabularyRepository.kt:1032`
- **شرح خطا:** هر دو متد از فرمول وزنی یکسان (`DAILY=20, WEEKLY=50, MONTHLY=80, LEARNED=100`) استفاده می‌کنند، اما:
  - در `LearningDao.kt:289` از `(round(rawProgressScore * 10.0) / 10.0)` استفاده شده است؛
  - در `VocabularyRepository.kt:1032` از `.toInt()` (برش مستقیم اعشار بدون گرد کردن، مثلاً `69.9` تبدیل به `69` می‌شود) استفاده شده است.
  این کار باعث می‌شود درصد نمایش‌داده‌شده در صفحه خانه و صفحه پیشرفت برای یک وضعیت واحد با هم اختلاف داشته باشند.

### ۵.۴. خطای کوئری پیش‌بینی مرورهای آینده (`getScheduledReviewsByDay`) در نادیده گرفتن کارت‌های عقب‌افتاده و لحاظ کردن کارت‌های `LEARNED` لایتنر
- **فایل و خط:** `data/local/dao/LearningDao.kt:385-398` و `data/repository/VocabularyRepository.kt:1058-1081`
- **شرح خطا:**
  1. کوئری `getScheduledReviewsByDay` شرط `ls.stage != 'LEARNED'` ندارد؛ اگر کارتی به مرحله `LEARNED` رسیده باشد ولی فیلد `nextReviewDay` آن در بازه `tomorrow` تا `endDay` باشد، در پیش‌بینی لایتنر شمارش می‌شود.
  2. در `VocabularyRepository.getUpcomingForecast`، فقط روزهای `offset = 1..7` (از فردا به بعد) جمع زده می‌شوند و کارت‌هایی که موعد آن‌ها امروز یا روزهای گذشته بوده (`nextReviewDay <= today`) اما هنوز مرور نشده‌اند، در بار مرور فردا و ۳ روز آینده محاسبه نمی‌شوند.

---

## بخش ۶: دوباره‌کاری‌ها، کدهای مرده و مشکلات معماری و برنامه‌نویسی

### ۶.۱. بازخوانی سنگین و تکراری تمام دسته‌بندی‌ها و شمارش کلمات در یک حلقه N+1 پس از هر تغییر کوچک
- **فایل و خط:** `data/repository/VocabularyRepository.kt:956-983` و `ui/viewmodel/StatisticsViewModel.kt:102-119`
- **شرح خطا:** در `VocabularyRepository.getCategoryMastery` به ازای هر دسته‌بندی در حلقه `for (cat in categories)`، دو کوئری جداگانه (`countConceptsInCategory` و `countLearnedConceptsInCategory`) اجرا می‌شود (الگوی N+1 Query). این متد در `StatisticsViewModel` داخل `combine(vocabularyRepo.getCategoriesFlow(), reviewRepo.getOverallStatsFlow(dir))` قرار دارد؛ یعنی با هر پاسخ به هر کارت در جلسه مرور، به تعداد کل دسته‌بندی‌ها × ۲ کوئری SQL به صورت زنجیره‌ای اجرا می‌شود.

### ۶.۲. استفاده از `java.util.Locale.getDefault()` در فرمت تاریخ بکاپ که در زبان فارسی ارقام تاریخ فایل را بههم می‌ریزد
- **فایل و خط:** `data/repository/BackupRepository.kt:238`
- **شرح خطا:** در `SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())`، اگر زبان دستگاه فارسی باشد، ارقام تاریخ در متادیتای JSON بکاپ با ارقام فارسی/عربی تولید می‌شوند که با فرمت استاندارد ISO در متادیتای بکاپ ناسازگار است.

### ۶.۳. عدم وجود محدودیت تراکنش یا بررسی خطا در `ImportScreen` هنگام ساخت دسته‌بندی جدید
- **فایل و خط:** `ui/screens/ImportScreen.kt:95` و `ui/viewmodel/LibraryViewModel.kt:241-251`
- **شرح خطا:** در `ImportScreen.kt:95` وقتی کاربر از داخل دراپ‌داون دسته‌بندی یک دسته‌بندی جدید می‌سازد، `onAddNewCategory = { name -> viewModel.addCategory(name) }` فراخوانی می‌شود اما دسته‌بندی ساخته‌شده به صورت خودکار به عنوان `selectedCategoryId` در صفحه درون‌ریزی انتخاب نمی‌شود (برخلاف متن دکمه دیالوگ که می‌گوید «ایجاد و انتخاب»). همین مشکل در `ReviewSetupScreen.kt:212` و `LibraryScreen.kt:412` نیز وجود دارد.

---

## بخش ۷: متن‌های هاردکدشده خارج از فایل‌های دوزبانه (`strings.xml`)

طبق الزام معماری دوزبانه، تمام متن‌های قابل نمایش به کاربر باید در `values/strings.xml`، `values-fa/strings.xml` و `values-en/strings.xml` باشند. در فایل‌های زیر متن‌های فارسی به صورت هاردکد داخل کد Kotlin نوشته شده‌اند و با تغییر زبان برنامه به انگلیسی، همچنان به زبان فارسی نمایش داده می‌شوند:

### ۷.۱. متن‌های هاردکدشده در لایه UI (`MainActivity.kt` و `LibraryScreen.kt`)
1. **`MainActivity.kt:524`:**
   - `"پیشنهاد: $it"` (در اسنک‌بار خطای اعتبارسنجی صفحه افزودن/ویرایش واژه).
2. **`ui/screens/LibraryScreen.kt:1321, 1323`:**
   - `"فرم/معنی دیگر: $w ($tr)"` و `"فرم جایگزین: $w"` (هنگام انتقال بخش دوم به یادداشت در بازبینی هوشمند کتابخانه).
3. **`ui/screens/LibraryScreen.kt:1390`:**
   - کاراکتر درصد فارسی هاردکدشده `"٪"` در `"${(item.confidence * 100).toInt().formatDigits()}٪"`.

### ۷.۲. متن‌های هاردکدشده در `VocabularyRepository.kt` (پیام‌های خطا و گزارش بازبینی هوشمند)
- **خطوط `438, 442, 472, 494, 600, 647, 651`:**
  - `"متن واژه نمی‌تواند خالی باشد"`
  - `"حداقل یک معنی برای واژه وارد کنید"`
  - `"این واژه قبلاً در کتابخانه ثبت شده است"`
  - `"خطا در ثبت واژه"`
  - `"واژه مورد نظر یافت نشد"`
- **خطوط `814, 822, 829`:**
  - `"ادغام خودکار با واژه هم‌معنی و انتقال فرم جایگزین به یادداشت"`
  - `"اصلاح و پاک‌سازی بر اساس تصمیم کاربر در بازبینی دستی"`
  - `"پاک‌سازی خودکار ساختار واژه و انتقال توضیحات به یادداشت"`

### ۷.۳. متن‌های هاردکدشده در `BackupRepository.kt` (پیام‌های وضعیت بکاپ، بازیابی و خروجی‌ها)
- **خطوط `157, 182, 197, 243, 269, 287`:**
  - `"فایل پشتیبان با موفقیت در حافظه برنامه ذخیره شد"`، `"خطا در ایجاد فایل پشتیبان"`، `"فایل CSV با موفقیت ایجاد شد"`، `"خطا در ایجاد فایل CSV"`، `"فایل پشتیبان انتخاب‌شده معتبر نیست یا آسیب دیده است."`، `"خطا در خواندن فایل پشتیبان"`
- **خطوط `351, 365, 380, 422, 429, 525`:**
  - `"مسیر ذخیره فایل باز نشد"`، `"فایل پشتیبان با موفقیت در مسیر انتخابی ذخیره شد"`، `"خطا در ذخیره فایل"`، `"فایل انتخاب‌شده خالی یا غیرقابل خواندن است"`، `"ساختار فایل پشتیبان نامعتبر است"`، `"بازیابی با موفقیت انجام شد..."`
- **خطوط `836, 845, 879, 899, 919, 928, 968, 987, 1008, 1032, 1161, 1428, 1494-1495`:**
  - تمام عناوین ستون‌های خروجی Excel/CSV/Word/PDF/HTML، متن‌های گزارش خروجی و پیام‌های خطای خروجی‌گیری به صورت فارسی هاردکد شده‌اند.

### ۷.۴. متن‌های هاردکدشده در موتورهای پردازش متن و درون‌ریزی (`VocabularyParser`, `FileReaders`, `FieldValidationEngine`, `VocabularyAuditService`, `SeedImporter`)
1. **`domain/importing/VocabularyParser.kt:96, 183, 257, 387, 403`:**
   - `"هر دو بخش اسپانیایی هستند"`، `"هر دو بخش فارسی هستند"`، `"نیاز به بررسی دستی ساختار و معنی"`، `"ترجمه فارسی تشخیص داده نشد"`، `"ساختار خط نامشخص است (جداکننده یا ترجمه یافت نشد)"`.
2. **`domain/importing/FileReaders.kt:44, 153, 297, 314, 324, 384, 426, 445`:**
   - تمام پیام‌های خطای خواندن فایل‌های TXT, CSV, XLSX, DOCX, PDF و JSON (مانند `"فرمت فایل پشتیبانی نمی‌شود"`، `"فایل انتخاب شده خالی است"` و...).
3. **`domain/validation/FieldValidationEngine.kt:94, 104, 111, 128, 137, 156, 164, 179, 206, 214, 223, 241, 250, 267, 275, 295`:**
   - تمام پیام‌های خطای اعتبارسنجی فیلدهای اسپانیایی و فارسی که در دیالوگ‌ها و اسنک‌بارها به کاربر نمایش داده می‌شوند هاردکد فارسی هستند.
4. **`domain/audit/VocabularyAuditService.kt:129-396`:**
   - تمام دلایل بازبینی (`reason`) و توضیحات عملیات (`description`) که در دیالوگ بازبینی هوشمند کتابخانه (`LibraryAuditDialog`) روی صفحه نمایش داده می‌شوند به صورت رشته‌های فارسی هاردکد شده‌اند.
5. **`data/seed/SeedImporter.kt:37-47`:**
   - نام دسته‌بندی‌های پیش‌فرض (`"افعال پرکاربرد"`، `"عبارات روزمره"` و...) هنگام ساخت اولیه دیتابیس به صورت فارسی هاردکد شده‌اند.

---

## بخش ۸: نقض سیستم طراحی و مقادیر هاردکدشده خارج از تم (`ThemeTokens`)

### ۸.۱. نادیده گرفتن توکن `dimensions.navHeight` در نوار ناوبری پایین صفحه (`MainActivity.kt`)
- **فایل و خط:** `MainActivity.kt:187` در مقایسه با `ui/theme/ThemeTokens.kt:71` و `ui/theme/GeminiTheme.kt:85`
- **شرح خطا:** در `ThemeTokens.kt` و `GeminiTheme.kt` متغیر `navHeight = 76.dp` تعریف شده است، اما در `MainActivity.kt:187` برای `NavigationBar` به صورت هاردکد `Modifier.height(64.dp)` نوشته شده و توکن `dimensions.navHeight` کاملاً نادیده گرفته شده است.

### ۸.۲. نادیده گرفتن توکن‌های `LocalYadinShapes` و `LocalYadinDimensions` در کامپوننت‌های مشترک و صفحات
- **فایل و خط:**
  - `ui/components/CommonComponents.kt:219, 229, 305, 312, 318, 384, 392, 397, 424, 432, 437, 488, 494, 524, 592`
  - `ui/components/CategoryDropdown.kt:197, 206, 224, 231, 242, 455, 464`
  - `ui/screens/LibraryScreen.kt:350, 527, 560, 775, 1586`
  - `ui/screens/BackupScreen.kt:145, 207, 262, 578, 957`
- **شرح خطا:**
  1. در `ThemeTokens.kt:89-95` و `GeminiTheme.kt:99-105` شکل‌های استاندارد `LocalYadinShapes` (`small = 10.dp`, `medium = 14.dp`, `large = 16.dp`, `pill = 50`, `badge = 6.dp`) تعریف شده‌اند، اما در ده‌ها نقطه از `CommonComponents.kt`، `CategoryDropdown.kt`، `LibraryScreen.kt` و `BackupScreen.kt` مستقیماً از `RoundedCornerShape(10.dp)`، `RoundedCornerShape(12.dp)` و `RoundedCornerShape(8.dp)` به صورت هاردکد استفاده شده است.
  2. در `ThemeTokens.kt:69-85` متغیرهای `iconSizeSmall`، `iconSizeMedium`، `iconSizeLarge`، `buttonHeight` و `chipHeight` تعریف شده‌اند، اما در `CommonComponents.kt` (`خط 587: minHeight = 38.dp`، `خط 524: size(38.dp)`) و سایر صفحات از اعداد هاردکد `dp` و `sp` به جای توکن‌های `LocalYadinDimensions` استفاده شده است.

---

## بخش ۹: خلاصه وضعیت رفع مشکلات (Resolution Status)

تمامی موارد گزارش‌شده در بخش‌های ۱ تا ۸ به صورت کامل در کدهای برنامه برطرف شدند:

- **بخش ۱ (رابط کاربری، ژست‌های حرکتی و تنظیمات):**
  - **۱.۱:** کشیدن افقی فلش‌کارت (`detectHorizontalDragGestures`) در `FlashcardScreen.kt` مقید به `isFlipped == true` شد تا پیش از دیدن پاسخ، کارت ثبت نشود.
  - **۱.۲:** کال‌بک `onError` به `saveWord` در `LibraryViewModel.kt` و `AddEditWordScreen.kt` اضافه شد تا در صورت بروز خطا، `isSaving = false` شده و متن خطا نمایش داده شود.
  - **۱.۳:** در حالت `CardDirection.REVERSE` از خرد شدن عبارت اسپانیایی با کاما جلوگیری شد و نگاشت زبان مبدا/مقصد به درستی حفظ گردید.
  - **۱.۴:** امکان حذف تمام دسته‌بندی‌های یک واژه و بازخوانی فوری `_selectedWordDetail` پس از ذخیره در `LibraryViewModel.kt` و `AddEditWordScreen.kt` فراهم شد.
  - **۱.۵:** متدهای `selectWordDetail` و `deleteWord` در `MainViewModel.kt` با پشته ناوبری (`navigateTo` / `navigateBack`) همگام شدند.
  - **۱.۶:** کلیک صوری روی کارت تک‌تم در `SettingsScreen.kt` حذف شد.
  - **۱.۷:** کنترل‌های تنظیمات صوتی (`ttsEnabled`, `ttsAutoPlay`, `ttsSpeechRate`) و ترجیحات مرور/آزمون (`showCategoryInReview`, `defaultReviewMode`, `defaultQuizLevel`, `quizAutoAdvanceSeconds`) به `SettingsScreen.kt` اضافه و در `FlashcardScreen.kt`، `QuizScreen.kt` و `SpeakButton` متصل شدند.
  - **۱.۸:** در `ImportReviewScreen.kt` نمونه اصلی `ParsedEntry` در `ReviewItem` نگهداری شد تا هنگام تأیید تکی یا گروهی، فیلدهای `entryType`، `categoryNames`، `variants`، `breakdowns` و `relations` حفظ شوند.
  - **۱.۹:** در `ReviewSetupScreen.kt` شمارنده سررسید کارت تصادفی (`dueRandom`) از شمارنده سررسید الگوریتمی (`dueAlgorithm` شامل کارت‌های بالغ SM-2/FSRS) تفکیک شد و قابلیت افزودن و انتخاب خودکار دسته‌بندی جدید متصل گردید.
  - **۱.۱۰:** در `WordDetailScreen.kt` برای کارت‌های `LEARNED` که دارای سابقه مرور الگوریتمی (`sm2Repetitions > 0 || fsrsReps > 0`) هستند، تاریخ واقعی مرور بعدی نمایش داده می‌شود.

- **بخش ۲ (الگوریتم درجه سختی کلمات):**
  - **۲.۱:** در `DifficultyCalculator.updateDifficultyForHardRating` برای واژگان با درجه سختی `HARD`، مقدار `consecutiveCorrect` حفظ شد (`consecutiveCorrect = currentCorrect`).

- **بخش ۳ (موتور تولید کوییز و گزینه‌های غلط):**
  - **۳.۱ تا ۳.۵:** تابع `entryTypeSimilarity` به `QuizDistractorScorer.kt` اضافه شد، فرمول `calculateConfusability` بر اساس وزن‌های استاندارد (`0.55 * lexSim + 0.25 * catMatch + 0.20 * typeMatch`) اصلاح گردید، دسته‌بندی‌های چندگانه (`concept_categories`) در تولید کوییز لحاظ شدند، رتبه‌بندی سطوح `BEGINNER` / `NORMAL` / `ADVANCED` در `rankCandidatesByLevel` یکپارچه شد و جریمه تکرار گزینه‌های غلط بین سؤالات متوالی (`usedDistractorCounts`) اعمال گردید.

- **بخش ۴ (الگوریتم‌های مرور لایتنر، SM-2 و FSRS):**
  - **۴.۱:** در `Sm2Algorithm.kt` فاصله مرور اول برای `HARD = 1`، `GOOD = 2` و `EASY = 4` تفکیک شد و یکنواختی اکید (`hardInt < goodInt < easyInt`) تضمین گردید.
  - **۴.۲ و ۴.۳:** در `FsrsAlgorithm.kt` فاصله `goodInterval` حداقل یک روز بیشتر از `hardInterval` (`max(hardInterval + 1, ...)`) تنظیم شد و در صورت افزایش فاصله بر اثر کف/سقف، مقدار `nextState.stability` همگام‌سازی گردید.
  - **۴.۴:** فیلدهای جدید SM-2 و FSRS در `SubmitResult` بازگردانده شده و در `_sessionCards` داخل `ReviewViewModel.kt` به‌روزرسانی می‌شوند.
  - **۴.۵:** کوئری `getDueCountFlow` در `LearningDao.kt` برای شمارش کارت‌های بالغ سررسیدشده اصلاح شد و `getRandomDueCountFlow` اضافه گردید.

- **بخش ۵ و ۶ (آمار، پیشرفت، دیتابیس و بکاپ):**
  - **۵.۱:** تمامی کوئری‌های تاریخچه و نمودار در `ReviewSessionDao.kt` با شرط `c.active = 1` و فیلتر `direction` اصلاح شده و در `StatisticsViewModel.kt` به جهت فعال متصل شدند.
  - **۵.۲:** ۶ کوئری زاید در `checkAchievements` حذف شد و از نوشتن تکراری دستاوردهای بدون تغییر جلوگیری گردید.
  - **۵.۳ و ۵.۴:** گرد کردن امتیاز ماندگاری با `roundToInt()` یکسان شد، کارت‌های عقب‌افتاده (`getOverduePracticedCount`) به پیش‌بینی مرور اضافه شدند و کارت‌های `LEARNED` غیرالگوریتمی از `getScheduledReviewsByDay` خارج شدند.
  - **۶.۱ تا ۶.۳:** کوئری تسلط موضوعی (`getCategoryMasteryRaw`) با جدول `concept_categories` یکپارچه شد و فیلدهای SM-2/FSRS در بازیابی خودکار `SeedImporter.kt` حفظ شدند.

- **بخش ۷ و ۸ (دوزبانه بودن کامل و توکن‌های طراحی تم):**
  - تمامی رشته‌های هاردکدشده در `MainActivity.kt`، `LibraryScreen.kt`، `VocabularyRepository.kt`، `BackupRepository.kt`، `VocabularyAuditService.kt`، `FieldValidationEngine.kt`، `FileReaders.kt` و `VocabularyParser.kt` به فایل‌های منابع دوزبانه (`values/strings.xml`, `values-fa/strings.xml`, `values-en/strings.xml`) منتقل شدند و توکن‌های تم (`dimensions.navHeight`, `LocalYadinShapes`) جایگزین مقادیر هاردکد شدند.

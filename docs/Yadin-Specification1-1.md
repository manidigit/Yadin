# یادین (Yadin) — مشخصات کامل و اجرایی برنامه

نسخه سند: 1.0 — تاریخ: 2026-10-04
مخزن: github.com/manidigit/Yadin

این سند تنها مرجع لازم برای ساخت برنامه از صفر است.
یک برنامه‌نویس یا یک هوش مصنوعی باید بتواند فقط با این سند و فایل بانک اولیه واژگان، برنامه را بنویسد.
هر جا سند چیزی را «قطعی» اعلام کرده، اجازه تغییر ندارد.
هر جا چیزی در سند نیامده، پیاده‌ساز نباید حدس بزند و باید از صاحب پروژه بپرسد.

---

## فهرست

0. نحوه استفاده و اولویت منابع
1. اصول محصول و ثبت تصمیم‌های قطعی
2. واژه‌نامه فارسی به کد
3. فناوری، ماژول‌ها و ساختار پروژه
4. مدل داده (جدول‌ها)
5. قراردادهای عمومی (زمان، شناسه، متن)
6. الگوریتم‌ها (۲۱ الگوریتم)
7. صفحه‌ها و رفتار رابط کاربری
8. تم و طراحی
9. تنظیمات
10. متن آموزش داخل برنامه
11. فهرست کامل تست‌ها
12. مراحل پیاده‌سازی و قواعد تحویل
13. موارد نیازمند تایید صاحب پروژه
14. پیوست‌ها

---

# 0. نحوه استفاده و اولویت منابع

## 0.1 اولویت منابع هنگام تعارض

۱. همین سند (بالاترین اولویت).
۲. سند مشخصات محصول یادین (فایل gpt.md) برای ناوردایی‌ها.
۳. کد زنده پروژه فلش‌لرن نسخه 6.84 برای رفتارهایی که این سند صریح نگفته است.
۴. اسناد قدیمی داخل پوشه docs مخزن (فقط به‌عنوان تاریخچه، نه مرجع).

## 0.2 اسناد قدیمی که نباید بر اساسشان کد نوشت

این موارد در اسناد قدیمی مخزن هست ولی با رفتار نهایی تعارض دارد و این سند آن‌ها را لغو کرده است:

۱. «برای رفتن از هفتگی به ماهانه سه پاسخ درست پیاپی لازم است». لغو شد. قاعده نهایی بخش 6.3 است.
۲. «پاسخ درست سختی را بالا می‌برد». لغو شد. پاسخ درست سختی را پایین می‌آورد.
۳. نشانه‌های شکست مسیر و شمارنده شکست ماهانه. حذف شدند.
۴. مجازات ویژه سختی هنگام پاسخ غلط هفتگی یا ماهانه. حذف شد.
۵. «آزمون از قفل مرور امروز معاف است». لغو شد. قفل هم‌روز برای همه مرورها یکسان است.
۶. «اگر گزینه غلط کم بود، تصادفی پر کن». لغو شد. باید به فلش‌کارت برگردد.
۷. انتخاب گزینه‌های غلط با توجه به سختی خود کلمه. لغو شد.
۸. پشتیبان رمزدار. لغو شد. پشتیبان بدون رمز است.
۹. جدول رگبار. حذف شد. رگبار از تاریخچه مرور محاسبه می‌شود.
۱۰. علاقه‌مندی (Favorite). وجود ندارد.

---

# 1. اصول محصول و ثبت تصمیم‌های قطعی

## 1.1 اصول

۱. فقط اندروید.
۲. اول آفلاین. هسته برنامه بدون اینترنت کامل کار می‌کند.
۳. هیچ هوش مصنوعی و هیچ سرویس ابری در محصول نیست.
۴. زبان رابط: فارسی و انگلیسی.
۵. جفت زبانی فعال در تنظیمات انتخاب‌پذیر است. جفت اولیه اسپانیایی به فارسی است.
۶. جهت مرور فقط یکی از دو حالت است: عادی یا برعکس.
۷. بانک اولیه حدود ۶۰۰۰ واژه آماده همراه ترجمه دارد.
۸. تلفظ فقط از موتور داخلی اندروید می‌آید.

## 1.2 سه سیستم مستقل

سه مفهوم زیر کاملاً از هم جدا هستند و هیچ‌کدام متغیر کنترلی دیگری نیست:

۱. مرحله یادگیری: روزانه، هفتگی، ماهانه، یادگرفته.
۲. سختی کلمه: آسان، متوسط، سخت، خیلی سخت. فقط از پاسخ‌های درست و غلط پیاپی به دست می‌آید.
۳. سطح آزمون: مبتدی، متوسط، حرفه‌ای. فقط روش انتخاب گزینه‌های غلط را تعیین می‌کند.

## 1.3 ثبت تصمیم‌ها

هر ردیف یک تصمیم قطعی است.
ستون «منبع» نشان می‌دهد تصمیم از کجا آمده است.

| کد | تصمیم | منبع |
|----|-------|------|
| D01 | پروژه از صفر ساخته می‌شود و روی کد فلش‌لرن ساخته نمی‌شود. اطلاعات لازم از فلش‌لرن برداشته می‌شود. | صاحب پروژه |
| D02 | پیشرفت یادگیری و سختی کلمه برای هر جهت مرور جداگانه نگهداری می‌شود. یعنی هر واژه دو «کارت» دارد: عادی و برعکس. | صاحب پروژه |
| D03 | شمارنده‌های سختی کلمه هم برای هر جهت جداست. | صاحب پروژه |
| D04 | جفت زبانی اولیه اسپانیایی به فارسی است. | صاحب پروژه |
| D05 | آموزش داخل برنامه را پیاده‌ساز می‌نویسد. متن آماده در بخش 10 هست. | صاحب پروژه |
| D06 | زمان دقیق مهم نیست. فقط تاریخ روز (با نیمه‌شب محلی دستگاه) مهم است. | صاحب پروژه |
| D07 | کلمه یادگرفته‌شده برای همیشه از چرخه خارج می‌شود و هرگز به روزانه برنمی‌گردد. | صاحب پروژه |
| D08 | همه کلمات در ابتدا آسان هستند. | صاحب پروژه |
| D09 | سطح آزمون: مبتدی، متوسط، حرفه‌ای. کاربر انتخاب می‌کند. | صاحب پروژه |
| D10 | پاسخ فلش‌کارت دو گزینه دارد: درست و غلط. | صاحب پروژه |
| D11 | هنگام افزودن (دستی، گروهی، بازیابی پشتیبان): اگر واژه تکراری باشد ولی بعضی ترجمه‌ها جدید باشند، فقط ترجمه‌های جدید اضافه می‌شود. | صاحب پروژه |
| D12 | طراحی صفحه‌ها و تم همان است که در اسناد و کد فلش‌لرن آمده. بخش 8 خلاصه آن است. | صاحب پروژه |
| D13 | بانک اولیه از فایل docs/Vocabulary.json مخزن می‌آید. | صاحب پروژه |
| D14 | هر واژه در مرور فلش‌کارت همه ترجمه‌هایش را نشان می‌دهد. | تصمیم‌های Grok |
| D15 | دکمه صدا فقط روی سمت سؤال است و در فلش‌کارت، آزمون و کتابخانه وجود دارد. | تصمیم‌های Grok |
| D16 | رگبار با یک تمرین (فلش‌کارت یا آزمون) در آن روز ثبت می‌شود. | تصمیم‌های Grok |
| D17 | پشتیبان بدون رمز است، نوار پیشرفت دارد و پیام موفقیت «با موفقیت انجام شد» است. | تصمیم‌های Grok |
| D18 | کلید یکتای متن: حذف فاصله ابتدا و انتها، حروف کوچک، یکی‌کردن فاصله‌های پشت‌سرهم، حفظ لهجه و علائم. | تصمیم‌های Grok |
| D19 | حذف واژه نرم است (غیرفعال می‌شود). در کتابخانه با علامت دیده می‌شود. در آمار شمرده نمی‌شود. فعال‌سازی مجدد دارد. | تصمیم‌های Grok |
| D20 | برای مرور از هر نوع، یک واژه که امروز (به وقت محلی) یک بار مرور شده، تا نیمه‌شب دوباره ظاهر نمی‌شود. | کد فلش‌لرن |
| D21 | با پاسخ درست، مرحله روزانه به هفتگی و هفتگی به ماهانه و ماهانه به یادگرفته می‌رود. در هر مرحله فقط یک پاسخ درست لازم است. | کد فلش‌لرن |
| D22 | فاصله مرور: هفتگی 7 روز و ماهانه 30 روز. از شروع روز محاسبه می‌شود. | کد فلش‌لرن |
| D23 | نشانه شکست مسیر و شمارنده شکست ماهانه در یادین وجود ندارد. | gpt.md |
| D24 | سطح آزمون به سختی کلمه وابسته نیست. فهرست گزینه‌های غلط از همه مفاهیم فعال ساخته می‌شود. | gpt.md |
| D25 | رابطه‌های واژگان، تنوع‌ها و فراداده پارسر جدول جدا ندارند. متن‌های تجزیه، مرتبط و تنوع داخل یادداشت واژه نگهداری می‌شوند. | تصمیم این سند |
| D26 | علاقه‌مندی و مثال (Example) در مدل نهایی وجود ندارند. | gpt.md |
| D27 | بانک اولیه فقط واژه‌های فعال را وارد می‌کند. ۳۴ واژه غیرفعال فایل نادیده گرفته می‌شود. | تصمیم این سند |
| D28 | یادداشت «واژهٔ نمونهٔ اولیه FlashLearn» هنگام وارد کردن بانک اولیه حذف می‌شود. | تصمیم این سند |
| D29 | دسته‌های فایل بانک اولیه به ۱۰ دسته رسمی یادین نگاشت می‌شوند (جدول پیوست 14.1). | تصمیم این سند |
| D30 | قابلیت راهنما (Hint) در نسخه اول وجود ندارد چون منبع داده ندارد. | تصمیم این سند |
| D31 | پاکسازی خودکار واژه‌های غیرفعال وجود ندارد. فقط دکمه دستی در تنظیمات هست. | تصمیم این سند |
| D32 | آمار صفحه پیشرفت براساس جهت انتخاب‌شده (عادی یا برعکس) محاسبه می‌شود. رگبار و دستاوردها سراسری‌اند. | تصمیم این سند |
| D33 | تم‌های شخصی‌ساز (وارد کردن تم از فایل) در نسخه اول نیست. | تصمیم این سند |
| D34 | شمارنده کل درست و کل غلط داخل جدول وضعیت یادگیری نگهداری نمی‌شود. آمار فقط از تاریخچه مرور محاسبه می‌شود. | gpt.md بخش آمار |

---

# 2. واژه‌نامه فارسی به کد

در متن فارسی از نام فارسی استفاده شده و در کد از نام زیر.

| فارسی | نام در کد |
|-------|-----------|
| مفهوم (واژه) | Concept |
| محتوا (متن یک زبان) | Content |
| دسته | Category |
| برچسب | Tag |
| کارت (یک واژه در یک جهت) | Card |
| وضعیت یادگیری | LearningState |
| وضعیت سختی | DifficultyState |
| مرحله | Stage |
| روزانه، هفتگی، ماهانه، یادگرفته | DAILY، WEEKLY، MONTHLY، LEARNED |
| سختی کلمه | VocabularyDifficulty |
| آسان، متوسط، سخت، خیلی سخت | EASY، MEDIUM، HARD، VERY_HARD |
| سطح آزمون | QuizLevel |
| مبتدی، متوسط، حرفه‌ای | EASY، MEDIUM، HARD |
| جهت عادی و برعکس | NORMAL، REVERSE |
| نوع مرور | ReviewType |
| روزانه، هفتگی، ماهانه، یادگرفته‌ها، تصادفی | DAILY، WEEKLY، MONTHLY، LEARNED، RANDOM |
| حالت مرور | ReviewMode |
| فلش‌کارت و آزمون | FLASHCARD، QUIZ |
| جلسه مرور | ReviewSession |
| تاریخچه مرور | ReviewHistory |
| گزینه غلط | Distractor |
| قفل هم‌روز | SameDayLock |
| آستانه سختی | difficultyThreshold |
| مورد بررسی ورود | ImportReviewItem |

---

# 3. فناوری، ماژول‌ها و ساختار پروژه

## 3.1 نسخه‌های قطعی ابزار

این نسخه‌ها در فلش‌لرن آزموده شده‌اند و تغییرشان فقط با تصمیم مکتوب صاحب پروژه مجاز است.

| مورد | نسخه |
|------|------|
| Kotlin | 1.9.20 |
| Android Gradle Plugin | 8.2.2 |
| Gradle | 8.2 |
| JDK | 17 |
| compileSdk و targetSdk | 34 |
| minSdk | 26 |
| Compose UI و Foundation | 1.5.4 |
| Compose Compiler | 1.5.4 |
| Material 3 | 1.2.1 |
| Material Icons Extended | 1.5.4 |
| Activity Compose | 1.8.0 |
| Lifecycle (runtime و viewmodel-compose) | 2.6.2 |
| Navigation Compose | 2.7.5 |
| Room (runtime، ktx، compiler، paging) | 2.6.1 |
| Paging | 3.2.1 |
| Hilt | 2.51.1 |
| Hilt Navigation Compose | 1.1.0 |
| Coroutines | 1.7.3 |
| JUnit | 4.13.2 |
| پردازشگر حاشیه‌نویسی | kapt (برای Room و Hilt) |
| شناسه برنامه و نام پکیج | com.manidigit.yadin |

نکته مهم: Gradle wrapper jar داخل مخزن گذاشته نمی‌شود.
ساخت فقط با GitHub Actions انجام می‌شود (محیط توسعه Termux است و ساخت محلی ندارد).

## 3.2 ماژول‌ها

سه ماژول گریدل:

۱. domain: کاتلین خالص (بدون اندروید). مدل‌ها، الگوریتم‌ها، موارد استفاده (UseCase)، رابط مخزن‌ها. تمام تست‌های واحد اصلی اینجاست.
۲. data: کتابخانه اندروید. Room، DAOها، پیاده‌سازی مخزن‌ها، ورود و خروج فایل، بانک اولیه، پارسر فایل‌ها (CSV، JSON، XLSX، SQLite).
۳. app: Compose، ViewModel، ناوبری، تم، Hilt، دارایی‌ها (assets).

قانون وابستگی: app به domain و data وابسته است. data به domain. domain به هیچ‌چیز وابسته نیست جز Coroutines و javax.inject.

## 3.3 ساختار پکیج‌ها

```text
domain/src/main/kotlin/com/manidigit/yadin/domain/
  model/            مدل‌ها و enumها
  text/             Normalizer، CanonicalKey، TextCleaner
  time/             Clock، DayMath
  algorithm/        LearningTransition، DifficultyCalculator، QuizBuilder، ...
  parser/           VocabularyParser و نشانگرها
  usecase/          هر مورد استفاده یک کلاس
  repository/       رابط‌های مخزن
  backup/           مدل پشتیبان و اعتبارسنج
data/src/main/kotlin/com/manidigit/yadin/data/
  db/               YadinDatabase، Entity، Dao، Converters، Migrations
  repository/       پیاده‌سازی‌ها
  io/               BackupFileIo، ImportReaders (csv/json/xlsx/sqlite)
  seed/             SeedImporter
app/src/main/kotlin/com/manidigit/yadin/
  YadinApp.kt، MainActivity.kt
  di/               ماژول‌های Hilt
  ui/theme/         تم
  ui/nav/           ناوبری
  ui/home، review، library، addword، importer، progress، settings، backup، help
```

## 3.4 ساختار مخزن

```text
Yadin/
  app/  domain/  data/
  docs/
    Yadin-Specification.md          (همین سند)
    Vocabulary.json                 (بانک اولیه، نسخه‌ای که در assets کپی می‌شود)
    PROGRESS_TRACKER.md  CHANGELOG.md  VERSION_LEDGER.md
  samples/
    import/sample.txt  sample.csv  sample.json  sample.xlsx
    backup/sample_backup.json
  tools/make_sample_xlsx.py
  .github/workflows/android-ci.yml
  README.md
```

نسخه بانک اولیه داخل برنامه: app/src/main/assets/seed/vocabulary_seed.json (کپی عینی docs/Vocabulary.json).

## 3.5 CI

فایل android-ci.yml باید در هر push و pull request این مراحل را اجرا کند:

۱. نصب JDK 17 و Gradle 8.2.
۲. نصب Android SDK پلتفرم 34 و build-tools 34.0.0.
۳. ساخت کلید دیباگ ثابت CI (برای نصب روی نسخه قبل بدون حذف داده).
۴. ساخت assembleDebug.
۵. اجرای همه تست‌های واحد (gradle test).
۶. ساخت assembleRelease با R8 فعال.
۷. بررسی نسخه (versionName و versionCode) APK.
۸. بارگذاری APK ها به‌عنوان artifact.

نسخه‌ها: versionName مطابق جدول بخش 12.2 و versionCode = عدد صحیح همان (مثلاً 0.3 به 3 و 1.0 به 100).

## 3.6 تزریق وابستگی

Hilt برای app و data. domain فقط با @Inject روی سازنده کلاس‌ها کار می‌کند.
زمان از طریق رابط Clock تزریق می‌شود (هرگز مستقیم از System.currentTimeMillis در منطق استفاده نشود).

```kotlin
interface Clock {
    fun now(): java.time.Instant
    val zone: java.time.ZoneId
}
```

اجرای تراکنش از طریق رابط TransactionRunner:

```kotlin
interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
```

---

# 4. مدل داده

پایگاه داده: Room نسخه 2.6.1، فایل yadin.db، نسخه schema برابر 1.
تنظیم exportSchema برابر true و خروجی schema در data/schemas ذخیره می‌شود.
مهاجرت مخرب (fallbackToDestructiveMigration) ممنوع است.

## 4.1 قراردادهای ذخیره‌سازی

۱. شناسه UUID به‌صورت متن 36 کاراکتری (حروف کوچک) ذخیره می‌شود.
۲. لحظه‌های زمانی (createdAt و مانند آن) به‌صورت عدد Long میلی‌ثانیه از 1970 UTC.
۳. روزها (nextReviewDay و lastReviewedDay و reviewedDay) به‌صورت Long: تعداد روز از 1970-01-01 به وقت محلی دستگاه (LocalDate.toEpochDay).
۴. enumها به‌صورت متن با نام enum ذخیره می‌شوند.
۵. Boolean به‌صورت 0 و 1.
۶. کلید خارجی‌ها با ON DELETE CASCADE.
۷. هیچ ستونی Nullable نیست مگر در جدول زیر صریحاً نوشته شده باشد.

## 4.2 enumها

```kotlin
enum class Stage { DAILY, WEEKLY, MONTHLY, LEARNED }
enum class VocabularyDifficulty { EASY, MEDIUM, HARD, VERY_HARD }
enum class QuizLevel { EASY, MEDIUM, HARD }          // مبتدی، متوسط، حرفه‌ای
enum class Direction { NORMAL, REVERSE }
enum class ReviewType { DAILY, WEEKLY, MONTHLY, LEARNED, RANDOM }
enum class ReviewMode { FLASHCARD, QUIZ }
enum class EntryType { WORD, PHRASE, SENTENCE, IDIOM, COLLOCATION, STRUCTURE }
enum class DuplicatePolicy { SKIP, MERGE, REPLACE, KEEP_SEPARATE }
enum class BackupType { VOCABULARY, PROGRESS, FULL }
enum class RestoreMode { MERGE, REPLACE }
enum class ImportReviewStatus { PENDING, APPROVED, REJECTED }
enum class ImportReviewReason { LOW_CONFIDENCE, CONFLICT, ORPHAN_SOURCE, ORPHAN_TRANSLATION, UNKNOWN_FORMAT, NOTE_TOO_LONG, TOO_MANY_TRANSLATIONS, INVALID_ROW }
```

مفهوم جهت:
در جفت es به fa، جهت NORMAL یعنی سؤال اسپانیایی و جواب فارسی.
جهت REVERSE یعنی سؤال فارسی و جواب اسپانیایی.
به‌طور کلی NORMAL یعنی سؤال زبان مبدأ جفت فعال و جواب زبان مقصد؛ REVERSE برعکس.

## 4.3 جدول‌ها

### concepts

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| entryType | TEXT | EntryType |
| active | INTEGER | پیش‌فرض 1 |
| createdAt | INTEGER | |
| updatedAt | INTEGER | |

ایندکس: (active).

### contents

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| conceptId | TEXT | کلید خارجی به concepts |
| languageCode | TEXT | مثلاً es یا fa |
| text | TEXT | متن اصلی دست‌نخورده (فقط trim) |
| canonicalKey | TEXT | بخش 5.2 |
| note | TEXT | Nullable. حداکثر 500 کاراکتر |
| pronunciation | TEXT | Nullable. فعلاً فقط ذخیره می‌شود |
| translationIndex | INTEGER | شروع از 0 و پشت‌سرهم |

یکتا: (conceptId، languageCode، translationIndex).
ایندکس: (languageCode، canonicalKey).

قواعد:
۱. برای هر مفهوم و هر زبان، حداقل یک محتوا (برای زبان‌هایی که مفهوم دارد).
۲. زبان مبدأ جفت فعال: دقیقاً یک محتوا با translationIndex برابر 0.
۳. زبان مقصد: بین 1 تا 10 محتوا. هر محتوا یک معنی جدا.
۴. یادداشت (note) فقط روی محتوای زبان مبدأ، با translationIndex 0 ذخیره می‌شود.
۵. در جفت‌های دیگر هم زبانی که «یک متن» دارد زبان مبدأ آن جفت است.

### categories

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| name | TEXT | یکتا |
| sortOrder | INTEGER | ترتیب نمایش |
| isDefault | INTEGER | 1 برای ده دسته رسمی |

### concept_categories

کلید اصلی: (conceptId، categoryId). هر دو کلید خارجی. ایندکس (categoryId، conceptId).
یک مفهوم می‌تواند 0 یا چند دسته داشته باشد.

### tags و concept_tags

tags: id، name (یکتا).
concept_tags: (conceptId، tagId) کلید اصلی.
برچسب با دسته یکی نیست.

### learning_states

برای هر مفهوم دقیقاً دو ردیف: یکی NORMAL و یکی REVERSE.

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| conceptId | TEXT | کلید خارجی |
| direction | TEXT | Direction |
| stage | TEXT | Stage |
| nextReviewDay | INTEGER | Nullable. برای LEARNED همیشه null و برای بقیه همیشه غیر null |
| lastReviewedDay | INTEGER | Nullable. null یعنی هنوز مرور نشده |
| createdAt | INTEGER | |
| updatedAt | INTEGER | |

یکتا: (conceptId، direction).
ایندکس: (direction، stage، nextReviewDay).

### difficulty_states

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| conceptId | TEXT | کلید خارجی |
| direction | TEXT | |
| current | TEXT | VocabularyDifficulty |
| consecutiveCorrect | INTEGER | بزرگ‌تر یا مساوی 0 |
| consecutiveWrong | INTEGER | بزرگ‌تر یا مساوی 0 |
| hasReachedVeryHard | INTEGER | پس از 1 شدن هرگز 0 نمی‌شود |

یکتا: (conceptId، direction).
ناوردا: consecutiveCorrect و consecutiveWrong هرگز هم‌زمان بزرگ‌تر از 0 نیستند.

### review_sessions

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| startedAt | INTEGER | |
| endedAt | INTEGER | Nullable |
| reviewType | TEXT | ReviewType |
| mode | TEXT | ReviewMode |
| direction | TEXT | |
| quizLevel | TEXT | Nullable. فقط وقتی mode برابر QUIZ |

### review_history

فقط درج. هیچ به‌روزرسانی یا حذفی (جز حذف زنجیره‌ای هنگام حذف دائمی مفهوم).

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| sessionId | TEXT | کلید خارجی |
| reviewAttemptId | TEXT | |
| conceptId | TEXT | کلید خارجی |
| direction | TEXT | |
| reviewedAt | INTEGER | |
| reviewedDay | INTEGER | روز محلی |
| isCorrect | INTEGER | |
| reviewType | TEXT | |
| mode | TEXT | |
| stageBefore | TEXT | |
| quizLevel | TEXT | Nullable |
| optionsJson | TEXT | Nullable. آرایه جیسون چهار گزینه به همان ترتیب نمایش |
| selectedIndex | INTEGER | Nullable. اندیس انتخاب کاربر |
| correctIndex | INTEGER | Nullable |

یکتا: (sessionId، reviewAttemptId).
ایندکس: (conceptId، direction، reviewedAt) و (reviewedDay).

### settings

key (کلید اصلی)، value (متن)، updatedAt.

### achievements

achievementId (کلید اصلی)، unlockedAt. ردیف فقط برای دستاوردهای باز شده وجود دارد.

### import_review_items

| ستون | نوع |
|------|-----|
| id | TEXT (کلید اصلی) |
| sessionTag | TEXT (شناسه عملیات ورود) |
| sourceText | TEXT |
| translationsText | TEXT (Nullable، ترجمه‌ها با « / » جدا) |
| note | TEXT (Nullable) |
| categoryNames | TEXT (Nullable، با « | » جدا) |
| entryType | TEXT (Nullable) |
| confidence | REAL |
| reason | TEXT |
| status | TEXT |
| lineNumber | INTEGER (Nullable) |
| rawText | TEXT |
| policy | TEXT (DuplicatePolicy موقع ورود) |
| createdAt | INTEGER |

### languages و language_pairs

languages: code (کلید اصلی)، name، active.
language_pairs: (sourceLanguageCode، targetLanguageCode) کلید اصلی، active.

داده اولیه:

| code | name |
|------|------|
| es | Español |
| fa | فارسی |
| en | English |

جفت اولیه: es به fa با active برابر 1.
جفت فعال در تنظیمات با کلید activePair به‌صورت متن es-fa ذخیره می‌شود.

## 4.4 ایجاد کارت‌ها

هر بار که مفهومی ساخته می‌شود (دستی، ورود، بانک اولیه، بازیابی بدون وضعیت) باید دقیقاً این‌ها ساخته شود:

۱. دو ردیف learning_states با stage برابر DAILY و nextReviewDay برابر امروز و lastReviewedDay برابر null.
۲. دو ردیف difficulty_states با current برابر EASY و هر دو شمارنده 0 و hasReachedVeryHard برابر 0.

اگر مفهومی ردیف کارت ندارد، تابع «اطمینان از وجود وضعیت‌ها» (EnsureStates، بخش 6.20) باید بسازد. هرگز هنگام ثبت پاسخ ساخته نمی‌شود؛ نبودن وضعیت در آن لحظه خطای DATA_INTEGRITY_ERROR است.

## 4.5 ناوردای‌های داده

۱. هر مفهوم فعال دقیقاً دو ردیف learning_states و دو ردیف difficulty_states دارد.
۲. stage برابر LEARNED اگر و فقط اگر nextReviewDay برابر null.
۳. شمارنده‌ها منفی نیستند.
۴. کارت‌های مفهوم غیرفعال در هیچ صف، آزمون و آماری شمرده نمی‌شوند.
۵. review_history اضافه‌شونده است.
۶. یکتایی (sessionId، reviewAttemptId).
۷. هر Content شماره ترجمه پشت‌سرهم 0 تا n-1 دارد.

---

# 5. قراردادهای عمومی

## 5.1 زمان و روز

```kotlin
fun today(clock: Clock): Long = LocalDate.now(clock.zone...).toEpochDay()
// دقیق‌تر:
fun dayOf(instant: Instant, zone: ZoneId): Long =
    instant.atZone(zone).toLocalDate().toEpochDay()
```

قواعد:
۱. امروز همیشه با dayOf(clock.now(), clock.zone) محاسبه می‌شود.
۲. «فردا» یعنی امروز + 1 (روز بعد، از ابتدای همان روز).
۳. «7 روز بعد» یعنی امروز + 7 و «30 روز بعد» یعنی امروز + 30.
۴. ساعت و دقیقه هیچ نقشی در سررسید ندارد. کارتی که سررسیدش امروز است از ساعت 00:00 امروز موعد است.
۵. در یک اجرای الگوریتم، مقدار «امروز» یک بار گرفته می‌شود و تغییر نمی‌کند.
۶. اگر ساعت یا منطقه زمانی دستگاه عوض شود، فقط محاسبه «امروز» عوض می‌شود. داده ذخیره‌شده دست نمی‌خورد.
۷. اگر کاربر ساعت را عقب ببرد و کارت «امروز مرور شده» محسوب شود (lastReviewedDay برابر امروز)، قفل هم‌روز اعمال می‌شود. اگر lastReviewedDay بزرگ‌تر از امروز باشد (ساعت عقب رفته)، کارت مرور نمی‌شود تا امروز به آن روز برسد. این رفتار قصد شده است.

## 5.2 تمیزکاری و کلید یکتای متن

تابع cleanText(s): برای متن‌های واردشده (ورود، دستی، بانک اولیه) به‌کار می‌رود.

```text
1. NFC
2. حذف همه U+200B در کل متن
3. حذف از ابتدا و انتها هر نویسه‌ای که یا فاصله (isWhitespace) باشد یا در این مجموعه:
   * _ ~ ` # | \ ^ = + < > • · ● ▪ ■ ◦ U+200B U+200C U+200D U+200E U+200F U+2060 U+FEFF
4. تبدیل tab به یک فاصله
5. تبدیل چند فاصله پشت‌سرهم به یک فاصله
```

نکته: U+200C (نیم‌فاصله فارسی) داخل کلمه حفظ می‌شود و فقط در ابتدا و انتها حذف می‌شود.
علائم داخل متن مثل پرانتز، نقطه، ¿ و ¡ و لهجه‌ها (á é í ó ú ñ ü) حفظ می‌شوند.

```kotlin
fun canonicalKey(text: String): String =
    java.text.Normalizer.normalize(text.trim(), java.text.Normalizer.Form.NFC)
        .replace(Regex("\\s+"), " ")
        .lowercase(java.util.Locale.ROOT)
```

متن ذخیره‌شده (text) پس از cleanText است. canonicalKey از text ساخته می‌شود.
یکسان‌سازی حروف عربی و فارسی (ی و ي، ک و ك) انجام نمی‌شود (تصمیم D18).

## 5.3 جداکننده ترجمه‌ها

هر متن ترجمه ورودی با این جداکننده‌ها به چند ترجمه شکسته می‌شود:

```text
regex:  \s*/\s*|\s*؛\s*|\s*;\s*
```

پس از شکستن: هر تکه cleanText می‌شود، تکه‌های خالی حذف می‌شوند، و تکه‌های با canonicalKey تکراری فقط یک‌بار (اولین) می‌مانند.

## 5.4 نمایش چند ترجمه

وقتی چند ترجمه باید در یک گزینه یا یک خط نمایش داده شوند:

```text
displayText = ترجمه‌ها به ترتیب translationIndex، بدون تکرار (با canonicalKey)، با جداکننده « / »
```

## 5.5 اعتبارسنجی ورودی

| مورد | قاعده |
|------|-------|
| متن مبدأ | بعد از cleanText خالی نباشد. حداکثر 200 کاراکتر |
| هر ترجمه | خالی نباشد. حداکثر 200 کاراکتر |
| تعداد ترجمه | بین 1 و 10 |
| یادداشت | اختیاری. حداکثر 500 کاراکتر |
| دسته | اختیاری. هر نام دسته غیرخالی |
| زبان مبدأ و مقصد | متفاوت و غیرخالی |

پیام خطا به فارسی و کوتاه نمایش داده می‌شود.

## 5.6 خطاها و مدل Result

لایه domain از استثنا فقط برای خطاهای برنامه‌نویسی (DATA_INTEGRITY_ERROR و ...) استفاده می‌کند.
خطاهای قابل‌انتظار (اعتبارسنجی، تکراری، فایل خراب) به‌صورت نتیجه صریح (sealed class) برمی‌گردند و UI پیام فارسی نشان می‌دهد.
هیچ استثنای مدیریت‌نشده‌ای نباید برنامه را ببندد؛ ViewModelها کل عملیات را در try/catch می‌گیرند و پیام «خطای غیرمنتظره، دوباره تلاش کنید» نشان می‌دهند و جزئیات را در Log می‌نویسند.

---

# 6. الگوریتم‌ها

هر الگوریتم مستقل است. الگوریتم‌ها فقط از طریق ورودی و خروجی تعریف‌شده با هم ارتباط دارند.
همه الگوریتم‌های محض (۶.۳، ۶.۴، ۶.۷ و ...) توابع کاتلین خالص در ماژول domain هستند و تست واحد دارند.

فهرست:

| شماره | الگوریتم |
|-------|----------|
| 6.1 | روز و ساعت (Clock) |
| 6.2 | انتقال مرحله یادگیری (LearningTransition) |
| 6.3 | محاسبه سختی کلمه (DifficultyCalculation) |
| 6.4 | ثبت پاسخ مرور (SubmitReviewAnswer) |
| 6.5 | انتخاب صف مرور (SelectReviewQueue) و شمارش |
| 6.6 | ساخت سؤال آزمون (QuizGeneration) |
| 6.7 | ساخت مفهوم و افزودن دستی واژه |
| 6.8 | ویرایش، حذف نرم و فعال‌سازی مجدد |
| 6.9 | پارسر متن واژگان (VocabularyParser) |
| 6.10 | خواننده‌های فایل (CSV، JSON، XLSX، SQLite) |
| 6.11 | یافتن مفهوم برای مدخل واردشده (ResolveConcept) و اجرای ورود |
| 6.12 | بانک اولیه (Seed) |
| 6.13 | پشتیبان‌گیری (Backup) |
| 6.14 | بازیابی (Restore) |
| 6.15 | آمار (Statistics) |
| 6.16 | رگبار (Streak) |
| 6.17 | دستاوردها (Achievements) |
| 6.18 | درصد پیشرفت |
| 6.19 | بروزرسانی و مهاجرت داده (Refresh) |
| 6.20 | اعتبارسنجی یکپارچگی داده (Integrity) |
| 6.21 | تلفظ (TTS) |

---

## 6.1 روز و ساعت

رابط Clock (بخش 3.6) تنها منبع زمان است.

```kotlin
class DayMath(private val clock: Clock) {
    fun today(): Long = clock.now().atZone(clock.zone).toLocalDate().toEpochDay()
    fun dayOf(instant: java.time.Instant): Long =
        instant.atZone(clock.zone).toLocalDate().toEpochDay()
}
```

در تست‌ها یک FakeClock با زمان و منطقه ثابت تزریق می‌شود.

---

## 6.2 انتقال مرحله یادگیری (LearningTransition)

### هدف

فقط تعیین مرحله جدید و روز مرور بعدی بعد از یک پاسخ.
این الگوریتم به سختی کلمه، سطح آزمون و شمارنده‌های سختی هیچ وابستگی ندارد.

### ورودی

```kotlin
data class TransitionInput(
    val stage: Stage,
    val isCorrect: Boolean,
    val today: Long
)
```

### خروجی

```kotlin
data class TransitionResult(
    val newStage: Stage,
    val nextReviewDay: Long?   // null فقط برای LEARNED
)
```

### قواعد

```text
stage = LEARNED (هر پاسخی):
    newStage = LEARNED
    nextReviewDay = null

stage = DAILY و پاسخ درست:
    newStage = WEEKLY
    nextReviewDay = today + 7

stage = WEEKLY و پاسخ درست:
    newStage = MONTHLY
    nextReviewDay = today + 30

stage = MONTHLY و پاسخ درست:
    newStage = LEARNED
    nextReviewDay = null

stage = DAILY یا WEEKLY یا MONTHLY و پاسخ غلط:
    newStage = DAILY
    nextReviewDay = today + 1
```

### نکته‌ها

۱. در هر مرحله فقط یک پاسخ درست برای رفتن به مرحله بعد لازم است. (این عدد از رفتار نسخه 6.84 فلش‌لرن گرفته شده است.)
۲. پاسخ غلط همیشه به DAILY و فردا می‌برد، حتی اگر کلمه خیلی سخت باشد.
۳. LEARNED برای همیشه LEARNED می‌ماند (تصمیم D07). پاسخ غلط در مرور یادگرفته‌ها مرحله را عوض نمی‌کند.
۴. هیچ شمارنده یا پرچم شکست مسیر وجود ندارد.

### شبه‌کد

```kotlin
fun learningTransition(i: TransitionInput): TransitionResult {
    if (i.stage == Stage.LEARNED) return TransitionResult(Stage.LEARNED, null)
    if (!i.isCorrect) return TransitionResult(Stage.DAILY, i.today + 1)
    return when (i.stage) {
        Stage.DAILY   -> TransitionResult(Stage.WEEKLY, i.today + 7)
        Stage.WEEKLY  -> TransitionResult(Stage.MONTHLY, i.today + 30)
        Stage.MONTHLY -> TransitionResult(Stage.LEARNED, null)
        Stage.LEARNED -> TransitionResult(Stage.LEARNED, null)
    }
}
```

### قانون سررسید

کارت غیر LEARNED سررسید است اگر nextReviewDay غیر null باشد و کوچک‌تر یا مساوی امروز.

---

## 6.3 محاسبه سختی کلمه (DifficultyCalculation)

### ورودی

```kotlin
data class DifficultyInput(
    val current: VocabularyDifficulty,
    val consecutiveCorrect: Int,
    val consecutiveWrong: Int,
    val hasReachedVeryHard: Boolean,
    val isCorrect: Boolean,
    val threshold: Int              // پیش‌فرض 3، بازه 1 تا 5
)
```

### خروجی

```kotlin
data class DifficultyOutput(
    val current: VocabularyDifficulty,
    val consecutiveCorrect: Int,
    val consecutiveWrong: Int,
    val hasReachedVeryHard: Boolean
)
```

### قواعد

آستانه ابتدا به بازه 1 تا 5 محدود می‌شود: t = threshold coerceIn 1..5.

پاسخ درست:

```text
consecutiveWrong = 0
n = consecutiveCorrect + 1
اگر n >= t:
    current = یک پله آسان‌تر
    consecutiveCorrect = 0
وگرنه:
    consecutiveCorrect = n
```

پاسخ غلط:

```text
consecutiveCorrect = 0
n = consecutiveWrong + 1
اگر n >= t:
    current = یک پله سخت‌تر
    consecutiveWrong = 0
وگرنه:
    consecutiveWrong = n
```

یک پله آسان‌تر:

```text
VERY_HARD به HARD
HARD به MEDIUM
MEDIUM به EASY
EASY به EASY
```

یک پله سخت‌تر:

```text
EASY به MEDIUM
MEDIUM به HARD
HARD به VERY_HARD
VERY_HARD به VERY_HARD
```

پرچم تاریخی:

```text
hasReachedVeryHard = hasReachedVeryHard OR (current جدید == VERY_HARD)
```

هرگز دوباره 0 نمی‌شود.

### نکته‌ها

۱. هر تغییر پله هر دو شمارنده را صفر می‌کند (در شاخه‌ها بالا انجام می‌شود: شمارنده طرف مقابل همان لحظه صفر شده است و شمارنده همین طرف هنگام تغییر پله صفر می‌شود).
۲. آستانه سختی فقط اینجا استفاده می‌شود و هرگز در انتقال مرحله استفاده نمی‌شود.
۳. اگر آستانه کم شود و شمارنده ذخیره‌شده از آن بیشتر باشد، چون مقایسه با n >= t است، در پاسخ بعدی پله عوض می‌شود.
۴. نوع پاسخ (فلش‌کارت یا آزمون) تفاوتی ندارد.
۵. سختی هر جهت جداست؛ پاسخ در جهت NORMAL فقط سختی NORMAL را تغییر می‌دهد.

### مثال با آستانه 3

| حالت اولیه | پاسخ‌ها | نتیجه |
|------------|---------|-------|
| EASY، شمارنده‌ها 0 | غلط، غلط، غلط | MEDIUM، شمارنده‌ها 0 |
| MEDIUM | درست، درست، درست | EASY |
| HARD، شمارنده درست 2 | غلط | HARD، شمارنده غلط 1 و درست 0 |
| EASY | درست، درست، درست | EASY (کف) |
| VERY_HARD | غلط ×3 | VERY_HARD (سقف) |

---

## 6.4 ثبت پاسخ مرور (SubmitReviewAnswer)

### ورودی

```kotlin
data class SubmitAnswerRequest(
    val conceptId: String,
    val direction: Direction,
    val sessionId: String,
    val reviewAttemptId: String,
    val reviewType: ReviewType,
    val mode: ReviewMode,
    val isCorrect: Boolean,
    val quizLevel: QuizLevel?,
    val optionsJson: String?,
    val selectedIndex: Int?,
    val correctIndex: Int?,
    val reviewedAt: java.time.Instant
)
```

### خروجی

```kotlin
data class SubmitAnswerResult(
    val learning: LearningState,
    val difficulty: DifficultyState,
    val transition: TransitionResult
)
```

### مراحل (همه در یک تراکنش)

```text
BEGIN TRANSACTION
 1. today = dayOf(reviewedAt)
 2. learning = learning_states(conceptId, direction)
    اگر نبود: خطای DATA_INTEGRITY_ERROR
 3. difficulty = difficulty_states(conceptId, direction)
    اگر نبود: خطای DATA_INTEGRITY_ERROR
 4. مفهوم باید active باشد وگرنه خطای CONCEPT_INACTIVE
 5. اگر review_history برای (sessionId، reviewAttemptId) وجود دارد:
    خطای DUPLICATE_ATTEMPT (هیچ تغییری ایجاد نمی‌شود)
 6. قفل هم‌روز: اگر learning.lastReviewedDay == today: خطای ALREADY_REVIEWED_TODAY
 7. اگر reviewType != LEARNED:
       باید learning.stage != LEARNED و learning.nextReviewDay <= today
       وگرنه خطای NOT_DUE
    اگر reviewType == LEARNED:
       باید learning.stage == LEARNED وگرنه خطای NOT_LEARNED
 8. transition = learningTransition(learning.stage, isCorrect, today)
 9. threshold = settings.difficultyThreshold (پیش‌فرض 3)
10. newDifficulty = difficultyCalculation(difficulty, isCorrect, threshold)
11. learning را به‌روز کن:
       stage = transition.newStage
       nextReviewDay = transition.nextReviewDay
       lastReviewedDay = today
       updatedAt = reviewedAt
12. difficulty را با newDifficulty جایگزین کن
13. یک ردیف review_history بساز (stageBefore = learning.stage قبل از مرحله 11)
COMMIT
```

هر استثنا یعنی ROLLBACK کامل.

### نکته‌ها

۱. بعد از ثبت موفق، جلسه مرور آمار خودش را از نتیجه می‌گیرد؛ دوباره خواندن از دیتابیس لازم نیست.
۲. در مرور یادگرفته‌ها (LEARNED) مرحله تغییر نمی‌کند ولی سختی تغییر می‌کند و تاریخچه ثبت می‌شود و قفل هم‌روز اعمال می‌شود.
۳. بعد از هر ثبت موفق، دستاوردها بررسی می‌شوند (بخش 6.17) و رگبار از تاریخچه محاسبه می‌شود.

---

## 6.5 انتخاب صف مرور (SelectReviewQueue)

صف دائمی در دیتابیس وجود ندارد. هر بار از وضعیت واقعی محاسبه می‌شود.

### ورودی

```kotlin
data class ReviewFilters(
    val reviewType: ReviewType,
    val direction: Direction,
    val activePair: LanguagePair,         // مبدأ و مقصد
    val difficulties: Set<VocabularyDifficulty> = emptySet(),  // خالی یعنی همه
    val categoryIds: Set<String> = emptySet(),                 // خالی یعنی همه
    val tagId: String? = null,
    val maxCards: Int = 30
)
```

### مجموعه کاندید بر اساس نوع مرور

همه شرط‌ها روی کارت‌هایی هستند که direction برابر direction ورودی دارند:

```text
DAILY   : stage = DAILY   AND nextReviewDay <= today
WEEKLY  : stage = WEEKLY  AND nextReviewDay <= today
MONTHLY : stage = MONTHLY AND nextReviewDay <= today
RANDOM  : stage IN (DAILY, WEEKLY, MONTHLY) AND nextReviewDay <= today
LEARNED : stage = LEARNED
```

### شرط‌های مشترک (همه با AND)

```text
۱. مفهوم active باشد
۲. lastReviewedDay != today  (قفل هم‌روز)
۳. مفهوم هم برای زبان مبدأ و هم برای زبان مقصد جفت فعال، حداقل یک محتوای غیرخالی داشته باشد
۴. اگر difficulties خالی نیست: difficulty کارت (همان جهت) در مجموعه باشد
۵. اگر categoryIds خالی نیست: مفهوم حداقل یکی از آن دسته‌ها را داشته باشد
۶. اگر tagId خالی نیست: مفهوم آن برچسب را داشته باشد
```

### ترتیب و برش

```text
DAILY، WEEKLY، MONTHLY:
    مرتب‌سازی صعودی بر اساس nextReviewDay سپس conceptId
RANDOM و LEARNED:
    shuffle (تصادفی)
سپس: برش به maxCards
maxCards ابتدا به بازه 1 تا 100 محدود می‌شود
```

هر مفهوم فقط یک‌بار در یک جلسه می‌آید (در یک جهت فقط یک کارت وجود دارد، پس تکرار رخ نمی‌دهد).

### خروجی

لیست ReviewCandidate (کارت، مفهوم، سختی، دسته‌ها). اگر خالی بود، لیست خالی برمی‌گردد (خطا نیست).

### ناوردا

این الگوریتم فقط خواندنی است و هیچ داده‌ای را تغییر نمی‌دهد.

### شمارش

شمارنده صفحه خانه و تنظیمات مرور باید دقیقاً از همین تابع شرط‌ها (بدون برش maxCards) استفاده کند تا هرگز عددی نمایش داده نشود که صف واقعی آن را رد کند.
برای صفحه خانه، سه نوع DAILY و WEEKLY و MONTHLY در یک بار خواندن جدول‌ها شمرده می‌شوند.

### کارایی

۱. فیلتر روز و مرحله باید در SQL انجام شود (روی ایندکس direction، stage، nextReviewDay) نه در حافظه.
۲. برای 6000 مفهوم نباید همه جدول‌ها در حافظه بارگذاری شوند مگر برای فیلترهای دسته و برچسب.

---

## 6.6 ساخت سؤال آزمون (QuizGeneration)

### اصل

این الگوریتم فقط سؤال را می‌سازد و هیچ‌چیز را ذخیره نمی‌کند.
سطح آزمون فقط روش انتخاب گزینه‌های غلط را تعیین می‌کند و به سختی کلمه وابسته نیست (تصمیم D24).

### ورودی

```kotlin
data class QuizRequest(
    val conceptId: String,
    val direction: Direction,
    val activePair: LanguagePair,
    val level: QuizLevel,                      // EASY مبتدی، MEDIUM متوسط، HARD حرفه‌ای
    val usedDistractorKeys: Set<String>        // کلید یکتای گزینه‌های غلط قبلاً دیده‌شده در همین جلسه
)
```

### خروجی

```kotlin
sealed interface QuizResult {
    data class Question(
        val promptText: String,
        val options: List<String>,        // دقیقاً 4 مورد، ترتیب تصادفی
        val correctIndex: Int,
        val distractorKeys: Set<String>
    ) : QuizResult
    data object FlashcardFallback : QuizResult
}
```

### مراحل

```text
۱. سمت سؤال و سمت جواب:
     NORMAL : سؤال = زبان مبدأ، جواب = زبان مقصد
     REVERSE: سؤال = زبان مقصد، جواب = زبان مبدأ
   promptText = displayText محتوای سمت سؤال (بخش 5.4)
   correctText = displayText محتوای سمت جواب (همه معنی‌ها یک گزینه، بخش 5.4)
   اگر هر کدام خالی بود: FlashcardFallback

۲. کاندیدها = همه مفاهیم فعال به‌جز مفهوم هدف که در زبان جواب حداقل یک محتوا دارند.
   برای هر کاندید displayText و مجموعه canonicalKeyهای آن ساخته می‌شود.

۳. حذف کاندیدهای نامعتبر:
   الف. displayText نرمال‌شده برابر correctText نرمال‌شده
   ب. اشتراک canonicalKeyها با canonicalKeyهای پاسخ صحیح غیرتهی
   ج. displayText نرمال‌شده تکراری بین کاندیدها (فقط اولی می‌ماند)
   نرمال‌سازی مقایسه: NFC و trim و تبدیل فاصله‌های چندتایی به یکی و حروف کوچک (همان canonicalKey)

۴. ترجیح گزینه‌های «تازه»:
   fresh = کاندیدهایی که distractorKey آنها در usedDistractorKeys نیست
   اگر |fresh| >= 3: مجموعه = fresh وگرنه مجموعه = همه کاندیدها

۵. اگر |مجموعه| < 3: FlashcardFallback

۶. انتخاب بر اساس سطح (بخش زیر)

۷. options = shuffle([correctText] + سه گزینه غلط)

۸. بررسی نهایی: چهار گزینه، نرمال‌شده‌ها دو به دو متفاوت، دقیقاً یکی برابر پاسخ صحیح.
   اگر نقض شد: FlashcardFallback
```

distractorKey هر کاندید = canonicalKey ترکیبی displayText آن (displayText نرمال‌شده).

### محاسبه شباهت و «اشتباه‌گرفتنی بودن»

برای هر کاندید در مقایسه با پاسخ صحیح (displayText ها):

```text
tokenSim  = |tokens(a) ∩ tokens(b)| / |tokens(a) ∪ tokens(b)|
            tokens = نرمال‌شده را روی هر نویسه غیر حرف و غیر رقم می‌شکنیم و تکه‌های خالی را حذف می‌کنیم
            اگر هر دو مجموعه خالی: 1.0 ، اگر فقط یکی خالی: 0.0
bigramSim = |bigrams(a) ∩ bigrams(b)| / |bigrams(a) ∪ bigrams(b)|
            bigrams = همه زیررشته‌های دونویسه‌ای متن نرمال‌شده؛ اگر طول <= 2: خود متن یک عضو است
levSim    = 1 - (فاصله لوِنشتاین / بیشینه طول دو متن)    ؛ اگر برابر: 1.0 ؛ اگر یکی خالی: 0.0

lexicalSimilarity = clamp01( 0.45 * tokenSim + 0.35 * levSim + 0.20 * bigramSim )

categoryShare = 1 اگر دو مفهوم حداقل یک دسته مشترک دارند وگرنه 0
typeMatch     = 1 اگر entryType دو مفهوم برابر است وگرنه 0

confusability = clamp01( 0.55 * lexicalSimilarity + 0.25 * categoryShare + 0.20 * typeMatch )
```

### انتخاب سه گزینه غلط برای هر سطح

```text
سطح EASY (مبتدی):
   tierPreferred = کاندیدهایی با categoryShare=0 و typeMatch=0
   tierFallback  = کاندیدهایی با categoryShare=0
   مرتب‌سازی: confusability صعودی
   انتخاب: سه عضو «میانه» لیست مرتب‌شده

سطح MEDIUM (متوسط):
   tierPreferred = کاندیدهایی با categoryShare=1 و typeMatch=0
   tierFallback  = کاندیدهایی با categoryShare=1
   مرتب‌سازی: confusability صعودی
   انتخاب: سه عضو «میانه» لیست مرتب‌شده

سطح HARD (حرفه‌ای):
   tierPreferred = کاندیدهایی با categoryShare=1 و typeMatch=1
   tierFallback  = کاندیدهایی با categoryShare=1
   مرتب‌سازی: confusability نزولی
   انتخاب: سه عضو «اول» لیست مرتب‌شده

انتخاب لایه:
   اگر |tierPreferred| >= 3 از آن
   وگرنه اگر |tierFallback| >= 3 از آن
   وگرنه از کل مجموعه (گام 4)

سه عضو میانه: اگر لیست مرتب n عضو دارد، start = (n - 3) / 2 (تقسیم صحیح) و اعضای start تا start+2.
تساوی confusability: ترتیب الفبایی displayText نرمال‌شده.
```

### کارایی (الزامی)

۱. شباهت فقط برای کاندیدهای لایه منتخب محاسبه می‌شود، نه همه 6000.
۲. اگر لایه منتخب بیش از 300 عضو دارد، قبل از محاسبه شباهت، 300 عضو تصادفی (shuffle سپس take) انتخاب می‌شود.
۳. شباهت‌ها همه‌جفت‌ها از پیش محاسبه نمی‌شوند (این کار در نسخه 6.83 فلش‌لرن صفحه را قفل کرد).
۴. فهرست مفاهیم و محتوای زبان جواب یک‌بار در حافظه ساخته و نگه داشته می‌شود و فقط هنگام تغییر داده (افزودن، ورود، حذف، بازیابی) باطل و بازسازی می‌شود. ساختن آن باید از ترد اصلی بیرون باشد.
۵. محاسبه فاصله لوِنشتاین با دو آرایه (حافظه O(n)).

### تعریف لایه‌ها در جمع‌بندی

سطح آزمون فقط تعیین می‌کند گزینه‌های غلط چقدر شبیه پاسخ باشند:
مبتدی: کم‌شباهت و معمولاً از دسته‌ای دیگر.
متوسط: هم‌دسته ولی نوع متفاوت و شباهت متوسط.
حرفه‌ای: هم‌دسته و هم‌نوع و بیشترین شباهت.

---

## 6.7 ساخت مفهوم و افزودن دستی واژه

### CreateConcept (داخلی)

ورودی: متن مبدأ، فهرست ترجمه‌ها، یادداشت، فهرست نام دسته‌ها، نوع ورودی، جفت زبانی.

```text
BEGIN TRANSACTION
 1. اعتبارسنجی (بخش 5.5)
 2. concepts: درج با active = 1
 3. contents: یک محتوای مبدأ با translationIndex 0 و note
    و محتوای مقصد برای هر ترجمه با translationIndex از 0
 4. concept_categories: برای هر نام دسته، دسته را بیاب (با name) یا بساز؛ سپس پیوند بزن
 5. ساخت دو کارت (بخش 4.4)
COMMIT
```

هر شکست یعنی ROLLBACK. مفهوم ناقص هرگز باقی نمی‌ماند.

### AddWord (فرم افزودن تکی)

فرم: متن مبدأ (اجباری)، یک یا چند ترجمه (حداقل یکی)، نوع (پیش‌فرض WORD)، دسته (چندانتخابی)، یادداشت (اختیاری).

```text
۱. اعتبارسنجی
۲. matched = مفاهیم فعال با محتوای مبدأ (languageCode=زبان مبدأ، canonicalKey=کلید متن مبدأ)
۳. اگر matched خالی: CreateConcept
۴. اگر matched غیرخالی: مفهوم قدیمی‌تر (createdAt سپس id)
     - ترجمه‌های جدید = ترجمه‌هایی که canonicalKeyشان در مقصد آن مفهوم نیست
     - اگر ترجمه جدید نیست: پیام «این واژه با همین معنی‌ها قبلاً وجود دارد» و هیچ تغییری ندارد
     - وگرنه فقط ترجمه‌های جدید با translationIndex بعدی اضافه می‌شود (تصمیم D11)
     - دسته‌هایی که ندارد اضافه می‌شود (جایگزین نمی‌شود)
     - یادداشت: اگر مفهوم یادداشت ندارد و ورودی دارد، گذاشته می‌شود؛ وگرنه دست نمی‌خورد
```

نتیجه برای کاربر: «ساخته شد» یا «N ترجمه جدید به واژه موجود اضافه شد» یا «تکراری بود».

---

## 6.8 ویرایش، حذف نرم و فعال‌سازی مجدد

### ویرایش مفهوم

```text
BEGIN TRANSACTION
 1. اعتبارسنجی (5.5)
 2. اگر مفهوم دیگری با همان کلید مبدأ و حداقل یک ترجمه مشترک (canonicalKey) وجود دارد (فقط مفاهیم فعال):
       خطای تکراری «این واژه با همین ترجمه قبلاً وجود دارد»
 3. entryType و دسته‌ها به‌روز شوند (دسته‌ها با مجموعه جدید جایگزین می‌شوند)
 4. محتوای مبدأ به‌روز شود (متن، canonicalKey، note)
 5. ترجمه‌ها: فهرست جدید به ترتیب نوشته می‌شود:
       ردیف i با translationIndex = i ، ردیف‌های اضافی قدیمی حذف می‌شوند
 6. updatedAt به‌روز شود
COMMIT
```

ویرایش هرگز کارت‌ها (مرحله، سختی، تاریخچه) را تغییر نمی‌دهد یا حذف نمی‌کند.

### حذف نرم

```text
active = 0 ؛ updatedAt = اکنون
```

۱. کارت‌ها و تاریخچه حفظ می‌شوند.
۲. مفهوم غیرفعال در صف مرور، آزمون (هم به‌عنوان سؤال و هم گزینه غلط)، آمار، دستاوردها و رگبار نیست.
   (رگبار از تاریخچه می‌آید؛ بخش 6.16 می‌گوید فقط ردیف‌های مفاهیم فعال شمرده شود.)
۳. در کتابخانه با فیلتر «غیرفعال‌ها» دیده می‌شود با نشان خاکستری و دکمه «فعال‌سازی».
۴. هنگام حذف، پنجره تایید نمایش داده می‌شود.

### فعال‌سازی مجدد

اگر مفهوم دیگری فعال با همان کلید مبدأ و ترجمه مشترک وجود دارد، خطای تکراری. وگرنه active = 1.

### حذف دائمی

فقط دکمه «پاک‌سازی غیرفعال‌ها» در تنظیمات، با تایید. تمام مفاهیم غیرفعال و داده وابسته (cascade) حذف می‌شوند. پاکسازی خودکار وجود ندارد (D31).

---

## 6.9 پارسر متن واژگان (VocabularyParser)

پارسر محض است (بدون دیتابیس) و در ماژول domain قرار دارد.
ورودی: متن خام. خروجی: ParseResult.

```kotlin
data class ParsedEntry(
    val sourceText: String,
    val translations: List<String>,   // پس از شکستن با بخش 5.3
    val note: String?,
    val categoryNames: List<String>,  // از متن ساده خالی؛ از فایل‌های ساخت‌یافته پر می‌شود
    val entryType: EntryType,
    val confidence: Double,           // 0.0 تا 1.0
    val lineNumber: Int,
    val rawLines: List<String>,
    val evidence: List<String>
)
data class ParseWarning(val type: ParseWarningType, val lineNumber: Int, val rawText: String, val message: String)
enum class ParseWarningType { ORPHAN_SOURCE, ORPHAN_TRANSLATION, ORPHAN_LINE, UNKNOWN_FORMAT }
data class ParseResult(val entries: List<ParsedEntry>, val warnings: List<ParseWarning>)
```

### اصول (الزامی)

۱. متن اصلی حفظ می‌شود. لهجه‌ها و پرانتزها و علائم معنی‌دار حذف نمی‌شوند.
۲. اصلاح معنایی خودکار ممنوع است.
۳. هیچ داده‌ای بی‌ردپا دور ریخته نمی‌شود: هر خط بی‌جفت یا ناشناخته در warnings می‌آید.
۴. شماره‌گذاری فقط یک نشانه است. خالی‌بودن یا پرش در شماره‌ها خطا نیست.
۵. تجزیه، یادداشت و نکته گرامری خودشان مفهوم مستقل نمی‌شوند.
۶. داده حدس‌زده‌شده با اعتماد پایین نشانه‌گذاری می‌شود و مستقیم ذخیره نمی‌شود (بخش 6.11).

### گام ۱: نرمال‌سازی کل متن

```text
CRLF و CR به LF
حذف همه U+200B
NFC
tab به فاصله
دو یا چند فاصله پشت‌سرهم به یک فاصله
```

سپس به خط‌ها شکسته می‌شود. شماره خط از 1 شروع می‌شود.

### گام ۲: توابع کمکی

```text
isSeparator(s):        s با regex  ^[-—_=*•]{3,}$  برابر باشد
isNumbered(s):         s با regex  ^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*  شروع شود
stripNumbering(s):     اگر isNumbered: آن بخش حذف شود، trim، سپس اگر با یکی از  - — * • # » « ➜ →  شروع شود همان یک نویسه حذف و trim
hasLatin(s):           حداقل یک نویسه حرف با اسکریپت LATIN (Character.UnicodeScript.LATIN) دارد (حروف با لهجه هم لاتین‌اند)
hasPersian(s):         حداقل یک نویسه در بازه‌های U+0600..06FF یا U+0750..077F یا U+08A0..08FF
detectScript(s):
    latinCount و persianCount = تعداد حروف هر اسکریپت
    هر دو بزرگ‌تر از 0     : MIXED
    latinCount > persianCount و latinCount > 0 : LATIN
    persianCount > latinCount و persianCount > 0 : PERSIAN
    وگرنه : UNKNOWN
```

زبان خط لاتین همیشه زبان مبدأ جفت فعال در نظر گرفته می‌شود (در نسخه اول جفت‌هایی پشتیبانی می‌شوند که یک سمتشان لاتین و سمت دیگر فارسی است).

```text
splitPair(s):
    ۱. اگر s شامل  →  باشد در اولین محل (اندیس بزرگ‌تر از 0 و کوچک‌تر از طول-1): چپ و راست
    ۲. وگرنه اگر شامل  ➜  باشد: همان‌طور
    ۳. وگرنه اولین تطبیق regex  \s+[-—]\s+  : چپ و راست
    ۴. وگرنه اولین «:» (اندیس بین 0 و طول-1): چپ و راست trim شود؛ فقط اگر چپ hasLatin و راست hasPersian باشد
    ۵. وگرنه null
```

### گام ۳: نشانگرها (مجموعه پیش‌فرض)

مقایسه نشانگر روی خط با حروف کوچک (lowercase) و trim انجام می‌شود.
اگر نشانگر با «:» تمام شود، تطبیق یعنی خط با آن شروع شود. وگرنه یعنی خط دقیقاً برابر نشانگر باشد.

| نوع | نشانگرها |
|-----|----------|
| یادداشت (NOTE) | نکته: ، توضیحات: ، احتمال اشتباه: ، توجه: ، مثال: ، Examples: ، Example: ، Note: ، Notes: ، Usage: ، Ejemplo: ، Ejemplos: ، Nota: ، Uso: |
| نکته گرامری (GRAMMAR_NOTE) | نکته گرامری: ، نکته گرامری ، Grammar: ، Grammar Note: ، Grammatical Note: ، Nota gramatical: |
| تجزیه (BREAKDOWN) | تجزیه: ، تجزیه ، Breakdown: ، Breakdown |
| مشتق (DERIVATIVE) | مشتق شده از: ، مشتق شده از ، Derived from: ، Derived from |
| تنوع (VARIANT) | Variant: ، Variants: ، حالت دیگر: ، حالت دیگر |
| مرتبط (RELATION) | مرتبط: ، مرتبط ، Related: ، Related ، Synonym: |

علاوه بر جدول: خط یادداشت است اگر با regex زیر (روی lowercase) تطبیق کند:
`^(example|examples|note|notes|usage|ejemplo|ejemplos|nota|uso)\s+[^:]{1,60}:`

### گام ۴: طبقه‌بندی هر خط

برای هر خط غیرخالی، rawTrimmed = خط trim‌شده. اگر isSeparator(rawTrimmed) آن را همان‌طور نگه دار، وگرنه line = stripNumbering(rawTrimmed).

ترتیب بررسی (اولین تطبیق برنده است):

```text
 1. rawTrimmed با #  یا  //  شروع شود          => COMMENT      (نادیده گرفته می‌شود)
 2. isSeparator(rawTrimmed)                      => SEPARATOR    (نادیده)
 3. line فقط ارقام                               => NUMBER       (نادیده)
 4. نشانگر BREAKDOWN                              => BREAKDOWN
 5. نشانگر DERIVATIVE                             => DERIVATIVE
 6. نشانگر VARIANT                                => VARIANT
 7. نشانگر RELATION                               => RELATION
 8. نشانگر GRAMMAR_NOTE                           => GRAMMAR_NOTE
 9. نشانگر NOTE یا regex مثال                      => NOTE
10. splitPair(line) غیر null و left لاتین و right فارسی => ENTRY_HEADER
11. detectScript(line) = PERSIAN                   => TRANSLATION
12. hasLatin(line)                                => ENTRY_HEADER
13. غیر از این                                    => UNKNOWN
```

(تفاوت آگاهانه با فلش‌لرن: تشخیص COMMENT قبل از حذف نویسه تزئینی انجام می‌شود تا خط # دیگر به ترجمه یا مدخل تبدیل نشود. خطوط COMMENT اصلاً وارد یادداشت‌ها نمی‌شوند.)

### گام ۵: ماشین حالت مدخل

حالت‌ها: pending (مدخل در حال ساخت یا null) و orphanTranslation (ترجمه‌ای که قبل از مبدأ دیده شده یا null).

مدخل در حال ساخت شامل: source، translation (متن واحد یا null)، lineNumber، confidence، noteLines، rawLines، evidence.

قواعد برای هر نوع خط:

```text
ENTRY_HEADER:
    pair = splitPair(line)
    numbered = isNumbered(rawTrimmed)
    nextIsPersian = (خط بعدی وجود دارد و detectScript(stripNumbering(خط بعدی trim)) = PERSIAN)

    اگر pending وجود دارد و pending.translation == null و pair == null و numbered == false
       و nextIsPersian و hasLatin(line):
         pending.source += " " + stripNumbering(line)       // مدخل چندخطی
         evidence += multilineSource ؛ confidence = max(confidence, 0.90)
         ادامه به خط بعد

    وگرنه:
         flush(pending)
         اگر orphanTranslation وجود دارد: هشدار ORPHAN_TRANSLATION قبلی آن حذف می‌شود
         source = pair.left (trim) یا کل line اگر pair == null
         inline = pair.right (trim) اگر غیرخالی
         translation = inline یا (اگر نبود) orphanTranslation
         confidence = 1.0 اگر pair != null ؛ 0.95 اگر translation != null ؛ وگرنه 0.60
         pending جدید ساخته شود ؛ orphanTranslation = null

TRANSLATION:
    اگر pending == null:
        اگر خط بعدی وجود دارد و hasLatin(stripNumbering(خط بعدی)) و آن خط جداکننده نیست:
            orphanTranslation = line ؛ هشدار ORPHAN_TRANSLATION (نگه داشته شد برای جفت‌کردن)
        وگرنه:
            هشدار ORPHAN_TRANSLATION ؛ آن خط به import_review_items می‌رود
    اگر pending.translation == null:
        pending.translation = line ؛ confidence = max(confidence, 0.95)
    وگرنه:
        pending.translation = pending.translation + " / " + line     // ترجمه اضافه
        confidence = max(confidence, 0.95)

BREAKDOWN، DERIVATIVE، VARIANT، RELATION:
    اگر pending == null: هشدار ORPHAN_LINE
    وگرنه: noteLines += "<برچسب فارسی>: <بدنه>"
        برچسب: BREAKDOWN=تجزیه ، DERIVATIVE=مشتق شده از ، VARIANT=حالت دیگر ، RELATION=مرتبط
        بدنه: متن بعد از اولین «:» در line ، یا کل line اگر «:» نیست

GRAMMAR_NOTE:
    اگر pending == null: هشدار ORPHAN_LINE
    وگرنه: noteLines += "نکته گرامری: " + بدنه

NOTE:
    اگر pending == null: هشدار ORPHAN_LINE
    وگرنه: noteLines += بدنه خط (متن بعد از اولین «:» ؛ برای regex مثال، خود line)

UNKNOWN:
    اگر pending وجود دارد و pending.translation == null و hasLatin(line):
        مدخل چندخطی: pending.source += " " + stripNumbering(line)
    اگر pending وجود دارد: noteLines += stripNumbering(line)
    وگرنه: هشدار UNKNOWN_FORMAT (اعتماد 0.70) و خط به import_review_items می‌رود

SEPARATOR، NUMBER، COMMENT:
    نادیده؛ ولی اگر SEPARATOR باشد، pending فعلی flush می‌شود.
```

flush(pending):

```text
note = noteLines با «\n» (بدون خط خالی) ؛ اگر خالی: null
translations = شکستن translation با بخش 5.3 (ممکن است خالی)
entryType = classifyEntry(source)
اگر translations خالی: هشدار ORPHAN_SOURCE ؛ confidence = min(confidence, 0.60)
مدخل به فهرست اضافه می‌شود (حتی اگر ناقص)
```

پایان ورودی: flush(pending).
اگر پایان ورودی orphanTranslation باقی مانده باشد: هشدار ORPHAN_TRANSLATION و ورود به import_review_items.

### گام ۶: ادغام مدخل‌های کاملاً تکراری داخل همان ورودی

کلید ادغام = canonicalKey(source) + نویسه NUL + canonicalKey(translations به هم چسبیده با « / »).
دو مدخل با کلید برابر یکی می‌شوند: noteLines و rawLines و evidence ترکیب (بدون تکرار)، confidence = بیشینه.

### classifyEntry

```text
tokens = تعداد کلمات source با شکستن روی فاصله
tokens == 1      => WORD
2 تا 5           => PHRASE
6 یا بیشتر       => SENTENCE
```

IDIOM و COLLOCATION و STRUCTURE فقط از ورودی‌های ساخت‌یافته یا انتخاب کاربر می‌آیند، نه از حدس پارسر.

### مثال ورودی و خروجی

```text
1. manzana → سیب
2. hacer falta
نیاز بودن
نکته: فعل با مفعول غیرمستقیم
# این خط فقط توضیح فایل است
3. la casa - خانه / منزل
```

خروجی:

| منبع | ترجمه‌ها | یادداشت | نوع | اعتماد |
|------|----------|---------|-----|--------|
| manzana | سیب | | WORD | 1.0 |
| hacer falta | نیاز بودن | فعل با مفعول غیرمستقیم | PHRASE | 0.95 |
| la casa | خانه، منزل | | PHRASE | 1.0 |

---

## 6.10 خواننده‌های فایل

همه خواننده‌ها لیست ParsedEntry (با categoryNames و entryType صریح در صورت وجود) و هشدارها را برمی‌گردانند. اعتماد مدخل‌های ساخت‌یافته 1.0 است مگر ردیف نامعتبر.
خواندن حتماً روی ترد IO و با پیشرفت قابل گزارش به UI است. حداکثر اندازه فایل 20 مگابایت.

### متن (Text)

از فایل یا متن چسبانده‌شده؛ UTF-8 (با BOM هم پذیرفته می‌شود)؛ پارسر بخش 6.9.

### CSV

۱. رمزگذاری UTF-8 (با یا بدون BOM).
۲. جداکننده خودکار: بین «,» و «;» و tab، نویسه‌ای که در خط اول بیشترین تعداد را دارد. قواعد نقل‌قول RFC 4180 (نقل‌قول دوتایی داخل فیلد با دو نقل‌قول).
۳. ردیف عنوان اختیاری. اگر ردیف اول یکی از نام‌های زیر را داشته باشد، عنوان است (بی‌توجه به حروف):

| ستون منطقی | نام‌های پذیرفته‌شده |
|------------|----------------------|
| مبدأ | source, word, spanish, واژه, کلمه, اسپانیایی |
| ترجمه | translation, meaning, translations, ترجمه, معنی, فارسی |
| یادداشت | note, notes, یادداشت |
| دسته | category, categories, دسته |
| نوع | type, entrytype, نوع |

۴. بدون عنوان: ستون 1 مبدأ، ستون 2 ترجمه، ستون 3 یادداشت، ستون 4 دسته، ستون 5 نوع.
۵. ستون ترجمه می‌تواند چند ترجمه با جداکننده بخش 5.3 داشته باشد.
۶. ستون دسته می‌تواند چند دسته با جداکننده «|» داشته باشد.
۷. ستون نوع یکی از نام‌های EntryType؛ در غیر این صورت classifyEntry.
۸. ردیف‌هایی که مبدأ یا ترجمه‌شان خالی است: ردیف نامعتبر (INVALID_ROW) می‌شود و در import_review_items قرار می‌گیرد.

### JSON

دو شکل پذیرفته می‌شود:

الف. آرایه‌ای از اشیا یا شیئی با کلید entries که آرایه اشیاست:

```json
{ "entries": [
  { "source": "manzana", "translations": ["سیب"], "note": "میوه",
    "categories": ["خوراکی و غذا"], "entryType": "WORD" }
] }
```

کلید translations می‌تواند به‌جای آرایه یک متن (translation) باشد که با بخش 5.3 شکسته می‌شود.

ب. اگر فایل کلید backupType دارد، «فایل پشتیبان» است. صفحه ورود آن را رد می‌کند و کاربر را به «بازیابی» راهنمایی می‌کند (پیام: «این فایل پشتیبان است. از بخش بازیابی استفاده کنید»).

### XLSX

بدون کتابخانه سنگین. فایل XLSX یک zip است.

```text
۱. باز کردن zip با java.util.zip
۲. خواندن xl/sharedStrings.xml با XmlPullParser: هر <si> یک رشته (ترکیب همه <t> داخل آن)
۳. خواندن اولین شیت (xl/worksheets/sheet1.xml):
     هر <c r="A1" t="..."> با <v> یا <is><t>
     t="s" : مقدار اندیس در sharedStrings
     t="inlineStr": متن <is><t>
     دیگر: مقدار عددی <v> به‌صورت متن
۴. ستون‌ها از حرف ارجاع سلول (A، B، ...) و ردیف‌ها از شماره
۵. سپس همان قواعد CSV (عنوان اختیاری، ستون‌ها)
```

فقط اولین شیت خوانده می‌شود.

### SQLite

فایل انتخابی کاربر روی حافظه موقت کپی و فقط‌خواندنی باز می‌شود (android.database.sqlite).
قالب‌های پذیرفته‌شده به ترتیب بررسی:

۱. دیتابیس یادین یا فلش‌لرن: وجود جدول‌های concepts و contents (و categories یا concept_categories):

```text
هر مفهوم: متن مبدأ = محتوای با languageCode برابر زبان مبدأ جفت فعال (translationIndex کمینه)
ترجمه‌ها = محتواهای languageCode مقصد به ترتیب translationIndex
یادداشت = note محتوای مبدأ
دسته: اگر concept_categories وجود دارد از آن؛ وگرنه اگر concepts.categoryId و جدول categories دارد، نام آن
نوع: concepts.entryType اگر معتبر
فقط مفاهیمی که active=1 هستند
```

۲. جدول ساده: جدولی با نام words یا vocabulary که ستون‌های source و translation دارد (و اختیاری note و category).

۳. هیچ‌کدام: خطای «ساختار این فایل پشتیبانی نمی‌شود».

---

## 6.11 یافتن مفهوم برای مدخل و اجرای ورود

### رابط ورود

کاربر پس از خواندن فایل، یک صفحه «پیش‌نمایش» می‌بیند:

```text
تعداد مدخل‌ها ، جدید ، تکراری ، اعتماد پایین ، هشدارها
انتخاب سیاست تکراری (الزامی، پیش‌فرض ادغام): رد کردن / ادغام / جایگزینی / نگه‌داری جدا
انتخاب دسته‌های دلخواه برای همه مدخل‌های بدون دسته (اختیاری)
دکمه «وارد کن»
```

سیاست توسط کاربر انتخاب می‌شود و بدون انتخاب (پیش‌فرض ادغام) نیز صریحاً نمایش داده می‌شود.

### گام ۱: غربال اعتماد

```text
برای هر ParsedEntry:
    اگر confidence < 0.80 یا translations خالی یا source خالی:
        بدون ایجاد مفهوم به import_review_items اضافه شود (reason مناسب، status=PENDING)
        رد به مدخل بعد
    اگر تعداد ترجمه > 10: دلیل TOO_MANY_TRANSLATIONS به review items
    اگر یادداشت > 500 کاراکتر: دلیل NOTE_TOO_LONG به review items
```

### گام ۲: ResolveConcept

```text
sourceKey = canonicalKey(cleanText(source))
matched = شناسه‌های مفاهیم فعال با محتوای (languageCode = مبدأ ، canonicalKey = sourceKey)
```

نتیجه:

| |matched| | نتیجه |
|----------|-------|
| 0 | CreateNew |
| 1 | Reuse(conceptId) |
| بیشتر از 1 | Conflict(matchedIds) |

### گام ۳: اعمال سیاست

```text
CreateNew  → ساخت مفهوم (بخش 6.7 CreateConcept) ؛ شمارنده created

Reuse(conceptId) :
   SKIP          → هیچ تغییری ؛ شمارنده skipped
   MERGE         → فقط ترجمه‌های جدید (canonicalKey جدید در مقصد) با translationIndex بعدی اضافه شود
                    دسته‌های جدید اضافه شود (دسته‌های قبلی حفظ)
                    یادداشت: اگر مفهوم یادداشت ندارد و ورودی دارد گذاشته شود
                    اگر ترجمه جدیدی نبود و دسته جدیدی نبود: شمارنده duplicateNoChange ؛ وگرنه merged
   REPLACE       → ترجمه‌های مقصد مفهوم کامل با ترجمه‌های ورودی جایگزین (ترتیب ورودی ، translationIndex از 0)
                    یادداشت ورودی (اگر غیرخالی) جایگزین یادداشت قبلی
                    اگر ورودی دسته دارد: دسته‌های مفهوم با آن جایگزین
                    کارت‌ها (مرحله، سختی، تاریخچه) دست نمی‌خورند ؛ شمارنده replaced
   KEEP_SEPARATE → ساخت مفهوم جدید (حتی با مبدأ تکراری) ؛ شمارنده created

Conflict(matchedIds) :
   SKIP          → skipped
   KEEP_SEPARATE → ساخت مفهوم جدید
   MERGE یا REPLACE → ادغام خودکار ممنوع است ؛ به import_review_items با reason = CONFLICT (status=PENDING)
```

### گام ۴: اجرا

۱. ورود در دسته‌های 200 تایی؛ هر دسته یک تراکنش مستقل.
۲. شکست یک دسته، دسته‌های موفق قبلی را برنمی‌گرداند؛ مدخل‌های آن دسته در گزارش نهایی «ناموفق» می‌آیند و وارد import_review_items با reason = INVALID_ROW می‌شوند.
۳. نوار پیشرفت: تعداد پردازش‌شده از کل.
۴. پس از پایان، حافظه نهان آزمون باطل می‌شود (بخش 6.6).
۵. گزارش نهایی: ساخته‌شده، ادغام‌شده، جایگزین‌شده، رد‌شده، بدون تغییر (تکراری)، به بررسی رفته، ناموفق.

### صف بررسی (import_review_items)

صفحه «بررسی ورود»: فهرست موارد PENDING با دلیل، متن خام و دکمه‌ها.
هر مورد:
- قابل ویرایش (مبدأ، ترجمه‌ها، یادداشت، دسته‌ها).
- «تایید»: مدخل با confidence = 1.0 و همان سیاست ثبت‌شده در ردیف اجرا می‌شود (برای CONFLICT کاربر باید سیاست را به SKIP یا KEEP_SEPARATE یا انتخاب مفهوم مقصد برای ادغام دستی تغییر دهد). status = APPROVED
- «رد»: status = REJECTED
موارد APPROVED و REJECTED قابل پاک‌سازی دستی هستند.

---

## 6.12 بانک اولیه (Seed)

### منبع

فایل assets/seed/vocabulary_seed.json (کپی عینی docs/Vocabulary.json).
ساختار فایل (قالب پشتیبان یادین/فلش‌لرن):

```text
schemaVersion, exportedAt, backupType
payloads → VOCABULARY → concepts[], contents[], categories[], tags[], relations[], variants[], languages[], languagePairs[]
```

مفهوم: {id, entryType, categoryId, favorite, active, createdAt, updatedAt}
محتوا: {id, conceptId, languageCode, text, canonicalKey, translationIndex، و اختیاری notes}
دسته: {id, name}
(در فایل فعلی: 6022 مفهوم که 5988 تای آن فعال است، 14149 محتوا، 10 دسته؛ relations و variants و tags خالی)

### زمان اجرا

هنگام اولین اجرا، بعد از ساخت دیتابیس:

```text
اگر settings["seedImported"] != "true":
    اگر هیچ مفهومی در دیتابیس نیست: اجرای SeedImporter
    تنظیم settings["seedImported"] = "true"
```

### قواعد SeedImporter

```text
۱. languages و language_pairs اولیه (بخش 4.3) درج شوند.
۲. ده دسته رسمی (جدول 14.1) درج شوند.
۳. نگاشت دسته‌های فایل به دسته‌های رسمی با جدول 14.1
۴. فقط مفاهیم با active = true وارد می‌شوند (D27)
۵. favorite و relations و variants و tags فایل نادیده گرفته می‌شوند
۶. نسبت به هر محتوا:
     text = cleanText(text)
     canonicalKey = canonicalKey(text) (دوباره محاسبه می‌شود)
     اگر note برابر «واژهٔ نمونهٔ اولیه FlashLearn» (یا با آن شروع شود) بود: note = null (D28)
     translationIndex از فایل؛ در پایان برای هر (مفهوم، زبان) به 0..n-1 پشت‌سرهم بازنویسی می‌شود
۷. مفهوم بدون محتوای مبدأ یا بدون محتوای مقصد وارد نمی‌شود
۸. مفاهیم فعال با کلید مبدأ تکراری با منطق ادغام (MERGE) یکی می‌شوند (در فایل فعلی بعد از حذف غیرفعال‌ها تکراری وجود ندارد؛ این منطق برای ایمنی و فایل‌های آینده است):
     مفهوم اول (به ترتیب فایل) می‌ماند ؛ ترجمه‌های جدید مفهوم بعدی به آن اضافه می‌شود ؛ دسته‌های آن هم افزوده می‌شود
۹. شناسه‌های UUID فایل برای مفهوم و محتوای نگه‌داشته‌شده حفظ می‌شود
۱۰. دو کارت برای هر مفهوم (بخش 4.4) ساخته می‌شود
۱۱. درج در دسته‌های 500 تایی، هر دسته یک تراکنش
۱۲. اجرا روی ترد IO با نوار پیشرفت روی صفحه آماده‌سازی («در حال آماده‌سازی واژگان...»)
۱۳. اگر وسط کار قطع شد (بستن برنامه)، در اجرای بعدی چون seedImported برابر true نشده، دوباره از ابتدا اجرا می‌شود:
     پیش از شروع، اگر مفاهیمی وجود دارند ولی seedImported=false است، همه مفاهیم و وابسته‌ها پاک و دوباره درج شوند
```

(مورد 13: چون seedImported فقط پس از موفقیت کامل true می‌شود، بانک ناقص هرگز پذیرفته نمی‌شود.)

### توافق نهایی

پس از اتمام: شمارش مفاهیم فعال باید برابر تعداد مفاهیم فعال یکتای فایل باشد (برای فایل فعلی دقیقاً 5988) و هر مفهوم دو کارت (جمعاً 11976 کارت) داشته باشد. در تست راه‌اندازی (تست ابزاری) بررسی می‌شود.

---

## 6.13 پشتیبان‌گیری (Backup)

### انواع

| نوع | محتوا |
|-----|-------|
| VOCABULARY | languages، language_pairs، categories، tags، concepts، contents، concept_categories، concept_tags |
| PROGRESS | learning_states، difficulty_states، review_sessions، review_history، settings، achievements |
| FULL | هر دو |

### قالب فایل

یک فایل جیسون UTF-8 بدون رمز و بدون فشرده‌سازی.

```json
{
  "format": "yadin-backup",
  "schemaVersion": 1,
  "exportedAt": "2026-10-04T10:00:00Z",
  "backupType": "FULL",
  "data": {
    "languages": [{"code":"es","name":"Español","active":true}],
    "languagePairs": [{"source":"es","target":"fa","active":true}],
    "categories": [{"id":"...","name":"...","sortOrder":1,"isDefault":true}],
    "tags": [{"id":"...","name":"..."}],
    "concepts": [{"id":"...","entryType":"WORD","active":true,"createdAt":0,"updatedAt":0}],
    "contents": [{"id":"...","conceptId":"...","languageCode":"es","text":"...","canonicalKey":"...","note":null,"pronunciation":null,"translationIndex":0}],
    "conceptCategories": [{"conceptId":"...","categoryId":"..."}],
    "conceptTags": [{"conceptId":"...","tagId":"..."}],
    "learningStates": [{"id":"...","conceptId":"...","direction":"NORMAL","stage":"DAILY","nextReviewDay":"2026-10-05","lastReviewedDay":null,"createdAt":0,"updatedAt":0}],
    "difficultyStates": [{"id":"...","conceptId":"...","direction":"NORMAL","current":"EASY","consecutiveCorrect":0,"consecutiveWrong":0,"hasReachedVeryHard":false}],
    "reviewSessions": [{"id":"...","startedAt":0,"endedAt":0,"reviewType":"DAILY","mode":"FLASHCARD","direction":"NORMAL","quizLevel":null}],
    "reviewHistory": [{"id":"...","sessionId":"...","reviewAttemptId":"...","conceptId":"...","direction":"NORMAL","reviewedAt":0,"reviewedDay":"2026-10-04","isCorrect":true,"reviewType":"DAILY","mode":"FLASHCARD","stageBefore":"DAILY","quizLevel":null,"options":null,"selectedIndex":null,"correctIndex":null}],
    "settings": [{"key":"themeId","value":"grok"}],
    "achievements": [{"achievementId":"FIRST_TEN_WORDS","unlockedAt":0}]
  }
}
```

قواعد قالب:

۱. روزها به‌صورت متن ISO ‏yyyy-MM-dd و nextReviewDay برای LEARNED برابر null.
۲. فقط بخش‌های مربوط به نوع پشتیبان در data می‌آیند (بخش‌های دیگر وجود ندارند).
۳. UUID تنها شناسه پایدار بین پشتیبان و بازیابی است.
۴. فایل با ساخت استریم (JsonWriter) نوشته می‌شود تا برای 6000 مفهوم حافظه زیاد مصرف نشود.
۵. نام فایل پیشنهادی: yadin-backup-FULL-2026-10-04.json
۶. ذخیره از طریق Storage Access Framework (ACTION_CREATE_DOCUMENT) و بازیابی از طریق ACTION_OPEN_DOCUMENT.
۷. نوار پیشرفت هنگام نوشتن. پیام موفقیت: «با موفقیت انجام شد».
۸. تنظیمات داخل پشتیبان: همه کلیدهای جدول settings به‌جز seedImported و کلیدهای نسخه داده.

---

## 6.14 بازیابی (Restore)

### جریان

```text
۱. کاربر فایل را انتخاب می‌کند
۲. خواندن و اعتبارسنجی کامل (بدون تغییر دیتابیس)
۳. نمایش خلاصه: نوع پشتیبان، تعداد مفاهیم، تعداد تاریخچه، تاریخ پشتیبان
۴. کاربر روش را انتخاب می‌کند: ادغام (MERGE) یا جایگزینی (REPLACE). بدون انتخاب صریح بازیابی انجام نمی‌شود
۵. اگر REPLACE: هشدار «اطلاعات فعلی حذف می‌شود» و تایید جدا
۶. پشتیبان ایمنی خودکار از وضعیت فعلی (FULL) در حافظه خصوصی برنامه ساخته می‌شود
   اگر شکست خورد: هشدار و دکمه لغو؛ ادامه فقط با تایید صریح کاربر
۷. اجرای بازیابی در یک تراکنش واحد
۸. پس از commit: اجرای اعتبارسنجی یکپارچگی (بخش 6.20) و در صورت نیاز EnsureStates
۹. باطل کردن حافظه نهان آزمون
۱۰. گزارش: افزوده‌شده، ادغام‌شده، رد‌شده، هشدارها
```

### اعتبارسنجی (گام 2)

شکست در هر مورد یعنی توقف با پیام فارسی و بدون هیچ تغییری:

```text
 1. فایل جیسون معتبر و format = yadin-backup
 2. schemaVersion پشتیبانی شود (1 تا نسخه فعلی)
 3. backupType یکی از سه نوع و بخش‌های لازم آن نوع موجود
 4. همه UUIDها معتبر
 5. UUID تکراری در یک بخش وجود نداشته باشد
 6. مرجع‌ها معتبر: contents.conceptId در concepts ؛ concept_categories به مفاهیم و دسته‌ها ؛
    concept_tags ؛ learningStates و difficultyStates به مفهوم ؛ reviewHistory به نشست و مفهوم ؛
    (برای نوع PROGRESS مرجع به مفهوم به دیتابیس فعلی نگاه می‌کند، بند «پیشرفت» پایین)
 7. مقادیر enum معتبر ؛ تاریخ‌ها قابل خواندن ؛ شمارنده‌ها منفی نباشند
 8. (conceptId, direction) در learningStates و difficultyStates یکتا
 9. (conceptId, languageCode, translationIndex) در contents یکتا
10. (sessionId, reviewAttemptId) در reviewHistory یکتا
11. نوع VOCABULARY شامل داده پیشرفت نباشد و نوع PROGRESS شامل مفهوم نباشد
12. stage = LEARNED اگر و فقط اگر nextReviewDay = null
```

### بازیابی واژگان

نگاشت: برای هر مفهوم پشتیبان یک شناسه محلی تعیین می‌شود:

```text
resolveLocal(backupConcept):
    اگر مفهومی با همان UUID در دیتابیس هست: همان
    وگرنه matched = مفاهیم فعال محلی با همان کلید مبدأ
         |matched| == 1: همان ؛ بیشتر از 1: قدیمی‌ترین (createdAt سپس id)
    وگرنه: جدید با همان UUID
```

حالت MERGE:

```text
دسته‌ها و برچسب‌ها: با name تطبیق؛ اگر نبود ساخته شود
برای هر مفهوم:
    جدید: درج مفهوم و محتواها و پیوندها و دو کارت
    موجود: فقط ترجمه‌های جدید (canonicalKey جدید در همان زبان) با translationIndex بعدی اضافه ؛
           دسته‌ها و برچسب‌های جدید اضافه ؛ یادداشت فقط اگر محلی خالی است
    مفهوم محلی غیرفعال و پشتیبان فعال: فعال می‌شود
هیچ داده‌ای حذف نمی‌شود
```

حالت REPLACE:

```text
حذف همه concepts (cascade روی contents و پیوندها و کارت‌ها و تاریخچه)
حذف categories و tags و جایگزینی با پشتیبان
درج همه داده پشتیبان
```

یادآوری: در REPLACE واژگان، پیشرفت قبلی هم با حذف مفاهیم پاک می‌شود (هشدار در گام 5 این را باید صریح بگوید).

### بازیابی پیشرفت

مفاهیم فقط با UUID شناسایی می‌شوند. ردیفی که مفهومش در دیتابیس نیست رد می‌شود و در گزارش «N ردیف پیشرفت بدون مفهوم رد شد» می‌آید (خطا نیست).

حالت MERGE:

```text
learning_states و difficulty_states (بر اساس conceptId و direction):
    اگر کارت محلی هنوز مرور نشده (lastReviewedDay = null): ردیف پشتیبان جایگزین می‌شود
    اگر ردیف پشتیبان lastReviewedDay دارد و از محلی جدیدتر است: پشتیبان جایگزین می‌شود
      (ردیف state و difficulty هر دو از پشتیبان گرفته می‌شود)
    در غیر این صورت: محلی می‌ماند
review_sessions: اگر شناسه نیست درج
review_history: اگر (sessionId, reviewAttemptId) نیست درج (و جلسه‌اش هم درج شود)
settings: فقط کلیدهایی که محلی ندارد
achievements: اجتماع (باز شدن هرگز لغو نمی‌شود)
```

حالت REPLACE:

```text
حذف همه learning_states و difficulty_states و review_history و review_sessions و achievements
درج داده پشتیبان
settings پشتیبان جایگزین شود (به‌جز seedImported)
مفاهیمی که در پشتیبان کارت ندارند: EnsureStates بعد از commit
```

### بازیابی کامل

ابتدا واژگان (بالا)، سپس پیشرفت (بالا) در همان تراکنش.

### ترتیب درج (برای رعایت کلید خارجی)

```text
languages → language_pairs → categories → tags → concepts → contents → concept_categories → concept_tags
→ review_sessions → review_history → learning_states → difficulty_states → settings → achievements
```

### نکته‌ها

۱. کل بازیابی در یک تراکنش است؛ هر استثنا یعنی ROLLBACK کامل.
۲. بازیابی فایل فلش‌لرن (قالب قدیمی) پشتیبانی نمی‌شود؛ ورود واژگان فلش‌لرن از طریق «ورود از SQLite» یا «ورود JSON» انجام می‌شود.
۳. دایرکتوری پشتیبان ایمنی: فقط آخرین 3 پشتیبان ایمنی نگه داشته می‌شود.

---

## 6.15 آمار (Statistics)

آمار فقط‌خواندنی است و از داده واقعی (review_history و وضعیت فعلی) محاسبه می‌شود. هیچ مقدار ذخیره‌شده یا حدسی استفاده نمی‌شود.
همه آمار فقط روی مفاهیم فعال و برای جهت انتخاب‌شده d (تنظیم activeDirection یا کلید صفحه پیشرفت) است، مگر در جدول خلاف آن نوشته شده باشد.
محاسبه‌ها با پرس‌وجوی تجمیعی SQL (COUNT و GROUP BY) انجام می‌شود، نه با بارگذاری کل تاریخچه.

| شاخص | تعریف |
|------|-------|
| totalActiveWords | تعداد مفاهیم فعال |
| learnedWords | تعداد کارت‌های جهت d با stage برابر LEARNED |
| practicedWords | تعداد کارت‌های جهت d با stage غیر LEARNED و lastReviewedDay غیر null |
| unpracticedWords | max(0، totalActiveWords − practicedWords − learnedWords) |
| reviewCount | تعداد ردیف‌های review_history جهت d |
| correctCount | از همان ردیف‌ها با isCorrect=1 |
| wrongCount | reviewCount − correctCount |
| accuracyPercent | اگر reviewCount=0 صفر وگرنه بخش صحیح از (correctCount × 100 / reviewCount) |
| stageDistribution | تعداد کارت‌های جهت d به تفکیک stage |
| difficultyDistribution | تعداد کارت‌های جهت d به تفکیک current |
| dueDaily، dueWeekly، dueMonthly | شمارش بخش 6.5 با همان شرط‌ها |
| trend | تعداد پاسخ‌ها (reviewCount) در هر یک از 4 هفته اخیر؛ هفته i شامل روزهای (امروز − 7i − 6) تا (امروز − 7i) |

ردیف‌های تاریخچه مفاهیم غیرفعال در هیچ‌کدام از شاخص‌ها شمرده نمی‌شوند.

---

## 6.16 رگبار (Streak)

رگبار سراسری است (هر دو جهت).

```text
ورودی: مجموعه روزهای متمایز reviewedDay از ردیف‌های review_history مفاهیم فعال ، امروز
اگر مجموعه خالی: current = 0 ، longest = 0
mostRecent = بزرگ‌ترین روز
اگر mostRecent != امروز و mostRecent != امروز − 1:
    current = 0
وگرنه:
    current = تعداد روزهای متوالی (فاصله دقیقاً 1) که از mostRecent به عقب می‌رود
longest = طولانی‌ترین رشته روزهای متوالی در کل مجموعه
```

رگبار فقط با ثبت پاسخ واقعی مرور ساخته می‌شود. باز کردن صفحه یا شروع جلسه بدون پاسخ رگبار نمی‌سازد.
تمرین فلش‌کارت و آزمون هر دو حساب می‌شوند (D16).

---

## 6.17 دستاوردها (Achievements)

دستاورد از داده پایدار محاسبه می‌شود و هیچ‌وقت صرفاً با تغییر نمایش باز نمی‌شود.

| شناسه | عنوان فارسی | شرط |
|-------|-------------|-----|
| FIRST_TEN_WORDS | اولین ۱۰ واژه | تعداد مفاهیم فعال متمایز دارای حداقل یک ردیف تاریخچه (هر جهت) بزرگ‌تر یا مساوی 10 |
| SEVEN_DAY_STREAK | هفت روز پیوسته | current بزرگ‌تر یا مساوی 7 |
| THIRTY_DAY_STREAK | سی روز پیوسته | current بزرگ‌تر یا مساوی 30 |
| MEMORY_BUILDER | سازنده حافظه | تعداد کارت‌های LEARNED (هر دو جهت، مفاهیم فعال) بزرگ‌تر یا مساوی 100 |
| VOCABULARY_BUILDER | سازنده واژگان | تعداد مفاهیم فعال متمایز تمرین‌شده بزرگ‌تر یا مساوی 500 |
| HARD_MODE_MASTER | استاد سخت | تعداد کارت‌های دارای hasReachedVeryHard=1 و stage=LEARNED بزرگ‌تر یا مساوی 25 |
| LONG_TERM_MEMORY | حافظه بلندمدت | تعداد کارت‌های (مفهوم و جهت) متمایز که پاسخ درست با stageBefore برابر MONTHLY داشته‌اند، بزرگ‌تر یا مساوی 50 |

قواعد:

۱. هر دستاورد فقط یک‌بار باز می‌شود. باز شدن هرگز لغو نمی‌شود.
۲. بررسی پس از هر جلسه مرور (و بعد از بازیابی) انجام می‌شود.
۳. دستاورد تازه باز شده در صفحه نتایج جلسه به‌صورت یک پیام ساده نشان داده می‌شود.
۴. صفحه پیشرفت فهرست همه دستاوردها را با حالت باز و قفل نشان می‌دهد.
۵. مقدارها از SQL تجمیعی خوانده می‌شوند.

---

## 6.18 درصد پیشرفت

برای جهت d روی همه مفاهیم فعال:

```text
امتیاز هر کارت:
    lastReviewedDay = null  => 0
    LEARNED => 100
    MONTHLY => 80
    WEEKLY  => 60
    DAILY   => 35
progressPercent = مجموع امتیازها / تعداد مفاهیم فعال
(اگر مفهوم فعالی نیست: 0)
```

نمایش با یک رقم اعشار گرد می‌شود.

---

## 6.19 بروزرسانی و مهاجرت داده (Refresh)

دو مسیر مستقل:

۱. مهاجرت ساختار (Room): با Migration رسمی بین نسخه‌های schema.
۲. مهاجرت محتوای داده (Refresh): برای اصلاح داده‌های موجود بدون پاک کردن.

### مهاجرت ساختار

۱. هر افزایش نسخه schema یک شیء Migration صریح دارد.
۲. fallbackToDestructiveMigration ممنوع است.
۳. برای هر Migration تست ابزاری با MigrationTestHelper نوشته می‌شود.
۴. داده کاربر نباید از بین برود.

### Refresh

نسخه‌ها در settings با کلیدهای dataVersion.concept و dataVersion.content نگه داشته می‌شوند. ثابت‌های برنامه:

```kotlin
const val CURRENT_CONCEPT_DATA_VERSION = 1
const val CURRENT_CONTENT_DATA_VERSION = 1
```

الگوریتم (در هر بار اجرای برنامه، بعد از Room و قبل از نمایش صفحه اصلی؛ در یک تراکنش):

```text
conceptVersion = settings[dataVersion.concept] (پیش‌فرض 0)
contentVersion = settings[dataVersion.content] (پیش‌فرض 0)

تا وقتی conceptVersion < CURRENT_CONCEPT_DATA_VERSION:
    conceptVersion = اجرای گام migrateConcept(conceptVersion)
    ذخیره در settings
تا وقتی contentVersion < CURRENT_CONTENT_DATA_VERSION:
    contentVersion = اجرای گام migrateContent(contentVersion)
    ذخیره در settings

اگر پس از حلقه نسخه‌ای با ثابت برابر نشد: خطا (نسخه ناشناخته)
```

گام‌های نسخه 1:

```text
migrateConcept 0 → 1:
    برای هر مفهوم فعال:
        اگر updatedAt < createdAt : updatedAt = createdAt
    EnsureStates (بخش 6.20) برای ساخت کارت‌های ناقص

migrateContent 0 → 1:
    برای هر محتوا: text = cleanText(text) ؛ canonicalKey = canonicalKey(text) ؛ اگر تغییر کرد ذخیره
    محتواهای با text خالی حذف شوند
    برای هر (مفهوم، زبان): translationIndex به 0..n-1 پشت‌سرهم بازنویسی شود (ترتیب فعلی حفظ)
    برای هر (مفهوم، زبان): محتواهای با canonicalKey تکراری فقط اولی بماند
```

ناوردا: هر گام تکرارپذیر (idempotent) است. اجرای دوباره نباید چیزی عوض کند.
افزودن گام جدید: ثابت CURRENT را 1 واحد زیاد کن و یک گام جدید در when بنویس و تست بنویس.
پس از ساخت بانک اولیه یا بازیابی، نسخه‌ها روی مقدار جاری تنظیم می‌شوند.

---

## 6.20 اعتبارسنجی یکپارچگی داده (Integrity)

### EnsureStates

```text
برای هر مفهوم فعال و برای هر direction در (NORMAL, REVERSE):
    اگر learning_states ندارد: ردیف جدید stage=DAILY ، nextReviewDay=امروز ، lastReviewedDay=null
    اگر difficulty_states ندارد: ردیف جدید current=EASY و شمارنده‌ها 0 و hasReachedVeryHard=0
```

فقط هنگام راه‌اندازی، بازیابی و Refresh فراخوانی می‌شود، نه هنگام ثبت پاسخ.

### ValidateIntegrity

خروجی: فهرست مشکل‌ها. موارد:

```text
LEARNING_STATE_CARDINALITY   : مفهوم فعالی که برای یک جهت دقیقاً یک ردیف learning_states ندارد
DIFFICULTY_STATE_CARDINALITY : مشابه برای difficulty_states
LEARNED_HAS_DUE_DATE         : LEARNED با nextReviewDay غیر null
ACTIVE_WITHOUT_DUE_DATE      : غیر LEARNED با nextReviewDay برابر null
NEGATIVE_COUNTER             : شمارنده منفی
DIFFICULTY_COUNTER_CONFLICT  : هر دو شمارنده بزرگ‌تر از 0
TRANSLATION_INDEX_GAP        : translationIndex پشت‌سرهم نیست
MISSING_SOURCE_CONTENT       : مفهوم فعال بدون محتوای مبدأ
MISSING_TARGET_CONTENT       : مفهوم فعال بدون محتوای مقصد
```

رفتار: پس از بازیابی و در Refresh اجرا می‌شود. مشکل‌های قابل تعمیر خودکار (کاردینالیتی کارت‌ها، شمارنده منفی به 0، فاصله translationIndex) تعمیر می‌شوند؛ باقی در گزارش (Log) می‌آیند. کاربر پیام فنی نمی‌بیند مگر مشکل تعمیرنشدنی باشد (پیام کوتاه «بخشی از داده نیاز به بررسی دارد» در صفحه تنظیمات).

---

## 6.21 تلفظ (TTS)

فقط Text-to-Speech بومی اندروید (android.speech.tts.TextToSpeech). هیچ سرویس خارجی.

```text
۱. TTS یک‌بار هنگام نیاز ساخته می‌شود (تنبل) و با onDestroy ثبت‌کننده آزاد می‌شود
۲. زبان متن = languageCode محتوا (es → Locale("es") ، fa → Locale("fa") ، en → Locale.ENGLISH)
۳. نتیجه setLanguage:
     LANG_MISSING_DATA یا LANG_NOT_SUPPORTED:
         پیام کوتاه «موتور تلفظ دستگاه این زبان را پشتیبانی نمی‌کند»؛ بدون خطای بسته‌شدن برنامه
     وگرنه: speak(text, QUEUE_FLUSH)
۴. متن گفتاری = text خام محتوا (برای چند معنی فقط اولین در جهت REVERSE که سمت سؤال چند معنی دارد؛ در جهت NORMAL متن مبدأ)
۵. سرعت گفتار: تنظیم ttsSpeechRate (0.5 تا 2.0 ، پیش‌فرض 1.0)
۶. دکمه صدا فقط روی «سمت سؤال» (D15) در فلش‌کارت، آزمون و کتابخانه/جزئیات واژه
۷. اگر ttsEnabled=false دکمه نمایش داده نمی‌شود
۸. اگر ttsAutoPlay=true هنگام نمایش هر سؤال یک‌بار پخش می‌شود
```

---

# 7. صفحه‌ها و رفتار رابط کاربری

## 7.1 قواعد عمومی رابط

۱. Material 3 و Jetpack Compose. ظاهر گرد، تمیز و واکنش‌گرا (طرح‌بندی برای عرض 360dp و بالاتر؛ در عرض بیشتر از 600dp محتوا وسط‌چین با حداکثر عرض 720dp).
۲. جهت صفحه: زبان رابط فارسی راست‌به‌چپ و انگلیسی چپ‌به‌راست. برای تعیین جهت در ریشه برنامه:

```kotlin
CompositionLocalProvider(
    LocalLayoutDirection provides if (uiLanguage == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
) { ... }
```

   فلش‌ها و آیکن‌های جهت‌دار با autoMirrored آینه می‌شوند. باید تست رابط وجود داشته باشد که جهت وارونه نشود (در فلش‌لرن یک‌بار این اشکال پیش آمد).
۳. هیچ رنگ، اندازه یا فاصله مستقیم در صفحه‌ها نوشته نمی‌شود. همه از توکن‌های تم (بخش 8) می‌آیند.
۴. همه متن‌ها در strings.xml با دو زبان fa و en.
۵. ارقام: وقتی زبان رابط فارسی است ارقام فارسی (۰۱۲۳) نمایش داده می‌شود.
۶. هر صفحه سه حالت دارد: در حال بارگذاری، خالی (پیام و دکمه عمل)، خطا (پیام کوتاه و دکمه «تلاش دوباره»).
۷. عملیات سنگین (ورود، بازیابی، بانک اولیه، ساخت صف بزرگ) روی ترد IO و با نشانگر پیشرفت. ترد اصلی هیچ‌وقت بیش از چند میلی‌ثانیه مسدود نشود.
۸. انیمیشن و میکرو‌تعامل کم و کوتاه (حداکثر 300 میلی‌ثانیه) و قابل غیرفعال‌شدن با تنظیمات سیستم «حذف انیمیشن‌ها».
۹. برای فهرست‌های بزرگ (کتابخانه) از Paging 3 استفاده می‌شود.
۱۰. دکمه بازگشت سیستم در همه صفحه‌ها رفتار طبیعی دارد. در جلسه مرور، بازگشت پنجره تایید خروج نشان می‌دهد.

## 7.2 ناوبری

نوار پایین چهار زبانه: خانه، کتابخانه، پیشرفت، تنظیمات.

مسیرها (Navigation Compose):

| مسیر | صفحه |
|------|------|
| splash | آماده‌سازی |
| tutorial | آموزش اولین اجرا |
| home | خانه |
| reviewSetup/{reviewType} | تنظیم مرور |
| review/{sessionId} | فلش‌کارت یا آزمون (بر اساس mode جلسه) |
| results/{sessionId} | نتایج |
| library | کتابخانه |
| wordDetail/{conceptId} | جزئیات واژه |
| addWord | افزودن (زبانه‌های تکی و گروهی) |
| editWord/{conceptId} | ویرایش |
| importPreview | پیش‌نمایش ورود |
| importReview | بررسی موارد ورود |
| progress | پیشرفت |
| settings | تنظیمات |
| backup | پشتیبان‌گیری و بازیابی |
| help | راهنما (همان آموزش) |
| about | درباره |

## 7.3 آماده‌سازی (Splash)

۱. نمایش لوگو و نام «یادین».
۲. مراحل پشت صحنه به ترتیب: باز کردن دیتابیس (Room Migration) ← بانک اولیه (اگر لازم) ← Refresh (بخش 6.19) ← EnsureStates ← بارگذاری تنظیمات.
۳. حداقل نمایش 2 ثانیه (طبق سند طراحی).
۴. اگر بانک اولیه در حال ساخت است: نوار پیشرفت و متن «در حال آماده‌سازی واژگان...».
۵. اولین اجرا: بعد از آماده‌سازی، صفحه آموزش نمایش داده می‌شود (قابل رد کردن و تکرار در تنظیمات)؛ مقدار settings["tutorialSeen"].
۶. خطای راه‌اندازی: پیام «خطای پایگاه داده. دوباره شروع کنید» و دکمه تلاش مجدد.

## 7.4 خانه

```text
عنوان: یادین
نوار انتخاب جهت: [ عادی | برعکس ] (تغییر آن activeDirection را در تنظیمات ذخیره می‌کند)
بنر رگبار: «رگبار: N روز» و «بهترین: M روز»
کارت‌های مرور (هر کدام عنوان، تعداد آماده، دکمه شروع):
    روزانه   (DAILY)
    هفتگی    (WEEKLY)
    ماهانه   (MONTHLY)
    تصادفی   (RANDOM) : تعداد = جمع سررسیدهای سه مرحله
    یادگرفته‌ها (LEARNED): تعداد = کارت‌های یادگرفته
خلاصه: کل واژه‌ها ، یادگرفته (درصد) ، دقت ، آماده امروز
```

۱. شمارنده‌ها دقیقاً با همان شرط‌های بخش 6.5 محاسبه می‌شوند (جهت = activeDirection).
۲. اگر شمارنده یک کارت صفر است، دکمه شروع غیرفعال و زیرعنوان «برای امروز چیزی نیست ✓» نمایش داده می‌شود.
۳. با زدن شروع، صفحه تنظیم مرور باز می‌شود (reviewType مشخص).
۴. شمارنده‌ها هنگام برگشت به صفحه به‌روز می‌شوند.

## 7.5 تنظیم مرور

```text
نوع مرور: فلش‌کارت | آزمون        (پیش‌فرض از تنظیمات defaultReviewMode)
سطح آزمون: مبتدی | متوسط | حرفه‌ای  (فقط وقتی آزمون ؛ پیش‌فرض defaultQuizLevel)
جهت: «اسپانیایی ← فارسی» (عادی) | «فارسی ← اسپانیایی» (برعکس)   (پیش‌فرض activeDirection)
فیلتر دسته‌ها: چندانتخابی (پیش‌فرض همه)
فیلتر سختی: چندانتخابی (پیش‌فرض همه)
فیلتر برچسب: اختیاری
حداکثر کارت: لغزنده 1 تا 100 (پیش‌فرض از تنظیمات maxReviewCards)
شمارنده زنده: «N کارت آماده» (بعد از هر تغییر فیلتر با تاخیر 200 میلی‌ثانیه)
دکمه: شروع مرور
```

۱. تغییرات این صفحه در تنظیمات دائمی ذخیره نمی‌شوند (فقط برای این جلسه).
۲. شمارنده زنده از همان تابع بخش 6.5 با ReviewFilters ساخته می‌شود.
۳. اگر شمارنده صفر است: دکمه غیرفعال، پیام «هیچ کلمه‌ای برای این مرور نیست ✓» و پیشنهاد «مرور تصادفی» یا «افزودن واژه».
۴. با زدن شروع: ReviewSession ساخته (endedAt=null) و صف انتخاب (6.5) در همان لحظه ثابت می‌شود، سپس صفحه مرور باز می‌شود.
۵. سطح آزمون و حالت آزمون در جلسه ثبت می‌شود.

## 7.6 صفحه فلش‌کارت

حالت اول (پنهان):

```text
هدر: پیشرفت «N از M» ، دکمه خروج
متن بزرگ سمت سؤال (جهت NORMAL: متن مبدأ ؛ جهت REVERSE: همه ترجمه‌های فارسی با « / »)
دکمه صدا (طبق 6.21)
دکمه «نمایش پاسخ»
```

حالت دوم (آشکار):

```text
سمت سؤال بالا
خط جداکننده
جواب: جهت NORMAL: همه ترجمه‌ها به صورت فهرست نقطه‌دار (D14) ؛ جهت REVERSE: متن مبدأ
یادداشت (اگر دارد): «یادداشت: ...»
دو دکمه: [✓ درست] [✗ غلط]
```

۱. زدن درست یا غلط: بخش 6.4 اجرا می‌شود (مود FLASHCARD). سپس کارت بعدی نشان داده می‌شود؛ پایان صف یعنی صفحه نتایج.
۲. ارائه کارت با ظاهر تم (reviewPresentation: STANDARD، SWIPE_STACK، FLIP_FULLSCREEN) فقط یک پوشش بصری است و منطق یکسان.
۳. اگر ثبت پاسخ خطا بدهد (مثلاً ALREADY_REVIEWED_TODAY در رقابت دو جلسه): کارت رد می‌شود و پیام کوتاه نشان داده می‌شود؛ جلسه ادامه می‌یابد.

## 7.7 صفحه آزمون

```text
هدر: «N از M»
سؤال: متن سمت سؤال + دکمه صدا
چهار گزینه (کارت‌های قابل لمس)
```

۱. برای هر کارت، سؤال بخش 6.6 ساخته می‌شود. آرگومان usedDistractorKeys تجمیع گزینه‌های غلط جلسه است.
۲. اگر نتیجه FlashcardFallback باشد، همان کارت با رابط فلش‌کارت نشان داده می‌شود و mode ثبت‌شده FLASHCARD است. (پیام اضافه نمایش داده نمی‌شود.)
۳. تپ روی گزینه: گزینه‌ها قفل می‌شوند. گزینه صحیح سبز؛ اگر انتخاب غلط بود، آن قرمز.
۴. بازخورد متنی «درست!» یا «غلط!» و بعد انتقال خودکار پس از quizAutoAdvanceSeconds (پیش‌فرض 3 ثانیه) یا با دکمه «بعدی».
۵. ثبت پاسخ (6.4) بلافاصله پس از تپ انجام می‌شود، نه پس از انتظار. ثبت شامل optionsJson و selectedIndex و correctIndex و quizLevel است.
۶. آماده‌سازی سؤال بعدی (حتی ساخت گزینه‌ها) در پس‌زمینه در زمان انتظار انجام می‌شود تا تاخیری حس نشود.

## 7.8 نتایج جلسه

```text
دقت (نوار و درصد) ، درست N از M ، غلط K از M ، مدت جلسه
رگبار فعلی
دستاوردهای تازه (اگر هست)
دکمه‌ها: [دوباره شروع] (برگشت به تنظیم مرور) و [بازگشت] (خانه)
```

endedAt جلسه هنگام ورود به این صفحه (یا خروج زودهنگام کاربر) ثبت می‌شود.
اگر کاربر وسط جلسه خارج شود: پاسخ‌های داده‌شده ذخیره‌اند؛ جلسه با endedAt برابر لحظه خروج بسته می‌شود؛ نتایج نمایش داده نمی‌شود.

## 7.9 کتابخانه

```text
جستجو (با تاخیر 300 میلی‌ثانیه): در متن مبدأ و ترجمه‌ها (canonicalKey شامل عبارت جستجو که خودش canonicalKey شده)
فیلترها: دسته ، سختی (جهت فعال) ، مرحله (جهت فعال) ، برچسب ، وضعیت (فعال | غیرفعال)
فهرست صفحه‌بندی‌شده: هر آیتم: متن مبدأ ، ترجمه‌ها ، دسته‌ها ، نشان مرحله و سختی (جهت فعال) ، دکمه‌های ویرایش و حذف
دکمه شناور «+» برای افزودن
```

۱. ترتیب: بر اساس متن مبدأ (canonicalKey) صعودی.
۲. ضربه روی آیتم: جزئیات واژه.
۳. حذف: پنجره تایید، سپس حذف نرم (6.8). بعد از حذف، پیام با دکمه «بازگردانی» (Snackbar).
۴. آیتم‌های غیرفعال با نشان خاکستری و دکمه «فعال‌سازی».
۵. دکمه صدا روی هر آیتم.
۶. فهرست خالی: پیام و دکمه «افزودن واژه».

## 7.10 جزئیات واژه

نمایش: متن مبدأ + صدا ، همه ترجمه‌ها ، یادداشت ، نوع ، دسته‌ها ، برچسب‌ها.
برای هر جهت (دو کارت): مرحله ، تاریخ مرور بعدی (تاریخ محلی)، سختی ، تعداد درست و غلط (از تاریخچه)، آخرین مرور.
دکمه‌ها: ویرایش ، حذف / فعال‌سازی.

## 7.11 افزودن واژه

دو زبانه:

تکی: فرم بخش 6.7 با اعتبارسنجی همزمان؛ دکمه «اضافه کن» تا زمانی که فرم معتبر نیست غیرفعال است. «+ ترجمهٔ دیگری» تا 10 ترجمه. انتخاب دسته چندانتخابی با امکان ساخت دسته جدید.
گروهی: چسباندن متن یا انتخاب فایل (txt، csv، json، xlsx، db). پس از خواندن، پیش‌نمایش (6.11).

## 7.12 پیش‌نمایش ورود و بررسی ورود

بخش 6.11. در پیش‌نمایش: شمارش‌ها ، فهرست 20 مدخل اول ، هشدارها (قابل باز شدن) ، انتخاب سیاست تکراری ، دسته‌های دلخواه ، دکمه وارد کن.
صفحه بررسی ورود: بخش 6.11 صف بررسی.

## 7.13 پیشرفت

```text
نوار جهت: [عادی | برعکس]
خلاصه: کل ، یادگرفته (درصد) ، تمرین‌شده ، تمرین‌نشده ، دقت ، رگبار
درصد پیشرفت (6.18)
توزیع مرحله: چهار میله
توزیع سختی: چهار میله
روند 4 هفته اخیر: میله‌های تعداد پاسخ هر هفته
دستاوردها: فهرست با حالت باز / قفل
```

نمودارها با Canvas رسم می‌شوند (بدون کتابخانه نمودار).

## 7.14 تنظیمات

```text
ظاهر: تم (کشویی) ، حالت تاریک / روشن
زبان: زبان رابط (فارسی | English) ، جفت زبانی فعال
یادگیری: آستانه سختی (لغزنده 1 تا 5) ، سطح پیش‌فرض آزمون ، حالت پیش‌فرض مرور ، حداکثر کارت هر مرور
آزمون: تاخیر انتقال خودکار (1 تا 10 ثانیه)
تلفظ: فعال بودن ، پخش خودکار ، سرعت گفتار
داده: [پشتیبان‌گیری و بازیابی] ، [پاک‌سازی دائمی غیرفعال‌ها] ، [بررسی ورود]
راهنما: [آموزش برنامه]
درباره: نام ، نسخه ، مخزن گیت‌هاب
```

تغییر تنظیمات بلافاصله ذخیره و اعمال می‌شود (بدون دکمه ذخیره).
تغییر زبان رابط بدون راه‌اندازی مجدد کل برنامه اعمال می‌شود.
تغییر جفت زبانی فعال: صف و آزمون با جفت جدید کار می‌کند؛ مفاهیمی که برای آن جفت محتوا ندارند در صف‌ها نمی‌آیند.

## 7.15 پشتیبان‌گیری و بازیابی

```text
بخش پشتیبان: انتخاب نوع (واژگان | پیشرفت | کامل) ← دکمه «ساخت پشتیبان» ← انتخاب محل ذخیره ← نوار پیشرفت ← پیام «با موفقیت انجام شد»
بخش بازیابی: «انتخاب فایل» ← خلاصه ← انتخاب روش (ادغام | جایگزینی) ← تایید (برای جایگزینی تایید جداگانه) ← نوار پیشرفت ← گزارش
```

## 7.16 آموزش و راهنما

آموزش اولین اجرا: 6 صفحه پیمایشی (Pager) با دکمه «بعدی» ، «رد کردن» و «شروع». متن در بخش 10.
همین محتوا در «راهنما» (تنظیمات) به‌صورت فهرست بخش‌های بازشونده نمایش داده می‌شود.

---

# 8. تم و طراحی

مرجع بصری اصلی: کد فلش‌لرن در مسیر app/src/main/java/com/flashlearn/app/ui/theme (فایل‌های FlashLearnThemeSpec.kt و FlashLearnThemeTokens.kt و FlashLearnTheme.kt).
پیاده‌ساز موظف است آن فایل‌ها را بخواند و رفتار را در یادین بازسازی کند. جدول‌های زیر مقادیر آن کد هستند.

## 8.1 چهار تم داخلی

شناسه‌ها: grok (گروک) ، claud (کلاد) ، modern_minimal (مدرن مینیمال) ، gtp (GTP).
تم پیش‌فرض: grok و حالت تاریک (طبق تنظیمات پیش‌فرض).
هر تم دو حالت روشن و تاریک دارد. همه رنگ‌ها به‌صورت ARGB.

### گروک (grok)

| توکن | روشن | تاریک |
|------|------|-------|
| primary | C79B32 | E0B44C |
| secondary | 9F7925 | F0C65A |
| background | F7F2E8 | 080D13 |
| surface | FFFCF5 | 111820 |
| surfaceVariant | F0E8D8 | 18222D |
| onSurface | 17130C | F7F0E3 |
| onSurfaceVariant | 756A59 | B9B2A6 |
| card | FFFBF2 | 121B24 |
| outline | D8C08A | 6A5833 |
| gradient | C79B32 تا E0B44C | |

مقیاس‌ها: elevationScale 1.35 ، cornerSmall/Medium/Large برابر 14/20/28 ، typographyScale 1.04 ، densityScale 0.97 ، spacingScale 1.0.
طرح: دکمه FILLED ، ناوبری PILL ، آمار GRID_4_COLUMNS ، مرورها HORIZONTAL_CARDS ، کتابخانه GRID_2_COLUMNS ، ارائه مرور SWIPE_STACK ، تزئینات مرور روشن.

### کلاد (claud)

| توکن | روشن | تاریک |
|------|------|-------|
| primary | 2C5F4E | 5DAA92 |
| secondary | 4A7C6E | 7BC9B3 |
| background | FAF9F7 | 1A1A1A |
| surface | FFFFFF | 252525 |
| surfaceVariant | F3F2F0 | 2F2F2F |
| onSurface | 1A1A1A | F5F5F5 |
| onSurfaceVariant | 6B7A76 | A8C4BB |
| card | FFFFFF | 252525 |
| outline | DDD8D4 | 3A3A3A |

مقیاس‌ها: elevationScale 0.8 ، گوشه‌ها 12/16/24 ، بقیه 1.0.
طرح: دکمه OUTLINED ، ناوبری STANDARD ، آمار VERTICAL_LIST ، مرورها VERTICAL_ROWS ، کتابخانه EXPANDED_LIST ، ارائه مرور FLIP_FULLSCREEN ، تزئینات مرور خاموش.

### مدرن مینیمال (modern_minimal)

رنگ‌ها همان گروک؛ اما outline روشن E8D8B6 و تاریک 2C3744؛ مقیاس‌ها همه 1.0 ؛ گوشه‌ها 12/16/24 ؛ طرح پایه: آمار GRID_2X2 ، ناوبری STANDARD ، دکمه FILLED ، مرورها VERTICAL_ROWS ، کتابخانه GRID_2_COLUMNS ، ارائه STANDARD.

### GTP (gtp)

| توکن | روشن | تاریک |
|------|------|-------|
| primary | 7C3AED | A78BFA |
| secondary | 06B6D4 | 22D3EE |
| background | F6F3FF | 090711 |
| surface | FFFFFF | 15101F |
| surfaceVariant | EDE7FF | 21192F |
| onSurface | 171225 | F7F2FF |
| onSurfaceVariant | 6F6485 | C4B9D6 |
| card | FCFAFF | 191222 |
| outline | D8CFF0 | 49375E |

مقیاس‌ها: elevationScale 1.55 ، گوشه‌ها 6/12/18 ، typographyScale 1.07 ، densityScale 0.94 ، spacingScale 0.88 ؛ آیکن FILLED.
طرح: دکمه FILLED ، ناوبری COMPACT ، آمار GRID_4_COLUMNS ، مرورها COMPACT_LIST ، کتابخانه GRID_2_COLUMNS ، ارائه SWIPE_STACK.

## 8.2 رنگ‌های معنایی مشترک

| معنی | روشن | تاریک |
|------|------|-------|
| success | 138A5B | 52D49A |
| warning | F59E0B | FBBF24 |
| error | D92D48 | FF8A9A |
| info | 536DFE | 7C8CFF |
| onPrimary | FFFFFF | FFFFFF |

(در گروک رنگ onPrimary روشن/تاریک همان سفید است.)

## 8.3 توکن‌های اندازه (پایه)

مقادیر پایه (dp) که در تم‌ها با ضرایب مقیاس می‌شوند؛ هر تم می‌تواند بازنویسی کند:

```text
screenPadding 20 ، screenVerticalPadding 12 ، contentPadding 16 ، cardPadding 16
compactPadding 8 ، tinyGap 4 ، microGap 6 ، contentGap 12 ، compactGap 8
sectionGap 16 ، itemGap 8 ، headerHeight 58 ، headerPadding 10
controlHeight 52 ، buttonHeight 52 ، fieldHeight 52 ، cardMinHeight 84 ، statCardHeight 132
largeChoiceHeight 96 ، mediumChoiceHeight 72 ، chartHeight 210 ، progressTrackHeight 9
borderThin 1 ، borderStrong 2 ، borderEmphasis 3
iconTileSize 48 ، choiceIconSize 30 ، iconSmall 20 ، iconMedium 24 ، iconLarge 28
navHeight 76 ، libraryHeaderHeight 58 ، librarySearchHeight 58
reviewHeaderHeight 92 ، reviewChoiceHeight 58 ، reviewQuizHeight 56 ، reviewButtonHeight 50
cardElevationBase 4 ، dividerAlpha 0.65 ، reviewSelectedAlpha 0.10 ، cardBorderAlpha 0.40
cardBorderStrongAlpha 0.65 ، accentSurfaceAlpha 0.10 ، hierarchyBoost 1.0
adaptiveMediumBreakpoint 600 ، adaptiveExpandedBreakpoint 840
elevationScale 1 ، typographyScale 1 ، densityScale 1 ، spacingScale 1
cornerSmall 12 ، cornerMedium 16 ، cornerLarge 24
```

بازنویسی‌های تم‌ها (فقط مقادیر متفاوت): 

```text
grok:  screenPadding 20 ، contentGap 12 ، sectionGap 18 ، statCardHeight 112 ، navHeight 80
       reviewHeaderHeight 96 ، cardElevationBase 5 ، dividerAlpha 0.72 ، reviewSelectedAlpha 0.16
       cardBorderAlpha 0.55 ، cardBorderStrongAlpha 0.85 ، accentSurfaceAlpha 0.16 ، hierarchyBoost 1.08
claud: screenPadding 24 ، contentGap 16 ، sectionGap 24 ، statCardHeight 112 ، navHeight 78
       reviewHeaderHeight 100 ، cardElevationBase 2 ، dividerAlpha 0.45 ، reviewSelectedAlpha 0.08
       cardBorderAlpha 0.25 ، cardBorderStrongAlpha 0.45 ، accentSurfaceAlpha 0.08
gtp:   همه مقادیر پایه × 0.88 ، سپس screenPadding 16 ، contentGap 10 ، sectionGap 14
       statCardHeight 118 ، navHeight 70 ، cardElevationBase 5 ، cardBorderAlpha 0.70
       cardBorderStrongAlpha 0.95 ، accentSurfaceAlpha 0.18 ، hierarchyBoost 1.12
```

## 8.4 قواعد

۱. فونت فارسی: وزیر (Vazirmatn) که در assets/fonts گذاشته می‌شود (مجوز آزاد). برای انگلیسی فونت پیش‌فرض سیستم.
۲. اندازه فونت‌ها: headlineLarge 28sp ، headlineMedium 24sp ، bodyLarge 16sp ، bodyMedium 14sp ، bodySmall 12sp ، labelMedium 12sp؛ همه ضربدر typographyScale تم.
۳. ارتفاع دکمه 48 تا 52dp؛ ارتفاع فیلد ورودی 56dp.
۴. حداقل ناحیه لمس همه کنترل‌ها 48dp.
۵. حالت کنتراست: متن روی primary با onPrimary؛ نسبت کنتراست متن اصلی حداقل 4.5 به 1.
۶. ممنوع: Color(0x...) مستقیم یا dp مستقیم در کد صفحه‌ها (باید از توکن تم).
۷. تغییر تم و حالت روشن / تاریک بدون راه‌اندازی مجدد اعمال می‌شود.
۸. رنگ‌های مرتبط با مرحله و سختی (نشان‌ها): سختی EASY با success ، MEDIUM با info ، HARD با warning ، VERY_HARD با error ؛ مرحله DAILY با primary ، WEEKLY با info ، MONTHLY با warning ، LEARNED با success.
۹. تم‌های شخصی از فایل در نسخه اول نیست (D33).

---

# 9. تنظیمات

ذخیره در جدول settings (کلید و مقدار متنی). خواندن با Flow تا صفحه‌ها واکنشی باشند.

| کلید | نوع | پیش‌فرض | بازه |
|------|-----|---------|------|
| themeId | متن | grok | grok، claud، modern_minimal، gtp |
| isDark | بولی | true | |
| uiLanguage | متن | fa | fa، en |
| activePair | متن | es-fa | از language_pairs فعال |
| activeDirection | متن | NORMAL | NORMAL، REVERSE |
| difficultyThreshold | عدد | 3 | 1 تا 5 |
| defaultQuizLevel | متن | MEDIUM | EASY، MEDIUM، HARD |
| defaultReviewMode | متن | FLASHCARD | FLASHCARD، QUIZ |
| maxReviewCards | عدد | 30 | 1 تا 100 |
| quizAutoAdvanceSeconds | عدد | 3 | 1 تا 10 |
| ttsEnabled | بولی | true | |
| ttsAutoPlay | بولی | false | |
| ttsSpeechRate | عدد اعشاری | 1.0 | 0.5 تا 2.0 |
| tutorialSeen | بولی | false | |
| seedImported | بولی | false | |
| dataVersion.concept | عدد | 0 | |
| dataVersion.content | عدد | 0 | |

مقدار خارج از بازه هنگام خواندن به نزدیک‌ترین مقدار مجاز محدود می‌شود (clamp).
تنظیم مقدار نامعتبر enum هنگام خواندن به پیش‌فرض برمی‌گردد.

---

# 10. متن آموزش داخل برنامه

متن فارسی زیر عین متن نهایی است. نسخه انگلیسی را پیاده‌ساز عیناً و وفادار ترجمه می‌کند (strings.xml انگلیسی).
شش صفحه پیمایشی. عنوان هر صفحه و متن آن:

### صفحه 1: به یادین خوش آمدید

یادین به شما کمک می‌کند واژه‌ها را با تکرار فاصله‌دار یاد بگیرید.
همه‌چیز روی گوشی شما می‌ماند و بدون اینترنت کار می‌کند.
برنامه با حدود شش هزار واژه آماده اسپانیایی به فارسی شروع می‌شود.

### صفحه 2: مرحله‌های یادگیری

هر واژه چهار مرحله دارد: روزانه، هفتگی، ماهانه و یادگرفته.
واژه تازه از «روزانه» شروع می‌شود.
اگر درست جواب بدهید، واژه یک مرحله جلو می‌رود: روزانه به هفتگی (مرور بعدی 7 روز بعد)، هفتگی به ماهانه (30 روز بعد) و ماهانه به یادگرفته.
اگر غلط جواب بدهید، واژه به «روزانه» برمی‌گردد و فردا دوباره می‌آید.
واژه یادگرفته از مرور عادی بیرون می‌رود و هرگز به روزانه برنمی‌گردد. برای مرور دوباره آن‌ها بخش «یادگرفته‌ها» را باز کنید.

### صفحه 3: مرور هر روز

در صفحه خانه تعداد واژه‌های آماده برای امروز دیده می‌شود.
هر واژه در هر روز فقط یک‌بار مرور می‌شود، تا نیمه‌شب بعد دوباره نمی‌آید.
می‌توانید مرور را «فلش‌کارت» یا «آزمون» انتخاب کنید و جهت را بین «اسپانیایی به فارسی» و «فارسی به اسپانیایی» عوض کنید.
پیشرفت هر جهت جداگانه نگه داشته می‌شود.

### صفحه 4: سختی واژه

هر واژه یک سختی دارد: آسان، متوسط، سخت و خیلی سخت.
همه واژه‌ها از «آسان» شروع می‌کنند.
چند پاسخ غلط پشت‌سرهم سختی را بالا می‌برد و چند پاسخ درست پشت‌سرهم آن را پایین می‌آورد.
تعداد لازم را در تنظیمات (بین 1 تا 5) انتخاب می‌کنید؛ پیش‌فرض 3 است.
سختی با مرحله یادگیری فرق دارد و روی زمان مرور اثری ندارد؛ فقط به شما نشان می‌دهد کدام واژه‌ها دردسر بیشتری دارند.

### صفحه 5: آزمون و سطح‌ها

آزمون چهار گزینه دارد: یک گزینه درست و سه گزینه غلط.
سه سطح دارد:
مبتدی: گزینه‌های غلط با پاسخ خیلی فرق دارند.
متوسط: گزینه‌های غلط از همان موضوع‌اند.
حرفه‌ای: گزینه‌های غلط از همان موضوع و بسیار شبیه پاسخ‌اند.
اگر برای واژه‌ای گزینه غلط کافی پیدا نشود، به‌جای آزمون فلش‌کارت نشان داده می‌شود.

### صفحه 6: کتابخانه، افزودن و پشتیبان

در «کتابخانه» می‌توانید واژه‌ها را جستجو، ویرایش یا حذف کنید.
با دکمه «+» واژه تازه اضافه کنید یا چند واژه را از متن یا فایل (متن، CSV، JSON، اکسل یا SQLite) وارد کنید.
اگر واژه‌ای از قبل هست و فقط ترجمه تازه دارد، فقط ترجمه‌های تازه اضافه می‌شود.
از «تنظیمات» می‌توانید از واژه‌ها و پیشرفت خود پشتیبان بگیرید و بعداً بازیابی کنید.
هر روز تمرین کنید تا رگبار شما بالا برود و دستاورد بگیرید.

---

# 11. فهرست کامل تست‌ها

تست‌های واحد (JUnit) در ماژول domain با FakeClock و مخزن‌های ساختگی. شناسه تست برای پیگیری در PROGRESS_TRACKER آمده است.

## 11.1 انتقال مرحله

| شناسه | مورد |
|-------|------|
| T-LT-01 | DAILY و درست ← WEEKLY و امروز+7 |
| T-LT-02 | WEEKLY و درست ← MONTHLY و امروز+30 |
| T-LT-03 | MONTHLY و درست ← LEARNED و تاریخ null |
| T-LT-04 | DAILY و غلط ← DAILY و امروز+1 |
| T-LT-05 | WEEKLY و غلط ← DAILY و امروز+1 |
| T-LT-06 | MONTHLY و غلط ← DAILY و امروز+1 |
| T-LT-07 | LEARNED با هر پاسخ ← LEARNED و null |
| T-LT-08 | نتیجه به آستانه سختی و سختی کلمه وابسته نیست (سختی خیلی سخت، پاسخ غلط، نتیجه همان) |
| T-LT-09 | عبور از ماه و سال (امروز آخرین روز سال) درست محاسبه شود |

## 11.2 سختی کلمه

| شناسه | مورد |
|-------|------|
| T-DF-01 | آستانه 1: یک پاسخ درست یک پله پایین |
| T-DF-02 | آستانه 1: یک پاسخ غلط یک پله بالا |
| T-DF-03 | آستانه 3: دو درست فقط شمارنده، سومی پله |
| T-DF-04 | آستانه 5: چهار غلط فقط شمارنده، پنجمی پله |
| T-DF-05 | درست شمارنده غلط را صفر می‌کند و برعکس |
| T-DF-06 | EASY با درست‌ها پایین‌تر نمی‌رود |
| T-DF-07 | VERY_HARD با غلط‌ها بالاتر نمی‌رود |
| T-DF-08 | رسیدن به VERY_HARD پرچم را 1 می‌کند و دیگر 0 نمی‌شود |
| T-DF-09 | تغییر پله هر دو شمارنده را صفر می‌کند |
| T-DF-10 | آستانه خارج از بازه به 1..5 محدود می‌شود |
| T-DF-11 | سختی هیچ اثری روی مرحله ندارد |
| T-DF-12 | سختی جهت NORMAL روی جهت REVERSE اثر نمی‌گذارد |

## 11.3 ثبت پاسخ

| شناسه | مورد |
|-------|------|
| T-SA-01 | ثبت موفق: هر سه (کارت، سختی، تاریخچه) در یک تراکنش |
| T-SA-02 | خطا در میانه ← هیچ تغییری ماندگار نشود |
| T-SA-03 | تلاش تکراری با همان (جلسه، شناسه تلاش) ← DUPLICATE_ATTEMPT و بدون تغییر |
| T-SA-04 | پاسخ دوم همان کارت در همان روز ← ALREADY_REVIEWED_TODAY |
| T-SA-05 | همان واژه در جهت دیگر همان روز مجاز |
| T-SA-06 | کارت غیر سررسید ← NOT_DUE |
| T-SA-07 | نبودن کارت ← DATA_INTEGRITY_ERROR و ساخته نشدن خودکار |
| T-SA-08 | مفهوم غیرفعال ← CONCEPT_INACTIVE |
| T-SA-09 | مرور LEARNED: مرحله ثابت، سختی عوض می‌شود، تاریخچه ثبت می‌شود |
| T-SA-10 | stageBefore درست ثبت می‌شود |

## 11.4 صف مرور

| شناسه | مورد |
|-------|------|
| T-Q-01 | موعد آینده انتخاب نمی‌شود |
| T-Q-02 | سررسید امروز انتخاب می‌شود |
| T-Q-03 | LEARNED در DAILY/WEEKLY/MONTHLY/RANDOM نیست |
| T-Q-04 | LEARNED فقط در نوع LEARNED |
| T-Q-05 | مفهوم غیرفعال انتخاب نمی‌شود |
| T-Q-06 | مفهوم مرور‌شده امروز انتخاب نمی‌شود |
| T-Q-07 | فیلتر دسته (چندانتخابی، یکی از چند دسته) |
| T-Q-08 | فیلتر سختی چندانتخابی |
| T-Q-09 | فیلتر برچسب |
| T-Q-10 | فقط کارت‌های جهت انتخابی |
| T-Q-11 | RANDOM فقط داخل مجموعه واجد شرایط |
| T-Q-12 | ترتیب DAILY بر اساس موعد سپس شناسه |
| T-Q-13 | برش maxCards و محدود شدن به 1..100 |
| T-Q-14 | خروجی خالی خطا نیست |
| T-Q-15 | الگوریتم هیچ داده‌ای تغییر نمی‌دهد |
| T-Q-16 | شمارنده خانه دقیقاً برابر اندازه صف بدون برش |
| T-Q-17 | مفهوم بدون محتوا برای جفت فعال انتخاب نمی‌شود |

## 11.5 آزمون

| شناسه | مورد |
|-------|------|
| T-QZ-01 | همیشه چهار گزینه و دقیقاً یک پاسخ صحیح |
| T-QZ-02 | سه گزینه غلط یکتا |
| T-QZ-03 | گزینه غلط از همان مفهوم نیست |
| T-QZ-04 | گزینه‌ای با canonicalKey تکراری با پاسخ حذف می‌شود |
| T-QZ-05 | چند معنی همان مفهوم یک گزینه واحد |
| T-QZ-06 | بانک کمتر از چهار مفهوم ← FlashcardFallback |
| T-QZ-07 | سطح EASY: ترجیح گزینه بدون دسته و نوع مشترک |
| T-QZ-08 | سطح MEDIUM: دسته مشترک و نوع متفاوت |
| T-QZ-09 | سطح HARD: دسته و نوع مشترک و بیشترین شباهت |
| T-QZ-10 | کمبود لایه ← استفاده از لایه جایگزین |
| T-QZ-11 | گزینه‌های غلط قبلی همان جلسه ترجیح داده نمی‌شوند ولی در کمبود مجازند |
| T-QZ-12 | سختی کلمه روی گزینه‌ها اثر ندارد |
| T-QZ-13 | جهت REVERSE: گزینه‌ها از زبان مبدأ |
| T-QZ-14 | ترتیب گزینه‌ها تصادفی است (آماری) |
| T-QZ-15 | کارایی: بانک 6000 مفهوم، ساخت سؤال زیر 150 میلی‌ثانیه روی JVM |
| T-QZ-16 | شباهت: مقادیر مرجع (tokenSim، levSim، bigramSim) |

## 11.6 پارسر

| شناسه | مورد |
|-------|------|
| T-P-01 | سطر «واژه → ترجمه» |
| T-P-02 | مدخل شماره‌دار با پرش شماره |
| T-P-03 | مبدأ و ترجمه در دو خط |
| T-P-04 | مدخل چندخطی |
| T-P-05 | چند ترجمه (چند خط و جداکننده‌ها) |
| T-P-06 | یادداشت و نکته گرامری و تجزیه چسبیده به مدخل |
| T-P-07 | ترجمه بدون مبدأ ← هشدار ORPHAN_TRANSLATION |
| T-P-08 | مبدأ بدون ترجمه ← ORPHAN_SOURCE و اعتماد پایین |
| T-P-09 | خط ناشناخته ← هشدار |
| T-P-10 | خط # و // نادیده |
| T-P-11 | حفظ لهجه‌ها و پرانتز |
| T-P-12 | ادغام مدخل‌های کاملاً تکراری |
| T-P-13 | ترجمه قبل از مبدأ (جفت‌شدن با مدخل بعد) |
| T-P-14 | classifyEntry بر اساس تعداد کلمه |
| T-P-15 | ZWNJ داخل کلمه حفظ و در ابتدا و انتها حذف |

## 11.7 ورود

| شناسه | مورد |
|-------|------|
| T-I-01 | سیاست SKIP |
| T-I-02 | سیاست MERGE فقط ترجمه جدید اضافه می‌کند |
| T-I-03 | MERGE بدون ترجمه جدید ← بدون تغییر |
| T-I-04 | سیاست REPLACE ترجمه‌ها را جایگزین و کارت‌ها را حفظ می‌کند |
| T-I-05 | سیاست KEEP_SEPARATE |
| T-I-06 | چند دسته |
| T-I-07 | چند ترجمه |
| T-I-08 | یادداشت |
| T-I-09 | اعتماد پایین ← import_review_items |
| T-I-10 | Conflict ← بدون ادغام خودکار |
| T-I-11 | CSV: عنوان، جداکننده‌های مختلف، نقل‌قول |
| T-I-12 | JSON: هر دو شکل و رد فایل پشتیبان |
| T-I-13 | XLSX: خواندن shared strings و inline |
| T-I-14 | SQLite: قالب یادین و فلش‌لرن |
| T-I-15 | شکست یک دسته، دسته‌های دیگر ماندگار |

## 11.8 بانک اولیه

| شناسه | مورد |
|-------|------|
| T-S-01 | دقیقاً 5988 مفهوم فعال وارد شود (فایل واقعی) |
| T-S-02 | مفاهیم غیرفعال وارد نشوند |
| T-S-03 | ادغام تکراری‌ها با فایل ساختگی دارای مبدأ تکراری |
| T-S-04 | نگاشت دسته‌ها طبق جدول 14.1 |
| T-S-05 | هر مفهوم دو کارت |
| T-S-06 | یادداشت نمونه اولیه حذف شود |
| T-S-07 | قطع در میانه ← اجرای بعدی از نو و بانک ناقص نماند |
| T-S-08 | اجرای دوباره seed اگر seedImported=true اتفاقی نمی‌افتد |

## 11.9 پشتیبان و بازیابی

| شناسه | مورد |
|-------|------|
| T-B-01 | پشتیبان VOCABULARY فقط بخش‌های خودش |
| T-B-02 | پشتیبان PROGRESS |
| T-B-03 | پشتیبان FULL |
| T-B-04 | رفت و برگشت FULL ← داده یکسان |
| T-B-05 | اعتبارسنجی هر دوازده مورد با فایل خراب |
| T-B-06 | فایل خراب ← دیتابیس بدون تغییر |
| T-B-07 | MERGE واژگان: فقط ترجمه جدید |
| T-B-08 | REPLACE واژگان |
| T-B-09 | MERGE پیشرفت: قانون جدیدتر برنده |
| T-B-10 | REPLACE پیشرفت |
| T-B-11 | ردیف پیشرفت بدون مفهوم رد و گزارش شود |
| T-B-12 | شکست وسط ← ROLLBACK |
| T-B-13 | پشتیبان ایمنی پیش از REPLACE |
| T-B-14 | بازیابی بدون انتخاب روش انجام نشود (UI) |

## 11.10 آمار، رگبار، دستاورد

| شناسه | مورد |
|-------|------|
| T-ST-01 | شاخص‌ها روی مجموعه کوچک با مقادیر دقیق |
| T-ST-02 | مفاهیم غیرفعال شمرده نمی‌شوند |
| T-ST-03 | آمار هر جهت جدا |
| T-ST-04 | رگبار: امروز، دیروز، شکاف |
| T-ST-05 | رگبار: بدون پاسخ ساخته نمی‌شود |
| T-ST-06 | رگبار: منطقه زمانی |
| T-AC-01 | هر دستاورد در مرز |
| T-AC-02 | فقط یک‌بار باز شدن |
| T-PG-01 | درصد پیشرفت مقادیر مرجع |

## 11.11 تنظیمات، داده و مهاجرت

| شناسه | مورد |
|-------|------|
| T-SE-01 | clamp مقادیر |
| T-SE-02 | enum نامعتبر ← پیش‌فرض |
| T-RF-01 | Refresh تکرارپذیر |
| T-RF-02 | بازنویسی translationIndex |
| T-IN-01 | EnsureStates |
| T-IN-02 | ValidateIntegrity هر کد |
| T-MG-01 | تست مهاجرت Room (ابزاری) برای هر نسخه |

## 11.12 تست‌های رابط (ابزاری)

| شناسه | مورد |
|-------|------|
| T-UI-01 | جهت RTL/LTR وارونه نیست |
| T-UI-02 | تغییر تم و حالت تاریک |
| T-UI-03 | جریان کامل فلش‌کارت تا نتایج |
| T-UI-04 | جریان کامل آزمون تا نتایج |
| T-UI-05 | افزودن واژه تکی |
| T-UI-06 | کتابخانه: جستجو و فیلتر روی 6000 مورد روان |
| T-UI-07 | راه‌اندازی اول با بانک اولیه |

---

# 12. مراحل پیاده‌سازی و قواعد تحویل

## 12.1 قواعد همکاری (الزامی)

۱. زبان برنامه‌نویسی: کاتلین. فقط کاتلین.
۲. توضیحات به فارسی، کوتاه و مستقیم و بدون مقدمه.
۳. قبل از تغییر فایل مهم (دیتابیس، مدل داده) ابتدا توضیح بده چه چیزی را چرا عوض می‌کنی.
۴. تغییرات بزرگ مرحله‌به‌مرحله انجام شود، نه یکجا روی کل پروژه.
۵. هر مرحله تمام شده تحویل شود و پیش از رفتن به مرحله بعد از صاحب پروژه تایید گرفته شود. بیرون از دامنه کار نام‌برده‌شده چیزی اضافه نشود. کد قبل از دستور نوشته نشود.
۶. هر تحویل یک بسته zip کامل و تجمعی کل پروژه است، نه فایل‌های تکه‌تکه و نه فقط تفاوت‌ها (تا اگر چیزی جا افتاد، نسخه بعدی فرصت جبران بدهد).
۷. در هر تحویل این فایل‌ها به‌روز می‌شوند: docs/PROGRESS_TRACKER.md (وضعیت مرحله، تاریخ، جزئیات، تصمیم‌های آگاهانه)، CHANGELOG.md، VERSION_LEDGER.md و عدد نسخه در app و CI.
۸. در ساخت zip الگوی exclude مثل  -x "*.git*"  نباید به کار برود (پوشه .github را هم حذف می‌کند). پوشه .github باید داخل zip باشد.
۹. فایل jar گریدل ریپازیتوری (gradle-wrapper.jar) داخل مخزن گذاشته نمی‌شود؛ CI با gradle/actions/setup-gradle کار می‌کند.
۱۰. هر تصمیم معماری آگاهانه در PROGRESS_TRACKER ثبت می‌شود.
۱۱. صاحب پروژه ساخت محلی ندارد؛ CI گیت‌هاب تنها کامپایلر است. خطاهای کامپایل به‌ترتیب ماژول ظاهر می‌شوند (Gradle در اولین ماژول خراب می‌ایستد) و این طبیعی است؛ هر اجرای سبز لایه بعدی را آشکار می‌کند.
۱۲. روش بارگذاری در گیت‌هاب (Termux): دانلود zip در ~/storage/downloads ، کپی به ~/work ، باز کردن در ~/work/repo ، سپس git init و git push --force (نه کامیت‌های تدریجی).
۱۳. هر 2 تا 3 مرحله صاحب پروژه گفتگوی جدید باز می‌کند و فقط آخرین zip و جمله «ادامه از مرحله X طبق PROGRESS_TRACKER» را می‌دهد. بنابراین PROGRESS_TRACKER باید برای شروع بدون زمینه کافی باشد.
۱۴. پیام‌های تحویل: یک خط وضعیت، فهرست کوتاه تغییرها، و فقط موارد نیازمند تصمیم.

## 12.2 مراحل

| مرحله | نسخه | محتوا | خروجی قابل بررسی |
|-------|------|-------|------------------|
| 1 | 0.1 | اسکلت پروژه سه‌ماژولی، گریدل، CI، تم (چهار تم)، ناوبری، صفحه‌های خالی، رشته‌های fa و en | CI سبز ؛ اجرا و دیدن نوار پایین و تغییر تم |
| 2 | 0.2 | دیتابیس: Entityها، DAOها، Converterها، Migration پایه، مخزن‌ها، تنظیمات، EnsureStates (قبل از شروع، توضیح تغییر داده می‌شود) | تست‌های DAO و T-IN و T-SE |
| 3 | 0.3 | الگوریتم‌های محض: متن، روز، انتقال مرحله، سختی، ثبت پاسخ، صف مرور و شمارش + تست‌ها | T-LT و T-DF و T-SA و T-Q |
| 4 | 0.4 | بانک اولیه + Splash + خانه + کتابخانه (Paging) + جزئیات واژه | T-S و T-UI-06 و T-UI-07 |
| 5 | 0.5 | تنظیم مرور + فلش‌کارت + نتایج + رگبار | T-ST-04..06 و T-UI-03 |
| 6 | 0.6 | ساخت آزمون + صفحه آزمون | T-QZ و T-UI-04 |
| 7 | 0.7 | افزودن/ویرایش/حذف + پارسر + خواننده‌ها + پیش‌نمایش + بررسی ورود | T-P و T-I و T-UI-05 |
| 8 | 0.8 | پشتیبان و بازیابی (سه نوع، ادغام و جایگزینی) | T-B |
| 9 | 0.9 | پیشرفت + آمار + دستاوردها + تنظیمات کامل + تلفظ | T-ST و T-AC و T-PG |
| 10 | 1.0 | آموزش، Refresh، مهاجرت، سخت‌سازی، تست‌های ابزاری، README، مستندات | همه تست‌ها سبز |

شماره نسخه برنامه: versionName برابر شماره جدول (مثلاً 0.3) و versionCode برابر عدد صحیح (3 برای 0.3 و 100 برای 1.0).

## 12.3 تعریف تمام‌شدن هر مرحله

۱. CI سبز (build و test و release).
۲. تست‌های مرحله نوشته و سبز.
۳. PROGRESS_TRACKER و CHANGELOG و VERSION_LEDGER به‌روز.
۴. صاحب پروژه تایید کرده.

---

# 13. موارد نیازمند تایید صاحب پروژه

این موارد از این سند به‌عنوان پیش‌فرض آمده‌اند؛ تا تایید یا تغییر صاحب پروژه، همین پیش‌فرض اجرا می‌شود.

| شماره | پیش‌فرض |
|-------|---------|
| A1 | رفتن هر مرحله با یک پاسخ درست (کد 6.84 فلش‌لرن). سند Claude-ALGORITHMS سه پاسخ را برای ماهانه نوشته بود؛ نادیده گرفته شد. |
| A2 | بانک اولیه به‌صورت فایل جیسون در assets و وارد کردن در اولین اجرا (به‌جای فایل دیتابیس آماده). نمونه yadin_seed.db در samples دیگر لازم نیست. |
| A3 | دستاورد «سازنده واژگان»: به‌جای «500 مفهوم فعال» که با بانک اولیه فوراً باز می‌شد، «500 واژه تمرین‌شده» شد. |
| A4 | حذف maxQuizPoolSize؛ فقط maxReviewCards. |
| A5 | قابلیت راهنما (Hint) در نسخه اول نیست. |
| A6 | رابطه‌ها و تنوع‌ها و فراداده پارسر جدول ندارند؛ متنشان در یادداشت می‌آید. |
| A7 | در REPLACE واژگان، پیشرفت هم پاک می‌شود. |
| A8 | MERGE پیشرفت: کارت با مرور جدیدتر برنده است. |
| A9 | ورود SQLite فقط از قالب یادین و فلش‌لرن و جدول ساده؛ بازیابی از فایل پشتیبان قدیمی فلش‌لرن پشتیبانی نمی‌شود. |
| A10 | افزودن جفت زبانی جدید در نسخه اول رابط ندارد (فقط es-fa)؛ اسکیما آماده است. |
| A11 | یکسان‌سازی ی و ي و ک و ك در canonicalKey انجام نمی‌شود. |
| A12 | رگبار و دستاوردها سراسری (هر دو جهت)؛ آمار و درصد پیشرفت برای جهت انتخاب‌شده. |
| A13 | Splash حداقل 2 ثانیه. |
| A14 | تم‌های شخصی از فایل در نسخه اول نیست. |
| A15 | خطوط # و // در متن واردشده نادیده گرفته می‌شوند (در فلش‌لرن به یادداشت می‌رفتند). |
| A16 | آموزش انگلیسی ترجمه متن فارسی بخش 10 است. |
| A17 | نوع IDIOM/COLLOCATION/STRUCTURE فقط از ورودی ساخت‌یافته یا انتخاب دستی. |
| A18 | حداکثر اندازه فایل ورودی 20 مگابایت. |

---

# 14. پیوست‌ها

## 14.1 نگاشت دسته‌های بانک اولیه به ده دسته رسمی

دسته‌های رسمی به ترتیب (sortOrder از 1):

```text
1 افعال
2 خوراکی و غذا
3 خانه و وسایل
4 خانواده و روابط
5 بدن و پزشکی
6 حمل‌ونقل و رانندگی
7 سفر و گردشگری
8 شغل و کار
9 اصطلاحات
10 مکالمه روزمره
```

نگاشت نام‌های فایل بانک اولیه:

| نام در فایل | دسته رسمی |
|-------------|-----------|
| افعال پایه | افعال |
| غذا | خوراکی و غذا |
| وسایل خانه | خانه و وسایل |
| خانواده | خانواده و روابط |
| بدن و پزشکی | بدن و پزشکی |
| حمل و نقل | حمل‌ونقل و رانندگی |
| سفر | سفر و گردشگری |
| کار و شغل | شغل و کار |
| اصطلاح | اصطلاحات |
| مکالمه روزمره | مکالمه روزمره |

در ورود از فایل‌ها: نام دسته‌ای که با هیچ دسته موجود (نام دقیق پس از canonicalKey) برابر نباشد، یک دسته جدید غیررسمی (isDefault=0) می‌سازد.
ده دسته رسمی قابل حذف نیستند؛ دسته‌های غیررسمی خالی را کاربر می‌تواند حذف کند.

## 14.2 نمونه فایل‌های ورود

### samples/import/sample.txt

```text
# نمونه فایل ورودی یادین
1. manzana → سیب
2. hacer falta
نیاز بودن
نکته: فعل با مفعول غیرمستقیم
3. la casa - خانه / منزل
casa
خانه
4. ir → رفتن
تجزیه: ir = رفتن
---
5. el agua → آب
```

### samples/import/sample.csv

```text
source,translation,note,category,type
manzana,سیب,,خوراکی و غذا,WORD
hacer falta,نیاز بودن,فعل با مفعول غیرمستقیم,اصطلاحات,PHRASE
la casa,خانه / منزل,,خانه و وسایل|مکالمه روزمره,PHRASE
ir,رفتن,,افعال,WORD
```

### samples/import/sample.json

```json
{
  "entries": [
    {"source": "manzana", "translations": ["سیب"], "categories": ["خوراکی و غذا"], "entryType": "WORD"},
    {"source": "hacer falta", "translations": ["نیاز بودن"], "note": "فعل با مفعول غیرمستقیم", "categories": ["اصطلاحات"], "entryType": "PHRASE"},
    {"source": "la casa", "translation": "خانه / منزل", "categories": ["خانه و وسایل", "مکالمه روزمره"]},
    {"source": "ir", "translations": ["رفتن"], "categories": ["افعال"], "entryType": "WORD"}
  ]
}
```

### samples/import/sample.xlsx

با اسکریپت tools/make_sample_xlsx.py (کتابخانه openpyxl) ساخته می‌شود و همان چهار ردیف CSV را با همان عنوان‌ها دارد.

### samples/backup/sample_backup.json

یک پشتیبان FULL کوچک (حدود 20 مفهوم با مقداری پیشرفت) با قالب بخش 6.13 که یک تست ابزاری بتواند بازیابی کند. با یک تست تولید می‌شود تا همیشه با قالب هماهنگ بماند.

## 14.3 نقشه مرجع کد فلش‌لرن

مخزن: github.com/manidigit/FlashLearn ، شاخه اصلی. مسیرها:

| موضوع | فایل |
|-------|------|
| انتقال مرحله و سختی | domain/.../algorithm/Algorithms.kt |
| صف مرور و شمارش | domain/.../usecase/ReviewEngineUseCases.kt و CountReviewQueueUseCase.kt |
| ثبت پاسخ و ساخت مفهوم | domain/.../usecase/UseCases.kt |
| ساخت آزمون | domain/.../usecase/QuizUseCases.kt |
| پارسر | domain/.../parser/VocabularyParser.kt و ParserMarkers.kt |
| ورود و تکراری‌ها | domain/.../usecase/ImportParsedEntryUseCase.kt و DuplicateConceptUseCases.kt |
| آمار، رگبار، پیشرفت، دستاورد | domain/.../statistics/Statistics.kt و progress/Progress.kt و gamification/Achievement.kt |
| Refresh | domain/.../usecase/RefreshDataUseCase.kt |
| بررسی یکپارچگی | domain/.../usecase/DataIntegrityUseCases.kt |
| پشتیبان | domain/.../backup/BackupModels.kt و data/.../backup/*.kt |
| تم | app/.../ui/theme/*.kt |

## 14.4 تفاوت‌های آگاهانه یادین با فلش‌لرن

۱. مرحله و سختی برای هر جهت جدا (دو کارت برای هر مفهوم).
۲. روز مرور به‌صورت روز تقویمی ذخیره می‌شود (نه لحظه).
۳. حذف hasPathFailure و monthlyWrongCount و شمارنده‌های کل درست و غلط روی وضعیت یادگیری.
۴. دسته چندبه‌چند (در فلش‌لرن تک‌مقداری).
۵. حذف علاقه‌مندی، رابطه‌ها، تنوع‌ها، فراداده پارسر.
۶. ساخت آزمون بدون وابستگی به سختی کلمه.
۷. خطوط # و // نادیده گرفته می‌شوند و تشخیص اسکریپت با Unicode انجام می‌شود (لهجه‌ها).
۸. پشتیبان بدون رمز؛ بازیابی با دو روش ادغام و جایگزینی.
۹. دستاورد «سازنده واژگان» بر اساس واژه تمرین‌شده.
۱۰. قفل هم‌روز برای آزمون و فلش‌کارت و همه مرورها یکسان.

## 14.5 فهرست کدهای خطا

| کد | معنی |
|----|------|
| DATA_INTEGRITY_ERROR | وضعیت کارت یا سختی موجود نیست |
| CONCEPT_INACTIVE | مفهوم غیرفعال است |
| DUPLICATE_ATTEMPT | تلاش تکراری با همان شناسه |
| ALREADY_REVIEWED_TODAY | کارت امروز مرور شده |
| NOT_DUE | کارت سررسید نیست |
| NOT_LEARNED | مرور یادگرفته‌ها برای کارتی که LEARNED نیست |
| DUPLICATE_CONCEPT | واژه با همین ترجمه‌ها موجود است |
| VALIDATION_ERROR | ورودی نامعتبر |
| BACKUP_INVALID | فایل پشتیبان نامعتبر |
| BACKUP_UNSUPPORTED_SCHEMA | نسخه فایل پشتیبان پشتیبانی نمی‌شود |
| IMPORT_UNSUPPORTED_FILE | ساختار فایل ورودی پشتیبانی نمی‌شود |
| IMPORT_FILE_TOO_LARGE | فایل بزرگ‌تر از 20 مگابایت |
| TTS_LANGUAGE_UNSUPPORTED | موتور تلفظ زبان را پشتیبانی نمی‌کند |

## 14.6 تعریف تمام‌شدن کل پروژه

۱. همه ناوردای‌های بخش‌های 1 تا 6 با تست پوشش داده شده‌اند.
۲. بانک اولیه در اولین اجرا کامل وارد می‌شود و صفحه قفل نمی‌کند.
۳. فلش‌کارت و آزمون و هر دو جهت و همه انواع مرور کار می‌کنند.
۴. ورود از پنج قالب و چهار سیاست تکراری کار می‌کند.
۵. پشتیبان سه نوع و بازیابی دو روش کار می‌کند.
۶. چهار تم، تاریک و روشن، فارسی و انگلیسی کار می‌کنند.
۷. آموزش و راهنما کامل است.
۸. CI سبز و APK دیباگ و ریلیز ساخته می‌شود.
۹. PROGRESS_TRACKER و README کامل است.

---

پایان سند

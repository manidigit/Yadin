# یادین (Yadin) — مشخصات کامل و اجرایی برنامه

نسخه سند: 1.1 — تاریخ به‌روزرسانی: 2026-10-10  
مخزن: github.com/manidigit/Yadin  
نسخه اجرایی منطبق: `1.13.0` (کد نسخه: `21`)

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
۱۱. «در بازیابی REPLACE واژگان، پیشرفت لایتنر هم پاک می‌شود». لغو شد (تصمیم D40 / ISS-17)؛ پاکسازی پیشرفت منحصراً مشروط به حضور داده‌های پیشرفت (hasProgressInData) در فایل بکاپ است.
۱۲. «سقف ۳۰۰تایی بارگذاری کتابخانه». لغو شد (تصمیم D41 / ISS-01)؛ صفحه‌بندی نامحدود تدریجی جایگزین شد.
۱۳. «شکستن ترجمه‌ها روی هر اسلش در عبارات توضیحی». لغو شد (تصمیم D45 / ISS-19)؛ تفکیک هوشمند اسلش اعمال می‌شود.
۱۴. «احیای ناخواسته واژگان حذف‌شده در جستجوی کانونیکال ورود واژگان». لغو شد (تصمیم D46 / ISS-20)؛ تطابق منحصراً با مفاهیم فعال (active = 1) انجام می‌شود.
۱۵. «اجرای مسدودکننده استریم‌های I/O پشتیبان‌گیری در نخ اصلی». لغو شد (تصمیم D47 / ISS-31)؛ تمام عملیات به بافت کورتین Dispatchers.IO منتقل شد.

---

# 1. اصول محصول و ثبت تصمیم‌های قطعی

## 1.1 اصول

۱. فقط اندروید.
۲. اول آفلاین. هسته برنامه بدون اینترنت کامل کار می‌کند.
۳. هیچ هوش مصنوعی و هیچ سرویس ابری در محصول نیست.
۴. زبان رابط: فارسی و انگلیسی.
۵. جفت زبانی فعال در تنظیمات انتخاب‌پذیر است. جفت اولیه اسپانیایی به فارسی است.
۶. جهت مرور فقط یکی از دو حالت است: عادی یا برعکس.
۷. بانک اولیه دارای ۶,۶۶۴ مفهوم و ۱۵,۵۶۱ ترجمه تفکیک‌شده در دسته‌بندی‌های استاندارد است.
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
| D07 | واژه یادگرفته‌شده از صف عادی خارج می‌شود، اما در صورت پاسخ غلط در مرور یادگرفته‌ها به مخزن روزانه (فردا) بازمی‌گردد. | صاحب پروژه |
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
| D35 | **نظام دستاوردهای مرحله‌ای (Tiered Achievements):** جایگزینی ۸ نشان ثابت با ۲۰ مدال زنجیره‌ای در ۵ شاخه اصلی و ۴ سطح (برنز، نقره، طلا، پلاتین) همراه با ارتقای مرحله‌ای کارت‌ها. | تصمیم پیاده‌سازی ۱.۷.۰ |
| D36 | **تفکیک ساختار تم و فونت وزیرمتن:** تفکیک معماری تم به ماژول‌های مستقل (`Color`, `ThemePalettes`, `Shape`, `Type`, `ThemeTokens`, `YadinTheme`) و استفاده انحصاری از فونت وزیرمتن. | تصمیم پیاده‌سازی ۱.۸.۰ |
| D37 | **ارزیابی قطعی آزمون و حفظ اکسان‌ها:** نمره‌دهی آزمون صرفاً بر اساس برابری ایندکس گزینه انتخابی با گزینه درست (`optionIndex == correctIndex`) بدون برخورد معنایی، و حفظ کامل اکسان‌های اسپانیایی در نرمال‌سازی آزمون. | تصمیم پیاده‌سازی ۱.۹.۰ (ISS-16) |
| D38 | **تفکیک لایه Presentation (معماری SRP):** تقسیم ویومدل ریشه به ۴ ویومدل تخصصی (`ReviewViewModel`, `LibraryViewModel`, `StatisticsViewModel`, `SettingsViewModel`) با حفظ `MainViewModel` به عنوان Facade هماهنگ‌کننده. | تصمیم پیاده‌سازی ۱.۹.۰ (ISS-27) |
| D39 | **اتمیک‌سازی کامل نشست و ثبت پاسخ:** ثبت تمامی گام‌های پاسخ درون تراکنش واحد دیتابیس (`withTransaction`)، رفع ریس وضعیت سشن، و گارد بی‌اثرسازی تکرار (`Idempotency Guard`). | تصمیم پیاده‌سازی ۱.۱۰.۰ (ISS-15) |
| D40 | **ایمن‌سازی بازیابی و گارد پیشرفت:** تفکیک داده‌های واژگان از پیشرفت و منع پاکسازی جداول لایتنر در هنگام بازیابی کلمات (`isReplace`)، تولید خودکار نسخه پشتیبان اضطراری (`Safety Backup`) با سقف ۳ نسخه، و نگاشت کامل شناسه‌ها (`RestoreContext`). | تصمیم پیاده‌سازی ۱.۱۱.۰ (ISS-17) |
| D41 | **صفحه‌بندی نامحدود کتابخانه (Infinite Paging):** حذف سقف سخت ۳۰۰تایی و پیاده‌سازی بارگذاری آفست تدریجی در کتابخانه جهت مرور روان تمامی ۶,۶۶۴ مفهوم مخزن واژگان. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-01) |
| D42 | **فیلترهای جامع کتابخانه و واژگان غیرفعال:** افزودن فیلتر ۴ سطح دشواری متناسب با جهت فعال و فیلتر واژگان فعال/غیرفعال با دکمه اختصاصی فعال‌سازی مجدد کلمه. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-02) |
| D43 | **انتخابگر سیستمی فایل در ورود گروهی:** تعبیه دکمه انتخاب فایل با Android SAF (`ActivityResultContracts.GetContent`) برای پشتیبانی مستقیم از فایل‌های XLSX, CSV, JSON, TXT. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-03) |
| D44 | **فال‌بک اکید آزمون به فلش‌کارت (Strict Fallback):** ممانعت قطعی از تولید آزمون‌های با گزینه‌های ناقص یا پرکردن تصادفی غلط در کمبود گزینه‌های انحرافی و بازگشت خودکار نشست به حالت فلش‌کارت. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-18) |
| D45 | **تفکیک هوشمند اسلش و نشانگرهای Breakdown در پارسر:** منع شکستن عبارات مرکب حاوی اسلش در ترجمه‌ها (مانند «کسی/چیزی») و ثبت دقیق خطوط تفکیک بدون اتمام زودهنگام مدخل. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-19) |
| D46 | **ممانعت از احیای ناخواسته واژگان حذف‌شده در ورود داده:** اعمال شرط صریح `active = 1` در جستجوی کانونیکال جهت پیشگیری از تداخل با کلمات حذف‌شده کاربر. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-20) |
| D47 | **غیرمسدودسازی کامل پشتیبان‌گیری در Dispatchers.IO:** انتقال کامل عملیات خواندن و نوشتن فایل‌های پشتیبان به بافت کورتین پس‌زمینه و حذف هرگونه خطر مسدودسازی UI یا ANR. | تصمیم پیاده‌سازی ۱.۱۲.۰ (ISS-31) |
| D48 | **یکپارچه‌سازی متمرکز جهت زبان (Global Language Direction Rule):** انتخاب و تغییر جهت زبان (اسپانیایی به فارسی یا فارسی به اسپانیایی) منحصراً و فقط در صفحه تنظیمات (`SettingsScreen`) قرار دارد. تغییر آن به‌صورت سراسری در کل برنامه اعمال شده و هیچ کلید سوئیچ محلی یا متفرقه در سایر صفحات نباید وجود داشته باشد. | تصمیم کارفرما / پیاده‌سازی ۱.۱۳.۰ |

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

نسخه‌های قطعی و پایدار محیط ساخت و وابستگی‌های پروژه (منطبق بر `libs.versions.toml` و `build.gradle.kts`):

| مورد | نسخه |
|------|------|
| Kotlin | 1.9.24 |
| Android Gradle Plugin (AGP) | 8.5.2 |
| Gradle | 8.9 |
| JDK | 17 |
| compileSdk و targetSdk | 34 |
| minSdk | 26 |
| Compose UI و Graphics | 1.6.8 |
| Compose Compiler | 1.5.14 |
| Material 3 | 1.2.1 |
| Material Icons Extended | 1.6.8 |
| Activity Compose | 1.9.1 |
| Lifecycle (runtime و viewmodel-compose) | 2.8.4 |
| Navigation Compose | 2.7.7 |
| Room (runtime، ktx، compiler) | 2.6.1 |
| Coroutines (core و android) | 1.8.1 |
| Serialization Json | 1.6.3 |
| پردازشگر حاشیه‌نویسی | KSP (1.9.24-1.0.20 - جایگزین kapt جهت ساخت سریع و سازگار) |
| JUnit | 4.13.2 |
| شناسه برنامه و نام پکیج | com.manidigit.yadin |

## 3.2 معماری و سازماندهی کد

ساختار پروژه به صورت ماژول یکپارچه `:app` همراه با تفکیک لایه‌ای سه‌گانه و تمیز (Clean Architecture / SRP) پیاده‌سازی شده است:

۱. **لایه دامنه (domain):** مدل‌ها، الگوریتم‌های خالص لایتنر، محاسبه سختی، موتور آزمون، نرمال‌سازی متن و کلید کانونیکال، پارسر و قواعد زبانی.
۲. **لایه داده (data):** پایگاه داده تکین `YadinDatabase`، کلیه DAOها و Entityهای روم، مخازن تخصصی (`VocabularyRepository`، `LearningRepository`، `ReviewRepository`، `SettingsRepository`، `BackupRepository`)، خواننده‌های جریانی فایل (`FileReaders`) و ورود بانک اولیه.
۳. **لایه نمایش (ui):** رابط کاربری تمام‌کامپوز (Material 3)، سامانه‌های تم، ناوبری بر پایه State، و تفکیک مسئولیت ویومدل‌ها (SRP - تصمیم D38):
   - `ReviewViewModel`: مدیریت نشست‌های مرور، فلش‌کارت، آزمون ۴ گزینه‌ای، فال‌بک اکید و ثبت اتمیک پاسخ‌ها.
   - `LibraryViewModel`: کتابخانه، اسکرول تدریجی نامحدود (Paging)، فیلترهای سختی/مرحله/فعال و عملیات فعال‌سازی مجدد.
   - `StatisticsViewModel`: آمار، درصد پیشرفت، رگبار، نمودارها و مدال‌های مرحله‌ای ۲۰گانه.
   - `SettingsViewModel`: تنظیمات جامع، تم‌ها، موتور غیرمسدودکننده پشتیبان‌گیری و بازیابی.
   - `MainViewModel`: هماهنگ‌کننده ریشه برنامه (Facade Coordinator).

قانون وابستگی: لایه `ui` به `domain` و `data` وابسته است؛ `data` به `domain` متصل است؛ `domain` کاتلین خالص و مستقل از وابستگی‌های پلتفرمی اندروید است.

## 3.3 ساختار پکیج‌ها

```text
app/src/main/java/com/manidigit/yadin/
  domain/
    model/            مدل‌ها و enumها (Direction, Stage, QuizLevel, ...)
    algorithm/        LearningTransition, DifficultyCalculator, QuizBuilder, ...
    text/             Normalizer, CanonicalKey, TextUtilities, VocabularyParser
    time/             DayMath, Clock
  data/
    local/
      database/       YadinDatabase (تکین)، Converters
      entity/         ConceptEntity, ContentEntity, LearningStateEntity, ...
      dao/            ConceptDao, LearningDao, ReviewSessionDao, SettingsDao, ...
    repository/       VocabularyRepository, LearningRepository, ReviewRepository, ...
    io/               FileReaders (xlsx, csv, json, txt, sqlite), BackupFileIo
    seed/             SeedImporter
  ui/
    screens/          Home, ReviewSetup, Flashcard, Quiz, Library, AddEditWord, ...
    components/       CommonCards, Chips, StatsCharts, Dialogs
    viewmodel/        MainViewModel, ReviewViewModel, LibraryViewModel, ...
    theme/            YadinTheme, ThemePalettes (GTP, Gemini, Claude, Googoli), Type, Shape
    nav/              Screen, Navigation State
```

## 3.4 ساختار مخزن

```text
Yadin/
  app/
  docs/
    Yadin-Specification1-1.md       (همین سند)
    Vocabulary.json                 (بانک اولیه، کپی در assets)
    CHANGELOG.md  ISSUES_AND_DEFECTS_BACKLOG.md  README.md
  samples/
    import/sample.txt  sample.csv  sample.json  sample.xlsx
    backup/sample_backup.json
  gradle/
    libs.versions.toml
  build.gradle.kts  settings.gradle.kts
```

## 3.5 CI

فایل گردش‌کار در گیت‌هاب اکشنز در هر push و pull request این مراحل را اجرا می‌کند:
۱. نصب JDK 17.
۲. نصب Android SDK پلتفرم 34 و build-tools.
۳. اجرای تست‌های واحد (gradle test).
۴. ساخت نسخه دیباگ (assembleDebug).
۵. ساخت نسخه ریلیز (assembleRelease).

## 3.6 مدیریت چرخه حیات و تزریق وابستگی

مدیریت مخازن به صورت الگوهای تکین (Singleton) در سطح `YadinDatabase` و تزریق سازنده به ویومدل‌ها پیاده‌سازی شده است.
زمان از طریق متدهای کمکی و تاریخ محلی دستگاه محاسبه می‌شود و عملیات حساس دیتابیس درون تراکنش‌های اتمیک (`withTransaction`) اجرا می‌گردند.

---

# 4. مدل داده

پایگاه داده تکین و یکپارچه: Room با کلاس رسمی `YadinDatabase` (تصمیم ISS-29)، فایل yadin.db.
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
| reviewType | TEXT | ReviewType (DAILY, WEEKLY, MONTHLY, LEARNED, RANDOM) |
| mode | TEXT | ReviewMode (FLASHCARD, QUIZ) — حالت ثابت جلسه |
| direction | TEXT | NORMAL, REVERSE |
| quizLevel | TEXT | Nullable. فقط وقتی mode برابر QUIZ |
| status | TEXT | ACTIVE, COMPLETED, ABANDONED (مدیریت پایداری و بازگشت پس از مرگ فرآیند) |
| currentPosition | INTEGER | اندیس موقعیت کارت فعلی در صف |
| totalItems | INTEGER | تعداد کل کارت‌های انتخاب‌شده برای جلسه |

### review_session_items

جدول پایداری صف جلسه مرور برای جلوگیری از ازدست‌رفتن صف هنگام Process Death:

| ستون | نوع | قید |
|------|-----|-----|
| id | TEXT | کلید اصلی |
| sessionId | TEXT | کلید خارجی به review_sessions |
| position | INTEGER | موقعیت ترتیبی کارت در صف جلسه |
| conceptId | TEXT | کلید خارجی به concepts |
| direction | TEXT | NORMAL, REVERSE |
| state | TEXT | PENDING, ANSWERED, SKIPPED |

یکتا: (sessionId, position).

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
| mode | TEXT | ReviewMode اصلی جلسه (FLASHCARD, QUIZ) |
| attemptMode | TEXT | FLASHCARD, QUIZ (در صورت Fallback در آزمون، روی تلاش ثبت می‌شود بدون تغییر mode جلسه) |
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
| status | TEXT (PENDING, APPROVED, REJECTED) |
| lineNumber | INTEGER (Nullable) |
| rawText | TEXT |
| policy | TEXT (DuplicatePolicy موقع ورود) |
| targetConceptId | TEXT (Nullable — شناسه مفهوم انتخابی کاربر برای ادغام دستی در حالت تضاد CONFLICT) |
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

## 5.3 جداکننده ترجمه‌ها و تفکیک هوشمند

در نسخه ۱.۱۲.۰ الگوریتم تفکیک ترجمه‌ها (`TextUtilities.splitTranslations`) جهت پیشگیری از شکستن عبارات توضیحی مرکب (مانند «روی کسی/چیزی حساب کردن») بهینه‌سازی شد:

۱. **تفکیک اولیه بر اساس جداکننده‌های قطعی:**
   ابتدا متن بر اساس کاما (`،` یا `,`)، سمی‌کولن (`؛` یا `;`) و خطوط جدید (`\n`) شکسته می‌شود.

۲. **مدیریت هوشمند اسلش (`/`):**
   - اسلش‌های دارای فاصله در دو طرف (` / `) یا اسلش‌هایی که دو واژه کاملاً مجزا را جدا کرده‌اند شکسته می‌شوند.
   - اسلش‌های تعبیه‌شده درون عبارات بدون فاصله (مانند «کسی/چیزی»، «او/آن»، «او/ایشان») به عنوان بخشی از ساختار معنایی عبارت حفظ می‌شوند و کلمه تکه‌تکه نمی‌شود (تصمیم D45 و ISS-19).

۳. **پاک‌سازی و حذف تکراری‌ها:**
   هر تکه حاصل `cleanText` می‌شود، تکه‌های خالی حذف می‌شوند، و تکه‌های با `canonicalKey` تکراری فقط یک‌بار (اولین مورد) حفظ می‌گردند.

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
پاسخ غلط (در هر مرحله‌ای اعم از DAILY، WEEKLY، MONTHLY یا LEARNED):
    newStage = DAILY
    nextReviewDay = today + 1

پاسخ درست:
    stage = DAILY   -> newStage = WEEKLY,  nextReviewDay = today + 7
    stage = WEEKLY  -> newStage = MONTHLY, nextReviewDay = today + 30
    stage = MONTHLY -> newStage = LEARNED, nextReviewDay = null
    stage = LEARNED -> newStage = LEARNED, nextReviewDay = null
```

### نکته‌ها

۱. در هر مرحله فقط یک پاسخ درست برای رفتن به مرحله بعد لازم است.
۲. پاسخ غلط در هر مرحله‌ای (بدون استثنا) همیشه کارت را به DAILY بازمی‌گرداند و تاریخ مرور بعدی را به فردا (today + 1) منتقل می‌کند؛ کلمه هرگز در همان روز دوباره تکرار نمی‌شود.
۳. واژه در مرحله LEARNED با پاسخ درست در همان مرحله می‌ماند، اما با پاسخ غلط در مرور یادگرفته‌ها به مرحله DAILY بازمی‌گردد و فردا سررسید می‌شود (تصمیم D07 به‌روزشده).
۴. هیچ شمارنده یا پرچم شکست مسیر وجود ندارد.

### شبه‌کد

```kotlin
fun learningTransition(i: TransitionInput): TransitionResult {
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
۲. در مرور یادگرفته‌ها (LEARNED) در صورت پاسخ درست مرحله در LEARNED باقی می‌ماند، اما با پاسخ غلط مرحله به DAILY بازمی‌گردد (جهت مرور از فردا). در هر دو حالت سختی به‌روز شده، تاریخچه ثبت می‌شود و قفل هم‌روز اعمال می‌گردد.
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

پارسر محض است (بدون وابستگی به دیتابیس یا فریم‌ورک اندروید) و در لایه `domain` قرار دارد.
هدف سیستم این است که متن‌های نامنظم و ترکیبی ورودی (متن چسبانده‌شده از کلیپ‌بورد یا فایل‌های متنی) را دریافت کرده و بدون نیاز به اینترنت و بدون هوش مصنوعی (کاملاً Deterministic، آفلاین و مبتنی بر قوانین زبانی قطعی)، واژگان، ترجمه‌ها، یادداشت‌ها، نکات گرامری، اجزای تفکیک‌شده و تنوع‌های زبانی را استخراج کند.

```kotlin
data class ParsedEntry(
    val sourceText: String,
    val translations: List<String>,       // پس از شکستن با جداکننده بخش 5.3
    val note: String?,
    val grammarNotes: String?,            // نکات اختصاصی گرامری
    val categoryNames: List<String>,      // از متن ساده خالی؛ از فایل‌های ساخت‌یافته پر می‌شود
    val entryType: EntryType,             // WORD, PHRASE, SENTENCE, IDIOM, COLLOCATION, STRUCTURE
    val confidence: Double,               // 0.0 تا 1.0 (95%+ عالی، 80%+ خوب/خودکار، 65%+ قابل قبول، <50% نیازمند بررسی)
    val lineNumber: Int,
    val rawLines: List<String>,
    val evidence: List<String>,           // دلایل انتساب اعتماد و ساختار (مانند numbered, spanishDetected)
    val variants: List<VocabularyVariantEntry> = emptyList(),
    val breakdowns: List<VocabularyBreakdownEntry> = emptyList(),
    val relations: List<VocabularyRelationEntry> = emptyList(),
    val possibleCorrection: String? = null // پیشنهاد اصلاح املا بدون دستکاری در منبع اصلی
)

data class VocabularyVariantEntry(
    val sourceText: String,
    val translationText: String?,
    val variantType: VariantType          // MASCULINE, FEMININE, ALTERNATIVE
)

data class VocabularyBreakdownEntry(
    val sourcePart: String,
    val translationPart: String,
    val orderIndex: Int
)

data class VocabularyRelationEntry(
    val relatedSourceText: String,
    val relationType: RelationType,       // USED_IN, INFLECTED_FORM, SYNONYM, ANTONYM, CONTRAST, EXAMPLE_OF, RELATED_TO, DERIVED_FROM
    val confidence: Double
)

data class ParseWarning(
    val type: ParseWarningType,
    val lineNumber: Int,
    val rawText: String,
    val message: String
)

enum class ParseWarningType {
    ORPHAN_SOURCE,
    ORPHAN_TRANSLATION,
    ORPHAN_LINE,
    UNKNOWN_FORMAT,
    NOTE_TOO_LONG,
    TOO_MANY_TRANSLATIONS,
    POSSIBLE_TYPO,
    CONFLICT
}

data class ParseResult(val entries: List<ParsedEntry>, val warnings: List<ParseWarning>)
```

---

### خط لوله کامل پارسر (Parser Pipeline)

پارسر نباید بر اساس یک Regex غول‌پیکر، شکننده و غیرقابل نگهداری ساخته شود. ساختار خط لوله یک ماشین حالت چندمرحله‌ای جریان‌محور ($O(n)$ نسبت به تعداد خطوط) است:

```text
┌────────────────────────────────────────────────────────┐
│                      RAW IMPORT                        │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              1. NORMALIZATION (NFC, Whitespace)        │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              2. LANGUAGE & SCRIPT DETECTOR             │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              3. LINE CLASSIFIER (10 Types)             │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              4. ENTRY STATE MACHINE                    │
│   (Entry Boundary / Translation Pairing / Breakdowns)  │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              5. ENTRY VALIDATOR & CONFIDENCE           │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│              6. DUPLICATE & RELATIONSHIP ENGINE        │
└───────────────────────────┬────────────────────────────┘
                            ↓
┌────────────────────────────────────────────────────────┐
│                  OUTPUT (ParseResult)                  │
└────────────────────────────────────────────────────────┘
```

---

### اصول و قواعد ۹۵ گانه بنیادین پارسر و ورود واژگان

این قواعد برگرفته از معماری استاندارد پردازش متن FlashLearn/Yadin و رفع باگ‌های ثبت‌شده در ممیزی هستند:

1. **اصل عدم تغییر متن اصلی (Preserve Original Text)**: پارسر هرگز متن اصلی را برای «زیباسازی» یا «اصلاح غلط املایی» تغییر نمی‌دهد. به عنوان مثال، اگر ورودی حاوی `lo melhor` باشد، متن مبدأ دقیقاً `lo melhor` ذخیره می‌شود و هرگز به صورت خودکار به `lo mejor` تبدیل نمی‌شود. در صورت نیاز، اصلاح پیشنهادی در `possibleCorrection` ذخیره می‌شود.
2. **حفظ کاراکترهای باارزش و لهجه‌های اسپانیایی**: لهجه‌های اسپانیایی (`á é í ó ú ü ñ`) و علائم تعجبی و سؤالی معکوس (`¿ ¡`)، پرانتزها و سه نقطه (`...`) کاملاً حفظ می‌شوند. حذف لهجه‌ها مطلقاً ممنوع است؛ زیرا در زبان اسپانیایی معنا را تغییر می‌دهد (مثلاً `sí` یعنی بله و `si` یعنی اگر؛ `él` یعنی او و `el` یعنی حرف تعریف مذکر).
3. **قاعده سیگنال شماره‌گذاری و گپ‌ها (Numbering Signal & Valid Gaps)**: شماره‌گذاری فقط یک سیگنال قوی برای تشخیص شروع مدخل جدید است، نه الزام. اگر ورودی دارای پرش شماره باشد (مثلاً ردیف ۲۴، سپس ۲۹، سپس ۴۷)، این فاصله‌ها (Gaps) کاملاً معتبرند و نباید فرض شود که ردیف‌های ۲۵ تا ۲۸ مفقود شده‌اند. همچنین مدخل‌های فاقد شماره نیز کاملاً معتبر بوده و بدون خطا پذیرفته می‌شوند.
4. **تفکیک مدخل اصلی از اجزای تجزیه (Main Entry vs. Breakdown)**: اگر پس از یک مدخل اصلی، چند خط متوالی با ساختار `Spanish : Persian` بیایند (مانند `estoy seguro de que: مطمئنم که`، `todo: همه‌چیز`، `irá bien: خوب پیش خواهد رفت`)، این خطوط به عنوان **BREAKDOWN** شناخته شده و در فیلد `breakdowns` یا یادداشت مدخل اصلی ادغام می‌شوند.
5. **پیش‌فرض عدم ارتقای اجزای تجزیه (`promoteBreakdownToEntry = false`)**: اجزای تفکیک‌شده به صورت خودکار واژه مستقل در دیتابیس ایجاد نمی‌کنند تا از شلوغی بانک واژگان جلوگیری شود.
6. **محصور بودن یادداشت‌ها (Notes Containment Rule)**: هنگامی که پارسر در وضعیت خواندن `NOTE`، `GRAMMAR_NOTE`، یا `COMMENT` است، هیچ کلمه یا عبارتی که در متن توضیحات ذکر شده (مثلاً: «این فعل از ir مشتق شده است») نباید به عنوان یک Concept مستقل وارد واژگان شود.
7. **تفکیک یادداشت پرانتزی از پرانتزهای ترجمه (Parenthetical Notes vs. Qualifiers)**:
   - پرانتزهای توضیحی در انتهای خط ترجمه مانند `la esperanza امید (در شماره ۹ استفاده شد)` به صورت خودکار تفکیک می‌شوند: متن مبدأ `la esperanza`، ترجمه `امید`، و متن داخل پرانتز به یادداشت (`note`) منتقل می‌شود.
   - اما پرانتزهای ساختاری ترجمه مانند `(مذکر)` و `(مؤنث)` جزء اطلاعات معنی بوده و هرگز حذف نمی‌شوند.
8. **تنوع‌های جنسیتی و نگارشی (Gender & Spelling Variants)**: عباراتی مانند `el científico; la científica` نباید به دو Concept نامرتبط و مستقل تبدیل شوند، بلکه به عنوان صورت‌های متنوع همان مفهوم با ترجمه‌های متناظر (`دانشمند (مذکر) / دانشمند (مؤنث)`) نگهداری می‌شوند. جداکننده‌های مجاز برای تنوع‌ها شامل `;`، `/` و کلمه `یا` هستند.
9. **جلوگیری از دور ریختن داده (No Data Loss / Orphan Logging)**: هر خطی که به دلیلی جفت نشود، با شماره خط و متن خام در `warnings` ثبت شده و به جدول `import_review_items` ارسال می‌شود (`ORPHAN_SOURCE` یا `ORPHAN_TRANSLATION`).
10. **کارایی بالا و بهینه‌سازی الگوریتم‌ها**: تمامی عبارات منظم (Regex) به صورت متغیرهای استاتیک و پیش‌کامپایل‌شده در سطح فایل تعریف می‌شوند. ساخت الگو درون حلقه خطوط یا مقایسه‌گرها اکیداً ممنوع است (رفع کامل باگ ب-۱۱ ممیزی).
11. **حل خطای نشانگرهای متنی (رفع باگ الف-۳ ممیزی)**: نشانگرهای متنی (مانند «مثال»، «توجه»، «نکته») هرگز نباید صرفاً با تطبیق ابتدای خط بدون دو‌نقطه عمل کنند، تا ترجمه‌های معتبری که با این کلمات شروع می‌شوند (مانند ترجمه فارسی واژه atención به «توجه») از بین نروند. نشانگر تنها در صورتی معتبر است که بلافاصله با علامت دو‌نقطه `:` همراه باشد یا کل خط دقیقاً برابر با نشانگر باشد.
12. **حالت‌های ماشین حالت پارسر**: شامل ۷ وضعیت `IDLE`, `READING_ENTRY`, `WAITING_FOR_TRANSLATION`, `READING_BREAKDOWN`, `READING_NOTE`, `READING_COMMENT`, `FINALIZING_ENTRY`.
13. **سیستم امتیازدهی وزن‌دار جفت‌ها (Candidate Scoring)**: تطبیق چندخطی مبدأ و ترجمه با وزن‌های عددی دقیق ارزیابی می‌شود و در صورت کسب امتیاز $\ge 70$ به عنوان جفت معتبر پذیرفته می‌شود.
14. **سطوح اعتماد سه‌گانه**:
    - اعتماد $\ge 80\%$: ورود خودکار (Auto).
    - اعتماد $50\%$ تا $79\%$: ورود با هشدار یا بررسی (Review).
    - اعتماد $< 50\%$: نیازمند تایید دستی در صف بررسی (Needs Review).
15. **تشخیص خودکار نوع مدخل (`classifyEntry`)**:
    - ۱ کلمه $ightarrow$ `WORD`
    - ۲ تا ۵ کلمه $ightarrow$ `PHRASE`
    - ۶ کلمه یا بیشتر $ightarrow$ `SENTENCE`
    - الگوهای ساختاری زبانی (مانند `a no ser que...`) $ightarrow$ `STRUCTURE`
    - اصطلاحات خاص $ightarrow$ `IDIOM` (در صورت مشخص بودن در فایل یا انتخاب کاربر).

---

### مرحله اول: نرمال‌سازی (Normalization)

قبل از اجرای هرگونه منطق پردازشی، متن ورودی به صورت استاندارد نرمال می‌شود:
۱. تمام پایان‌خط‌های ناهمگون و سیستم‌های عامل مختلف (`
` و ``) به `
` تبدیل می‌شوند.
۲. نویسه فاصله با پهنای صفر (Zero-Width Space یا `U+200B`) از تمام متن حذف می‌شود.
۳. نرمال‌سازی یونی‌کد به فرم استاندارد **NFC** (ترکیب نویسه‌های تفکیک‌شده اعراب با حروف پایه).
۴. تبدیل نویسه Tab به فاصله استاندارد.
۵. ادغام فاصله‌های متوالی (تبدیل فاصله‌های چندگانه به یک فاصله واحد).
۶. تبدیل ارقام فارسی و عربی (`۰-۹` و `٠-٩`) به ارقام استاندارد لاتین صرفاً برای ارزیابی الگوی شماره‌گذاری خطوط؛ اما متن اصلی جهت نمایش بدون تغییر نگهداری می‌شود.
۷. علائمی که از ابتدا و انتهای خطوط برای استخراج حذف می‌شوند:
   `• * - — _ ~ # | \ ^ = + < > » « ➜ →`
۸. علائم معنایی ساختاری که **نباید** حذف شوند:
   `¿ ? ¡ ! ... , . : ; ( )`

---

### مرحله دوم: الگوهای کامپایل‌شده در سطح فایل (Precompiled Regexes)

برای جلوگیری از افت کارایی روی دستگاه‌های موبایل ($O(n)$ به جای ایجاد الگوهای تکراری روی هزاران خط متن):

```kotlin
// الگوهای ثابت و پیش‌کامپایل‌شده در سطح فایل (File-level constants)
private val REGEX_SEPARATOR = Regex("^[-—_=*•]{3,}$")
private val REGEX_NUMBERED = Regex("^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*")
private val REGEX_DASH_SPLIT = Regex("\s+[-—]\s+")
private val REGEX_STRIP_DECORATIONS = Regex("^[-—*•#»«➜→\s]+")
private val REGEX_PARENTHETICAL_NOTE = Regex("^(.*?)\s*[(（]([^)）]{4,})[)）]\s*$")
private val REGEX_EXAMPLE_MARKER = Regex("^(example|examples|note|notes|usage|ejemplo|ejemplos|nota|uso)\s+[^:]{1,60}:", RegexOption.IGNORE_CASE)
private val WHITESPACE_COLLAPSE = Regex("\s+")
```

توابع ارزیابی کمکی:
- `isSeparator(line)`: تطبیق کامل خط با `REGEX_SEPARATOR`.
- `isNumbered(line)`: بررسی آغاز خط با الگوی عددی `REGEX_NUMBERED`.
- `stripNumbering(line)`: حذف پیشوند شماره‌گذاری و سپس پاک‌سازی نویسه‌های تزئینی ابتدای خط با `REGEX_STRIP_DECORATIONS`.
- `hasLatin(line)`: بررسی وجود کاراکترهای اسکریپت لاتین (`Character.UnicodeScript.LATIN`).
- `hasPersian(line)`: بررسی وجود کاراکترهای بازه الفبای عربی/فارسی (`\u0600..\u06FF`, `\u0750..\u077F`, `\u08A0..\u08FF`).
- `detectScript(line)`:
  - اگر هم دارای لاتین و هم فارسی باشد: `MIXED`
  - اگر فقط لاتین: `LATIN` (کاندید زبان اسپانیایی/مبدأ)
  - اگر فقط فارسی: `PERSIAN` (کاندید زبان فارسی/ترجمه)
  - سایر موارد: `UNKNOWN`

تابع شکستن جفت درون‌خطی (`splitPair(line)`):
1. اگر خط شامل علامت پیکان `→` یا `➜` باشد، در اولین وقوع به بخش چپ (مبدأ) و راست (ترجمه) شکسته می‌شود.
2. در غیر این صورت، با خط تیره احاطه‌شده توسط فاصله (`REGEX_DASH_SPLIT`) به دو بخش شکسته می‌شود.
3. در غیر این صورت، با اولین علامت دو‌نقطه `:` شکسته می‌شود مشروط بر آنکه بخش چپ دارای حروف لاتین و بخش راست دارای حروف فارسی باشد.

---

### مرحله سوم: فرهنگ نشانگرها (Markers Dictionary) و رفع خطای کلیدواژه‌ها

جهت رفع باگ الف-۳ ممیزی: **نشانگرها فقط زمانی شناسایی می‌شوند که به دو‌نقطه ختم شوند یا کل خط معادل نشانگر باشد**. تطبیق روی متن کوچک‌شده (`lowercase(Locale.ROOT)`) یک بار در ابتدای ارزیابی خط محاسبه می‌شود:

| رده نشانگر | کلیدواژه‌های معتبر (فارسی، اسپانیایی، انگلیسی) |
|------------|------------------------------------------------|
| **NOTE** | `نکته:` ، `توضیحات:` ، `احتمال اشتباه:` ، `توجه:` ، `مثال:` ، `Note:` ، `Notes:` ، `Usage:` ، `Ejemplo:` ، `Ejemplos:` ، `Nota:` ، `Uso:` |
| **GRAMMAR_NOTE** | `نکته گرامری:` ، `نکته گرامری` ، `Grammar:` ، `Grammar Note:` ، `Grammatical Note:` ، `Nota gramatical:` |
| **BREAKDOWN** | `تجزیه:` ، `تجزیه` ، `Breakdown:` ، `Breakdown` |
| **DERIVATIVE** | `مشتق شده از:` ، `مشتق شده از` ، `Derived from:` ، `Derived from` |
| **VARIANT** | `حالت دیگر:` ، `حالت دیگر` ، `Variant:` ، `Variants:` |
| **RELATION** | `مرتبط:` ، `مرتبط` ، `Related:` ، `Related` ، `Synonym:` ، `Antonym:` |

---

### مرحله چهارم: طبقه‌بندی خطوط (Line Classification)

هر خط بر اساس قواعد زیر به یکی از ۱۰ نوع طبقه‌بندی تبدیل می‌شود:
۱. اگر خط با `#` یا `//` شروع شود $\rightarrow$ `COMMENT` (صرف‌نظر و نادیده گرفته می‌شود).
۲. اگر خط منطبق بر `isSeparator` باشد $\rightarrow$ `SEPARATOR` (پایان‌دهنده مدخل جاری).
۳. اگر خط صرفاً از ارقام تشکیل شده باشد $\rightarrow$ `NUMBER` (نادیده گرفته می‌شود).
۴. تطبیق با نشانگرهای رسمی $\rightarrow$ بر حسب نوع: `BREAKDOWN`, `DERIVATIVE`, `VARIANT`, `RELATION`, `GRAMMAR_NOTE`, `NOTE`.
۵. جفت درون‌خطی موفق (`splitPair` با چپ لاتین و راست فارسی) $\rightarrow$ `ENTRY_HEADER`.
۶. اسکریپت فارسی خالص بدون لاتین $\rightarrow$ `TRANSLATION`.
۷. وجود حروف لاتین بدون فارسی $\rightarrow$ `ENTRY_HEADER`.
۸. متن ترکیبی (`MIXED`) با ساختار تفکیک‌ناپذیر $\rightarrow$ `UNKNOWN`.

---

### مرحله پنجم: ماشین حالت پردازش مدخل‌ها (Entry State Machine)

ماشین حالت وظیفه دارد خطوط طبقه‌بندی‌شده را به مدخل‌های منسجم پیوند دهد:

```text
حالت‌های ماشین حالت:
- IDLE: وضعیت ابتدایی یا پس از بستن یک مدخل
- READING_ENTRY: دریافت خط اول یا خطوط ادامه مبدأ
- WAITING_FOR_TRANSLATION: مبدأ دریافت شده و منتظر خط یا خطوط ترجمه
- READING_BREAKDOWN: جمع‌آوری خطوط تفکیک و اجزای واژه
- READING_NOTE: جمع‌آوری نکات، گرامر یا روابط وابسته
- FINALIZING_ENTRY: بستن مدخل، اعتبارسنجی و اضافه به خروجی
```

#### سیستم امتیازدهی و جفت‌سازی کاندیدها (Candidate Scoring Formula)
برای متونی که ساختار چندخطی دارند و جفت درون‌خطی صریح ندارند، انطباق خطوط مجاور با این ضرایب محاسبه می‌شود:
- کاندید مبدأ اسپانیایی: $+30$ امتیاز
- کاندید ترجمه فارسی: $+30$ امتیاز
- قرارگیری در دو خط متوالی و مجاور: $+20$ امتیاز
- دارا بودن شماره‌گذاری مشترک در ابتدای خط: $+10$ امتیاز
- ساختار بصری و تورفتگی مشابه مدخل‌های قبل: $+10$ امتیاز
- وجود نشانگر نکته در خط: $-30$ امتیاز
- طول خط بیش از ۱۰۰ کاراکتر (احتمال پاراگراف توضیحی): $-40$ امتیاز
- **حد آستانه ساخت مدخل**: مجموع امتیاز $\ge 70$.

#### قواعد پردازش انواع خطوط:
- **ENTRY_HEADER**:
  - اگر مدخل جاری ترجمه نداشته باشد، خط جاری فاقد شماره باشد و خط بعدی فارسی باشد $\rightarrow$ به عنوان مدخل چندخطی به مبدأ اضافه می‌شود (`multilineSource`).
  - در غیر این صورت، مدخل قبلی نهایی‌سازی و ذخیره (`flush`) شده و مدخل جدیدی آغاز می‌گردد.
- **TRANSLATION**:
  - اگر مدخل جاری ترجمه نداشته باشد $\rightarrow$ به عنوان ترجمه اول ثبت می‌شود.
  - اگر مدخل جاری ترجمه داشته باشد $\rightarrow$ ترجمه جدید به لیست ترجمه‌ها اضافه می‌شود (یا با `" / "` ادغام می‌گردد).
  - اگر مدخل جاری وجود نداشته باشد و خط بعدی لاتین باشد $\rightarrow$ نگهداری موقت به عنوان `orphanTranslation` برای انطباق با خط بعد (الگوی فارسی قبل از اسپانیایی).
- **BREAKDOWN / DERIVATIVE / VARIANT / RELATION / GRAMMAR_NOTE / NOTE**:
  - به صورت داده‌های تفکیک‌شده به مدخل جاری ضمیمه می‌شوند.
- **UNKNOWN**:
  - اگر مدخل جاری در انتظار ترجمه است و خط شامل حروف لاتین باشد $\rightarrow$ ادامه مبدأ.
  - در غیر این صورت به عنوان یادداشت اضافه می‌شود یا هشدار `UNKNOWN_FORMAT` صادر می‌گردد.

#### تفکیک هوشمند پرانتزهای توضیحی (Parenthetical Note Extraction):
پس از استخراج ترجمه، الگوی `REGEX_PARENTHETICAL_NOTE` بررسی می‌شود:
- اگر متن داخل پرانتز حاوی کلیدواژه‌های یادداشت، ارجاع به شماره‌ها (مانند `در شماره ۹ استفاده شد`) یا توضیحات بلند باشد $\rightarrow$ پرانتز از متن ترجمه حذف شده و متن داخل آن به فیلد `note` منتقل می‌شود.
- اگر متن داخل پرانتز کوتاه و گرامری باشد (مانند `(مذکر)`، `(مؤنث)`، `(فعل)`، `(صفت)`) $\rightarrow$ در متن ترجمه باقی می‌ماند.

---

### مرحله ششم: اعتبارسنجی، درجه اعتماد و شواهد

#### درجه‌بندی سطح اعتماد (Confidence Scoring):
- **1.0 (بسیار مطمئن)**: جفت درون‌خطی با علامت فلش یا شماره‌گذاری به همراه ترجمه صریح.
- **0.95 (مطمئن)**: خطوط مجاور مبدأ و ترجمه بدون ابهام یا ترجمه قبل از مبدأ جفت‌شده.
- **0.80 (خوب / استاندارد)**: مدخل فاقد شماره با خطوط مجاور معتبر.
- **0.65 (قابل قبول)**: ساختارهای چندخطی با نمرات مرزی یا وجود ابهام جزئی.
- **زیر 0.50 (مشکوک)**: ارسال مستقیم به جدول `import_review_items` برای بررسی دستی توسط کاربر.

#### برچسب‌های شواهد (Evidence Flags):
شامل: `numbered`, `spanishDetected`, `adjacentPersianTranslation`, `inlineTranslationMarker`, `multilineSource`, `parentheticalNoteExtracted`, `genderVariantsDetected`, `noteMarkerAbsent`.

#### تعیین نوع مدخل (`classifyEntry`):
```text
تعداد کلمات مبدأ (با شکستن روی فاصله):
  ۱ کلمه           => WORD
  ۲ تا ۵ کلمه       => PHRASE
  ۶ کلمه یا بیشتر   => SENTENCE
```
(انواع `IDIOM`، `COLLOCATION` و `STRUCTURE` فقط در صورت انتخاب صریح کاربر یا فایل ساخت‌یافته اعمال می‌شوند).

---

### تست‌های مرجع الگوریتم پارسر (Test Cases)

۱. **مدخل ساده با چند ترجمه و فلش**:
   - ورودی: `1. manzana → سیب / سیب درختی`
   - خروجی: مبدأ: `manzana` | ترجمه‌ها: `["سیب", "سیب درختی"]` | نوع: `WORD` | اعتماد: `1.0` | شواهد: `[numbered, inlineTranslationMarker]`
۲. **مدخل چندخطی با نکته رسمی**:
   - ورودی:
     ```text
     hacer falta
     نیاز بودن
     نکته: فعل با مفعول غیرمستقیم
     ```
   - خروجی: مبدأ: `hacer falta` | ترجمه‌ها: `["نیاز بودن"]` | یادداشت: `فعل با مفعول غیرمستقیم` | نوع: `PHRASE` | اعتماد: `0.95`
۳. **مدخل چندخطی بدون شماره**:
   - ورودی:
     ```text
     no puedes hacer una tortilla
     sin romper huevos
     نمی‌توانی بدون شکستن تخم‌مرغ املت درست کنی
     ```
   - خروجی: مبدأ: `no puedes hacer una tortilla sin romper huevos` | نوع: `SENTENCE` | اعتماد: `0.85`
۴. **تفکیک یادداشت پرانتزی ارجاعی**:
   - ورودی: `la esperanza امید (در شماره ۹ استفاده شد)`
   - خروجی: مبدأ: `la esperanza` | ترجمه‌ها: `["امید"]` | یادداشت: `در شماره ۹ استفاده شد`
۵. **حفظ پرانتزهای جنسیتی و معنایی**:
   - ورودی: `el científico - دانشمند (مذکر) / دانشمند (مؤنث)`
   - خروجی: مبدأ: `el científico` | ترجمه‌ها: `["دانشمند (مذکر)", "دانشمند (مؤنث)"]` (پرانتزها دست‌نخورده می‌مانند).
۶. **ترجمه قبل از واژه اسپانیایی**:
   - ورودی:
     ```text
     دانشمند
     el científico
     ```
   - خروجی: مبدأ: `el científico` | ترجمه‌ها: `["دانشمند"]` | هشدار مفقودی رفع شده و جفت معتبر تشکیل می‌شود.
۷. **حفظ املای اصلی ورودی (عدم اصلاح خودکار)**:
   - ورودی: `lo melhor → بهترین`
   - خروجی: مبدأ دقیقاً `lo melhor` ثبت می‌شود و متن هرگز به `lo mejor` تغییر داده نمی‌شود.
۸. **پرش در شماره‌گذاری (Valid Gaps)**:
   - ورودی: ردیف‌های ۲۴، ۲۹ و ۴۷
   - خروجی: ۳ مدخل مستقل ساخته می‌شوند بدون هیچ پیام خطایی مبنی بر گم‌شدن ردیف‌های میانی.
۹. **تفکیک اجزای عبارت (Breakdown)**:
   - ورودی:
     ```text
     estoy seguro de que todo irá bien
     مطمئنم که همه‌چیز خوب پیش می‌رود
     estoy seguro de que: مطمئنم که
     todo: همه‌چیز
     irá bien: خوب پیش خواهد رفت
     ```
   - خروجی: یک مدخل اصلی ساخته می‌شود و سه خط بعدی به عنوان فیلد `breakdowns` ذخیره می‌گردند.
۱۰. **رد خطوط کامنت و تزئینی**:
    - ورودی: خطوطی که با `#` یا `//` یا خطوط جداکننده `---` شروع می‌شوند.
    - خروجی: کاملاً از چرخه ساخت واژه حذف می‌شوند.

---

## 6.10 خواننده‌های فایل (File Readers)

همه خواننده‌ها خروجی را به صورت لیستی از `ParsedEntry` همراه با هشدارها برمی‌گردانند. خواندن فایل‌ها همواره روی دیسپچر `Dispatchers.IO` و با امکان گزارش پیشرفت درصد به UI انجام می‌شود. حداکثر اندازه مجاز فایل ۲۰ مگابایت است.

### ۱. متن ساده (Text / Paste)
از فایل متنی یا کلیپ‌بورد؛ پشتیبانی کامل از رمزگذاری UTF-8 (با و بدون BOM)؛ پردازش توسط موتور پارسر بخش ۶.۹ انجام می‌شود.

### ۲. فایل‌های جداشده با نویسه (CSV / TSV)
۱. رمزگذاری UTF-8 (با و بدون BOM).
۲. تشخیص هوشمند جداکننده (Delimiter Auto-detection): شمارش نویسه‌های `,`، `;` و `\t` در خط اول فایل با رعایت فیلدهای احاطه‌شده در گیومه (طبق استاندارد RFC 4180). نویسه‌ای که بیشترین تکرار را داشته باشد انتخاب می‌شود.
۳. نگاشت ستون‌های استاندارد بر اساس سطر عنوان (اختیاری، بی‌توجه به بزرگی و کوچکی حروف):

| ستون مفهومی | نام‌های سرستون پذیرفته‌شده |
|-------------|----------------------------|
| مبدأ | `source`, `word`, `spanish`, `واژه`, `کلمه`, `اسپانیایی` |
| ترجمه | `translation`, `meaning`, `translations`, `ترجمه`, `معنی`, `فارسی` |
| یادداشت | `note`, `notes`, `یادداشت`, `نکته` |
| دسته | `category`, `categories`, `دسته`, `دسته‌بندی` |
| نوع | `type`, `entrytype`, `نوع` |

۴. در صورت نبود سطر عنوان، ترتیب پیش‌فرض ستون‌ها: ستون ۱: مبدأ، ستون ۲: ترجمه، ستون ۳: یادداشت، ستون ۴: دسته، ستون ۵: نوع.
۵. پشتیبانی از چند ترجمه در یک ستون با استفاده از جداکننده‌های استاندارد بخش ۵.۳ (`/`, `،`, `,`, `;`).
۶. پشتیبانی از چند دسته در یک ستون با استفاده از جداکننده `|`.
۷. ردیف‌هایی که فاقد مبدأ یا فاقد ترجمه باشند به عنوان ردیف نامعتبر (`INVALID_ROW`) در `import_review_items` ثبت می‌شوند.

### ۳. فایل JSON
پشتیبانی از دو ساختار:
الف. آرایه‌ای از اشیا یا شیء با کلید `entries`:
```json
{
  "entries": [
    {
      "source": "manzana",
      "translations": ["سیب", "سیب درختی"],
      "note": "میوه",
      "categories": ["خوراکی و غذا"],
      "entryType": "WORD"
    }
  ]
}
```
فیلد `translations` می‌تواند به جای آرایه، یک متن باشد که با بخش ۵.۳ تفکیک می‌شود.
ب. اگر فایل حاوی کلید `backupType` باشد، برنامه تشخیص می‌دهد که فایل پشتیبان است و کاربر را با پیام «این فایل پشتیبان است. لطفاً از بخش بازیابی استفاده کنید» به صفحه بازیابی هدایت می‌کند.

### ۴. فایل اکسل (XLSX)
بدون وابستگی به کتابخانه‌های سنگین خارجی (نظیر Apache POI):
۱. فایل XLSX به عنوان یک آرشیو زیپ با `java.util.zip.ZipInputStream` گشوده می‌شود.
۲. جدول رشته‌های مشترک (`xl/sharedStrings.xml`) با `XmlPullParser` خوانده شده و به آرایه‌ای از رشته‌ها تبدیل می‌شود.
۳. کاربرگ اول (`xl/worksheets/sheet1.xml`) به صورت جریانی پارس شده و مقادیر سلول‌ها بر اساس ارجاع ستون‌ها استخراج می‌گردد.
۴. قواعد انطباق ستون‌ها دقیقاً منطبق بر قوانین CSV اعمال می‌شود.

### ۵. پایگاه داده SQLite
فایل ارائه‌شده توسط کاربر روی حافظه موقت کپی شده و به صورت فقط‌خواندنی (`OPEN_READONLY`) باز می‌شود:
۱. در صورت وجود جداول استاندارد یادین/فلش‌لرن (`concepts`, `contents`): داده‌ها با نگاشت کامل استخراج می‌شوند.
۲. در صورت وجود جداول ساده با نام‌های `words` یا `vocabulary` (دارای ستون‌های `source` و `translation`): داده‌ها به عنوان مدخل‌های ساده استخراج می‌شوند.
۳. در غیر این صورت، خطای صریح «ساختار دیتابیس پشتیبانی نمی‌شود» گزارش می‌شود.

---

## 6.11 یافتن مفهوم برای مدخل واردشده (ResolveConceptForParsedEntry) و اجرای ورود

این الگوریتم وظیفه دارد برای هر مدخل تولیدشده از پارسر، وضعیت تطبیق با مفاهیم موجود دیتابیس را بر اساس کلیدهای یکتا تعیین کند.

### مراحل تطبیق و تصمیم‌گیری:
```kotlin
sealed interface ResolveResult {
    data class CreateNewConcept(val contentsToInsert: List<Piece>) : ResolveResult
    data class ReuseConcept(val conceptId: String, val newContentsToInsert: List<Piece>) : ResolveResult
    data class Conflict(val matchedConceptIds: Set<String>, val pieces: List<Piece>) : ResolveResult
}
```

۱. **مرحله جست‌وجوی سریع ایندکس‌دار (حل باگ ب-۱ و ب-۲ ممیزی)**:
به جای خواندن کل جدول واژه‌ها یا متن‌ها در حافظه، از کوئری تک‌ردیفی ایندکس‌دار استفاده می‌شود:
```sql
SELECT c.* FROM concepts c 
JOIN contents x ON x.conceptId = c.id 
WHERE x.languageCode = :langCode AND x.canonicalKey = :canonicalKey AND c.active = 1 
LIMIT 1;
```
برای واردات‌های انبوه (بیش از ۱۰۰ مدخل)، در ابتدای عملیات یک نگاشت سبک حافظه‌ای از `canonicalKey -> conceptId` برای مفاهیم فعال ساخته می‌شود تا عملیات تطبیق با سرعت $O(1)$ انجام گیرد.

۲. **تعیین وضعیت**:
- **تعداد شناسه‌های منطبق $= 0$**: `CreateNewConcept` (مفهوم جدید ساخته می‌شود).
- **تعداد شناسه‌های منطبق $= 1$**: `ReuseConcept` (مفهوم موجود پیدا شد؛ ترجمه‌ها و دسته‌های جدید بررسی می‌شوند).
- **تعداد شناسه‌های منطبق $> 1$**: `Conflict` (تکه‌های این ورودی به چند مفهوم مختلف متصل شده‌اند؛ ادغام خودکار اکیداً ممنوع بوده و به صف بررسی منتقل می‌شود).

### اعمال سیاست‌های تکراری (Duplicate Policies):
- **SKIP**: در صورت وجود مفهوم، بدون تغییر رد می‌شود؛ شمارنده `skipped` افزایش می‌یابد.
- **MERGE (پیش‌فرض)**:
  - ترجمه‌های جدید ورودی با `translationIndex` افزایشی به مفهوم اضافه می‌شوند.
  - دسته‌های جدید اضافه می‌شوند (دسته‌های قبلی حفظ می‌گردند).
  - در صورت نبود یادداشت قبلی، یادداشت ورودی ثبت می‌شود.
  - کارت‌های وضعیت یادگیری، سختی و تاریخچه مرور مطلقاً دست نمی‌خورند و پایدار می‌مانند.
  - اگر هیچ ترجمه یا دسته جدیدی وجود نداشته باشد: شمارنده `duplicateNoChange` افزایش می‌یابد؛ وگرنه `merged`.
- **REPLACE**:
  - ترجمه‌های موجود با ترجمه‌های جدید ورودی جایگزین می‌شوند (با شروع `translationIndex` از صفر).
  - یادداشت و دسته‌های ورودی جایگزین مقادیر قبلی می‌شوند.
  - وضعیت یادگیری و پیشرفت کارت‌ها برای جلوگیری از بین رفتن دستاوردهای کاربر حفظ می‌شوند؛ شمارنده `replaced`.
- **KEEP_SEPARATE**: مفهوم جدیدی در دیتابیس ساخته می‌شود حتی اگر کلید مبدأ تکراری باشد؛ شمارنده `created`.

### پایداری تصمیمات تضاد (Conflict Resolution Persistence):
در جدول `import_review_items`، ستون اختصاصی `targetConceptId` وجود دارد. هنگامی که کاربر در صفحه بررسی تضادها، مفهوم مقصد را برای ادغام دستی انتخاب می‌کند، شناسه مفهوم در `targetConceptId` ذخیره شده و وضعیت ردیف به `APPROVED` تغییر می‌یابد تا هنگام اعمال، داده دقیقاً در همان مفهوم ادغام شود.

### اجرای دسته‌ای با قابلیت لغو و گزارش پیشرفت (حل باگ ب-۳ و ج-۵ ممیزی):
۱. ورود داده‌ها در دسته‌های **۱۰۰ تا ۲۰۰ تایی** در تراکنش‌های مستقل دیتابیس انجام می‌شود.
۲. شکست یک دسته مانع از ثبت دسته‌های موفق قبلی نمی‌شود و موارد شکست‌خورده با وضعیت `INVALID_ROW` به صف بررسی منتقل می‌شوند.
۳. گزارش پیشرفت زنده (درصد و تعداد موارد پردازش‌شده) از طریق Flow به UI ارسال می‌گردد.
۴. عملیات کورتین از تابع کمکی `runCatchingCancellable` استفاده می‌کند تا با فشردن دکمه لغو توسط کاربر، استثنای `CancellationException` بلعیده نشود و فرآیند سریعاً متوقف گردد.
۵. پس از پایان ورود موفق، کش بانک سؤالات آزمون (`QuizBankCache`) باطل می‌شود تا واژگان جدید بلافاصله در آزمون‌ها در دسترس باشند.

### صف بررسی (import_review_items):
موارد با اعتماد زیر ۸۰٪ یا دارای تضاد در این جدول قرار می‌گیرند:
- امکان ویرایش متن مبدأ، ترجمه‌ها، یادداشت و دسته‌ها توسط کاربر.
- دکمه «تایید»: اعمال به دیتابیس با اعتماد ۱.۰ و تغییر وضعیت به `APPROVED`.
- دکمه «رد»: تغییر وضعیت به `REJECTED`.
- دکمه «پاک‌سازی بررسی‌شده‌ها»: حذف رکوردهای `APPROVED` و `REJECTED`.

---

## 6.12 بانک اولیه (Seed)

### منبع

فایل `assets/seed/vocabulary_seed.json` (همگام با `docs/vocabulary.json` و `docs/yadin-vocabulary.csv`).
ساختار فایل (قالب استاندارد یادین):

```text
schemaVersion, exportedAt, backupType
payloads → VOCABULARY → concepts[], contents[], categories[], tags[], relations[], variants[], languages[], languagePairs[]
یا data → concepts[], contents[], categories[]
```

مفهوم: {id, entryType, categoryId, favorite, active, createdAt, updatedAt}
محتوا: {id, conceptId, languageCode, text, canonicalKey, translationIndex، و اختیاری notes}
دسته: {id, name}
(در نسخه فعلی: ۶,۶۶۴ مفهوم، ۱۵,۵۶۱ محتوا و رکورد ترجمه، در دسته‌بندی‌های موضوعی؛ واردسازی اتمیک با `withTransaction`)

### زمان اجرا و بازیابی از خطا (Startup & Interrupted Recovery)

هنگام راه‌اندازی برنامه، بعد از ساخت دیتابیس:

```text
App Start
   ↓
settings["seedImported"] == "true" ?
   ├─ بله → پرش از مرحله و ورود عادی به برنامه (Skip)
   └─ خیر
       ↓
   ارزیابی وضعیت فعلی دیتابیس (Validate Seed State)
       ├─ دیتابیس خالی است (تعداد مفاهیم = 0):
       │     اجرای کامل SeedImporter در یک تراکنش
       ├─ دیتابیس معتبر و کامل است (تعداد مفاهیم فعال = 5988 و هر مفهوم دارای 2 کارت):
       │     ثبت settings["seedImported"] = "true"
       └─ دیتابیس ناقص یا مخدوش است (مثلاً خروج یا کرش در میان فرایند Seed قبلی):
              ↓
          پاکسازی کامل تمام داده‌های مرتبط با Seed (Clear Seed-owned data)
              ↓
          اجرای SeedImporter از ابتدا در یک تراکنش واحد
              ↓
          موفقیت کامل → ثبت settings["seedImported"] = "true"
```

نکته مهم: تا زمانی که کل فرایند Seed با موفقیت کامل به پایان نرسد، `seedImported` به هیچ عنوان `true` نمی‌شود تا هیچ دیتای ناقصی به عنوان بانک اولیه پذیرفته نشود.

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

پس از اتمام: شمارش مفاهیم فعال باید برابر تعداد مفاهیم فعال یکتای فایل باشد (برای فایل فعلی دقیقاً ۶,۶۶۴ مفهوم فعال و ۱۵,۵۶۱ ترجمه) و هر مفهوم دو کارت (جمعاً ۱۳,۳۲۸ کارت) داشته باشد. در تست راه‌اندازی (تست ابزاری) بررسی می‌شود.

---

## 6.13 پشتیبان‌گیری (Backup)

### انواع و گزینه‌های سفارشی (BackupOptions)

علاوه بر ۳ حالت کلی، پشتیبان‌گیری تفکیک‌پذیر با انتخاب مستقل بخش‌ها (واژگان، دسته‌ها، سختی، رگبار، آمار مرور، پروسه جلسات، تنظیمات) در `BackupOptions` پیاده‌سازی شده است:

| نوع / قالب | محتوا | فرمت خروجی |
|-----|-------|------------|
| VOCABULARY | کلمات، ترجمه‌ها، دسته‌ها، یادداشت‌ها | JSON |
| PROGRESS | وضعیت‌های یادگیری، دشواری، تاریخچه، جلسات، دستاوردها | JSON |
| FULL | تمامی جداول و اطلاعات دیتابیس به صورت یکپارچه | JSON |
| EXCEL_CSV_VOCABULARY | خروجی جدولی اکسل از واژگان، ترجمه‌ها، دسته‌ها و مراحل | CSV (UTF-8 BOM) |
| EXCEL_CSV_PROGRESS | خروجی اکسل از رگبار، دقت روزانه، پیشرفت ۳۶۵ روز اخیر | CSV (UTF-8 BOM) |
| EXCEL_CSV_COMPLETE | گزارش جامع اکسل شامل تمام بخش‌ها در یک فایل | CSV (UTF-8 BOM) |

### غیرمسدودسازی در کورتین (Non-blocking I/O - تصمیم D47 / ISS-31)
کلیه فرآیندهای تولید، خواندن و نوشتن فایل‌های پشتیبان و استریم‌های خروجی SAF منحصراً بر روی `Dispatchers.IO` اجرا می‌شوند تا از هرگونه فریز نخ اصلی یا خطای ANR در گوشی‌های با حجم واژگان بالا ممانعت به عمل آید.

### قالب فایل JSON

یک فایل جیسون UTF-8 بدون رمز و بدون فشرده‌سازی با اعتبارسنجی `schemaVersion <= 2`.

```json
{
  "format": "yadin-backup",
  "schemaVersion": 2,
  "exportedAt": "2026-10-09T10:00:00Z",
  "backupType": "FULL",
  "options": {
    "includeVocabulary": true,
    "includeCategories": true,
    "includeDifficulty": true,
    "includeStreakAndProgress": true,
    "includeReviewStats": true,
    "includeProcessHistory": true,
    "includeSettings": true
  },
  "data": { ... }
}
```

قواعد قالب:

۱. روزها به‌صورت متن ISO ‏yyyy-MM-dd و nextReviewDay برای LEARNED برابر null.
۲. فقط بخش‌های مربوط به نوع پشتیبان در data می‌آیند.
۳. UUID تنها شناسه پایدار بین پشتیبان و بازیابی است.
۴. نام فایل پیشنهادی: `yadin-full-backup-<timestamp>.json` یا `yadin-custom-backup-<timestamp>.json`.
۵. پیشوند UTF-8 BOM (`\uFEFF`) برای تمامی خروجی‌های CSV اکسل جهت باز شدن بی‌نقص نویسه‌های فارسی و اسپانیایی در Microsoft Excel.
۶. ذخیره و بازیابی از طریق Storage Access Framework (SAF).
۷. گزارش خطی پیشرفت از 0.0 تا 1.0 و پیام موفقیت.

---

## 6.14 بازیابی (Restore) و نگاشت شناسه‌ها (RestoreContext)

### جریان

```text
کاربر فایل پشتیبان را انتخاب می‌کند (SAF)
      ↓
گام 1: بررسی اولیه فایل
      ↓
گام 2: اعتبارسنجی ساختار، نسخه و نوع
      ↓
گام 3: نمایش اطلاعات فایل به کاربر + هشدار جایگزینی کامل در FULL + انتخاب سیاست ادغام
      ↓
کاربر تایید می‌کند
      ↓
گام 4: اجرای بازیابی (در تراکنش)
      ↓
گام 5: باطل‌سازی کش‌ها و به‌روزرسانی UI
```

### اعتبارسنجی (گام 2)

۱. فرمت معتبر: کلید `format` برابر `"yadin-backup"` یا فایل استاندارد نسخه فلش‌لرن.
۲. نسخه: `schemaVersion <= 2`.
۳. نوع: یکی از `VOCABULARY` ، `PROGRESS` ، `FULL`.
۴. فایل ناقص یا خراب یا نسخه ناشناخته: صدور خطای شفاف اعتبارسنجی.

### موتور خودکار تهیه فایل پشتیبان اضطراری (`Safety Backup`)
پیش از اجرای هرگونه تغییر در دیتابیس طی فرآیند بازیابی، یک نسخه پشتیبان جامع اضطراری به طور خودکار در پوشه `safety_backups/` تولید و ذخیره می‌شود. چرخش خودکار حداکثر ۳ نسخه اخیر را حفظ کرده و فایل‌های قدیمی‌تر را پاک می‌کند.

### ساختار RestoreContext برای اتصال یکپارچه Vocabulary و Progress
برای حل مشکل ناسازگاری UUID در حالت ادغام (MERGE): هنگامی که یک مفهوم پشتیبان با مفهوم محلی متفاوتی بر اساس کلید کانونیکال واژه مبدأ (اسپانیایی) ادغام می‌شود، شناسه نسخه محلی جایگزین می‌شود. برای اینکه رکوردهای پیشرفت (`learning_states`، `difficulty_states`، `review_history`) به مفهوم اشتباهی یا شناسه قدیمی متصل نشوند، شیء `RestoreContext` ایجاد می‌شود:

```kotlin
class RestoreContext {
    private val conceptIdMap = mutableMapOf<String, String>() // backupConceptId -> localConceptId
    fun remapConceptId(backupId: String): String = conceptIdMap[backupId] ?: backupId
    fun registerMapping(backupId: String, localId: String) { conceptIdMap[backupId] = localId }
}
```
تمام بخش‌های بازیابی پیشرفت، شناسه‌ها را صرفاً از طریق `RestoreContext` بازخوانی و نگاشت می‌کنند. رکوردهای پیشرفت بی‌سرپرست که مفهومی برای آن‌ها وجود ندارد رد می‌شوند.

### گارد محافظتی از پیشرفت یادگیری در حالت جایگزینی (`isReplace`)
در فرآیند بازیابی، پاک‌سازی جداول پیشرفت (`learning_states`، `difficulty_states`، `review_history`، `review_sessions`) صرفاً و منحصراً مشروط به حضور داده‌های پیشرفت (`hasProgressInData`) در فایل بکاپ است. بازیابی فایل‌های صرفاً واژگان (`VOCABULARY`) هرگز پیشرفت لایتنر کاربر را دستخوش تغییر یا پاک‌سازی نمی‌کند (ISS-17).

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
حذف مفاهیم موجود و جایگزینی با مفاهیم فایل پشتیبان
حذف categories و tags و جایگزینی با پشتیبان
درج همه داده پشتیبان
```

یادآوری مهم (گارد محافظت از پیشرفت - D40 / ISS-17): در صورتی که فایل انتخابی صرفاً حاوی واژگان (`VOCABULARY`) باشد، حتی در حالت REPLACE جداول پیشرفت لایتنر (`learning_states`، `difficulty_states`، `review_history`، `review_sessions`) پاکسازی نمی‌شوند و پیشرفت کاربر کاملاً حفظ می‌گردد. پاکسازی جداول پیشرفت منحصراً و قطعی مشروط به حضور داده‌های پیشرفت (`hasProgressInData`) در فایل است.

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
از نسخه ۱.۷.۰ نظام دستاوردها به یک سیستم زنجیره‌ای و مرحله‌ای ۲۰ مدالی در ۵ شاخه اصلی و ۴ سطح (برنز، نقره، طلا، پلاتین) ارتقا یافته است:

| شاخه (Category) | سطح ۱ (برنز 🥉) | سطح ۲ (نقره 🥈) | سطح ۳ (طلا 🥇) | سطح ۴ (پلاتین 💎) | شرط ارزیابی |
|:---|:---:|:---:|:---:|:---:|:---|
| **دایره واژگان (VOCABULARY)** | ۱۰ واژه | ۵۰ واژه | ۱۰۰ واژه | ۵۰۰ واژه | تعداد مفاهیم فعال متمایز تمرین‌شده |
| **رگبار و مداومت (STREAK)** | ۳ روز | ۷ روز | ۱۴ روز | ۳۰ روز | روزهای مداومت متوالی در مطالعه |
| **تثبیت لایتنر (RETENTION)** | ۲۰ واژه | ۵۰ واژه | ۱۰۰ واژه | ۳۰۰ واژه | تعداد کارت‌های تثبیت‌شده در مرحله `LEARNED` |
| **واژگان سخت (HARD_WORDS)** | ۵ واژه | ۱۵ واژه | ۳۰ واژه | ۶۰ واژه | کارت‌های سخت و خیلی سخت تسلط‌یافته |
| **استاد آزمون (QUIZ)** | ۵ سؤال | ۱۰ سؤال | ۲۰ سؤال | ۵۰ سؤال | آزمون‌های ۴گزینه‌ای با دقت ۱۰۰٪ |

قواعد:

۱. هر دستاورد فقط یک‌بار باز می‌شود. باز شدن هرگز لغو نمی‌شود.
۲. **جایگزینی مرحله‌ای (Progressive Replacement):** جهت حفظ زیبایی و خلوت بودن رابط کاربری، در هر شاخه فقط کارت چالش جاری نشان داده می‌شود و با دستیابی به آن، خودکار به سطح بعدی تبدیل می‌گردد.
۳. بررسی پس از هر جلسه مرور (و بعد از بازیابی) انجام می‌شود.
۴. دستاورد تازه باز شده در صفحه نتایج جلسه و هدر آمار نمایش داده می‌شود.
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
هدر: «N از M» + نوار کپسولی شمارنده و وضعیت سؤالات (پاسخ‌داده‌شده / درست / غلط)
سؤال: متن سمت سؤال + دکمه صدا
چهار گزینه (کارت‌های قابل لمس)
دکمه‌های ناوبری: [قبلی] جهت بازبینی سوالات پیشین و [بعدی] جهت عبور
```

۱. برای هر کارت، سؤال بخش 6.6 ساخته می‌شود.
۲. **فال‌بک اکید به فلش‌کارت (Strict Fallback - تصمیم D44 / ISS-18):** در صورت کمبود گزینه‌های انحرافی معتبر (کمتر از ۳ گزینه متمایز غیرمتضاد) یا عدم تشکیل ۴ گزینه متمایز، سوال از آزمون حذف شده و در صورت خالی شدن آزمون، نشست به طور خودکار به حالت فلش‌کارت هدایت می‌شود.
۳. **ارزیابی دقیق نمره (تصمیم D37 / ISS-16):** سنجش صحت پاسخ صرفاً بر اساس برابری گزینه انتخابی با ایندکس گزینه صحیح (`optionIndex == correctIndex`) بدون برخورد معنایی کاذب انجام می‌شود و اکسان‌های اسپانیایی (`á`, `é`, `í`, `ó`, `ú`, `ñ`) کاملاً حفظ می‌گردند.
۴. **ناوبری و بازبینی آزادانه سؤالات:** کاربر امکان بازگشت به سؤالات پیشین با دکمه اختصاصی «قبلی» و سوئیچ سریع با نوار عددی بالای صفحه همراه با مشاهده رنگ وضعیت پاسخ‌ها (سبز برای درست، قرمز برای غلط) را داراست.
۵. تپ روی گزینه: گزینه‌ها قفل می‌شوند. گزینه صحیح سبز؛ اگر انتخاب غلط بود، آن قرمز.
۶. بازخورد متنی «درست!» یا «غلط!» و انتقال خودکار پس از `quizAutoAdvanceSeconds` (قابل تنظیم از ۱ تا ۱۰ ثانیه در تنظیمات) یا با لمس دکمه «بعدی».
۷. ثبت پاسخ (6.4) درون تراکنش اتمیک دیتابیس با شناسه یکتای تلاش (`reviewAttemptId`) ثبت می‌شود.

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
فیلترها:
  - دسته موضوعی (انتخاب دسته‌بندی خاص یا همه دسته‌ها)
  - سختی کلمه (همه سختی‌ها ، آسان ، متوسط ، سخت ، خیلی سخت) متناسب با جهت مطالعه فعال
  - مرحله لایتنر (همه مراحل ، روزانه ، هفتگی ، ماهانه ، تثبیت‌شده)
  - وضعیت: سوئیچ نمایش واژگان فعال / واژگان غیرفعال (حذف‌شده)
فهرست صفحه‌بندی‌شده نامحدود (Infinite Scroll / Paging):
  - بارگذاری تدریجی قطعه‌ای بدون محدودیت سخت ۳۰۰تایی
  - هر آیتم: متن مبدأ ، تلفظ صوتی ، ترجمه‌ها ، دسته‌ها ، نشان مرحله و سختی ، دکمه ویرایش و حذف
دکمه شناور «+» برای افزودن واژه
```

۱. ترتیب: بر اساس متن مبدأ (canonicalKey) یا آخرین به‌روزرسانی.
۲. ضربه روی آیتم: جزئیات واژه.
۳. حذف: پنجره تایید، سپس حذف نرم (6.8).
۴. **مدیریت واژگان غیرفعال (D42 / ISS-02):** نمایش آیتم‌های غیرفعال با نشان برجسته و دکمه تک‌لمسی «فعال‌سازی مجدد» (`reactivateWord`) جهت بازگردانی مستقیم واژه به صف فعال.
۵. دکمه صدا روی هر آیتم سمت مبدأ.
۶. فهرست خالی: پیام راهنما و دکمه «افزودن واژه».

## 7.10 جزئیات واژه

نمایش: متن مبدأ + صدا ، همه ترجمه‌ها ، یادداشت ، نوع ، دسته‌ها ، برچسب‌ها.
برای هر جهت (دو کارت): مرحله ، تاریخ مرور بعدی (تاریخ محلی)، سختی ، تعداد درست و غلط (از تاریخچه)، آخرین مرور.
دکمه‌ها: ویرایش ، حذف / فعال‌سازی.

## 7.11 افزودن واژه

دو زبانه:

تکی: فرم بخش 6.7 با اعتبارسنجی همزمان؛ دکمه «اضافه کن» تا زمانی که فرم معتبر نیست غیرفعال است. «+ ترجمهٔ دیگری» تا 10 ترجمه. انتخاب دسته چندانتخابی با امکان ساخت دسته جدید.
گروهی:
  - چسباندن متن دستی با تشخیص خودکار فرمت.
  - **انتخابگر سیستمی فایل (File Picker - تصمیم D43 / ISS-03):** دکمه اختصاصی «انتخاب فایل» با Android Storage Access Framework (`ActivityResultContracts.GetContent`) با پشتیبانی مستقیم از فایل‌های `.xlsx`, `.csv`, `.tsv`, `.txt`, `.json` و پیش‌نمایش بلادرنگ ورودی‌ها.

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

### 8.1 تم‌های برنامه (GTP و Gemini)

برنامه دارای دو تم متمایز و مدرن است که هر کدام دارای دو حالت روشن (Light) و تاریک (Dark) هستند:

۱. **GTP (`gtp`)**: تم بر پایه طیف بنفش و ارغوانی نئونی، مدرن، متراکم و شارپ.
۲. **Gemini (`gemini`)**: تم آینده‌نگرانه و بسیار خلاقانه با الهام از طراحی هوش مصنوعی، بر پایه رنگ‌های سرمه‌ای اعماق فضا (Cosmic Deep Space)، فیروزه‌ای ستاره‌ای (Electric Cyan)، گرادیان‌های نئونی، شیشه‌ای و کارت‌های شناور مدرن.

### ۱. تم GTP (gtp)

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

### ۲. تم Gemini (gemini)

| توکن | روشن | تاریک |
|------|------|-------|
| primary | 1A73E8 | 8AB4F8 |
| secondary | 00B4D8 | 70D6FF |
| background | F8FAFD | 0B0F19 |
| surface | FFFFFF | 111827 |
| surfaceVariant | EDF2F9 | 1F2937 |
| onSurface | 1E293B | F1F5F9 |
| onSurfaceVariant | 64748B | 94A3B8 |
| card | F1F5F9 | 161F30 |
| outline | CBD5E1 | 334155 |

ویژگی‌های Gemini:
- استایل گوشه‌ها: کاملاً گرد و نرم (Pill & Rounded Shapes: گوشه‌ها 12/20/28).
- آیکن‌ها: آیکن‌های به‌روز با جلوه مدرن هوش مصنوعی (طراحی منسجم و لطیف).
- کارت‌ها: عمق بصری لطیف با حاشیه ظریف شیشه‌ای (`cardBorderAlpha 0.35`).
- افکت‌های بصری: گرادیان‌های نوری و فضایی بین `primary` و `secondary` در سربرگ‌ها و نشان‌ها.

## 8.2 رنگ‌های معنایی مشترک

| معنی | روشن | تاریک |
|------|------|-------|
| success | 138A5B | 52D49A |
| warning | F59E0B | FBBF24 |
| error | D92D48 | FF8A9A |
| info | 536DFE | 7C8CFF |
| onPrimary | FFFFFF | FFFFFF |

## 8.3 توکن‌های اندازه تم‌ها

```text
gtp:    همه مقادیر پایه × 0.88 ، سپس screenPadding 16 ، contentGap 10 ، sectionGap 14
        statCardHeight 118 ، navHeight 70 ، cardElevationBase 5 ، cardBorderAlpha 0.70
        cardBorderStrongAlpha 0.95 ، accentSurfaceAlpha 0.18 ، hierarchyBoost 1.12

gemini: screenPadding 18 ، contentGap 12 ، sectionGap 16 ، statCardHeight 124
        navHeight 76 ، cardElevationBase 3 ، cardBorderAlpha 0.35 ، cardBorderStrongAlpha 0.60
        accentSurfaceAlpha 0.12 ، hierarchyBoost 1.05 ، cornerSmall 12 ، cornerMedium 20 ، cornerLarge 28
```

## 8.4 قواعد

۱. فونت فارسی: وزیر (Vazirmatn) که در assets/fonts گذاشته می‌شود (مجوز آزاد). برای انگلیسی فونت پیش‌فرض سیستم.
۲. اندازه فونت‌ها: headlineLarge 28sp ، headlineMedium 24sp ، bodyLarge 16sp ، bodyMedium 14sp ، bodySmall 12sp ، labelMedium 12sp؛ همه ضربدر typographyScale تم.
۳. ارتفاع دکمه 48 تا 52dp؛ ارتفاع فیلد ورودی 56dp.
۴. حداقل ناحیه لمس همه کنترل‌ها 48dp.
۵. حالت کنتراست: متن روی primary با onPrimary؛ نسبت کنتراست متن اصلی حداقل 4.5 به 1.
۶. ممنوع: Color(0x...) مستقیم یا dp مستقیم در کد صفحه‌ها (باید از توکن تم).
۷. تغییر تم و حالت روشن / تاریک بدون راه‌اندازی مجدد و به صورت بلادرنگ اعمال می‌شود.
۸. رنگ‌های مرتبط با مرحله و سختی (نشان‌ها): سختی EASY با success ، MEDIUM با info ، HARD با warning ، VERY_HARD با error ؛ مرحله DAILY با primary ، WEEKLY با info ، MONTHLY با warning ، LEARNED با success.
۹. برنامه دارای ۴ پوسته رسمی و متمایز (`GTP Cyber`, `Gemini Future AI`, `Claude`, `Googoli`) با پشتیبانی کامل از هر دو حالت دارک و لایت است (تصمیم D36 و مستندات `THEME_CLAUDE.md` و `THEME_GOOGOLI.md`).

---

# 9. تنظیمات

ذخیره در جدول settings (کلید و مقدار متنی). خواندن با Flow تا صفحه‌ها واکنشی باشند.

| کلید | نوع | پیش‌فرض | بازه |
|------|-----|---------|------|
| themeId | متن | gtp | gtp, gemini, claude, googoli |
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
اگر غلط جواب بدهید، واژه (از هر مرحله‌ای، حتی یادگرفته‌ها) به «روزانه» برمی‌گردد و فردا دوباره می‌آید.
واژه یادگرفته از مرور عادی بیرون می‌رود، اما از فردا در بخش «یادگرفته‌ها» به صورت اختیاری قابل تمرین است و در صورت پاسخ غلط، دوباره چرخه روزانه را آغاز می‌کند.

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
| T-QZ-17 | فال‌بک اکید به فلش‌کارت در کمبود گزینه‌های انحرافی و ممانعت از پر کردن تصادفی گزینه‌ها با داده مخدوش (ISS-18) |

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
| T-P-16 | تفکیک هوشمند اسلش در عبارات مرکب (مانند «کسی/چیزی») بدون خرد شدن عبارت (ISS-19) |
| T-P-17 | ثبت خطوط تفکیک (Breakdown) درون مدخل جاری بدون تخلیه یا اتمام زودهنگام ورودی (ISS-19) |

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
| T-I-16 | تطابق کانونیکال ورود داده منحصراً با مفاهیم فعال (active=1) و ممانعت از احیای واژگان حذف‌شده (ISS-20) |

## 11.8 بانک اولیه

| شناسه | مورد |
|-------|------|
| T-S-01 | دقیقاً ۶,۶۶۴ مفهوم فعال و ۱۵,۵۶۱ ترجمه وارد شود (فایل رسمی جدید) |
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
| T-B-08 | REPLACE واژگان: جایگزینی مفاهیم با حفظ قطعی پیشرفت لایتنر کاربر (گارد hasProgressInData - تصمیم D40 / ISS-17) |
| T-B-09 | MERGE پیشرفت: قانون جدیدتر برنده و بازنگاشت شناسه‌ها با RestoreContext (ISS-17) |
| T-B-10 | REPLACE پیشرفت |
| T-B-11 | ردیف پیشرفت بدون مفهوم رد و گزارش شود |
| T-B-12 | شکست وسط ← ROLLBACK |
| T-B-13 | پشتیبان ایمنی اضطراری خودکار (Safety Backup) پیش از اعمال تغییرات با چرخش ۳ نسخه (ISS-17) |
| T-B-14 | بازیابی بدون انتخاب روش انجام نشود (UI) |
| T-B-15 | اجرای I/O و نوشتن استریم‌های پشتیبان و بازیابی بر روی Dispatchers.IO بدون مسدودسازی UI (ISS-31) |

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
| T-UI-06 | کتابخانه: جستجو و فیلتر روی تمامی ۶,۶۶۴ مورد با اسکرول تدریجی نامحدود (Paging) روان |
| T-UI-07 | راه‌اندازی اول با بانک اولیه |
| T-UI-08 | فیلتر ۴ سطح سختی و فیلتر واژگان غیرفعال همراه با دکمه اختصاصی فعال‌سازی مجدد در کتابخانه (ISS-02) |
| T-UI-09 | انتخابگر سیستمی فایل (SAF) در ورود گروهی واژگان برای XLSX, CSV, JSON, TXT (ISS-03) |
| T-UI-10 | تنظیمات سرعت TTS، تاخیر عبور خودکار آزمون، و پاکسازی قطعی غیرفعال‌ها در SettingsScreen (ISS-06) |

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
| 11 | 1.11.0 | ایمن‌سازی بازیابی (Restore)، گارد محافظت از پیشرفت لایتنر، بکاپ اضطراری Safety Backup، نگاشت کامل شناسه‌ها با RestoreContext (ISS-17) | تست‌های واحد سبز و صحت نگاشت شناسه |
| 12 | 1.12.0 | حل کامل تمامی اولویت‌های بالای باقیمانده: صفحه‌بندی نامحدود کتابخانه (ISS-01)، فیلترهای سختی و وضعیت و فعال‌سازی مجدد (ISS-02)، انتخابگر سیستمی فایل SAF (ISS-03)، تنظیمات پیشرفته و سرعت TTS (ISS-06)، فال‌بک اکید آزمون (ISS-18)، پارسر هوشمند اسلش و تفکیک (ISS-19)، ممانعت از احیای واژگان حذف‌شده در ورود (ISS-20)، تثبیت دیتابیس تکین (ISS-29)، غیرمسدودسازی I/O در کورتین (ISS-31) | بیلد سبز، تست‌های واحد سبز، رفع کامل گلوگاه‌ها |

شماره نسخه برنامه: versionName برابر شماره جدول و versionCode برابر عدد صحیح متناظر (کد ۲۰ برای نسخه ۱.۱۲.۰).

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
| A7 | در بازیابی REPLACE واژگان، پیشرفت لایتنر کاربر کاملاً حفظ می‌شود و پاک‌سازی جداول پیشرفت منحصراً مشروط به حضور داده‌های پیشرفت (hasProgressInData) در فایل است (تصمیم D40 و ISS-17). |
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

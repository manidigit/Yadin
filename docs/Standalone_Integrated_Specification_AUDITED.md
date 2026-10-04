# FlashLearn --- مشخصات یکپارچه و مستقل محصول، معماری، داده و الگوریتم‌ها

> **هدف این سند:** این فایل به‌تنهایی باید برای فهم محصول، رفتار مورد
> انتظار، معماری، مدل داده، قراردادهای پایداری، تست و الگوریتم‌های اصلی
> کافی باشد. خواننده نباید برای فهم قراردادهای اجرایی به اسناد قبلی یا
> تاریخچهٔ مراحل نیاز داشته باشد.
>
> **قاعدهٔ خواندن:** بخش‌های ابتدایی «چه چیزی باید وجود داشته باشد و چگونه
> رفتار کند» را توضیح می‌دهند. جزئیات الگوریتمی و شبه‌کد در انتهای سند
> آمده‌اند تا متن اصلی برای خواننده قابل فهم بماند.

------------------------------------------------------------------------

## 1. FlashLearn چیست؟

FlashLearn یک برنامهٔ اندرویدی آموزش واژگان و جمله است که برای استفادهٔ
اصلی به‌صورت **کاملاً آفلاین** طراحی شده است.

هستهٔ محصول یک چرخهٔ مرور فاصله‌دار چهارمرحله‌ای دارد:

**Daily → Weekly → Monthly → Learned**

کاربر می‌تواند واژه یا محتوای زبانی را به‌صورت دستی اضافه کند، متن را
Paste کند، داده را Import/Export کند، کتابخانه را جست‌وجو و فیلتر کند،
کارت‌های مرور و سؤال چهارگزینه‌ای ببیند، پاسخ بدهد و نتیجهٔ یادگیری خود را
در Progress/Statistics و Achievement/Streak مشاهده کند.

### محدودهٔ پایه

-   Android با حداقل API 26.
-   پشتیبانی رابط کاربری برای فارسی و انگلیسی و معماری آماده برای
    زبان‌های بیشتر.
-   در حالت پایه یک Language Pair فعال است، اما مدل داده و مرزهای معماری
    نباید افزودن چند Language Pair را در آینده غیرممکن کنند.
-   عملیات اصلی Library، Review، Learning، Statistics و Settings بدون
    شبکه قابل اجرا هستند.
-   منطق کسب‌وکار نباید برای عملکرد عادی به API یا سرویس آنلاین وابسته
    باشد.
-   مجوز شبکه نباید صرفاً برای «آمادگی آینده» به برنامه تحمیل شود.
-   الگوریتم‌های آموزشی داخلی هستند و وابستگی اجباری به AI یا سرویس خارجی
    ندارند.

### خارج از محدودهٔ پایه

-   زمان‌بندی تطبیقی پیشرفته خارج از چرخهٔ تعریف‌شدهٔ فعلی.
-   چند Language Pair هم‌زمان به‌عنوان رفتار اجباری نسخهٔ پایه.
-   Notificationهای پیشرفته به‌عنوان الزام هستهٔ یادگیری.

معماری باید برای توسعهٔ آینده آماده باشد، اما قابلیت آینده نباید قرارداد
فعلی را بی‌دلیل تغییر دهد.

------------------------------------------------------------------------

## 2. مفاهیم پایهٔ یادگیری

### 2.1 Concept

`Concept` هویت یک مفهوم آموزشی است؛ نه صرفاً یک رشتهٔ متنی.

یک Concept می‌تواند چند Content در زبان‌های مختلف داشته باشد. بنابراین
ترجمه‌های متعدد یک مفهوم نباید الزاماً Conceptهای مستقل بسازند.

### 2.2 Content

`Content` متن زبانی متعلق به یک Concept و یک زبان مشخص است.

فیلدهای پایه:

-   `conceptId`
-   `languageCode`
-   `text`
-   `canonicalKey`
-   `note`
-   `pronunciation`
-   `example`

قانون یکتایی پایه:

`conceptId + languageCode`

### 2.3 چرخهٔ چهارمرحله‌ای

#### Daily

مرحلهٔ شروع یادگیری و مرور نزدیک.

-   پاسخ صحیح → Weekly
-   پاسخ غلط → Daily و مرور در ابتدای روز تقویمی بعد

#### Weekly

مرور فاصله‌دار هفتگی.

-   پاسخ صحیح → Monthly با فاصلهٔ ۳۰ روز
-   پاسخ غلط → Daily، مرور در ابتدای روز بعد، و `hasPathFailure = true`
-   شکست Weekly باید سختی Concept را حداقل به Medium برساند.

#### Monthly

مرور فاصله‌دار ماهانه.

-   پاسخ صحیح → Learned
-   پاسخ غلط → Daily، مرور در ابتدای روز بعد، `hasPathFailure = true`
-   شمارندهٔ `monthlyWrongCount` یک واحد افزایش می‌یابد.
-   اولین شکست Monthly سختی را به Hard می‌رساند.
-   شکست‌های بعدی Monthly سختی را به Very Hard می‌رسانند.

#### Learned

وضعیت یادگیری تثبیت‌شده.

-   مرور Learned مسیر جداگانه دارد.
-   انتخاب Learned به `nextReviewAt` وابسته نیست.
-   پاسخ در Learned به‌طور خودکار Concept را به Daily/Weekly/Monthly
    برنمی‌گرداند.
-   قواعد ارائهٔ Learned باید از چرخهٔ معمول Due جدا بمانند.

------------------------------------------------------------------------

## 3. تفاوت Stage، Concept Difficulty و Test Difficulty

این سه مفهوم نباید با یکدیگر ادغام شوند.

### Learning Stage

مشخص می‌کند Concept در چرخهٔ یادگیری در کدام مرحله است:

`DAILY | WEEKLY | MONTHLY | LEARNED`

### Concept Difficulty

سختی خود Concept است:

`EASY | MEDIUM | HARD | VERY_HARD`

این مقدار مستقل از Stage است.

### Test Difficulty

سختی خود آزمون چهارگزینه‌ای است:

`BEGINNER | INTERMEDIATE | PROFESSIONAL`

Test Difficulty فقط روی انتخاب و دشواری Distractorها اثر می‌گذارد و نباید
Concept Difficulty را تغییر دهد.

بنابراین این حالت کاملاً معتبر است:

`Concept Difficulty = VERY_HARD`

و هم‌زمان:

`Test Difficulty = BEGINNER`

------------------------------------------------------------------------

## 4. قوانین تغییرناپذیر سیستم

این قوانین Contract هستند:

1.  هر Concept دقیقاً یک Learning State دارد.
2.  هر Concept دقیقاً یک Concept Difficulty State دارد.
3.  Learning State و Concept Difficulty مستقل هستند.
4.  Test Difficulty مستقل از Concept Difficulty است.
5.  هر پاسخ پذیرفته‌شده دقیقاً یک ReviewHistory ایجاد می‌کند.
6.  Learning State + Difficulty State + ReviewHistory باید در یک مرز
    Transaction اتمیک ثبت شوند.
7.  Stage و Difficulty یک مفهوم واحد نیستند.
8.  برای Daily/Weekly/Monthly فقط کارت‌هایی با `nextReviewAt <= now` واجد
    شرایط مرور هستند.
9.  Learned مسیر انتخاب جداگانه دارد و Due Check معمول روی آن اعمال
    نمی‌شود.
10. UI و ViewModel نباید Transition یا Difficulty Algorithm را خودشان
    پیاده کنند.
11. تغییر Content نباید Learning State یا ReviewHistory را بی‌دلیل تغییر
    دهد.
12. حذف Concept در حالت عادی Soft Delete است و History حفظ می‌شود.
13. نبودن Learning State یا Difficulty State برای Concept فعال خطای
    `DATA_INTEGRITY_ERROR` است؛ سیستم نباید State جعلی بسازد.
14. Update برنامه نباید باعث حذف دادهٔ کاربر شود.
15. رشد Concept و ReviewHistory نباید باعث بارگذاری کل Dataset در RAM
    شود.
16. Algorithm، Data و UI/Theme باید مرز مستقل داشته باشند.
17. Theme نباید Learning Data یا Scheduling را تغییر دهد.
18. Help/Education نباید به یک Learning Engine دوم تبدیل شود.
19. TTS نباید Scoring، Difficulty، History یا Scheduling را تغییر دهد.
20. Retry و اجرای تکراری عملیات حساس نباید رکورد تکراری تولید کند.
21. ReviewHistory Append-only است؛ حذف یا Update خودسرانهٔ آن مجاز نیست.
22. `monthlyWrongCount` تجمعی است و در مسیر موفق معمول Reset نمی‌شود.
23. `hasPathFailure` پس از ثبت شکست مسیرهای Weekly/Monthly نباید با یک
    Review معمولی به False برگردد.
24. `hasReachedVeryHard` یک Flag تاریخی است و پس از True شدن با کاهش
    Difficulty False نمی‌شود.
25. Selection صرفاً انتخاب است؛ شروع Session یا نمایش کارت نباید به‌تنهایی
    State را تغییر دهد.

------------------------------------------------------------------------

## 5. قابلیت‌های کاربر

### Library

-   افزودن
-   ویرایش
-   Soft Delete
-   Favorite
-   Category
-   Tag
-   Search
-   Filter
-   Import
-   Export
-   Refresh
-   Paging/Lazy Loading

Library نباید برای ساختن یک صفحهٔ فیلترشده کل Concept، History،
LearningState یا DifficultyState را وارد حافظه کند.

### Review

-   Flashcard
-   Multiple Choice
-   Reviewهای Daily/Weekly/Monthly
-   مسیر مستقل Learned
-   ثبت پاسخ از یک مسیر مشترک
-   Feedback پس از پاسخ
-   حرکت خودکار به کارت بعدی در حالت‌های تعریف‌شده

### Statistics و Progress

-   آمار پایه
-   Progress مرحله‌ای
-   Daily Goal
-   Streak
-   Achievement

این بخش‌ها Read-only هستند و نباید با نمایش آمار، Review ثبت کنند یا
Eligibility را تغییر دهند.

### Settings

تنظیمات باید از UI State موقتی تفکیک شوند. مقادیر محصولی مانند Threshold
و Maximum Review Cards باید از یک قرارداد پایدار و قابل Migration خوانده
شوند.

------------------------------------------------------------------------

## 6. معماری

جریان اصلی:

``` text
UI
 ↓
ViewModel / Presentation
 ↓
Use Case
 ↓
Repository
 ↓
Database
```

### لایه‌ها

  ---------------------------------------------------------------------
  لایه                               مسئولیت
  ---------------------------------- ----------------------------------
  Database                           Entity، Constraint، Index،
                                     Transaction، Referential Integrity

  Data / Repository                  قرارداد دسترسی و نگاشت Data ↔
                                     Domain

  Domain                             مدل‌ها، Use Caseها و Algorithmهای
                                     Pure

  Presentation / ViewModel           UI State و هماهنگی با Use Case

  UI / Compose                       نمایش و دریافت تعامل

  Theme                              ThemeSpec، Tokenها و ظاهر

  Navigation                         مقصدها و مسیرها

  Education / Help                   محتوای آموزشی و راهنما
  ---------------------------------------------------------------------

### مرزهای اجباری

-   UI مستقیماً به Database متصل نشود.
-   Algorithm مستقیماً به Room یا Compose وابسته نباشد.
-   Repository منطق UI نداشته باشد.
-   Theme از Learning/Data مستقل باشد.
-   Parser از Learning Engine مستقل باشد.
-   Help از Business Logic مستقل باشد.
-   TTS از Learning Persistence مستقل باشد.
-   تعویض یک Module نباید نیازمند بازنویسی کل برنامه باشد.

------------------------------------------------------------------------

## 7. مدل داده

### Concept

-   `id`
-   `inputType / entryType`
-   `category`
-   `favorite`
-   `active`
-   `createdAt`
-   `updatedAt`
-   در صورت استفاده از Data Refresh: `dataVersion`
-   برای انتقال بین Backup/Restore: شناسهٔ پایدار UUID

### Content

-   `id`
-   `conceptId`
-   `languageCode`
-   `text`
-   `canonicalKey`
-   `note`
-   `pronunciation`
-   `example`
-   در صورت استفاده از Refresh: `dataVersion`

### LearningState

-   `id`
-   `conceptId`
-   `stage`
-   `nextReviewAt`
-   `monthlyWrongCount`
-   `hasPathFailure`
-   `totalCorrect`
-   `totalWrong`
-   `lastReviewAt`

### DifficultyState

-   `id`
-   `conceptId`
-   `current`
-   `consecutiveCorrect`
-   `consecutiveWrong`
-   `hasReachedVeryHard`

### ReviewHistory

-   شناسهٔ رکورد
-   `conceptId`
-   `sessionId`
-   `reviewAttemptId`
-   زمان پاسخ
-   نتیجهٔ پاسخ
-   اطلاعات لازم برای ثبت رخداد مرور

ReviewHistory فقط Append-only است.

Unique Constraint مورد نیاز:

`(sessionId, reviewAttemptId)`

### ReviewSession

حداقل:

-   `id`
-   `startedAt`
-   `endedAt`
-   `reviewType`

### Tag و ConceptTag

رابطهٔ چندبه‌چند بین Concept و Tag.

### Settings

مدل Key/Value با Default مشخص برای تنظیمات محصول.

### Achievement

برای وضعیت Unlock و جلوگیری از نمایش تکراری Achievementهای قبلاً Unlock
شده.

------------------------------------------------------------------------

## 8. Canonical Key و هویت داده

Canonicalization پایه:

1.  Trim
2.  تبدیل به lowercase
3.  Collapse کردن فاصله‌های پشت‌سرهم
4.  حفظ Accent
5.  حفظ Punctuation معنی‌دار

بنابراین:

`si` و `sí`

دو مقدار متفاوت هستند.

CanonicalKey برای Duplicate Detection و تطبیق دادهٔ واردشده با دادهٔ موجود
استفاده می‌شود، اما نباید بهانه‌ای برای تغییر خودکار متن اصلی کاربر باشد.

------------------------------------------------------------------------

## 9. یکپارچگی داده و Transaction

### ایجاد Concept

ایجاد Concept یک عملیات اتمیک است و باید این مجموعه را با هم ایجاد کند:

1.  Concept
2.  Contentها
3.  LearningState اولیه = Daily
4.  DifficultyState اولیه = Easy

اگر هر بخش شکست بخورد، کل عملیات Rollback می‌شود.

### ثبت پاسخ

Flashcard و Multiple Choice باید از یک مسیر مشترک ثبت پاسخ استفاده کنند:

1.  وجود LearningState و DifficultyState بررسی شود.
2.  Duplicate Detection انجام شود.
3.  Due Validation انجام شود.
4.  Learning Transition محاسبه شود.
5.  Difficulty محاسبه شود.
6.  LearningState + DifficultyState + ReviewHistory در یک Transaction
    ذخیره شوند.

ترتیب Duplicate Detection نسبت به Due Validation مهم است: یک Attempt
تکراری باید به‌عنوان Duplicate شناخته شود، حتی اگر در اجرای قبلی
`nextReviewAt` جلو رفته باشد.

### Referential Integrity

-   Foreign Key فعال باشد.
-   Orphan Record ایجاد نشود.
-   Unique Constraintها از Duplicate جلوگیری کنند.
-   Import/Restore نتواند Constraintها را دور بزند.
-   دادهٔ نامعتبر قبل از Commit رد شود.

------------------------------------------------------------------------

## 10. Library، Parser و Import

### اصل Parser

Parser وظیفه دارد:

**Detect → Separate → Classify → Relate → Preserve**

و نباید متن را «زیباتر» یا از خودش «اصلاح» کند.

### ورودی‌های مورد انتظار

Parser باید بتواند با متن‌های فارسی، انگلیسی و اسپانیایی رفتار قابل
پیش‌بینی داشته باشد.

### طبقه‌بندی خط

خط می‌تواند در دسته‌هایی مانند:

-   Entry
-   Translation
-   Breakdown
-   Note
-   Grammar Note
-   Comment
-   Blank/Separator
-   Unknown

قرار گیرد.

### قوانین مهم

-   Numbering فقط Signal است.
-   Gap در Numbering لزوماً خطا نیست.
-   Breakdown به‌صورت پیش‌فرض Entry مستقل نیست.
-   Note و Grammar Note Entry مستقل نیستند.
-   Accent اسپانیایی حفظ می‌شود.
-   Parentheses بدون تحلیل حذف نمی‌شوند.
-   متن اصلی خودکار تصحیح نمی‌شود.
-   Duplicate بدون Policy حذف نمی‌شود.
-   اطلاعات استخراج‌شده باید به متن اصلی قابل ردیابی باشند.
-   Parser نباید اطلاعاتی را که در متن وجود ندارد به‌عنوان حقیقت تولید
    کند.

### وضعیت‌های Import

حداقل وضعیت‌های مورد نیاز:

`READY | INCOMPLETE | NEEDS_REVIEW | IMPORTED | DUPLICATE | FAILED`

و برای عملیات Batch نیز وضعیت عملیاتی:

`NOT_STARTED | RUNNING | SUCCESS | FAILED | CANCELLED`

### Confidence

برای موارد مبهم Confidence می‌تواند از 0 تا 100 ثبت شود.

یک قرارداد مستندشدهٔ Parser شامل این دسته‌هاست:

-   `>= 80` → Auto
-   `50..79` → Review / Warning
-   `< 50` → Needs Review

Confidence نباید جایگزین Validation شود.

### Warning / Evidence

برای موارد مبهم می‌توان ثبت کرد:

-   `warningType`
-   `lineNumber`
-   `rawText`
-   `message`
-   `confidence`
-   evidenceهایی مانند numbered، spanishDetected،
    adjacentPersianTranslation و translationMarker

### Orphan

-   Source بدون Translation → `ORPHAN_SOURCE`
-   Translation بدون Source → `ORPHAN_TRANSLATION`

نباید این موارد بی‌اجازه حذف شوند.

### Bulk Import

-   فایل بزرگ نباید کامل در RAM قرار گیرد.
-   Parsing باید Streaming/Batch محور باشد.
-   Duplicate Detection تا حد امکان DB/Index/Query محور باشد.
-   خطای یک رکورد باید طبق Contract عملیات مدیریت شود.
-   Cancel نباید Commit تصادفی دادهٔ نیمه‌کاره ایجاد کند.
-   Resume فقط با Checkpoint/Contract معتبر مجاز است.
-   Retry بدون Checkpoint معتبر باید از ابتدا و به‌صورت امن انجام شود.
-   Persistence پارس‌شده و Metadata وابسته باید در مرز اتمیک تعریف‌شده ثبت
    شوند.

------------------------------------------------------------------------

## 11. Duplicate و Merge

برای هر Piece ورودی، ابتدا Contentهای موجود با همان Language و
CanonicalKey جست‌وجو می‌شوند.

نتیجه می‌تواند:

-   `CreateNewConcept`
-   `ReuseConcept`
-   `Conflict`

باشد.

اگر چند Concept مختلف با Pieceهای یک ورودی Match شوند، سیستم نباید
خودکار آن Conceptها را Merge کند.

در Conflict:

-   Concept یا Content جدید ایجاد نشود.
-   دادهٔ موجود تغییر نکند.
-   شناسه‌های Conceptهای Match شده به کاربر ارائه شوند.
-   کاربر یکی از Conceptها را انتخاب یا Import را لغو کند.

اگر فقط یک Concept Match شود، فقط Contentهای واقعاً جدید زیر همان Concept
اضافه شوند.

اگر هیچ Piece جدیدی وجود نداشته باشد، عملیات باید Idempotent باشد و
رکورد تکراری نسازد.

------------------------------------------------------------------------

## 12. Multiple Choice و Quiz

### قواعد پایه

هر QuizQuestion باید:

-   دقیقاً ۴ گزینه داشته باشد.
-   دقیقاً یک پاسخ صحیح داشته باشد.
-   سه گزینهٔ دیگر Distractor باشند.
-   گزینه‌ها از نظر متن Normalize شده Duplicate نباشند.
-   Distractor متعلق به Concept دیگری باشد.
-   Answer Identity پایدار باشد.

### ترجمه‌های متعدد

اگر یک Concept چند Translation معتبر داشته باشد، Translationهای غیرخالی
آن باید در نمایش سؤال به‌صورت گروه‌بندی‌شده و با ترتیب Deterministic نمایش
داده شوند؛ نه اینکه یک Concept صرفاً به‌خاطر چند ترجمه چند بار در بانک
سؤال تکرار شود.

### انتخاب Distractor

اولویت کلی:

1.  همان Difficulty
2.  Difficultyهای مجاور
3.  کل بانک در صورت نیاز

در صورت وجود Category مناسب، Category-aware filtering می‌تواند به مرتبط‌تر
شدن Distractorها کمک کند.

همچنین قواعد ثبت‌شدهٔ Quiz شامل محافظت در برابر:

-   CanonicalKey duplicate
-   lexical similarity نامناسب
-   Concept یکسان
-   همان Session
-   Entry type نامعتبر

است.

اگر حداقل ۳ Distractor معتبر پیدا نشود، خروجی باید به Flashcard fallback
شود.

### Read-only بودن تولید سؤال

Quiz Generation نباید:

-   LearningState را تغییر دهد.
-   Difficulty را تغییر دهد.
-   ReviewHistory ثبت کند.
-   Stage را تغییر دهد.
-   Scheduling را تغییر دهد.

------------------------------------------------------------------------

## 13. Review Session و Feedback

-   Session باید یک سقف کارت داشته باشد.
-   سقف Session اندازهٔ Session است و Eligibility را تغییر نمی‌دهد.
-   Selection باید قبل از شروع پاسخ‌ها انجام شود.
-   ثبت پاسخ تنها از SubmitReviewAnswer عبور می‌کند.
-   Feedback چهارگزینه‌ای پس از پاسخ طبق قرارداد فعلی ۳ ثانیه است.
-   سپس کارت بعدی به‌صورت خودکار ارائه می‌شود.
-   Replay یا Coroutine قدیمی نباید باعث ثبت دوبارهٔ پاسخ یا جابه‌جایی
    Session شود.

------------------------------------------------------------------------

## 14. Statistics، Progress، Goal، Streak و Achievement

### اصل

این بخش‌ها Consumer دادهٔ پایدار هستند و نباید منطق Learning را دوباره
پیاده کنند.

### Performance

-   Statistics نباید کل ReviewHistory را به ViewModel منتقل کند.
-   Aggregation باید در Database/Repository یا Background انجام شود.
-   Query باید بر اساس Window زمانی باشد.
-   History برای حل مشکل Performance حذف نمی‌شود.
-   Retention خودسرانه اضافه نمی‌شود.

### Daily Goal

-   مقدار پیش‌فرض Daily Goal: **20 کارت**
-   قابل تنظیم است.
-   تعداد کارت Session Eligibility را تغییر نمی‌دهد.

### Streak

Streak بر اساس دادهٔ Review ثبت‌شده و روزهای تقویمی محاسبه می‌شود، نه بر
اساس اینکه کاربر صرفاً صفحه‌ای را باز کرده است.

### Achievementهای مستندشده

نمونهٔ Thresholdهای تعریف‌شده:

-   Seven Day Streak → ۷ روز پیوسته
-   Thirty Day Streak → ۳۰ روز پیوسته
-   Memory Builder → ۱۰۰ کلمه Learned
-   Vocabulary Builder → ۵۰۰ Concept
-   Long Term Memory → ۵۰ پاسخ صحیح در Monthly
-   Achievement مرتبط با رسیدن Concept به Very Hard بر اساس
    `hasReachedVeryHard`

Achievementی که قبلاً Unlock شده نباید مجدداً به‌عنوان New Unlock گزارش
شود.

------------------------------------------------------------------------

## 15. Theme و Design System

Theme یک سیستم مستقل و Versioned است.

ThemeSpec باید مالک Appearance باشد و شامل مواردی مانند:

-   Accent
-   Light/Dark values
-   Semantic colors
-   Typography
-   Font size scaling
-   Density/spacing
-   Shapes
-   Control/icon tokens

Accentهای تعریف‌شده شامل:

-   Purple
-   Blue
-   Green
-   Orange
-   Pink

و برای آن‌ها حالت‌های Light/Dark وجود دارد.

Semantic tokenها شامل مفاهیمی مانند:

-   on-primary
-   success
-   warning
-   error

هستند.

### Compatibility

اگر Theme JSON قدیمی Field جدیدی نداشته باشد، Default امن استفاده می‌شود.

### مرز Theme

-   Theme نباید Concept را تغییر دهد.
-   Theme نباید LearningState را تغییر دهد.
-   Theme نباید DifficultyState را تغییر دهد.
-   Theme نباید ReviewHistory را تغییر دهد.
-   Theme نباید Scheduling را تغییر دهد.
-   UI نباید Paletteهای مستقل و Hard-coded خارج از ThemeSpec/ThemeTokens
    داشته باشد.

------------------------------------------------------------------------

## 16. Localization و Accessibility

### Localization

-   فارسی Default است.
-   منابع انگلیسی در `values-en` نگهداری می‌شوند.
-   UI Stringها از Resource می‌آیند.
-   Locale فقط Presentation را تغییر می‌دهد.
-   Locale نباید متن ذخیره‌شدهٔ Concept را بازنویسی کند.
-   Locale نباید LearningState/Difficulty/History را تغییر دهد.
-   Layout direction باید از Locale تبعیت کند.

### Accessibility

-   معنی کنترل‌های تعاملی باید برای Accessibility قابل تشخیص باشد.
-   Iconهای تعاملی Description معنادار داشته باشند.
-   Icon تزئینی در صورت وجود Label متنی مجاور می‌تواند Description نداشته
    باشد.
-   اطلاعات نباید فقط با رنگ منتقل شوند.
-   Touch targetها باید قابل استفاده باشند.
-   افزایش Font Size نباید Actions اصلی را پنهان یا غیرقابل دسترس کند.
-   Accessibility metadata بخشی از Business Data نیست.

مرز مهم: معماری و کنترل‌های High-value باید پوشش داده شوند؛ ادعای
Exhaustive Migration برای همهٔ رشته‌های قدیمی فقط با Audit واقعی قابل
اثبات است.

------------------------------------------------------------------------

## 17. Education / Help

Help یک Feature مستقل و Presentation-only است.

بخش‌های محتوایی مورد نیاز:

-   Quick Start
-   Onboarding
-   Library
-   Review
-   Difficulty
-   Progress
-   Settings
-   Theme
-   Import/Export
-   Backup/Restore
-   FAQ
-   Contextual Help

Help باید مفاهیم حساس را با مثال توضیح دهد:

-   تفاوت Stage و Difficulty
-   تفاوت Concept Difficulty و Test Difficulty
-   نقش Daily/Weekly/Monthly/Learned
-   معنی Session Card Count
-   مفهوم Eligibility

Help نباید LearningState، DifficultyState یا ReviewHistory را تغییر دهد.

Onboarding باید قابل Skip و بعداً قابل Reopen باشد.

------------------------------------------------------------------------

## 18. TTS و Quiz صوتی

TTS کاملاً Presentation-only است.

### موتور

Android System TextToSpeech.

Audio نباید در Database ذخیره شود مگر اینکه در آینده Contract مستقلی
تصویب شود.

### قرارداد قطعی

**تمام سؤال‌های Audio Quiz همیشه اسپانیایی هستند.**

#### Word Recognition

-   Audio: Spanish
-   Options: Spanish
-   کاربر باید کلمهٔ اسپانیایی شنیده‌شده را شناسایی کند.

#### Meaning Recognition

-   Audio: Spanish
-   Options: Persian
-   کاربر باید معنی فارسی کلمهٔ اسپانیایی شنیده‌شده را شناسایی کند.

زبان Optionها هرگز زبان Audio Question را تغییر نمی‌دهد.

### Replay

Replay فقط سؤال فعلی را تکرار می‌کند و:

-   سؤال جدید نمی‌سازد.
-   Answer Identity را تغییر نمی‌دهد.
-   Review ثبت نمی‌کند.
-   Difficulty را تغییر نمی‌دهد.
-   Scheduling را تغییر نمی‌دهد.

اگر Spanish Voice روی دستگاه موجود نباشد:

-   Crash ممنوع است.
-   Learning Data نباید تغییر کند.
-   باید Graceful Degradation انجام شود.

وجود Spanish Voice روی دستگاه فقط با Runtime/Device Verification قابل
اثبات است.

------------------------------------------------------------------------

## 19. Backup / Restore

### Backup

انواع:

-   Vocabulary
-   Progress
-   Full

شناسه‌های بین دیتابیس‌ها باید UUID پایدار داشته باشند.

Database ID فقط شناسهٔ داخلی همان Database است و نباید مبنای تطبیق
Backup/Restore باشد.

### Restore

ترتیب وابستگی:

1.  Languages
2.  Categories
3.  Tags
4.  Language Pairs
5.  Concepts
6.  Contents
7.  Concept-Tags
8.  Sessions
9.  Review History
10. Learning States
11. Difficulty States
12. Settings
13. Achievements

### قبل از Restore

-   Backup فعلی به‌صورت خودکار تهیه شود.
-   Backup ورودی Validate شود.
-   Schema Version بررسی شود.
-   UUIDهای تکراری بررسی شوند.
-   Referential Referenceها بررسی شوند.
-   Required fieldها Validate شوند.

### اجرای Restore

کل عملیات باید Transactional باشد.

در Failure:

-   Rollback کامل
-   Restore ناقص نباید Success گزارش شود.
-   پس از Commit، Integrity Check اجرا شود.

------------------------------------------------------------------------

## 20. Migration و Update بدون از دست رفتن داده

چهار مفهوم باید مستقل نگه داشته شوند:

-   Application Version
-   Schema Version
-   Data Version
-   Algorithm Version

همچنین Theme و Import/Backup Format Version قراردادهای مستقل خود را
دارند.

### Schema Migration

فقط ساختار Database را تغییر می‌دهد:

-   Column
-   Table
-   Index
-   Constraint

### Data Refresh / Migration

فقط دادهٔ موجود را به نسخهٔ مورد انتظار برنامه می‌رساند.

Refresh نباید:

-   Schema را تغییر دهد.
-   LearningState را تغییر دهد.
-   ReviewHistory را تغییر دهد.

مگر اینکه در آینده Migration صریح و مستقلی برای آن تصویب شود.

### Idempotency

هر Migration باید Idempotent باشد.

اگر:

`dataVersion == CURRENT_VERSION`

هیچ Migration دیگری اجرا نشود.

Concept Version و Content Version مستقل هستند.

------------------------------------------------------------------------

## 21. Performance و Scalability

### اصل اصلی

افزایش تعداد Concept یا ReviewHistory نباید باعث رشد خطی و کنترل‌نشدهٔ RAM
در عملیات روزمره شود.

### Library

ممنوع:

-   `getAll()` برای کل History/State/Concept صرفاً برای ساختن UI
-   Materialize کردن Dataset کامل

الزام:

-   Query محدود
-   Projection محدود
-   Paging/Lazy
-   Aggregate/Count در Database

### Progress

ممنوع:

-   Load کردن کل ReviewHistory و سپس Filter در Memory.

الزام:

-   Query بر اساس Window زمانی
-   Aggregate
-   Payload کوچک برای Chart

### Review Queue

فیلترهای:

-   stage
-   due time
-   difficulty
-   category
-   tag
-   language pair
-   exclusionهای روزانه

باید تا حد امکان در Repository/DAO اعمال شوند و سپس Candidate محدود وارد
حافظه شود.

### Quiz Bank

نباید کل بانک Vocabulary را برای هر عملیات معمول در RAM نگه دارد.

### Import

Streaming/Batch.

### UI

-   عملیات سنگین DB/File روی Main Thread اجرا نشود.
-   Recomposition نباید کل Dataset را دنبال کند.
-   Cache Source of Truth نیست.
-   Cache باید محدود و قابل تخلیه باشد.

### Benchmark

هیچ عدد ساختگی مجاز نیست.

سناریوهای اجباری:

-   Cold Start
-   Library
-   Search
-   Filter
-   Card Selection
-   Review Registration
-   Statistics
-   Import
-   Export
-   Restore
-   Migration
-   Help

هم Dataset کوچک و هم متوسط و بزرگ باید آزمایش شوند.

------------------------------------------------------------------------

## 22. Reliability، Crash، Retry و Concurrency

### Failureهای مورد نیاز

-   Crash وسط Review
-   Crash وسط Create
-   Crash وسط Import
-   Crash وسط Restore
-   Crash وسط Migration
-   Process Death
-   Rotation
-   Low Memory
-   Double Submit
-   Retry
-   Concurrent Review
-   Concurrent Import

### قوانین

-   Transaction مرز Commit است.
-   UI State جایگزین Persistence نیست.
-   Retry نباید Review تکراری بسازد.
-   دو Import همزمان باید ممنوع یا صریحاً Serialize شوند.
-   Restore نباید با عملیات Mutation داده همزمان شود.
-   Migration فقط در Database State امن اجرا شود.
-   Background شدن برنامه نباید Commit ناقص ایجاد کند.
-   بازگشت از File Picker باید State عملیات را درست بازیابی کند.
-   Low Memory نباید باعث فساد داده شود.

------------------------------------------------------------------------

## 23. امنیت، حریم خصوصی و حافظه

-   محصول پایه Offline است.
-   Permissionهای Android حداقلی باشند.
-   Import/Export فقط به فایل انتخاب‌شده توسط کاربر دسترسی داشته باشد.
-   Log نباید بدون ضرورت متن کامل محتوای شخصی را ثبت کند.
-   Database Source of Truth است.
-   UI State و Cache فقط مشتق هستند.
-   Dataset بزرگ نباید کامل در RAM قرار گیرد.
-   Backup حساس می‌تواند در آینده قرارداد رمزنگاری مستقل داشته باشد؛ این
    موضوع نباید بدون Contract جدید به‌عنوان رفتار قطعی فرض شود.

------------------------------------------------------------------------

## 24. Testing و Verification

### Test Pyramid

1.  Pure Unit Tests
2.  Repository/Integration Tests
3.  Database Tests
4.  Migration Tests
5.  Performance/Stress Tests
6.  Recovery Tests
7.  UI Tests
8.  End-to-End Tests

### Invariant Coverage

حداقل باید تست مستقیم برای این موارد وجود داشته باشد:

-   Exactly One LearningState per Concept
-   Exactly One DifficultyState per Concept
-   Exactly One ReviewHistory per Accepted Answer
-   Atomic Learning + Difficulty + History
-   Missing State → DATA_INTEGRITY_ERROR
-   Due Rule
-   Card Selection Priority
-   Easy/Very Hard boundaries
-   Soft Delete + History Preservation
-   Content Update بدون تغییر ناخواستهٔ Learning/History
-   Duplicate Attempt
-   Transaction Rollback
-   Import Cancel
-   Restore Rollback
-   Migration Safety
-   TTS Presentation-only
-   Locale Presentation-only
-   Help Presentation-only

------------------------------------------------------------------------

## 25. GitHub CI و مرز اثبات

GitHub Actions مسیر اصلی Verification پروژه است.

CI می‌تواند شامل:

-   Clean Debug Build
-   Unit Tests
-   Instrumentation Tests
-   APK Signature Check
-   Version Code Check
-   Update/Install-with-`-r` Verification
-   Source Archive

باشد.

اما:

> **وجود فایل Workflow به‌تنهایی برابر با اجرای موفق CI نیست.**

موفقیت Build/Instrumentation فقط وقتی Runtime/CI Evidence محسوب می‌شود که
یک Run واقعی و قابل مشاهده وجود داشته باشد.

همین اصل برای TTS دستگاه نیز برقرار است: وجود کد TTS به معنی وجود
Spanish Voice روی همهٔ دستگاه‌ها نیست.

------------------------------------------------------------------------

## 26. وضعیت پیاده‌سازی و مرزهای اثبات

در اسناد ممیزی منبع، بعضی موارد به‌صورت Source-level تأیید شده‌اند و بعضی
موارد هنوز Runtime Evidence می‌خواهند.

مواردی که صرفاً با Source Audit نمی‌توان به‌عنوان Runtime Success اعلام
کرد:

-   موفقیت واقعی Android Build
-   موفقیت واقعی Unit/Instrumentation در آخرین Commit
-   وجود Spanish TTS Voice روی دستگاه واقعی
-   Exhaustive Localization Sweep
-   Exhaustive Accessibility Verification
-   Benchmark عددی واقعی

همچنین ممیزی‌های منبع نشان داده‌اند که در برخی Snapshotهای بررسی‌شده مشکلات
Scalability در Library، Progress، Review Category Count و Review Queue
وجود داشته یا نیازمند Verification مستقیم بوده‌اند. این موارد نباید در یک
سند مشخصات به‌عنوان «حل‌شده» جا زده شوند مگر اینکه Evidence جدید آن را
ثابت کند.

------------------------------------------------------------------------

# 26. جزئیات تکمیلی استخراج‌شده از منابع

> این بخش جزئیات تکمیلی را نگه می‌دارد تا پوشش محتوایی از بین نرود. نام
> فایل‌ها و شماره‌های نسخهٔ تاریخی که برای فهم قرارداد اجرایی لازم نیستند
> عمداً عمومی‌سازی شده‌اند؛ قواعد محصول، داده، الگوریتم و وضعیت اثبات در
> بخش‌های اصلی سند و مراجع فنی آمده‌اند.

> این بخش عمداً قبل از Technical Reference قرار گرفته است. هدف آن حفظ
> جزئیات اجرایی مهمی است که در منابع در قالب‌های مختلف تکرار شده بودند.
> موارد تکراری حذف شده‌اند، اما محتوای منحصربه‌فرد نگه داشته شده است.
> تاریخچهٔ نسخه‌ای برای Traceability به‌صورت خلاصه بیان می‌شود و قرارداد
> محصولی به نسخهٔ خاصی وابسته نیست.

## نسخهٔ تاریخی معماری و مدل داده

-   Architecture: Offline-First

-   2.  Descriptions v4 --- 3 occurrences of the SubmitReviewAnswer /
        Restore atomic-transaction description corrected to include
        DifficultyState alongside LearningState and ReviewHistory
        (previously DifficultyState was omitted in 3 of 4 places).

-   4.  Descriptions v4 --- stale reference to the legacy
        "SpacedRepetitionEngine" in the Future Extensions section
        replaced with "Learning Transition / Difficulty Calculation" to
        match the current architecture.

-   5.  Phase 1 v4 --- added EntryType enum, Concept and ReviewHistory
        data classes, and 4 missing repository interfaces
        (ConceptRepository, ReviewHistoryRepository, SettingsRepository,
        FlashLearnDatabase) that were used in Phase 1's own example code
        but never formally declared.

-   7.  Phase 4 v4 --- fixed 5 concrete mismatches against the Phase 1
        v4 repository interfaces:
        learningStateRepository/difficultyStateRepository
        .getByConceptId() → .get(); contentRepository.insert() →
        .upsert(); conceptTagRepository.addTagToConcept() →
        .insert(ConceptTag(...)); contentType/ContentType →
        entryType/EntryType.

-   8.  Phase 4 v4 --- status changed from "IMPLEMENTATION-READY
        (Contract-Aligned)" to "PROVISIONAL --- Interface-level only,
        pending Phase 3 (Room)", with an explicit note that this
        document was produced out of the declared phase order (Phase 3
        does not exist yet).

-   10. Phase 3 (Room Entities/DAOs) has not been written yet. Phase 4
        was produced before it. This does not block compilation of the
        domain layer (UseCases depend only on interfaces), but the
        declared phase order (1→2→3→4→5) was not followed.

-   FlashLearn --- File Replacement Index

-   سند/فایل تاریخی سند/فایل تاریخی

-   PASS --- LearningState owns no difficulty (fixed in Descriptions
    نسخهٔ مستندشده --- CreateConcept sentence rewritten and section-4
    Data Model table corrected)

-   PASS --- DifficultyState independent model

-   PASS --- ConceptTag present

-   PASS --- Backup/Restore DifficultyState

-   PASS --- Code initializes DifficultyState EASY

-   PASS --- Restore Content uses UUID then conceptId+languageCode

-   -   Statistics aggregates accepted ReviewHistory activity.

-   -   Exposes total reviews, correct, wrong, accuracy and reviewed
        concept count.

-   -   Statistics does not mutate LearningState or DifficultyState.

-   -   Progress aggregates canonical LearningState by DAILY, WEEKLY,
        MONTHLY and LEARNED.

-   -   LearningState remains the owner of learning-stage data.

-   -   DifficultyState remains independent of Stage.

-   -   ReviewHistory remains append-only.

-   -   No Room schema change is introduced.

-   DatabaseModule

-   RoomFlashLearnDatabase is provided as a Singleton and all DAOs are
    provided from that database instance. The existing migration 1→2
    remains the configured migration.

-   RepositoryModule

-   All domain repository interfaces are bound to their Room
    implementations, including ReviewSessionRepository.
    FlashLearnDatabase is bound to FlashLearnDatabaseImpl.

-   Room repositories and FlashLearnDatabaseImpl use @Inject
    constructors, allowing Hilt to resolve the complete repository
    graph.

-   The existing use cases already use @Inject constructors. Their
    dependencies are now covered by the complete Hilt repository graph.

-   Schema impact

-   No database schema change or migration was introduced in Phase 7.

-   Navigation is isolated in the app module and does not introduce
    dependencies from domain/data back into UI.

-   Schema

-   No Room schema or migration changes were introduced.

-   -   ReviewSession model, repository contract, Room
        DAO/mapper/repository, and Hilt binding.

-   -   Constructor injection added to Room repository implementations
        so Hilt can instantiate them.

-   -   LearningStateRepository extended with queue-wide stage and due
        queries backed by Room.

-   -   GetDifficultyStateUseCase with explicit DATA_INTEGRITY_ERROR
        when state is missing.

-   -   GetProgressSummaryUseCase using active Concepts and
        LearningState as the source of truth.

-   -   Metrics: active concept count, learned count, due count, total
        correct, total wrong, accuracy percentage.

-   -   No duplicated Progress persistence and no Room schema change.

-   Contract constraints preserved

-   -   LearningState and DifficultyState remain separate.

-   -   Missing required LearningState is treated as data-integrity
        failure rather than silently repaired.

-   Random Review اصلی فقط از Conceptهای DAILY/WEEKLY/MONTHLY واجد شرایط
    و Due انتخاب می‌کند؛ LEARNED در Mode مستقل خود قرار دارد.

-   در هر دسته، ترتیب بر اساس nextReviewAt صعودی (قدیمی‌ترین ابتدا) و سپس
    Concept.id صعودی (برای ثبات) است.

-   Exact Duplicate بر اساس کلید پایدار و محتوای نرمال‌شده شناسایی می‌شود؛
    در Content، یکتایی داخل یک Concept با (conceptId, languageCode) حفظ
    می‌شود و Concept-Matching از (languageCode, canonicalKey) استفاده
    می‌کند.

-   Backup: یک Snapshot کامل از جداول به‌همراه UUID هر رکورد و
    schemaVersion.

-   Languages → Categories → Tags → LanguagePairs → Concepts → Contents
    → ConceptTags → ReviewSessions → ReviewHistory → LearningStates →
    DifficultyStates → Settings → Achievements.

-   تعارض‌ها با UUID تشخیص داده می‌شوند و طبق سیاست Merge/Update مدیریت
    می‌شوند.

-   Backup پیشرفت: Settings، DifficultyState، ReviewSessions،
    ReviewHistory، LearningStates و Achievements.

-   Concept و Content هرکدام دارای فیلد dataVersion (Int) هستند که آخرین
    نسخه داده‌ای اعمال‌شده روی آن رکورد را نشان می‌دهد.

-   Refresh مجاز به تغییر LearningState، ReviewHistory یا هر Learning
    Data دیگری نیست؛ مگر با یک Migration جداگانه و تصمیم صریح آینده.

-   Data Migration (تغییر محتوا) کاملاً از Room Schema Migration (تغییر
    ساختار جدول‌ها) جداست؛ این دو نباید با هم قاطی شوند.

-   این رفتار باید برای Concept و Content

-   Concept 1 → موفق

-   Concept 2 → موفق

-   Concept 3 → موفق

-   Concept 4 → ERROR

-   ساخت یک سؤال چهارگزینه‌ای برای Concept انتخاب‌شده،

-   concept : Concept

-   difficultyState : DifficultyState

-   promptContent =

-   یک Content معتبر متعلق به concept

-   content.languageCode

-   correctContent =

-   اگر promptContent پیدا نشد:

-   اگر correctContent پیدا نشد:

-   اگر concept دارای چند Content در

-   یکی از Contentهای معتبر به عنوان

-   correctContent انتخاب می‌شود.

-   سایر Contentهای همان concept:

-   آنها پاسخ‌های معتبر همان Concept هستند

-   پیدا کردن Contentهایی که می‌توانند

-   1.  Content متعلق به targetLanguage باشد:

-   2.  متعلق به Concept دیگری باشد:

-   content.conceptId

-   != concept.id

-   3.  Concept مربوطه متعلق به همان

-   difficultyState.current

-   normalize(content.text)

-   normalize(correctContent.text)

-   بنابراین اگر چند Concept مختلف

-   difficulty == difficultyState.current

-   Difficulty فعلی Concept باشند.

-   correctContent.text,

-   promptContent.text,

-   6.  LearningState را تغییر نمی‌دهد.

-   10. Distractor باید متعلق به Concept دیگری باشد.

-   11. Contentهای متعدد متعلق به Concept فعلی

-   «این Concept سخت‌تر شود»

-   «این Concept آسان‌تر شود»

-   «Concept وارد Stage بعد شود»

-   «Concept دوباره چه زمانی نمایش داده شود»

-   حذف صحیح Concept فعلی با content.conceptId != concept.id

-   جلوگیری قطعی از استفاده از ترجمه‌های دیگر همان Concept به‌عنوان
    Distractor

-   مشخص کردن رفتار دقیق در چندترجمه‌ای بودن Concept

-   Transition باید Pure و Deterministic باقی بماند و مستقیماً Database
    را تغییر ندهد. در عین حال، تمام state fields که مستقیماً به نتیجه
    Stage Transition وابسته‌اند باید در خروجی آن محاسبه شوند.

-   • TransitionResult فقط نتیجه محاسبات را برمی‌گرداند؛ اعمال آن روی
    LearningState و ثبت ReviewHistory در SubmitReviewAnswer و داخل یک
    Transaction انجام می‌شود

-   M. Content Data Model Finalization --- canonicalKey / normalizedText

-   M.1 --- رابطه normalizedText و canonicalKey: این دو یک فیلد نیستند.
    normalizedText صرفاً خروجی میانی مرحله Parsing است (حذف artifact های
    Import مثل شماره خط) و در نهایت به Content.text تبدیل می‌شود؛ فیلد
    مستقل و Persistent نیست. canonicalKey یک فیلد Persistent روی Content
    است که از روی Content.text نهایی محاسبه و Index می‌شود و فقط برای
    Matching/Duplicate Detection استفاده می‌شود.

-   Content Entity نهایی: Content { id, conceptId, languageCode, text,
    canonicalKey, notes, pronunciation, example } با UNIQUE(conceptId,
    languageCode) و INDEX(languageCode, canonicalKey). هر بار که
    Content.text در Create یا Update تغییر کند، canonicalKey هم‌زمان با
    computeCanonicalKey بازمحاسبه می‌شود و هرگز Stale نمی‌ماند.

-   O.1 --- اصلاح Random Review (Override بخش نسخهٔ تاریخی FROZEN): تعریف
    قبلی بخش نسخهٔ تاریخی که Random Review را منحصراً از Concept‌های
    LEARNED انتخاب می‌کرد، اشتباه بود و از این پس با تعریف زیر جایگزین
    می‌شود. Random Review اکنون فقط از Concept‌هایی در Stage های DAILY،
    WEEKLY و MONTHLY انتخاب می‌شود که Eligible هستند (nextReviewAt \<=
    now)؛ Concept های LEARNED دیگر بخشی از Random Review نیستند. ترتیب
    انتخاب کاملاً Shuffle است، بدون توجه به nextReviewAt یا اولویت Stage.
    صف مرور اصلی (بخش ۹) بدون تغییر باقی می‌ماند و کاملاً مستقل از Random
    Review است؛ ترتیب آن همچنان بر اساس اولویت (nextReviewAt ASC) است،
    نه Shuffle.

-   O.2 --- مرور کلمات یادگرفته‌شده (بخش جدید): یک بخش مستقل برای مرور
    Concept‌های LEARNED در نظر گرفته می‌شود که به‌صورت زیرمجموعه همان قسمت
    مرور/تمرین در اپ نمایش داده می‌شود (نه یک منوی جداگانه در صفحه اصلی).
    این بخش شرط Eligibility ندارد (چون nextReviewAt در LEARNED برابر
    null است) و ترتیب آن Shuffle است --- دقیقاً همان رفتاری که قبلاً
    به‌اشتباه زیر نام Random Review تعریف شده بود.

-   • Vocabulary Difficulty سختی خود Vocabulary/Concept است و چهار سطح
    دارد: EASY / MEDIUM / HARD / VERY_HARD.

-   • این دو مفهوم مستقل‌اند. Quiz Difficulty نباید Vocabulary
    Difficulty، DifficultyState، LearningState، Stage یا Scheduling را
    تغییر دهد.

-   • Quiz Difficulty در V1 یک کنترل برای تولید Distractor است و به‌عنوان
    Difficulty پایدار Concept ذخیره نمی‌شود.

-   • Distractor نباید از همان Concept باشد.

-   • Content/Translation یکسان نباید به‌عنوان گزینه تکراری استفاده شود.

-   Category می‌تواند برای سخت‌تر کردن Quiz استفاده شود. در MEDIUM دسته
    مشابه و در HARD همان یا نزدیک‌ترین Category معتبر ترجیح داده می‌شود؛
    اما اعتبار، یکتایی، عدم تعلق به همان Concept و عدم تکرار
    Content/Translation همیشه مقدم است.

-   Random Review اصلی فقط از Conceptهای واجد شرایط انتخاب می‌کند:

-   • پاسخ ثبت‌شده باید ReviewHistory و LearningState را به‌صورت اتمیک
    به‌روزرسانی کند.

-   1.  DifficultyState به Progress Backup اضافه شد و Restore مستقل آن
        تعریف شد.

-   2.  Restore Content با قرارداد نهایی بخش M هماهنگ شد؛ معیار Merge
        داخل Concept فقط UUID و سپس (conceptId + languageCode) است.

-   4.  قرارداد ورودی DifficultyState شامل hasReachedVeryHard نیز شد.

-   قطعی اصلاح‌شده: Backup/Restore شامل DifficultyState؛ Restore Content
    بر اساس UUID سپس (conceptId + languageCode)؛ Difficulty از
    DifficultyState.current؛ Tag از ConceptTag؛ DifficultyState شامل
    hasReachedVeryHard. -
    -----------------------------------------------------------------------
    آمار منبع محاسبه -----------------------------------
    ----------------------------------- تعداد کل کلمات فعال COUNT(\*) از
    concept با active=1

-   کلمات تمرین‌شده COUNT(DISTINCT conceptId) از review_history

-   VOCABULARY_BUILDER ۵۰۰ Concept

-   LearningState { id: UUID conceptId: UUID stage: Stage nextReviewAt:
    Instant? monthlyWrongCount: Int hasPathFailure: Boolean
    totalCorrect: Int totalWrong: Int lastReviewedAt: Instant? }
    DifficultyState { id: UUID conceptId: UUID current:
    VocabularyDifficulty // EASY \| MEDIUM \| HARD \| VERY_HARD
    consecutiveCorrect: Int consecutiveWrong: Int hasReachedVeryHard:
    Boolean } ConceptTag { conceptId: UUID tagId: UUID // Primary key:
    (conceptId, tagId) } Content { id: UUID conceptId: UUID
    languageCode: String text: String canonicalKey: String notes:
    String? pronunciation: String? example: String? // Unique:
    (conceptId, languageCode) // Index: (languageCode, canonicalKey) }
    RULE: LearningState does NOT contain difficulty. RULE:
    DifficultyState is persisted and updated independently. RULE:
    hasReachedVeryHard is monotonic during normal review and becomes
    true once VERY_HARD is reached.

-   2.  DifficultyState Persistence

-   @Entity( tableName = "difficulty_states", indices = \[ Index(value =
    \["conceptId"\], unique = true)\] ) data class
    DifficultyStateEntity( @PrimaryKey val id: UUID, val conceptId:
    UUID, val current: String, val consecutiveCorrect: Int, val
    consecutiveWrong: Int, val hasReachedVeryHard: Boolean ) @Dao
    interface DifficultyStateDao { @Query("SELECT \* FROM
    difficulty_states WHERE conceptId = :conceptId LIMIT 1") suspend fun
    getByConceptId(conceptId: UUID): DifficultyStateEntity?
    @Insert(onConflict = REPLACE) suspend fun upsert(entity:
    DifficultyStateEntity) @Delete suspend fun delete(entity:
    DifficultyStateEntity) } interface DifficultyStateRepository {
    suspend fun get(conceptId: UUID): DifficultyState? suspend fun
    upsert(state: DifficultyState) suspend fun delete(conceptId: UUID) }

-   3.  LearningState Persistence

-   @Entity( tableName = "learning_states", indices = \[ Index(value =
    \["conceptId"\], unique = true), Index(value = \["stage",
    "nextReviewAt"\])\] ) data class LearningStateEntity( @PrimaryKey
    val id: UUID, val conceptId: UUID, val stage: String, val
    nextReviewAt: Instant?, val monthlyWrongCount: Int, val
    hasPathFailure: Boolean, val totalCorrect: Int, val totalWrong: Int,
    val lastReviewedAt: Instant? ) @Dao interface LearningStateDao {
    @Query("SELECT \* FROM learning_states WHERE conceptId = :conceptId
    LIMIT 1") suspend fun getByConceptId(conceptId: UUID):
    LearningStateEntity? @Insert(onConflict = REPLACE) suspend fun
    upsert(entity: LearningStateEntity) }

-   4.  Mappers --- No Difficulty in LearningState

-   fun LearningStateEntity.toDomain(): LearningState = LearningState(
    id = id, conceptId = conceptId, stage = Stage.valueOf(stage),
    nextReviewAt = nextReviewAt, monthlyWrongCount = monthlyWrongCount,
    hasPathFailure = hasPathFailure, totalCorrect = totalCorrect,
    totalWrong = totalWrong, lastReviewedAt = lastReviewedAt ) fun
    DifficultyStateEntity.toDomain(): DifficultyState = DifficultyState(
    id = id, conceptId = conceptId, current =
    VocabularyDifficulty.valueOf(current), consecutiveCorrect =
    consecutiveCorrect, consecutiveWrong = consecutiveWrong,
    hasReachedVeryHard = hasReachedVeryHard )

-   5.  CreateConcept --- Atomic Initialization

-   @Transaction suspend fun createConcept(command:
    CreateConceptCommand): UUID { val conceptId =
    conceptRepository.insert(command.toConcept())
    learningStateRepository.upsert( LearningState( id =
    UUID.randomUUID(), conceptId = conceptId, stage = Stage.DAILY,
    nextReviewAt = null, monthlyWrongCount = 0, hasPathFailure = false,
    totalCorrect = 0, totalWrong = 0, lastReviewedAt = null ) )
    difficultyStateRepository.upsert( DifficultyState( id =
    UUID.randomUUID(), conceptId = conceptId, current =
    VocabularyDifficulty.EASY, consecutiveCorrect = 0, consecutiveWrong
    = 0, hasReachedVeryHard = false ) ) return conceptId }

-   @Transaction suspend fun submitReviewAnswer( request:
    SubmitReviewAnswerRequest ): SubmitReviewAnswerResult { val learning
    = learningStateRepository.get(request.conceptId) ?:
    error("LearningState not found") val difficulty =
    difficultyStateRepository.get(request.conceptId) ?:
    error("DifficultyState not found") validateDue(learning,
    request.reviewedAt) validateAttemptUniqueness(request.sessionId,
    request.reviewAttemptId) val transition =
    learningTransition.calculate( learningState = learning, reviewType =
    request.reviewType, isCorrect = request.isCorrect, reviewedAt =
    request.reviewedAt ) val newDifficulty =
    difficultyCalculation.calculate( state = difficulty, reviewType =
    request.reviewType, isCorrect = request.isCorrect )
    learningStateRepository.upsert( learning.copy( stage =
    transition.newStage, nextReviewAt = transition.nextReviewAt,
    hasPathFailure = transition.hasPathFailure, monthlyWrongCount =
    transition.monthlyWrongCount, totalCorrect = if (request.isCorrect)
    learning.totalCorrect + 1 else learning.totalCorrect, totalWrong =
    if (!request.isCorrect) learning.totalWrong + 1 else
    learning.totalWrong, lastReviewedAt = request.reviewedAt ) )
    difficultyStateRepository.upsert(newDifficulty)
    reviewHistoryRepository.insert( ReviewHistory( sessionId =
    request.sessionId, reviewAttemptId = request.reviewAttemptId,
    conceptId = request.conceptId, reviewedAt = request.reviewedAt,
    isCorrect = request.isCorrect, reviewType = request.reviewType ) )
    return SubmitReviewAnswerResult( learningState = transition,
    difficultyState = newDifficulty ) } const val THRESHOLD_DIFFICULTY =
    3

-   Random Review = DAILY + WEEKLY + MONTHLY candidates that are Due.
    LEARNED is not part of Random Review; it is a separate voluntary
    review flow. Due means: nextReviewAt \<= now. Candidate difficulty
    is read from DifficultyState.current. Candidate tags are read
    through ConceptTag.

-   Backup export MUST include:
    -   Concepts
    -   Contents
    -   ConceptTags
    -   LearningStates
    -   DifficultyStates
    -   ReviewSessions
    -   ReviewHistory
    -   Settings
    -   Achievements (and the remaining canonical backup entities)
        Restore order: Languages → Categories → Tags → LanguagePairs →
        Concepts → Contents → ConceptTags → ReviewSessions →
        ReviewHistory → LearningStates → DifficultyStates → Settings →
        Achievements Content restore:

    1.  UUID match first.
    2.  If UUID absent, lookup by (conceptId, languageCode).
    3.  Same text =\> skip.
    4.  Changed text =\> update existing.
    5.  Missing =\> insert.
    6.  canonicalKey is recalculated from final Content.text. ConceptTag
        restore is idempotent on (conceptId, tagId).

-   DifficultyState migration is independent from LearningState
    migration. For legacy DifficultyState records without evidence for
    hasReachedVeryHard, initialize hasReachedVeryHard = false.
    canonicalKey migration is computed from current Content.text: trim +
    lowercase + collapse consecutive spaces. Do not remove accents or
    punctuation. For old installs, ConceptTag table is created empty; no
    unsupported automatic V1 Tag-per-Concept backfill is performed.

-   The original source contained older implementations in which:
    -   LearningState owned a difficulty field.
    -   SpacedRepetitionEngine referenced LearningState.difficulty.
    -   Backup/restore did not independently persist DifficultyState.
        These blocks are retained only as historical audit material.
        They MUST NOT be compiled, registered, invoked, or treated as an
        implementation path. The canonical implementation is the
        contract in Sections 1--9 above. PENDING REVIEW: Any additional
        legacy helper whose only purpose is the old
        LearningState.difficulty path may be deleted after manual
        review. No uncertain legacy material is automatically deleted by
        this reconstruction.

-   RED blockers addressed in this reconstruction: \[FIXED\]
    SubmitReviewAnswer/CreateConcept section boundary reconstructed
    cleanly. \[FIXED\] LearningState.difficulty removed from the
    canonical model. \[FIXED\] DifficultyState has independent
    persistence, repository, mapper and transaction updates. \[FIXED\]
    CreateConcept initializes LearningState and DifficultyState
    atomically. \[FIXED\] SubmitReviewAnswer atomically updates
    LearningState + DifficultyState + ReviewHistory. \[FIXED\] Legacy
    SpacedRepetitionEngine is isolated as NOT EXECUTABLE. \[FIXED\]
    Backup/Restore and migration contracts include independent
    DifficultyState. FINAL STATUS: Content-level cross-consistency is
    ready for the final three-way audit. This document is a
    contract-aligned code specification; it has not been claimed as a
    build-tested Android project because the uploaded artifact does not
    provide a complete compilable repository.

-   ۴. مدل داده و Entityهای اصلی 5

-   ۱۴. قراردادهای UseCase / Repository / DAO 7

-   در این نسخه (نسخهٔ تاریخی) هر دو مشکل رفع شده است: محتوای پراکنده
    نسخه کامل، با تطبیق شماره‌های قدیمی به فهرست مطالب صحیح، به‌درستی زیر
    عنوان واقعی خودش قرار گرفت؛ سپس این محتوا با نسخه کوتاه ادغام شد. در
    جاهایی که دو فایل با هم مغایرت داشتند (مثل نام کلید تنظیم آستانه
    سختی) یک نسخه به‌عنوان مرجع انتخاب و مغایرت صراحتاً ثبت شد. بخش‌هایی که
    در هیچ‌کدام از دو فایل محتوای واقعی نداشتند (مثل تعریف کامل Entityهای
    دیتابیس) با استفاده از سایر بخش‌های همین دو سند بازسازی و «تکمیل»
    شدند؛ این موارد با برچسب «اصلاح یکپارچه‌سازی» مشخص شده‌اند تا با
    تصمیم‌های اصلی شما اشتباه گرفته نشوند.

-   برای هر Concept دقیقاً یک LearningState وجود دارد.

-   هر پاسخ پذیرفته‌شده دقیقاً یک ReviewHistory ایجاد می‌کند؛ ReviewHistory
    فقط اضافه می‌شود (append-only) و هرگز حذف/بازنویسی نمی‌شود.

-   تغییر LearningState، DifficultyState و ثبت ReviewHistory در یک
    Transaction اتمیک انجام می‌شود.

-   Refresh فقط Data/Content را به‌روزرسانی می‌کند و هرگز Learning Data
    (وضعیت یادگیری، تاریخچه) را دست‌کاری نمی‌کند.

-   Backup یک Snapshot کامل همراه با UUIDها است.

-   Clean Architecture + MVVM + Repository Pattern + Jetpack
    Compose/Material 3 + Room/SQLite + Flow/Coroutines + Hilt.

-   database: Room Entities، DAOها، Migrationها

-   domain: مدل‌های دامنه، رابط‌های Repository، UseCaseها، Review
    Engine/Scheduler، Refresh/Migration Engine (Local Data Update
    Manager)

-   data: پیاده‌سازی Repositoryها، Mapperها، Import/Export،
    Backup/Restore

-   Compose UI → ViewModel → UseCase → رابط Repository در Domain ←
    پیاده‌سازی Repository در Data → DAO → Room → SQLite.

-   Domain Layer کاملاً خالص و بدون وابستگی به Android SDK، Room یا
    کتابخانه‌های UI است.

-   دسترسی به داده فقط از طریق Repository Pattern انجام می‌شود.

-   ۴. مدل داده و Entityهای اصلی

-   مدل وضعیت یادگیری و Difficulty در V1 به‌صورت دو State مستقل تعریف
    می‌شود: LearningState و DifficultyState. این دو State هر دو به
    Concept فعال متصل‌اند، اما Entity واحد محسوب نمی‌شوند.

-   LearningState فقط وضعیت مسیر یادگیری، زمان‌بندی Review و Historical
    Progress مرتبط با مسیر را نگهداری می‌کند:

-   Active Concept بدون LearningState یا بدون DifficultyState یک
    DATA_INTEGRITY_ERROR است و Statistics، Review یا Queue نباید State
    مفقود را حدس بزنند یا خودکار جایگزین کنند.

-   CreateConcept باید Concept + Content(s) + LearningState اولیه +
    DifficultyState اولیه را به‌صورت اتمیک ایجاد کند. مقدار اولیه
    DifficultyState: current=EASY, consecutiveCorrect=0,
    consecutiveWrong=0, hasReachedVeryHard=false.

-   consecutiveWrong → DifficultyState.consecutiveWrong

-   consecutiveCorrect → DifficultyState.consecutiveCorrect

-   difficulty → DifficultyState.current

-   فیلدهای زیر نباید در LearningState نگهداری شوند و باید در
    DifficultyState باشند:

-   DifficultyState.hasReachedVeryHard یک Historical Flag است: مقدار
    اولیه false است؛ پس از رسیدن Difficulty به VERY_HARD برابر true
    می‌شود و در کاهش Difficulty هرگز به false برنمی‌گردد. فقط Explicit
    Reset/Delete مجاز به تغییر آن است.

-   DifficultyState.consecutiveCorrect و
    DifficultyState.consecutiveWrong هر دو \>= 0 هستند و همزمان
    نمی‌توانند بزرگ‌تر از صفر باشند.

-   DifficultyState.current یکی از EASY / MEDIUM / HARD / VERY_HARD است.

-   LearningState.hasPathFailure پس از شکست WEEKLY یا MONTHLY به true
    می‌رسد و در Review عادی Reset نمی‌شود.

-   LearningState.monthlyWrongCount فقط در MONTHLY + Wrong افزایش می‌یابد
    و در موفقیت Reset نمی‌شود.

-   LearningState.nextReviewAt توسط Learning Transition تعیین می‌شود؛
    برای LEARNED باید NULL باشد.

-   در نسخه آینده، در صورت وابسته‌شدن Progress به LanguagePair، کلید
    یکتایی می‌تواند به UNIQUE(conceptId, languagePairId) ارتقا یابد؛ این
    تغییر در V1 اعمال نمی‌شود.

-   DifficultyState.conceptId UNIQUE

-   LearningState.conceptId UNIQUE

-   در V1 برای هر Concept فعال دقیقاً یک LearningState و دقیقاً یک
    DifficultyState باید وجود داشته باشد.

-   Concept 1:1 DifficultyState

-   Concept 1:1 LearningState

-   DifficultyState { id, conceptId, current, consecutiveCorrect,
    consecutiveWrong, hasReachedVeryHard }

-   DifficultyState فقط وضعیت Difficulty و شمارنده‌های مربوط به محاسبه
    Difficulty را نگهداری می‌کند:

-   LearningState { id, conceptId, stage, nextReviewAt,
    monthlyWrongCount, hasPathFailure, totalCorrect, totalWrong,
    lastReviewedAt }

-   ۱۴. قراردادهای UseCase / Repository / DAO

-   CreateConcept: ایجاد اتمیک Concept + Content(s) + LearningState
    اولیه با stage=DAILY + DifficultyState اولیه با difficulty=EASY.

-   Repository تنها مسیر دسترسی لایه Domain به داده است؛ UI هرگز مستقیم
    به DAO یا دیتابیس دسترسی ندارد.

-   تمام عملیات پایگاه‌داده با یک نمونه واحد (Single Instance) از Room
    Database و روی IO Dispatcher انجام می‌شوند.

-   کد کامل DAOها، Mapperها، UseCaseها، ViewModelها و صفحات Compose UI
    در هیچ‌کدام از دو فایل ارائه نشده بود (فقط عنوان بخش‌های مربوطه در
    فهرست مطالب آمده بود). طبق روش کاری اعلام‌شده در هر دو سند («این نسخه
    برای بررسی مرحله‌ای طراحی شده است»)، این بخش‌ها عمداً برای مرحله
    جداگانه کدنویسی نگه داشته شده‌اند و بخشی از این سند طراحی نیستند.

-   Lazy Loading: در Compose با LazyColumn/LazyRow؛ در Repository با
    Flow همراه buffer().

-   یک نمونه واحد (Single Instance) از Room Database؛ همه عملیات دیتابیس
    روی Dispatchers.IO.

-   دیتابیس فقط از طریق Repository در دسترس است؛ UI هرگز مستقیم به
    دیتابیس دسترسی ندارد.

-   Domain: مدل جدید در domain/model؛ منطق تجاری جدید در domain/usecase؛
    رابط دسترسی داده در domain/repository.

-   Data: پیاده‌سازی Repository در data/repository؛ به‌روزرسانی Mapperها؛
    در صورت نیاز DAO/Entity جدید در database.

-   DI: افزودن وابستگی جدید به DatabaseModule، RepositoryModule یا
    AppModule در صورت نیاز.

-   1.  ایجاد یک کلاس جدید در database/migration که از Migration ارث‌بری
        می‌کند.

-   2.  نوشتن SQL تغییر Schema داخل متد migrate().

-   3.  افزایش شماره version دیتابیس در FlashLearnDatabase.

-   پشتیبانی از چند جفت‌زبان فعال هم‌زمان (نه فقط یکی) با LearningState
    مستقل برای هر جفت‌زبان.

-   هر Concept می‌تواند برای هر جفت‌زبان یک LearningState مستقل داشته
    باشد.

-   سازگاری معماری: جدول language_pair فعلی این قابلیت را بدون تغییر در
    Schema پشتیبانی می‌کند؛ برای پیاده‌سازی فقط لازم است Unique Index شرطی
    روی isActive (WHERE isActive=1) حذف و منطق انتخاب جفت‌زبان پیش‌فرض در
    AppSetting اضافه شود؛ LearningState باید به languagePairId متصل شود
    تا وضعیت یادگیری برای هر جفت‌زبان مستقل باشد.

-   5.  Schema Migration و Data Migration کاملاً از هم جدا هستند.

-   6.  ReviewHistory تنها منبع فعالیت‌های واقعاً ثبت‌شده است.

-   8.  هر Concept دقیقاً یک LearningState دارد.

-   9.  هر پاسخ پذیرفته‌شده دقیقاً یک ReviewHistory ایجاد می‌کند.

-   14. مرور Learned به‌طور خودکار Concept را دوباره وارد چرخه فعال
        نمی‌کند.

-   • این قابلیت‌ها هیچ تغییری در LearningState، DifficultyState،
    Scheduling، monthlyWrongCount یا hasPathFailure ایجاد نمی‌کنند.

-   • LearningState مالک Stage، nextReviewAt، monthlyWrongCount،
    hasPathFailure و Totalها است.

-   • DifficultyState مالک current، consecutiveCorrect، consecutiveWrong
    و hasReachedVeryHard است.

-   • Core Domain Concepts --- وضعیت: نهایی. Concept، Content،
    LearningState، DifficultyState، Review Stage و روابط اصلی باید با
    تعاریف همین سند خوانده شوند.

-   Two remaining inconsistencies from the legacy model have been
    corrected:
    (1) the CreateConcept UseCase sentence in section 14, which
        previously stated "LearningState اولیه با stage=DAILY و
        difficulty=EASY" with an inline LEGACY/SUPERSEDED marker, was
        rewritten as "LearningState اولیه با stage=DAILY +
        DifficultyState اولیه با difficulty=EASY" to match the canonical
        ownership model directly, rather than carrying the contradiction
        forward with a label; (2) the canonical Data Model table in
        section 4 still listed difficulty, consecutiveCorrect, and
        consecutiveWrong as LearningState fields --- these were removed
        from that row, and a dedicated DifficultyState row (id,
        conceptId, current, consecutiveCorrect, consecutiveWrong,
        hasReachedVeryHard) was added to the same table so it matches
        the ownership rules already defined elsewhere in this document.
        The canonical نسخهٔ مستندشده model assigns difficulty exclusively
        to DifficultyState. No uncertain material was deleted
        automatically.

-   پایگاه داده Room + SQLite

-   ## معماری Clean Architecture + MVVM + Repository Pattern -

    Entity فیلدهای اصلی توضیح -----------------------
    ----------------------- ----------------------- Concept id، uuid،
    contentType، هسته معنایی مستقل از categoryId، favorite، زبان.
    active، createdAt،\
    updatedAt

-   Content id، conceptId، ترجمه/محتوای Concept languageCode، text، برای
    یک زبان؛ یکتا بر notes، pronunciation، اساس (conceptId, example
    languageCode).

-   LearningState conceptId (۱به۱)، یک‌به‌یک با Concept؛ stage،
    nextReviewAt، وضعیت فعلی یادگیری. monthlyWrongCount،\
    hasPathFailure،\
    totalCorrect،\
    totalWrong،\
    lastReviewedAt

-   DifficultyState id، conceptId، current، وضعیت سختی فعلی و
    consecutiveCorrect، شمارنده‌های محاسبه consecutiveWrong، Difficulty؛
    یک‌به‌یک با hasReachedVeryHard Concept.

-   ReviewHistory id، conceptId، ثبت append-only؛ sessionId، جلوگیری از
    پاسخ تکراری reviewAttemptId، با (sessionId, isCorrect، ...
    reviewAttemptId).

-   ۴ ورود داده تکراری تشخیص با UUID و ترکیب (Duplicate) (conceptId,
    languageCode)؛ ترجمه‌های جدید به همان Concept اضافه می‌شوند، ترجمه‌های
    تکراری دقیقاً یکسان ذخیره نمی‌شوند.

-   ۷ فایل Import خراب یا پیش از Import، فایل ناقص اعتبارسنجی می‌شود
    (JSON Schema یا بررسی ساختار)؛ در صورت نامعتبر بودن، خطا نمایش داده
    می‌شود و Import انجام نمی‌شود.

-   ۱۰ تغییر جهت زبان (Swap) Swap فقط sourceLanguage و targetLanguage را
    در LanguagePair فعال عوض می‌کند؛ هیچ تغییری در داده Concept/Content
    ایجاد نمی‌شود.
    ---------------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    ماژول توضیح -----------------------------------
    ----------------------------------- core ابزارهای مشترک، کلاس‌های
    پایه، DI Modules.

-   database Room Entities، DAOها، Database class، Migrationها.

-   domain مدل‌های دامنه، رابط‌های Repository، UseCaseها، منطق تجاری خالص.

-   data پیاده‌سازی Repositoryها، Mapperها، Import/Export،
    Backup/Restore.

-   ستون‌های ثابت ترجمه (مثل جایگزین با جدول Content امکان افزودن زبان
    جدید بدون spanish_text) تغییر Schema

-   ۳ تکمیل متدهای گم‌شده Repository

-   -   Compose UI → ViewModel → UseCase → Repository → DAO → Room.

-   -   SubmitReviewAnswer updates LearningState, DifficultyState and
        ReviewHistory atomically.

-   -   Statistics reads accepted ReviewHistory.

-   -   Progress reads LearningState and DifficultyState.

-   -   DifficultyState remains independently persisted.

-   -   Verify Hilt graph and Room migrations.

-   -   Runtime PASS must only be claimed after the actual repository
        passes the gates.

-   -   LearningState and DifficultyState remain independent.

-   -   LearningState + DifficultyState + ReviewHistory updates remain
        atomic.

-   -   Backup includes canonical entities including DifficultyState.

-   -   canonicalKey is derived from final Content.text.

-   -   ConceptTag restore is idempotent.

-   5.  Architecture / DI

-   -   Only canonical repository/use-case implementations may be
        registered.

-   -   No new Room schema change is introduced by this hardening
        package.

-   -   The next required operation is actual Android/Gradle/CI/runtime
        verification against the complete project repository.

-   -   Keep business rules out of DI modules.

-   -   Ensure canonical repositories, UseCases, database and ViewModels
        are wired through Hilt-compatible construction boundaries.

-   -   database owns Room Database, DAO and migration construction.

-   -   domain remains Android/Room/UI independent.

-   -   data implements domain repository contracts.

-   -   LearningState owns Stage, nextReviewAt, monthlyWrongCount,
        hasPathFailure and totals.

-   -   DifficultyState independently owns difficulty counters and
        hasReachedVeryHard.

-   -   Legacy LearningState.difficulty / SpacedRepetitionEngine paths
        must not be registered or invoked.

-   4.  Database Wiring

-   -   Use the already-defined FlashLearnDatabase and canonical DAOs.

-   -   Keep the existing Room schema and migration chain unchanged in
        this phase.

-   Proceed to End-to-End Integration: connect the complete runtime path
    from Compose UI through ViewModels, UseCases, repositories and Room,
    then execute the final cross-consistency and hardening audit before
    GitHub/Build.

-   -   Use the actual complete Android repository as the build source.

-   -   Repository structure validation.

-   -   Room/database/migration validation.

-   -   Integrate Phase 9 نسخهٔ مستندشده, Final DI نسخهٔ مستندشده, E2E
        نسخهٔ مستندشده and Audit/Hardening نسخهٔ مستندشده into the
        complete repository.

-   -   Upload the complete accumulated repository to GitHub and run the
        CI gates.

-   -   ReviewHistory is append-only and unique on (sessionId,
        reviewAttemptId).

-   -   Concept deletion is soft: active = false; review history is
        never deleted.

-   -   CreateConcept and SubmitReviewAnswer execute through the real
        Room transaction bridge.

-   class Converters { @TypeConverter fun fromUUID(value: UUID?):
    String? = value?.toString() @TypeConverter fun toUUID(value:
    String?): UUID? = value?.let(UUID::fromString) @TypeConverter fun
    fromInstant(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun toInstant(value: Long?): Instant? =
    value?.let(Instant::ofEpochMilli) }

-   ConceptEntity

-   @Entity(tableName = "concepts") data class ConceptEntity(
    @PrimaryKey val id: UUID, val entryType: String, val categoryId:
    UUID?, val favorite: Boolean, val active: Boolean, val createdAt:
    Instant, val updatedAt: Instant )

-   ContentEntity

-   @Entity( tableName = "contents", indices = \[ Index(value =
    \["conceptId", "languageCode"\], unique = true), Index(value =
    \["languageCode", "canonicalKey"\])\] ) data class ContentEntity(
    @PrimaryKey val id: UUID, val conceptId: UUID, val languageCode:
    String, val text: String, val canonicalKey: String, val notes:
    String?, val pronunciation: String?, val example: String? )

-   LearningStateEntity

-   @Entity( tableName = "learning_states", indices = \[ Index(value =
    \["conceptId"\], unique = true), Index(value = \["stage",
    "nextReviewAt"\])\] ) data class LearningStateEntity( @PrimaryKey
    val id: UUID, val conceptId: UUID, val stage: String, val
    nextReviewAt: Instant?, val monthlyWrongCount: Int, val
    hasPathFailure: Boolean, val totalCorrect: Int, val totalWrong: Int,
    val lastReviewedAt: Instant? )

-   DifficultyStateEntity

-   @Entity( tableName = "difficulty_states", indices = \[Index(value =
    \["conceptId"\], unique = true)\] ) data class
    DifficultyStateEntity( @PrimaryKey val id: UUID, val conceptId:
    UUID, val current: String, val consecutiveCorrect: Int, val
    consecutiveWrong: Int, val hasReachedVeryHard: Boolean )

-   TagEntity

-   @Entity(tableName = "tags") data class TagEntity(@PrimaryKey val id:
    UUID, val name: String)

-   ConceptTagEntity

-   @Entity( tableName = "concept_tags", primaryKeys = \["conceptId",
    "tagId"\], indices = \[Index(value = \["tagId", "conceptId"\])\] )
    data class ConceptTagEntity(val conceptId: UUID, val tagId: UUID)

-   ReviewSessionEntity

-   @Entity(tableName = "review_sessions") data class
    ReviewSessionEntity( @PrimaryKey val id: UUID, val startedAt:
    Instant, val endedAt: Instant?, val reviewType: String )

-   ReviewHistoryEntity

-   @Entity( tableName = "review_history", indices = \[Index(value =
    \["sessionId", "reviewAttemptId"\], unique = true)\] ) data class
    ReviewHistoryEntity( @PrimaryKey val id: UUID, val sessionId: UUID,
    val reviewAttemptId: UUID, val conceptId: UUID, val reviewedAt:
    Instant, val isCorrect: Boolean, val reviewType: String )

-   SettingsEntity

-   @Entity(tableName = "settings") data class SettingsEntity(
    @PrimaryKey val key: String, val value: String, val updatedAt:
    Instant )

-   3.  DAOs

-   ConceptDao

-   @Dao interface ConceptDao { @Insert(onConflict =
    OnConflictStrategy.ABORT) suspend fun insert(entity: ConceptEntity)
    @Update suspend fun update(entity: ConceptEntity) @Query("SELECT \*
    FROM concepts WHERE id = :id AND active = 1 LIMIT 1") suspend fun
    getById(id: UUID): ConceptEntity? @Query("SELECT \* FROM concepts
    WHERE active = 1") suspend fun getAllActive():
    List`<ConceptEntity>`{=html} @Query("UPDATE concepts SET active = 0,
    updatedAt = :now WHERE id = :id") suspend fun softDelete(id: UUID,
    now: Instant) }

-   ContentDao

-   @Dao interface ContentDao { @Insert(onConflict =
    OnConflictStrategy.ABORT) suspend fun insert(entity: ContentEntity)
    @Update suspend fun update(entity: ContentEntity) @Query("SELECT \*
    FROM contents WHERE id = :id LIMIT 1") suspend fun getById(id:
    UUID): ContentEntity? @Query("SELECT \* FROM contents WHERE
    conceptId = :conceptId AND languageCode = :languageCode LIMIT 1")
    suspend fun getByConceptIdAndLanguage(conceptId: UUID, languageCode:
    String): ContentEntity? @Query("SELECT \* FROM contents WHERE
    conceptId = :conceptId") suspend fun getAllByConceptId(conceptId:
    UUID): List`<ContentEntity>`{=html} @Query("SELECT \* FROM contents
    WHERE languageCode = :languageCode AND canonicalKey =
    :canonicalKey") suspend fun findByCanonicalKey(languageCode: String,
    canonicalKey: String): List`<ContentEntity>`{=html} }

-   LearningStateDao

-   @Dao interface LearningStateDao { @Query("SELECT \* FROM
    learning_states WHERE conceptId = :conceptId LIMIT 1") suspend fun
    getByConceptId(conceptId: UUID): LearningStateEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun
    upsert(entity: LearningStateEntity) @Query("SELECT \* FROM
    learning_states WHERE stage = :stage") suspend fun
    getAllByStage(stage: String): List`<LearningStateEntity>`{=html}
    @Query("""SELECT \* FROM learning_states WHERE stage = :stage AND
    nextReviewAt IS NOT NULL AND nextReviewAt \<= :now ORDER BY
    nextReviewAt ASC, conceptId ASC""") suspend fun getDueByStage(stage:
    String, now: Instant): List`<LearningStateEntity>`{=html}
    @Query("""SELECT \* FROM learning_states WHERE stage IN
    ('DAILY','WEEKLY','MONTHLY') AND nextReviewAt IS NOT NULL AND
    nextReviewAt \<= :now ORDER BY nextReviewAt ASC, conceptId ASC""")
    suspend fun getAllDueNonLearned(now: Instant):
    List`<LearningStateEntity>`{=html} }

-   DifficultyStateDao

-   @Dao interface DifficultyStateDao { @Query("SELECT \* FROM
    difficulty_states WHERE conceptId = :conceptId LIMIT 1") suspend fun
    getByConceptId(conceptId: UUID): DifficultyStateEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun
    upsert(entity: DifficultyStateEntity) @Delete suspend fun
    delete(entity: DifficultyStateEntity) }

-   ConceptTagDao

-   @Dao interface ConceptTagDao { @Insert(onConflict =
    OnConflictStrategy.IGNORE) suspend fun insert(entity:
    ConceptTagEntity) @Delete suspend fun delete(entity:
    ConceptTagEntity) @Query("SELECT tagId FROM concept_tags WHERE
    conceptId = :conceptId") suspend fun getTagIdsForConcept(conceptId:
    UUID): List`<UUID>`{=html} @Query("SELECT conceptId FROM
    concept_tags WHERE tagId = :tagId") suspend fun
    getConceptIdsForTag(tagId: UUID): List`<UUID>`{=html} }

-   ReviewHistoryDao

-   @Dao interface ReviewHistoryDao { @Insert(onConflict =
    OnConflictStrategy.ABORT) suspend fun insert(entity:
    ReviewHistoryEntity) @Query("SELECT \* FROM review_history WHERE
    conceptId = :conceptId") suspend fun getByConceptId(conceptId:
    UUID): List`<ReviewHistoryEntity>`{=html} @Query("SELECT \* FROM
    review_history WHERE sessionId = :sessionId") suspend fun
    getBySessionId(sessionId: UUID): List`<ReviewHistoryEntity>`{=html}
    @Query("SELECT EXISTS(SELECT 1 FROM review_history WHERE sessionId =
    :sessionId AND reviewAttemptId = :attemptId)") suspend fun
    existsByAttemptId(sessionId: UUID, attemptId: UUID): Boolean
    @Query("SELECT DISTINCT conceptId FROM review_history") suspend fun
    getDistinctConceptIds(): List`<UUID>`{=html} }

-   TagDao

-   @Dao interface TagDao { @Insert(onConflict =
    OnConflictStrategy.ABORT) suspend fun insert(entity: TagEntity)
    @Query("SELECT \* FROM tags WHERE id = :id LIMIT 1") suspend fun
    getById(id: UUID): TagEntity? }

-   ReviewSessionDao

-   @Dao interface ReviewSessionDao { @Insert(onConflict =
    OnConflictStrategy.ABORT) suspend fun insert(entity:
    ReviewSessionEntity) @Update suspend fun update(entity:
    ReviewSessionEntity) @Query("SELECT \* FROM review_sessions WHERE id
    = :id LIMIT 1") suspend fun getById(id: UUID): ReviewSessionEntity?
    }

-   SettingsDao

-   @Dao interface SettingsDao { @Query("SELECT value FROM settings
    WHERE key = :key LIMIT 1") suspend fun getString(key: String):
    String? @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun
    put(entity: SettingsEntity) }

-   4.  Room Database + Transaction Bridge

-   @Database( entities = \[ ConceptEntity::class, ContentEntity::class,
    LearningStateEntity::class, DifficultyStateEntity::class,
    TagEntity::class, ConceptTagEntity::class,
    ReviewSessionEntity::class, ReviewHistoryEntity::class,
    SettingsEntity::class\], version = CURRENT_SCHEMA_VERSION,
    exportSchema = true ) @TypeConverters(Converters::class) abstract
    class RoomFlashLearnDatabase : RoomDatabase() { abstract fun
    conceptDao(): ConceptDao abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao abstract fun
    difficultyStateDao(): DifficultyStateDao abstract fun tagDao():
    TagDao abstract fun conceptTagDao(): ConceptTagDao abstract fun
    reviewSessionDao(): ReviewSessionDao abstract fun
    reviewHistoryDao(): ReviewHistoryDao abstract fun settingsDao():
    SettingsDao } @Singleton class FlashLearnDatabaseImpl @Inject
    constructor( private val roomDb: RoomFlashLearnDatabase ) :
    FlashLearnDatabase { override suspend fun `<T>`{=html}
    withTransaction(block: suspend () -\> T): T = roomDb.withTransaction
    { block() } }

-   The exact numeric schema version is intentionally not invented here;
    it must match the real Android project.

-   5.  Mappers

-   object ConceptMapper { fun toDomain(e: ConceptEntity) = Concept(
    e.id, EntryType.valueOf(e.entryType), e.categoryId, e.favorite,
    e.active, e.createdAt, e.updatedAt ) fun toEntity(d: Concept) =
    ConceptEntity( d.id, d.entryType.name, d.categoryId, d.favorite,
    d.active, d.createdAt, d.updatedAt ) } object LearningStateMapper {
    fun toDomain(e: LearningStateEntity) = LearningState( e.id,
    e.conceptId, Stage.valueOf(e.stage), e.nextReviewAt,
    e.monthlyWrongCount, e.hasPathFailure, e.totalCorrect, e.totalWrong,
    e.lastReviewedAt ) fun toEntity(d: LearningState) =
    LearningStateEntity( d.id, d.conceptId, d.stage.name,
    d.nextReviewAt, d.monthlyWrongCount, d.hasPathFailure,
    d.totalCorrect, d.totalWrong, d.lastReviewedAt ) } object
    DifficultyStateMapper { fun toDomain(e: DifficultyStateEntity) =
    DifficultyState( e.id, e.conceptId,
    VocabularyDifficulty.valueOf(e.current), e.consecutiveCorrect,
    e.consecutiveWrong, e.hasReachedVeryHard ) fun toEntity(d:
    DifficultyState) = DifficultyStateEntity( d.id, d.conceptId,
    d.current.name, d.consecutiveCorrect, d.consecutiveWrong,
    d.hasReachedVeryHard ) } object ConceptTagMapper { fun toDomain(e:
    ConceptTagEntity) = ConceptTag(e.conceptId, e.tagId) fun toEntity(d:
    ConceptTag) = ConceptTagEntity(d.conceptId, d.tagId) } object
    ReviewHistoryMapper { fun toDomain(e: ReviewHistoryEntity) =
    ReviewHistory( e.id, e.sessionId, e.reviewAttemptId, e.conceptId,
    e.reviewedAt, e.isCorrect, ReviewType.valueOf(e.reviewType) ) fun
    toEntity(d: ReviewHistory) = ReviewHistoryEntity( d.id, d.sessionId,
    d.reviewAttemptId, d.conceptId, d.reviewedAt, d.isCorrect,
    d.reviewType.name ) }

-   ContentMapper and Settings/Tag/ReviewSession mappers follow the same
    thin conversion pattern; no business logic belongs in mappers.

-   -   Migration must support the DifficultyState schema including
        hasReachedVeryHard.

-   -   Migration must ensure contents.canonicalKey exists and is
        populated according to the v4 normalization rule.

-   -   Backfilling DifficultyState is allowed only where required by
        the actual prior schema/data contract.

-   -   Exact from/to version numbers must be taken from the actual
        project schema; no version number is invented in this Phase 3
        specification.

-   □ Room database opens and exported schema is generated.

-   □ UUID and Instant converters round-trip correctly.

-   □ Concept uses entryType and maps to EntryType.

-   □ Content uniqueness on (conceptId, languageCode) is enforced.

-   □ DifficultyState is independent from LearningState.

-   □ ConceptTag duplicate insert is idempotent.

-   □ FlashLearnDatabaseImpl executes real Room withTransaction.

-   □ In-memory integration tests cover CreateConcept and
    SubmitReviewAnswer atomicity.

-   نسخهٔ مستندشده --- Room/Schema Corrections

-   Theme Architecture + Hard-coded Appearance Consolidation ---
    COMPLETE

-   2.  ThemeSpec Architecture

-   No Room migration is required for نسخهٔ مستندشده. Theme
    customizations remain presentation preferences and do not modify
    Concept, Learning State, Concept Difficulty, Review History, or
    scheduling data.

-   Static source validation was completed on the supplied working
    source. The supplied repository snapshot does not contain an
    executable Gradle wrapper or a local Gradle installation, so this
    artifact does not claim an Android build result. GitHub Actions
    remains the authoritative build verification path.

-   PASS --- No database/schema changes are introduced.

-   Source archive: بستهٔ تاریخی

-   نسخهٔ مستندشده establishes the localization and accessibility
    architecture and applies it to the highest-value navigation,
    add-word, and settings controls. Localization remains
    presentation-only and does not affect learning state, difficulty,
    review history, scheduling, or database schema.

-   Locale handling is isolated from domain/data modules.

-   Interactive navigation/save/settings icons use meaningful content
    descriptions.

-   No Room schema change.

-   No Concept Difficulty change.

-   No content mutation caused by locale changes.

-   The current نسخهٔ مستندشده artifact does not claim that every
    existing hard-coded Persian sentence across every screen has already
    been migrated to resources. The architecture and core/high-value
    migration are complete; an exhaustive string-by-string localization
    sweep remains part of the later full integration audit if required.

-   Help is presentation and educational content; it is not a second
    learning engine and does not own learning-state persistence.

-   2.  Architecture

-   Settings provides the user-facing entry point; the Help surface owns
    explanatory content and returns through normal back navigation.

-   3.  Content Contract

-   Help explains the application's major user flows and the purpose of
    its core screens using user-facing instructional content.

-   Content follows the localization/resource architecture rather than
    introducing a parallel text mechanism.

-   No Learning State, Concept Difficulty, Review History, or scheduling
    mutation.

-   PASS --- Help-facing text follows the established localization
    architecture.

-   No Room schema change required for the presentation layer.

-   Audio playback does not alter answer identity, scoring semantics,
    review history, difficulty, or scheduling.

-   نسخهٔ مستندشده audits integration among localization, accessibility,
    Help, TTS, Settings, navigation, and the existing learning
    architecture.

-   Existing Library, Add Word, Review, and Settings flows remain
    independent of Help content.

-   User-visible additions follow the existing localization
    architecture.

-   No presentation-only action mutates Learning State, Concept
    Difficulty, Review History, or scheduling.

-   No locale operation rewrites stored learning content.

-   Repeated TTS replay must not change the current question or answer
    identity.

-   نسخهٔ مستندشده establishes GitHub Actions as the repository-level
    build/test verification path, consistent with the project's
    GitHub-first workflow.

-   2.  CI Architecture

-   The repository state is the authoritative build input.

-   No database migration is introduced solely for CI.

-   PASS --- CI workflow configuration is represented in the release
    architecture.

-   نسخهٔ مستندشده freezes the release-candidate architecture after
    localization/accessibility, Help, TTS, integration, testing, and CI
    preparation.

-   No localization change may rewrite stored learning content.

-   PASS --- Release-candidate architecture preserves the نسخهٔ مستندشده
    boundaries.

-   This final section consolidates the documented contracts from نسخهٔ
    مستندشده through نسخهٔ مستندشده while preserving the complete
    original نسخهٔ مستندشده content above.

-   Learning state, difficulty, review history, and scheduling remain
    governed by their existing architecture.

-   The presentation stages introduce no required Room schema migration.

-   Quiz presentation must not redefine the persisted identity of the
    correct answer.

-   The repository is prepared for GitHub-based verification.

## نسخهٔ تاریخی قابلیت‌ها و UI/UX

-   ¿qué quieres?

-   quiero: می‌خواهم (از querer)

-   The نسخهٔ مستندشده Freeze Declaration claimed 12/12 PASS on the
    Three-Way Audit. However, review of the actual FINAL/UPDATED files
    showed that the 7 corrections listed in سند/فایل تاریخی were
    recommended but not fully propagated into the master documents. This
    report records what was verified and fixed in this revision (v4).

-   These require discussion before any change is applied, per project
    methodology:

-   هیچ‌کدام از این فایل‌ها ادعای Build/CI PASS ندارند؛ این مجموعه نتیجه
    Static Audit و اصلاحات پیش از Build است. GitHub و Build عمداً برای
    مرحله آخر نگه داشته شده‌اند.

-   Checks: 12 \| PASS: 12 \| DOCUMENTATION LEGACY FLAG: 0 \| RED
    IMPLEMENTATION BLOCKERS: 0 (re-verified after Descriptions نسخهٔ
    مستندشده fix --- see Section 5) (re-confirmed after applying
    Corrections for SpacedRepetitionEngine residual and
    SubmitReviewAnswer summary)

-   PASS --- Random Review excludes LEARNED

-   PASS --- SubmitReviewAnswer shared/transactional contract

-   PASS --- Code atomically persists three review artifacts

-   No external knowledge or web research was used. No uncertain legacy
    material was silently deleted. The Code document is a reconstructed
    contract-aligned code specification, not a claim of compile/build
    validation.

-   5.  Pending Review --- Descriptions

-   -   Achievement evaluation is isolated from UI.

-   -   AchievementUnlockEvent is not required for V1.

-   -   Review transition remains owned by the Review/Transition Engine.

-   Hilt remains the single runtime DI mechanism. FlashLearnApplication
    uses

    1.  MainActivity has no injected dependencies at this stage, so
        @AndroidEntryPoint is not required yet.

-   This phase performs source-level DI wiring correction. A real Gradle
    compile/runtime verification is intentionally deferred to the final
    audit because the project plan postpones GitHub upload/build until
    all phases are complete.

-   FlashLearn --- Phase 8 App/UI + Navigation

-   Home, Review, Progress, and Settings are declared with stable
    routes.

-   Navigation boundary

-   UI state

-   AppUiState contains only the currently selected route; it does not
    duplicate persisted learning, difficulty, or progress state.

-   Build status

-   بستهٔ تاریخی

-   سند/فایل تاریخی

-   Checkpoint status: statically integrated; Build/Runtime verification
    intentionally deferred.

-   -   LEARNED is handled as an independent review flow.

-   -   StartReviewSessionUseCase and EndReviewSessionUseCase persist
        and close review sessions safely.

-   Proceed to Settings / Difficulty / Progress, then final DI/service
    wiring, UI/navigation, end-to-end integration, and only at the end
    Compile/Runtime audit, GitHub and Build.

-   -   GitHub and Build remain deferred.

-   Proceed to final Dependency Injection / Service Wiring audit, then
    Application/UI and Navigation integration.

-   LEARNED به‌طور خودکار وارد چرخه عادی نمی‌شود؛ فقط از طریق مرور اختیاری
    صریح کاربر قابل دسترس است و آن مرور Stage/Difficulty/nextReviewAt را
    تغییر نمی‌دهد.

-   نسخهٔ تاریخی قانون Random Review

-   در V1 مقدار threshold_difficulty برابر 3 است و UI برای تغییر آن وجود
    ندارد. امکان تنظیم توسط کاربر به آینده موکول شده است.

-   وضعیت این تنظیم هنوز به‌طور کامل قطعی نیست: فایل کوتاه آن را هم
    «قابلیت توسعه آینده» و هم دارای «مقدار پیش‌فرض قطعی ۳» توصیف کرده
    بود؛ روشن نیست آیا خودِ امکان تغییردادن این عدد در نسخه فعلی برای
    کاربر در دسترس است یا فقط زیرساخت دیتابیسی آن آماده و ورودی UI آن به
    آینده موکول شده. این مورد در بخش ۲۱ (تصمیم‌های باز) دوباره فهرست شده
    است.

-   شرط اصلی واجد‌شرایط بودن: nextReviewAt \<= now.

-   اصلاح یکپارچه‌سازی: فایل کامل دو نسخه متفاوت از این ترتیب‌بندی را در
    جاهای مختلف آورده بود: نسخه قدیمی‌تر بر پایه اصطلاحات Anki («Learning
    → Relearning → Review → New») و نسخه اصلاح‌شده بعدی بر پایه همین چهار
    Stage خودِ FlashLearn (Daily→Weekly→Monthly→Learned). طبق تغییرات
    ثبت‌شده در تاریخچه نسخه («اصلاح ترتیب مرور به روزانه → هفتگی →
    ماهانه»)، نسخه دوم (این جدول) نهایی و معتبر است؛ نسخه مبتنی بر
    اصطلاحات Anki صرفاً باقیمانده‌ای از یک پیش‌نویس قدیمی‌تر بود و حذف شد.

-   نسخهٔ تاریخی جلسه مرور (Review Session)

-   هر پاسخ Accepted دارای reviewAttemptId یکتا در محدوده (sessionId,
    reviewAttemptId) است؛ دکمه پاسخ پس از ثبت باید غیرفعال شود تا کلیک
    تکراری، پاسخ تکراری ثبت نکند.

-   جزئیات بصری/تعاملی Gamification (نمایش دقیق Achievement، انیمیشن باز
    شدن آن، صفحه اختصاصی) در هیچ‌کدام از دو فایل مشخص نشده و باید در
    مرحله طراحی UI تکمیل شود.

-   الگوریتم نهایی --- Quiz Question Generation

-   GenerateQuizQuestion

-   QuizQuestion(

-   FlashcardFallback

-   قید خروجی QuizQuestion:

-   return FlashcardFallback

-   زیرا برای ساخت Quiz چهارگزینه‌ای

-   return QuizQuestion(

-   7.  Review Schedule را تغییر نمی‌دهد.

-   فرآیند Review انتخاب شده و اکنون نوبت ساخت سؤال آن است.

-   13. گزینه‌های Quiz نباید Duplicate باشند.

-   14. QuizQuestion همیشه دقیقاً ۴ گزینه دارد.

-   تضمین اینکه خروجی Quiz همیشه دقیقاً ۴ گزینه دارد یا به
    FlashcardFallback برمی‌گردد

-   حفظ استقلال کامل از Difficulty Calculation، Stage و Review
    Scheduling

-   • nextReviewAt

-   O. Review Modes Correction & Backlog Items

-   1.  Vocabulary Difficulty و Quiz Difficulty

-   • Quiz Difficulty سختی سؤال چهارگزینه‌ای از نظر کیفیت و شباهت
    Distractorها است و سه سطح دارد: EASY / MEDIUM / HARD.

-   2.  Quiz Difficulty --- Distractor Selection

-   • خروجی باید دقیقاً 4 گزینه معتبر و یکتا داشته باشد؛ در غیر این صورت
    FlashcardFallback.

-   5.  Random Review --- Eligibility نهایی

-   • nextReviewAt \<= now

-   • LEARNED در Random Review اصلی نیست.

-   8.  Review Session و Atomicity

-   • Flashcard و Quiz هر دو از SubmitReviewAnswerUseCase مشترک استفاده
    می‌کنند.

-   1.  یک بلوک Review Scheduling که دقیقاً تکراری بود حذف شد؛ هیچ مورد
        مشکوکی خودکار حذف نشده است.

-   2.  Random Review با قرارداد نهایی هم‌راستا شد: DAILY/WEEKLY/MONTHLY
        و فقط Due؛ LEARNED در Mode مستقل.

-   3.  Learning Transition به TransitionResult شامل newStage،
        nextReviewAt، hasPathFailure و monthlyWrongCount ارتقا یافت.

-   7.  Due Rule برابر nextReviewAt \<= now باقی ماند.

-   Pending Manual Review:

-   
      Pe   nding Review --- بدون حذف خودکار:
      ---- ---------------------------------------------------------------------------
           مرحله فعلی پاسخ مرحله بعدی nextReviewAt اثر روی Difficulty سایر شمارنده‌ها

         DAILY صحیح WEEKLY اکنون + ۷ روز بدون تغییر totalCorrect +۱
      -- ------------------------------------------------------------
         اولویت نوع مرور شرط

    ۱ DAILY stage='DAILY' AND nextReviewAt\<=now

-   ۲ WEEKLY stage='WEEKLY' AND nextReviewAt\<=now

-   ۳ MONTHLY stage='MONTHLY' AND nextReviewAt\<=now

-   ## ۴ LEARNED فقط با انتخاب صریح کاربر (reviewType='LEARNED')

-   MEMORY_BUILDER ۱۰۰ کلمه Learned

-   6.  SubmitReviewAnswer --- Single Transaction / Single Source of
        Truth

-   7.  Review Selection Contract

-   نسخهٔ تاریخی مدیریت وضعیت UI (UI State Management --- MVI سبک) 4

-   ۱۲. UI/UX و Design System 6

-   نسخهٔ تاریخی قوانین UI/UX (فهرست کامل و شماره‌گذاری‌شده) 6

-   نسخهٔ تاریخی حالت‌های مرور در UI 7

-   ۱۳. Navigation و Onboarding 7

-   ۱۸. راهنمای توسعه‌دهندگان و Build 9

-   A. تصمیمات معماری و یکپارچگی Review 14

-   I. Test Strategy -- قبل از UI Integration 15

-   FlashLearn --- نسخهٔ مستندشده Unified Corrections & Final
    Requirements 17

-   4.  Review Help --- Hint و Show Note 17

-   6.  Settings 17

-   7.  About Page 17

-   About Page باید شامل: 17

-   زمان‌بندی مرور بر پایه شرط nextReviewAt \<= now است.

-   presentation: ViewModel و UiState

-   ui: Theme و کامپوننت‌های مشترک

-   navigation: Routes و NavGraph

-   Scheduler/Review Engine مستقل از UI و پایگاه داده طراحی می‌شود.

-   نسخهٔ تاریخی مدیریت وضعیت UI (UI State Management --- MVI سبک)

-   UiState کاملاً immutable است؛ برای تغییر وضعیت یک کپی جدید ساخته
    می‌شود.

-   Learning Transition → stage, nextReviewAt, monthlyWrongCount,
    hasPathFailure

-   ۱۲. UI/UX و Design System

-   Compose + Material 3؛ طراحی مستقل و غیرکپی از Duolingo یا
    اپلیکیشن‌های مشابه. پشتیبانی کامل از Light و Dark Theme.

-   اصلاح یکپارچه‌سازی: فایل کامل یک لایه رنگی دقیق‌تر و مجزا برای
    Difficulty و برای Stage تعریف کرده بود که در فایل کوتاه نبود (فایل
    کوتاه فقط Due و Learned را جدا می‌کرد). این لایه دقیق‌تر و جدیدتر است
    و در این نسخه به‌عنوان مکمل رنگ‌های عمومی بالا نگه داشته شده، نه
    جایگزین آن‌ها؛ رنگ‌های عمومی برای Chromeی کلی UI و این دو گروه برای
    نشان‌دادن وضعیت کلمه استفاده می‌شوند.

-   نسخهٔ تاریخی قوانین UI/UX (فهرست کامل و شماره‌گذاری‌شده)

-   3.  اعداد Hardcoded در UI ممنوع است؛ تمام اعداد باید از UiState
        خوانده شوند.

-   نسخهٔ تاریخی حالت‌های مرور در UI

-   در حالت چهارگزینه‌ای (Quiz)، گزینه‌های غلط بر اساس Quiz Difficulty و
    قواعد نهایی انتخاب Distractor تولید می‌شوند؛ Quiz Difficulty مستقل از
    Vocabulary Difficulty است و Category/شباهت معنایی و زبانی می‌تواند
    برای افزایش سختی گزینه‌ها استفاده شود.

-   ۱۳. Navigation و Onboarding

-   جزئیات کامل صفحات Onboarding و نقشه دقیق Navigation Graph در هیچ‌کدام
    از دو فایل به‌صورت مجزا نیامده بود؛ این بخش در مرحله طراحی UI باید
    تکمیل شود.

-   ایندکس‌گذاری بر اساس کوئری‌های پرکاربرد (مثل (stage, nextReviewAt)).

-   ۱۸. راهنمای توسعه‌دهندگان و Build

-   این مقادیر همان مقادیر مندرج در سند مبناست؛ اگر با فایل‌های واقعی
    پروژه فرق دارد، باید با نسخه واقعی Build فعلی هماهنگ شود --- این سند
    عمداً مقدار جدیدی را جایگزین آن‌ها نمی‌کند.

-   Presentation: مسیر جدید در navigation/Routes؛ ViewModel متناظر؛ صفحه
    Compose متناظر؛ افزودن مسیر به FlashLearnNavGraph.

-   تنظیمات دقیق Gradle و GitHub Actions (بخش ۲۸ فهرست اصلی) در هیچ‌کدام
    از دو فایل داده نشده بود؛ طبق قانون کلی پروژه («فعلاً وارد
    Build/GitHub نشویم مگر با درخواست صریح») این بخش عمداً کامل نشد و
    باید هنگام رسیدن به مرحله واقعی Build/CI تعریف شود.

-   2.  Transition Engine تنها مرجع تعیین Stage/Difficulty/nextReviewAt
        است.

-   15. زمان‌بندی بر پایه nextReviewAt \<= now است.

-   مرحله NEW به‌طور کامل حذف شد؛ مرور اختیاری LEARNED هیچ تغییری در
    Stage/Difficulty ایجاد نمی‌کند؛ nextReviewAt پس از پاسخ غلط برابر «+۱
    روز» است (نه فوری)؛ Difficulty مستقل از Stage و با آستانه قابل‌تنظیم
    (پیش‌فرض ۳) به‌علاوه استثنای Monthly تعریف شد.

-   6.  Settings

-   Settings حداقل باید شامل موارد زیر باشد:

-   • Maximum Review Cards / حداکثر تعداد کارت یا کلمه در هر Review
    Session.

-   • Theme / Color Scheme / طرح‌های رنگی برنامه.

-   threshold_difficulty در V1 برابر 3 و بدون UI قابل تنظیم است. V1
    محدودیت روزانه برای New Word ندارد.

-   7.  About Page

-   About Page باید شامل:

-   • Learning Transition باید Pure/Deterministic باشد و
    TransitionResult شامل newStage، nextReviewAt، hasPathFailure و
    monthlyWrongCount باشد.

-   • Due rule در کل Specification: nextReviewAt \<= now.

-   • GenerateQuizQuestion: Read-Only.

-   • SubmitReviewAnswerUseCase: مسیر مشترک ثبت پاسخ Flashcard/Quiz.

-   • Quiz Difficulty مستقل از Vocabulary Difficulty.

-   • Quiz دقیقاً 4 گزینه معتبر و یکتا یا FlashcardFallback.

-   • threshold_difficulty = 3 در V1 و بدون UI.

-   • Quiz Difficulty = EASY / MEDIUM / HARD.

-   • Random Review فقط Due/Eligible و غیر-LEARNED را انتخاب می‌کند.

-   □ Vocabulary Difficulty و Quiz Difficulty مستقل‌اند.

-   □ Random Review فقط Due/Eligible را انتخاب می‌کند.

-   □ WEEKLY/MONTHLY آینده‌موعد در Random Review وارد نمی‌شوند.

-   □ Maximum Review Cards و Theme/Color Scheme در Settings تعریف
    شده‌اند.

-   □ About Page و اجزای آن تعریف شده‌اند.

-   □ threshold_difficulty = 3 و بدون UI در V1 است.

-   □ Due rule = nextReviewAt \<= now است.

-   □ Quiz Generator هیچ Stateای را تغییر نمی‌دهد.

-   • Random Review اصلی: فقط DAILY/WEEKLY/MONTHLY واجد شرایط؛ LEARNED
    در Mode مستقل.

-   • Due Rule: nextReviewAt \<= now.

-   • threshold_difficulty: مقدار V1 برابر 3 و بدون UI برای تغییر.

-   • Learning / Review Behavior --- وضعیت: نهایی. Stageها، Due Rule و
    منطق کلی مرور در سطح توضیحات تثبیت شده‌اند.

-   • Statistics / Gamification --- وضعیت: توضیحات محصول تکمیل شده؛
    جزئیات صرفاً UI/Presentation که در Scope V1 نیستند نباید به‌عنوان
    الزام Domain اضافه شوند.

-   • Settings / About --- وضعیت: تکمیل در حد Scope توضیحات V1؛ جزئیات
    ظاهری UI در صورت نیاز باید در طراحی UI مشخص شوند، نه با تغییر قواعد
    Domain.

-   PENDING ITEMS --- MANUAL REVIEW ONLY

-   Pending Items: در حال حاضر موردی که با اطمینان کافی نیازمند حذف
    خودکار باشد وجود ندارد؛ بررسی‌های مشکوک فقط به‌صورت Manual Review
    انجام می‌شوند. -
    -----------------------------------------------------------------------
    جزء توضیح -----------------------------------
    ----------------------------------- UiState یک data class شامل تمام
    داده‌های موردنیاز صفحه؛ تمام فیلدها immutable هستند.

-   ViewModel دریافت Intent از UI، اجرای منطق از طریق UseCaseها، تولید
    UiState جدید و انتشار آن از طریق StateFlow.

-   Compose UI مصرف UiState از StateFlow و نمایش آن؛ ارسال Intent به
    ViewModel در پاسخ به رویدادهای کاربر.
    -----------------------------------------------------------------------

-   ReviewSession id، startedAt، endedAt، جلسه مرور با شناسه reviewType
    یکتا، مثلاً 2026-08-16-001.

-   ۲ تغییر منطقه زمانی nextReviewAt به‌صورت Unix Timestamp (میلی‌ثانیه از
    ۱۹۷۰) ذخیره می‌شود، پس مستقل از Timezone است.

-   ۶ کلیک پیاپی روی دکمه با reviewAttemptId یکتا در پاسخ (sessionId,
    reviewAttemptId) از ثبت تکراری جلوگیری می‌شود؛ دکمه بعد از اولین کلیک
    غیرفعال می‌شود.

-   presentation ViewModelها، UiStateها، صفحه‌های Compose.

-   ui تم‌ها، کامپوننت‌های مشترک، انیمیشن‌ها.

-   navigation Routes، NavGraph، Navigation Extensions.
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    \# موضوع تصمیم نهایی V1 -----------------------
    ----------------------- ----------------------- ۱ خروج از Review
    Session پاسخ‌های ثبت‌شده حفظ ناتمام می‌شوند؛ پاسخ ثبت‌نشده ایجاد نمی‌شود؛
    Session می‌تواند با endedAt=null باقی بماند؛ Resume دقیق موقعیت Queue
    در V1 الزامی نیست و Session بعدی جدید است.

-   ۲ threshold_difficulty مقدار V1 برابر 3 است. زیرساخت تنظیم وجود
    دارد، اما تغییر این مقدار توسط کاربر در UI نسخه V1 ارائه نمی‌شود.

-   ۷ افزوده شدن مدیریت وضعیت UI (UI State Management)

-   ۵ تکمیل مسیرهای Navigation

-   -   Navigation remains outside domain/data business logic.

-   -   ViewModels orchestrate and do not implement review-transition
        rules.

-   2.  Review E2E Path

-   -   Flashcard and Quiz use the same canonical SubmitReviewAnswer
        path.

-   -   Review transition and scheduling remain owned by the Review
        Engine.

-   3.  Post-Review Consumers

-   -   Neither Statistics nor Progress mutates review state.

-   -   Verify app launch and primary navigation.

-   -   Verify Flashcard and Quiz answer submission.

-   -   No Android/Gradle build was executed in this environment.

-   Proceed to Final Cross-Consistency Audit and Hardening. After that,
    execute the real Android/Gradle and instrumentation gates, then
    perform the final GitHub/build stage.

-   -   Review transition remains centralized.

-   3.  Transaction and Review Integrity

-   -   SubmitReviewAnswer remains the single answer-submission path.

-   -   Domain remains independent of UI/navigation.

-   -   Missing required state is surfaced rather than synthesized.

-   -   Build/runtime status is kept separate from static-audit status.

-   -   Only after those gates pass may final Build PASS and GitHub
        release status be claimed.

-   -   presentation receives UseCases/repositories and only
        orchestrates UI state.

-   -   navigation remains outside domain/data and owns stable
        application routes.

-   -   SubmitReviewAnswer remains the shared Flashcard/Quiz answer
        path.

-   -   Review ViewModel delegates answer submission to the canonical
        UseCase.

-   6.  Navigation Boundary

-   -   Stable primary routes remain Home, Review, Progress and
        Settings.

-   -   Navigation does not leak into domain/data.

-   -   The real Android project, CI and instrumentation gates remain
        required before final PASS.

-   -   Do not claim a successful build from static preparation alone.

-   -   Compile/build.

-   -   Instrumentation/UI tests when configured.

-   3.  Build Commands

-   -   Only after successful gates should final Build PASS be declared.

-   □ Duplicate (sessionId, reviewAttemptId) is rejected.

-   Accent colors are now versioned inside FlashLearnThemeSpec for
    Purple, Blue, Green, Orange, and Pink, with light/dark values.

-   Semantic appearance colors for on-primary, success, warning, and
    error are versioned inside FlashLearnThemeSpec.

-   The existing ThemeSpec JSON serialization now persists the new
    appearance fields.

-   Imported JSON that does not contain the new fields falls back to
    safe defaults, preserving compatibility with existing custom themes.

-   The ThemeSpec format remains explicitly versioned.

-   3.  Theme Runtime

-   FlashLearnTheme now resolves accent colors entirely from the
    selected ThemeSpec.

-   Material 3 primary/secondary/tertiary and error semantics are
    derived from the selected theme specification.

-   FlashLearnThemeTokens continues to expose presentation-level
    semantic tokens to screens.

-   Theme remains independent from business logic and learning data.

-   4.  UI Consolidation

-   LibraryScreen no longer owns local hard-coded color palettes.

-   Library difficulty/status colors now derive from semantic theme
    tokens.

-   ReviewScreen no longer owns local hard-coded quiz/review color
    palettes.

-   Review selection, correct, wrong, and difficulty presentation now
    derives from MaterialTheme semantic colors.

-   A static source scan found no direct Color(0x...) declarations
    outside the app's ui/theme package.

-   FlashLearnThemeSpecTest.builtInThemesRoundTripAllVersionedAppearanceFields

-   FlashLearnThemeSpecTest.themeFormatVersionIsExplicitAndStable

-   PASS --- ThemeSpec owns the appearance palette and semantic status
    colors.

-   PASS --- Theme JSON round-trips the new appearance fields.

-   PASS --- Library and Review screens do not define independent
    hard-coded color palettes.

-   PASS --- Source audit completes without direct Color(0x...)
    declarations outside ui/theme.

-   Localization + Accessibility --- COMPLETE STAGE OUTPUT

-   2.  Localization

-   Added FlashLearnLocales presentation helper with supported Persian
    and English locales and safe normalization.

-   Core Add Word labels, navigation, save action, and Settings
    title/actions use string resources.

-   Localized accessibility labels were added for selected interactive
    icons.

-   3.  Accessibility

-   Accessibility wording is localized through Android resources for the
    migrated controls.

-   No accessibility behavior is coupled to business logic.

-   No Review History behavior change.

-   PASS --- Localization helper exists in the UI/presentation layer.

-   PASS --- Targeted Add Word and Settings screens compile structurally
    against Android string resources.

-   PASS --- No Gradle wrapper executable/JAR is present in the supplied
    source snapshot; therefore no local Android build is claimed.

-   نسخهٔ مستندشده → Education / Help

-   FlashLearn --- Education / Help --- Stage Output

-   نسخهٔ مستندشده introduces the Education / Help layer while preserving
    the existing learning, review, difficulty, persistence, and
    scheduling boundaries established by نسخهٔ مستندشده.

-   A dedicated Help destination is integrated into the existing
    navigation structure.

-   No Help action is permitted to mutate learning records.

-   4.  Localization / Accessibility

-   New Help-facing strings use the established resource-based
    localization boundary.

-   Interactive navigation controls retain meaningful accessible
    descriptions where an icon or non-text control requires one.

-   Help navigation is presentation-only.

-   PASS --- Help destination and Settings entry are explicitly defined.

-   PASS --- Help does not own domain persistence.

-   PASS --- Back navigation preserves existing navigation boundaries.

-   نسخهٔ مستندشده → TTS / Pronunciation Implementation

-   FlashLearn --- TTS / Pronunciation Implementation --- Stage Output

-   Every audio quiz question is ALWAYS Spanish.

-   3.  TTS Runtime

-   Spanish is the required TTS locale for audio quiz questions.

-   TTS remains a presentation consumer of quiz data.

-   TTS and quiz-language preferences remain configuration/presentation
    state and must not mutate learning records, review history,
    difficulty, or scheduling.

-   Spanish remains the sole language of every audio quiz question.

-   PASS --- Spanish is the TTS locale for audio questions.

-   PASS --- TTS is isolated from learning-state mutation.

-   2.  Navigation Integration

-   Settings → Help remains presentation navigation.

-   Back navigation does not create or remove learning records.

-   3.  Localization / Accessibility Integration

-   Accessibility descriptions remain UI metadata and do not become
    domain data.

-   4.  TTS Integration

-   TTS consumes the current quiz question as presentation input.

-   The fixed Spanish-audio contract remains true in both quiz modes.

-   Answer evaluation remains governed by quiz/domain data rather than
    the speech engine.

-   PASS --- Navigation boundaries are explicit.

-   PASS --- Localization and accessibility remain presentation
    concerns.

-   This is an integration/source audit; no runtime result is invented
    without an actual executed build/test artifact.

-   Navigation tests cover Help entry and return.

-   Persistence tests cover the Spanish quiz-language mode.

-   Existing learning-state and review behavior remains
    regression-sensitive.

-   Repeated Help entry/exit must not duplicate navigation state.

-   Switching quiz modes must not change the Spanish audio-question
    contract.

-   Locale/settings changes must not mutate learning records.

-   No learning-state reset caused by presentation settings.

-   No review-history mutation caused by TTS replay.

-   No scheduling change caused by presentation settings.

-   PASS --- Source verification is distinguished from runtime CI
    execution.

-   The Spanish TTS question-language contract remains unchanged.

-   PASS --- CI is defined as the authoritative build/test boundary.

-   BOUNDARY --- Actual CI PASS requires an executed GitHub Actions
    result.

-   Help remains presentation-only.

-   TTS remains presentation-only.

-   Every audio quiz question remains Spanish; Word Recognition uses
    Spanish options and Meaning Recognition uses Persian options.

-   No presentation setting may mutate review history or scheduling.

-   PASS --- Help, TTS, and Settings integrations remain separated from
    learning persistence.

-   BOUNDARY --- Remote CI success and device TTS availability require
    actual execution evidence.

-   Localization and accessibility are presentation concerns.

-   Education / Help is presentation-only.

-   TTS is presentation-only and every audio quiz question is Spanish.

-   Locale, Help, accessibility, and TTS interactions must not mutate
    learning records.

-   Observed Android build/test success requires an actual GitHub
    Actions result.

-   Device-specific Spanish TTS availability requires runtime/device
    verification.

-   PASS --- نسخهٔ مستندشده Help contract documented.

-   PASS --- نسخهٔ مستندشده Spanish-audio TTS contract documented.

-   PASS --- No unsupported runtime/build result is represented as
    observed fact.

-   This document is the integrated specification/certification record.
    Production release certification still depends on the actual GitHub
    Actions execution result and any required Android runtime/device
    verification.

## نسخهٔ تاریخی Import و داده

-   سیستم Import باید بتواند متن‌های نامنظم و ترکیبی را دریافت کرده و
    به‌صورت خودکار:
-   2.  اصل بنیادی Parser
-   Parser نباید بر اساس یک Regex بزرگ ساخته شود.
-   Translation Detection
-   Breakdown Detection
-   Notes / Grammar Detection
-   Duplicate Detection
-   Parser باید یک State Machine چندمرحله‌ای باشد.
-   از نظر Parser معادل:
-   TRANSLATION
-   BREAKDOWN
-   NOTE
-   GRAMMAR_NOTE
-   9.  TRANSLATION
-   10. BREAKDOWN
-   Breakdown توضیح اجزای یک Entry است.
-   ├── Breakdown 1
-   ├── Breakdown 2
-   └── Breakdown 3
-   11. NOTE
-   PASS --- canonicalKey present
-   3.  Important Scope / Non-Claims
-   -   Read-only threshold_difficulty access with canonical default 3.
-   اصلاح یکپارچه‌سازی: فایل کوتاه کلید ذخیره این تنظیم را
    «threshold_difficulty» و فایل کامل آن را «threshold_difficulty»
    نوشته بود. این دو فایل با هم مغایرت داشتند. در این نسخه نام
    «threshold_difficulty» به‌عنوان مرجع انتخاب شد (الگوی «موجودیت_ویژگی»
    با سایر کلیدهای app_setting مثل backup_encryption_enabled هماهنگ‌تر
    است)؛ در پیاده‌سازی واقعی همین یک نام باید استفاده شود.
-   ۸. مدیریت واژگان و Paste Text Parser
-   Parenthetical text فقط در صورت تشخیص قطعی Note به notes منتقل می‌شود؛
    پرانتزهای حاوی اطلاعات ترجمه/جنسیت یا داده معنادار نباید خودکار حذف
    شوند.
-   اصلاح یکپارچه‌سازی: در بخش ۱۶ فایل کامل، فقط عنوان «۱۶-۱. Paste Text
    Parser» بدون هیچ محتوایی رها شده بود. جزئیات بالا از فهرست قابلیت‌های
    فایل کوتاه (بخش ۳-۱) و از قوانین Edge Case مرتبط با Import بازسازی
    شدند؛ ولی جزئیات دقیق‌تر پارسر (نظیر قواعد کامل تشخیص شماره
    فارسی/عربی، فرمت‌های چندخطی ورودی) در هیچ‌کدام از دو فایل موجود نبود و
    باید در مرحله پیاده‌سازی واقعی از نو مشخص شود.
-   ۹. Import / Export / Backup / Restore
-   Import: فرمت‌های CSV، JSON، XLSX، SQLite. Export: فرمت‌های JSON، CSV،
    XLSX، SQLite.
-   نسخهٔ تاریخی ترتیب Restore (بر اساس وابستگی)
-   پیش از هر Restore، یک Backup خودکار از وضعیت فعلی دستگاه گرفته می‌شود
    تا در صورت خطا قابل بازگشت باشد.
-   نسخهٔ تاریخی انواع Backup
-   Backup واژگان: کلمات، معنی‌ها، دسته‌بندی‌ها و یادداشت‌ها.
-   Backup کامل: هر دو بخش بالا با هم.
-   Restore با قابلیت تشخیص رکوردهای تکراری و مدیریت تعارض انجام می‌شود.
-   حذف Duplicate
-   M.3 --- قاعده نرمال‌سازی canonicalKey: computeCanonicalKey(text) =
    text.trim().lowercase() با یکسان‌سازی فاصله‌های متوالی به یک Space.
    هیچ حذف Accent یا علامت نگارشی انجام نمی‌شود، زیرا در اسپانیایی
    Accent معنا را تغییر می‌دهد (si ≠ sí، el ≠ él) و ñ حرفی مستقل از n
    است و هرگز نباید با آن یکسان در نظر گرفته شود. حذف علائم نگارشی زائد
    (مانند !!!) وظیفه مرحله Normalization در Parser است، نه
    canonicalKey.
-   8.  Parser/Import همچنان Preserve-first است و حذف خودکار فقط برای
        Exact Duplicate مجاز است.
-   • تفاوت‌های غیر Exact Duplicate میان متن‌های قدیمی و نسخه‌های نهایی
    عمداً حذف نشده‌اند.
-   ۱ انتقال پرانتزها به یادداشت: در textهای دارای (...) یا （...）،
    محتوای داخل پرانتز به notes منتقل و خود پرانتزها از text حذف می‌شوند.
    -----------------------------------------------------------------------
-   1.  Canonical Domain Model
-   8.  Backup / Restore Contract
-   اصلاح یکپارچه‌سازی --- Data Model Canonical نسخهٔ مستندشده (FROZEN) 5
-   رمزنگاری Backup (توسعه آینده) 8
-   F. Parser -- Scope اجرایی V1 14
-   افزودن دستی، Paste Text Parser، Import/Export، جست‌وجو، فیلتر،
    علاقه‌مندی و مدیریت واژگان.
-   اصلاح یکپارچه‌سازی --- Data Model Canonical نسخهٔ مستندشده (FROZEN)
-   9.  یادداشت (Notes) پشت فلش‌کارت نمایش داده شود.
-   پیش از هر Restore، یک Backup خودکار از دیتابیس فعلی گرفته می‌شود.
-   رمزنگاری Backup (توسعه آینده)
-   کاربر می‌تواند در تنظیمات، گزینه «رمزنگاری Backup» را فعال کند.
-   در صورت فعال بودن، فایل Backup با کلیدی از Android Keystore یا کلیدی
    مشتق از ورودی کاربر (مثل PIN) رمزنگاری می‌شود.
-   وضعیت این تنظیم در app_setting با کلید backup_encryption_enabled
    ذخیره می‌شود؛ مقدار پیش‌فرض false (غیرفعال) است.
-   Paste Text Parser (بخش ۸)
-   Import/Export
-   جلوگیری از Duplicate
-   رمزنگاری Backup با Android Keystore یا کلید مشتق از کاربر (بخش نسخهٔ
    تاریخی).
-   • Show Note یادداشت/Parenthetical Note ذخیره‌شده را، در صورت وجود،
    نمایش می‌دهد.
-   □ Hint و Show Note تعریف شده‌اند.
-   • هیچ مورد مشکوکی برای حذف خودکار وجود ندارد؛ حذف خودکار فقط برای
    Exact Duplicate مجاز است.
-   • Parser / Paste Text --- وضعیت: نهایی برای Scope فعلی V1. مسیر P0
    باید محدود به قابلیت‌هایی باشد که در سند به‌عنوان V1/P0 تعریف شده‌اند؛
    قابلیت‌های پیشرفته‌تر Future محسوب می‌شوند.
-   • Duplicate / Validation --- وضعیت: نهایی در سطح توضیحات. داده ورودی
    نباید بدون تصمیم مشخص و قابل‌ردیابی حذف یا تغییر معنایی داده شود.
-   • Backup / Restore --- وضعیت: نهایی در سطح توضیحات. ترتیب و وابستگی
    موجودیت‌ها باید همان ترتیب تعریف‌شده در Specification باشد.
-   نسخهٔ مستندشده --- Canonical Static-Audit Corrections
-   AppSetting key، value، updatedAt تنظیمات برنامه؛ از جمله کلید آستانه
    تغییر سختی (بخش نسخهٔ تاریخی) و رمزنگاری Backup (بخش ۱۶).
    -----------------------------------------------------------------------
-   ۵ دیتابیس خراب یا برنامه یک دیتابیس جدید غیرقابل‌خواندن می‌سازد و به
    کاربر اطلاع می‌دهد؛ در صورت وجود Backup قبلی، امکان Restore وجود
    دارد.
-   ۹ بستن برنامه حین عملیات عملیات در یک Transaction طولانی (مثل
    Import) انجام می‌شود؛ در صورت قطع، Transaction به‌طور کامل Rollback
    می‌شود.
-   ۸ افزوده شدن امنیت داده‌ها (رمزنگاری اختیاری Backup)
-   ۷ اصلاح Paste Text Parser برای مدیریت بهتر ورودی‌ها
-   ۸ اصلاح ترتیب درج در Backup/Restore
-   1.  Canonical Application Path
-   -   Duplicate-attempt and due-validation behavior remains governed
        by the canonical contract.
-   -   Verify duplicate prevention and due validation.
-   -   Duplicate-attempt protection and due validation remain enforced.
-   4.  Data / Migration / Restore
-   -   Restore ordering remains deterministic.
-   3.  Canonical Rules Preserved
-   -   No duplicate persisted learning/difficulty/progress state is
        introduced.
-   6.  canonicalKey
-   fun computeCanonicalKey(text: String): String =
    text.trim().lowercase().replace(Regex("`\s`{=tex}+"), " ")
-   □ canonicalKey matching preserves accents and punctuation.
-   6.  Important Boundary
-   7.  Important Boundary

## نسخهٔ تاریخی قابلیت اطمینان و مقیاس

-   کل عملیات داخل یک Transaction انجام می‌شود؛ هر خطا باید Rollback کامل
    ایجاد کند.

-   ## LONG_TERM_MEMORY ۵۰ پاسخ صحیح در مرور Monthly

-   G. Scalability و Performance -- تصمیم اجرایی 14

-   برای Performance به‌جای تضمین سخت \<200ms از Benchmark/Test استفاده
    شود؛ PagingSource/Cursor-based Pagination برای لیست‌های بزرگ ترجیح
    داده شود.

-   ۲ افزوده شدن عملکرد و مقیاس‌پذیری (Performance)

-   نسخهٔ مستندشده → Full Test & Stress Audit

-   FlashLearn --- Full Test & Stress Audit --- Stage Output

-   نسخهٔ مستندشده establishes regression and stress coverage for the
    newly integrated presentation paths and their boundaries with the
    learning engine.

-   3.  Stress / Regression Targets

-   PASS --- نسخهٔ مستندشده test/stress boundary documented.

## نسخهٔ تاریخی تست و انتشار

-   ۱. el científico

-   1.  el científico

-   el científico; la científica

-   4.  Verification Method

-   -   بستهٔ تاریخی

-   PASS --- Code explicitly isolates legacy engine

-   5.  Verification

-   -   Static source-level contracts and unit-test scaffolding are
        included.

-   -   Real Android/Gradle compilation and runtime gates are
        intentionally deferred.

-   -   GitHub upload remains the final-stage operation.

-   Hilt runtime graph

-   ServiceLocator is retained only as a deprecated composition/test
    boundary. It does not construct services and therefore does not form
    a second runtime DI container.

-   Verification boundary

-   Testing

-   A source-level contract test verifies all primary destinations and
    their stable routes.

-   A real Gradle compile/runtime test remains deferred to the final
    project audit, as planned.

-   Migrationهای داده باید Idempotent باشند (اجرای دوباره تأثیری روی
    داده‌ی از‌قبل به‌روزشده ندارد).

-   9.  Refresh / Migration Contract

-   FlashLearn --- Master Specification --- نسخه نسخهٔ تاریخی (Final
    Unified & Audited Edition)

-   یکپارچه‌شده از دو فایل نسخهٔ مستندشده (Master Specification) و نسخهٔ
    مستندشده FULL (Consolidated Edition) --- با رفع ناسازگاری‌ها و تکمیل
    بخش‌های ناقص

-   نسخهٔ تاریخی افزودن Migration جدید 9

-   ۲۱. تصمیم‌های نهایی بسته‌شده (Closed Decisions) 10

-   سند/فایل تاریخی («نسخه کوتاه»): ساختار ۲۲بخشی تمیز و منطقی دارد، اما
    بسیاری از جزئیات دقیق (کدها، جدول‌های کامل، مقادیر رنگ اختصاصی، فهرست
    کامل Edge Caseها، جدول پیش‌نیازهای توسعه) را خلاصه یا حذف کرده است.

-   سند/فایل تاریخی («نسخه کامل»): قرار بود همان بازسازمان‌دهی را با
    جزئیات کامل انجام دهد، ولی در تولید فایل یک اشکال ساختاری رخ داده
    بود: تمام عنوان‌های «MASTER SECTION» پشت‌سرهم و بدون محتوا آمده بودند
    و متن واقعی هر بخش، جدا و بدون اتصال درست به عنوانش، در انتهای فایل
    با شماره‌گذاری قدیمی (مثل ۵-۲، ۸-۴، ۱۵-۱) ظاهر شده بود. همچنین
    شماره‌های داخل عنوان «MASTER SECTION N» با شماره واقعی آن بخش در
    فهرست مطالب یکی نبودند.

-   قاعده وضعیت (طبق سند مبنا حفظ شده): FROZEN = بخشی از نسخه فعلی ·
    FUTURE = توسعه آینده · OPEN DECISION = هنوز نهایی نشده · REMOVED =
    خارج از v1.

-   Refresh/Migrationهای داده (بخش ۱۰)

-   نسخهٔ تاریخی افزودن Migration جدید

-   4.  افزودن Migration جدید به addMigrations().

-   ۲۱. تصمیم‌های نهایی بسته‌شده (Closed Decisions)

-   4.  Refresh تنها مرجع Migration محتوایی داده است و هرگز Learning
        Data را تغییر نمی‌دهد.

-   23. هر Feature جدید باید همین Master Specification را به‌روزرسانی
        کند؛ هیچ قانون موازی خارج از این سند ساخته نمی‌شود.

-   • Date / Release Date

-   باشد. نام سازنده نباید حدس زده شود؛ در صورت نبود مقدار قطعی در
    Specification، فیلد باید قابل تکمیل باشد.

-   • Data Refresh / Migration --- وضعیت: نهایی در سطح توضیحات. Refresh
    و Migration باید با نسخه‌بندی داده و رفتار idempotent تعریف‌شده در سند
    هماهنگ باشند.

-   این فایل از نظر توضیحات محصول برای V1 در وضعیت FROZEN قرار دارد. از
    این مرحله به بعد، تغییر در رفتار محصول فقط با ثبت تصمیم جدید و ایجاد
    نسخه جدید Specification مجاز است. موارد مشکوک یا غیرقطعی نباید
    خودکار حذف شوند و باید در بخش Pending Items برای بررسی دستی ثبت
    شوند.

-   ## تست JUnit + Android Instrumentation Tests

-   -   Verify no legacy runtime path is registered.

-   5.  Verification Status

-   -   No GitHub upload was performed.

-   -   Legacy paths are explicitly excluded.

-   -   No new migration is invented merely for DI.

-   7.  Verification

-   FlashLearn --- Final GitHub / CI Preparation

-   -   Prepare the final project for GitHub Actions verification.

-   2.  CI Gates

-   -   Unit tests.

-   -   ./gradlew test

-   -   Treat actual compiler, test and runtime errors as the
        authoritative correction list.

-   7.  Migration Contract

-   -   The migration must not silently substitute lower(trim(text)) for
        the complete whitespace-collapse rule.

-   6.  Tests Added

-   7.  Verification Boundary

-   8.  Release Acceptance Criteria for نسخهٔ مستندشده

-   6.  Verification

-   The نسخهٔ مستندشده boundary concerning exhaustive migration of every
    legacy hard-coded string remains authoritative until the later
    audit; نسخهٔ مستندشده does not invent an exhaustive migration result.

-   Device-specific availability of a Spanish speech voice cannot be
    certified from source inspection alone.

-   2.  Test Coverage

-   Audio semantics are tested independently from answer-option
    language.

-   PASS --- Source-level test targets are defined for the new
    integration points.

-   PASS --- Regression-sensitive invariants are explicit.

-   An actual Android runtime or GitHub Actions result is not claimed
    unless an executed result is available.

-   نسخهٔ مستندشده → GitHub CI Final

-   FlashLearn --- GitHub CI Final --- Stage Output

-   The Android Gradle verification workflow is maintained in GitHub
    Actions.

-   CI configuration is separate from product learning behavior.

-   3.  Verification Contract

-   Observed CI success must be tied to an actual GitHub Actions run
    result.

-   4.  Release Invariants

-   CI changes must not alter learning semantics.

-   نسخهٔ مستندشده → Release Candidate

-   FlashLearn --- Release Candidate --- Stage Output

-   2.  Release Composition

-   No release-candidate change may silently redefine learning-state
    semantics.

-   4.  Verification

-   PASS --- Spanish audio semantics remain explicit.

-   4.  CI / Runtime Boundary

-   5.  Final Acceptance Criteria

-   PASS --- نسخهٔ مستندشده GitHub CI boundary documented without
    inventing a runtime result.

-   PASS --- نسخهٔ مستندشده release-candidate boundary documented.

## 26.X سایر جزئیات منحصربه‌فرد منابع

-   استخراج‌شده از فایل ادغام‌شده
    FlashLearn_ALL_WORD_DOCUMENTS_MERGED_v4.20 --- بدون حذف محتوا. فقط
    جداسازی الگوریتم‌های اصلی از بقیه توضیحات.

-   Version: مستندشده

-   Platform: Android

-   Processing: کاملاً Deterministic / Local

-   AI / Cloud Dependency: ندارد

-   1.  هدف سیستم

-   1.  واژه یا عبارت اصلی اسپانیایی را تشخیص دهد.

-   2.  ترجمه فارسی آن را پیدا کند.

-   3.  شماره‌گذاری، بولت، علامت‌ها و فرمت‌های غیرضروری را حذف کند.

-   4.  توضیحات، نکات گرامری، مثال‌ها و تحلیل اجزای عبارت را از واژه اصلی
        جدا کند.

-   5.  تشخیص دهد چه چیزی Entry اصلی است و چه چیزی صرفاً توضیح آن است.

-   6.  موارد تکراری را شناسایی کند.

-   7.  اطلاعات مرتبط با Entry را بدون از بین بردن اطلاعات اصلی ذخیره
        کند.

-   8.  تمام عملیات را بدون اینترنت و بدون AI انجام دهد.

-   ساختار پیشنهادی:

-   RAW TEXT

-   ↓

-   Normalization

-   Line Segmentation

-   Language Detection

-   Line Classification

-   Entry Boundary Detection

-   Main Entry Detection

-   Entry Type Detection

-   Relationship Detection

-   Validation

-   Vocabulary DB

-   3.  مرحله اول --- Normalization

-   قبل از هرگونه تشخیص، متن باید Normalize شود.

-   نسخهٔ تاریخی مواردی که باید حذف شوند

-   این موارد معنای واژه را تغییر نمی‌دهند:

-   •

-   -   

-   -   

------------------------------------------------------------------------

-   \_

-   →

-   »

-   «

-   ➜

-   # 

-   همچنین:

-   1.  

-   2)  

-   3 -

-   47:

-   در صورت تشخیص شماره‌گذاری باید شماره از ابتدای Entry حذف شود.

-   نسخهٔ تاریخی مواردی که نباید حذف شوند

-   علائم زیر ممکن است بخشی از معنی یا ساختار باشند:

-   ¿ ?

-   ¡ !

-   ...

-   ,

-   .

-   :

-   ;

-   (

-   )

-   مثلاً:

-   si hubiese...

-   tener miedo de ...

-   باید حفظ شوند.

-   4.  نرمال‌سازی Unicode

-   متن باید با Unicode NFC نرمال شود.

-   -   فاصله‌های پشت سر هم → یک فاصله

-   -   Tab → Space

-   -   Zero Width Space → حذف

-   -   Line Endingهای مختلف → "`\n`{=tex}"

-   -   اعداد فارسی → در صورت نیاز به اعداد استاندارد برای تحلیل
        شماره‌گذاری تبدیل شوند.

-   است.

-   اما متن اصلی برای نمایش کاربر باید حفظ شود.

-   5.  تشخیص زبان

-   سیستم حداقل باید سه حالت داشته باشد:

-   SPANISH

-   PERSIAN

-   MIXED

-   UNKNOWN

-   نسخهٔ تاریخی تشخیص فارسی

-   وجود حروف اصلی فارسی:

-   ا ب پ ت ث ج چ ح خ

-   د ذ ر ز ژ س ش ص ض

-   ط ظ ع غ ف ق ک گ

-   ل م ن و ه ی

-   امتیاز فارسی را افزایش می‌دهد.

-   نسخهٔ تاریخی تشخیص اسپانیایی

-   موارد زیر امتیاز اسپانیایی را افزایش می‌دهند:

-   á

-   é

-   í

-   ó

-   ú

-   ü

-   ñ

-   ¿

-   ¡

-   el

-   la

-   los

-   las

-   un

-   una

-   de

-   del

-   que

-   para

-   con

-   por

-   en

-   a

-   y

-   o

-   pero

-   si

-   como

-   اما این Dictionary نباید به‌تنهایی ملاک باشد.

-   6.  متن MIXED

-   این خط:

-   اما این به معنی Entry جدید نیست.

-   ممکن است:

-   ANALYSIS

-   باشد.

-   بنابراین:

-   «Language Detection به‌تنهایی Line Classification را تعیین نمی‌کند.»

-   7.  Line Classification

-   هر خط باید ابتدا به یکی از این انواع تبدیل شود:

-   ENTRY_HEADER

-   DERIVATIVE

-   RELATION

-   COMMENT

-   NUMBER

-   SEPARATOR

-   8.  ENTRY_HEADER

-   خطی که احتمالاً واژه یا عبارت اصلی است.

-   مثال:

-   یا:

-   contar con

-   estoy seguro de que todo irá bien

-   no puedes hacer una tortilla sin romper huevos

-   ترجمه فارسی Entry اصلی.

-   دانشمند (مذکر)؛ دانشمند (مؤنث)

-   روی کسی/چیزی حساب کردن

-   مطمئنم که همه‌چیز خوب پیش می‌رود

-   estoy seguro de que: مطمئنم که

-   todo: همه‌چیز

-   irá bien: خوب پیش خواهد رفت

-   این موارد نباید به‌صورت پیش‌فرض Entry مستقل ایجاد شوند.

-   ساختار:

-   Main Entry

-   FlashLearn --- توضیحات، گزارش‌های Audit، فازها و سایر محتوا نسخهٔ
    مستندشده

-   FlashLearn --- Audit Report (Post-Correction)

-   1.  Background

-   2.  Corrections Verified as Applied in v4

-   3.  Open Issues --- Flagged, NOT Fixed in This Revision

-   Each corrected file was: (1) unzipped and edited at the XML level to
    preserve all original formatting; (2) validated with the docx XSD
    structural validator against the original file (all PASSED); (3)
    converted to PDF and visually inspected on the affected pages to
    confirm correct rendering and RTL/LTR text mixing.

-   5.  File Map (نسخهٔ مستندشده → v4)

-   -   سند/فایل تاریخی → سند/فایل تاریخی

-   -   سند/فایل تاریخی → سند/فایل تاریخی

-   -   سند/فایل تاریخی → سند/فایل تاریخی (status downgraded to
        PROVISIONAL)

-   -   سند/فایل تاریخی → unchanged, still valid (no issues found)

-   نسخهٔ مستندشده --- Final Static Audit Addendum

-   این فهرست مرجع جایگزینی فایل‌هاست. فایل‌های نسخهٔ مستندشده زیر نسخه‌های
    جدید و مورد استفاده از این مرحله هستند. فایل‌های قدیمیِ متناظر را پس
    از نگهداری شخصی، می‌توان حذف کرد تا از اشتباه نسخه‌ای جلوگیری شود.

-   نسخه‌های نهایی Word

-   نسخه‌های نهایی ZIP

-   -   بستهٔ تاریخی

-   -   بستهٔ تاریخی

-   -   بستهٔ تاریخی

-   
      نک   ته مهم
      ---- -----------------------------
           قدیمی جایگزین نسخهٔ مستندشده

    سند/فایل تاریخی / سند/فایل تاریخی سند/فایل تاریخی

-   سند/فایل تاریخی سند/فایل تاریخی

-   سند/فایل تاریخی سند/فایل تاریخی

-   سند/فایل تاریخی سند/فایل تاریخی

-   سند/فایل تاریخی / سند/فایل تاریخی / سند/فایل تاریخی سند/فایل تاریخی
    ------------------------------------------------------------------------------------------------

-   FlashLearn --- FINAL THREE-WAY CROSS-CONSISTENCY AUDIT

-   1.  Audit Result

-   PASS --- Difficulty threshold = 3

-   PASS --- Due rule present

-   2.  Final Gate

-   The previously RED item is now resolved in Descriptions نسخهٔ
    مستندشده (see Section 5). No RED items remain based on this check. A
    fresh full three-way audit run is still recommended before declaring
    formal FREEZE.

-   4.  Artifacts Audited

-   Descriptions: سند/فایل تاریخی

-   Code: سند/فایل تاریخی

-   FlashLearn --- Phase 9 Statistics / Progress / Gamification

-   Version: نسخهٔ مستندشده

-   1.  Statistics

-   -   Empty history is zero-safe.

-   2.  Progress

-   -   It also reports path-failure and hasReachedVeryHard counts.

-   -   It does not synthesize missing learning state.

-   3.  Gamification / Achievement

-   -   The evaluator returns achievement states and newlyUnlocked IDs.

-   -   No unverified achievement threshold table is invented.

-   4.  Consistency

-   FlashLearn --- Phase 7 Dependency Injection + Service Wiring

-   Version: نسخهٔ مستندشده

-   Scope

-   Injectable implementations

-   UseCases

-   ServiceLocator

-   بستهٔ تاریخی

-   سند/فایل تاریخی

-   Version: نسخهٔ مستندشده

-   Primary destinations

-   ViewModel

-   Deliverables

-   Implemented

-   -   RANDOM is limited to due DAILY/WEEKLY/MONTHLY items and does not
        include LEARNED.

-   -   LanguagePair was not invented because it is not part of the
        current executable model contract.

-   Next phase

-   -   ProgressSummary domain model.

-   -   threshold_difficulty is not made editable in V1.

-   Next

-   ۵. چرخه یادگیری و Scheduler

-   مراحل: DAILY → WEEKLY → MONTHLY → LEARNED. مرحله NEW در این نسخه حذف
    شده است.

-   monthlyWrongCount تجمعی است و هرگز با یک انتقال موفق Reset نمی‌شود.

-   شمارنده‌های failure نیز با انتقال موفق Reset نمی‌شوند.

-   نسخهٔ تاریخی قانون «روز بعد» در Daily

-   هر کلمه در هر جهت زبانی، در یک روز حداکثر یک بار در مرور Daily نمایش
    داده می‌شود.

-   اگر کاربر در Daily پاسخ غلط دهد، کلمه فردا دوباره نمایش داده می‌شود
    (نه بلافاصله).

-   اگر فردا دوباره غلط بدهد، باز هم فردای همان روز دوباره نمایش داده
    می‌شود.

-   نسخهٔ تاریخی قانون hasPathFailure

-   hasPathFailure فقط پس از شکست در WEEKLY یا MONTHLY به true تبدیل
    می‌شود.

-   شکست در DAILY این پرچم را تغییر نمی‌دهد.

-   این پرچم هرگز توسط مرور عادی Reset نمی‌شود.

-   کاربرد: تعیین «اولین موفقیت کامل مسیر بدون خطا» --- یعنی رسیدن به
    EASY هنگام موفقیت در MONTHLY فقط زمانی رخ می‌دهد که
    hasPathFailure=false باشد.

-   ۶. Difficulty (درجه سختی)

-   سطوح: EASY → MEDIUM → HARD → VERY_HARD. Difficulty مستقل از Stage
    است و دو منبع تغییر دارد: (الف) قاعده عمومی شمارش پاسخ‌های متوالی،
    (ب) استثناهای قطعی وابسته به Stage که در بخش ۵ آمدند.

-   نسخهٔ تاریخی قاعده عمومی: شمارش پاسخ‌های متوالی

-   غلط = افزایش سختی یک سطح.

-   صحیح = کاهش سختی یک سطح.

-   تغییر فقط وقتی اعمال می‌شود که تعداد پاسخ‌های متوالی هم‌نوع (پیش‌فرض ۳
    عدد) تکمیل شود؛ به‌محض ثبت یک پاسخ از نوع مخالف، هر دو شمارنده
    (consecutiveCorrect و consecutiveWrong) صفر می‌شوند.

-   اصلاح یکپارچه‌سازی: هر تغییر Difficulty --- چه از طریق همین قاعده
    شمارشی، چه از طریق استثناهای Weekly/Monthly در بخش ۵ --- باید هر دو
    شمارنده consecutiveCorrect و consecutiveWrong را صفر کند تا شمارش
    بعدی از نو و بدون تداخل با رویداد قبلی آغاز شود. این نکته در هیچ‌کدام
    از دو فایل صریحاً ذکر نشده بود ولی برای پیاده‌سازی صحیح ضروری است.

-   نسخهٔ تاریخی استثنای قطعی Monthly

-   اولین شکست در MONTHLY همیشه Difficulty را مستقیماً HARD می‌کند و
    شکست‌های بعدی مستقیماً VERY_HARD؛ این اتفاق بدون توجه به شمارنده ۳تایی
    و بلافاصله رخ می‌دهد (همان‌طور که در جدول بخش ۵ آمد).

-   نسخهٔ تاریخی تنظیم آستانه (Difficulty Calibration)

-   وضعیت فعلی: تعداد پاسخ‌های متوالی لازم برای تغییر سختی، عدد ثابت ۳
    است.

-   ۷. انتخاب کارت و جلسات مرور

-   نسخهٔ تاریخی ترتیب اولویت انواع مرور (نسخه نهایی، اصلاح‌شده)

-   فیلترهای Stage، Difficulty، Category، Tag و Language Pair قابل
    ترکیب‌اند (مثلاً Hard+Travel+Spanish یا Monthly+Food).

-   کلمات LEARNED هرگز در مرورهای عادی (Daily/Weekly/Monthly/Random)
    ظاهر نمی‌شوند، مگر با انتخاب صریح کاربر.

-   هر جلسه یک شناسه یکتا دارد (مثلاً 2026-08-16-001).

-   تصمیم نهایی V1: پاسخ‌های ثبت‌شده حفظ می‌شوند، پاسخ ثبت‌نشده ایجاد
    نمی‌شود، Session ناقص می‌تواند endedAt=null بماند و Resume دقیق Queue
    Position الزامی نیست.

-   افزودن دستی و افزودن گروهی از یک متن Paste‌شده.

-   اولویت جداکننده‌ها (Separator priority): (پرانتز)، →، خط تیره -،
    دونقطه :، به‌همراه قواعد حفاظتی برای جلوگیری از اختلاط اشتباه حروف
    فارسی و لاتین در یک ردیف.

-   تشخیص شماره‌گذاری/Bullet (numbering/bullets) و scriptMismatch
    (ناهماهنگی الفبا) برای جلوگیری از تفسیر اشتباه جهت ترجمه.

-   جست‌وجو و فیلتر با LIKE + JOIN و Pagination انجام می‌شود؛ جست‌وجو در
    متن اصلی، ترجمه، مثال، توضیحات و Tag صورت می‌گیرد.

-   ۱۰. Refresh و نسخه‌گذاری داده (Data Versioning)

-   هدف: اعمال تغییرات محتوایی/منطقی نسخه‌های جدید برنامه روی رکوردهای
    قدیمی، بدون از‌دست‌رفتن وضعیت یادگیری کاربر.

-   نقشه راه نسخه‌های داده تاکنون

-   ۱۱. آمار، پیشرفت، Streak و Gamification

-   نسخهٔ تاریخی آمار پایه

-   نسخهٔ تاریخی درصد پیشرفت (مدل مرحله‌ای)

-   این فرمول جایگزین فرمول ساده‌ی «یادگرفته‌شده ÷ کل» شده تا حتی پیش از
    رسیدن به LEARNED هم پیشرفت واقعی کاربر دیده شود و انگیزه کاهش پیدا
    نکند. امتیاز هر کلمه بر اساس وضعیتش:

-   درصد پیشرفت کل = (مجموع امتیاز پیشرفت تمام کلمات) ÷ (تعداد کل
    کلمات).

-   نسخهٔ تاریخی Streak

-   تعریف: تعداد روزهای متوالی تقویمی محلی، از امروز به عقب، که کاربر در
    آن‌ها حداقل یک پاسخ ثبت کرده است؛ با قطع یک روز، Streak از ۱ آغاز
    می‌شود. تمام پاسخ‌های یک روز فقط یک روز به‌حساب می‌آیند (even اگر ۵۰
    پاسخ ثبت شده باشد).

-   نسخهٔ تاریخی Achievements

-   version مستندشده

-   version مستندشده

-   version مستندشده

-   version مستندشده

-   version مستندشده

-   رکورد version مستندشده در زمان اجرای version مستندشده:

-   1 → 2 → 3 → 4 → 5

-   رکورد version مستندشده:

-   رکورد version مستندشده:

-   بدون تغییر

-   به‌صورت مستقل اجرا شود.

-   تحلیل بحرانی

-   یک اصلاح مهم نسبت به نسخه قبلی اضافه کردم: Transaction.

-   بدون Transaction، این سناریو خطرناک است:

-   در این حالت بخشی از دیتابیس جدید و بخشی قدیمی می‌ماند. برای یک سیستم
    آفلاین که دیتابیس محلی منبع اصلی داده است، این ریسک قابل قبول نیست.

-   نوع:

-   Read-only

-   بدون ایجاد هیچ تغییر در داده‌های سیستم.

-   ورودی

-   activeLanguagePair : LanguagePair

-   خروجی

-   یکی از دو حالت:

-   promptText : String,

-   correctAnswerText : String,

-   options : List`<String>`{=html}

-   options باید دقیقاً ۴ گزینه داشته باشد

-   و ترتیب گزینه‌ها باید Shuffle شده باشد.

-   مرحله ۱ --- پیدا کردن Prompt و Correct Answer

-   که:

-   == activeLanguagePair.sourceLanguage

-   == activeLanguagePair.targetLanguage

-   قاعده چند ترجمه‌ای

-   activeLanguagePair.targetLanguage باشد:

-   هرگز Distractor محسوب نمی‌شوند.

-   دلیل:

-   و نباید به عنوان گزینه غلط نمایش داده شوند.

-   مرحله ۲ --- تعریف تابع پیدا کردن Distractorها

-   تابع:

-   findCandidates(difficultyFilter)

-   وظیفه:

-   به عنوان گزینه غلط استفاده شوند.

-   شرایط Candidate

-   هر Candidate باید تمام شرایط زیر را داشته باشد:

-   activeLanguagePair باشد.

-   4.  اگر difficultyFilter مشخص شده باشد:

-   مطابق difficultyFilter باشد.

-   5.  ترجمه Candidate با پاسخ صحیح

-   از نظر normalize یکسان نباشد:

-   !=

-   برای مقایسه متنی، از:

-   normalize(text)

-   استفاده می‌شود.

-   Normalization حداقل باید مواردی مانند

-   Whitespace و تفاوت‌های قابل‌نادیده‌گرفتن

-   در نمایش متن را یکسان کند.

-   جلوگیری از ورود دو گزینه‌ای که از نظر

-   محتوای متنی یکسان هستند.

-   پس از استخراج Candidateها:

-   Candidateها بر اساس:

-   یکتا می‌شوند.

-   یک متن یکسان داشته باشند،

-   آن متن فقط یک بار در Pool قرار می‌گیرد.

-   مرحله نسخهٔ تاریخی --- اولویت اول: همان Difficulty

-   findCandidates(

-   در این مرحله فقط Candidateهایی انتخاب می‌شوند

-   که Difficulty آنها دقیقاً برابر با

-   مرحله نسخهٔ تاریخی --- اضافه کردن Difficultyهای مجاور

-   candidates.size \< 3

-   adjacentLevels =

-   Difficulty یک سطح بالاتر

-   Difficulty یک سطح پایین‌تر

-   نسبت به:

-   در مرزهای Difficulty:

-   اگر فقط یک سمت وجود داشته باشد،

-   فقط همان سمت استفاده می‌شود.

-   candidates +=

-   difficulty IN adjacentLevels

-   و مجدداً:

-   UniqueByNormalizedText(candidates)

-   Candidateهای مرحله قبل حذف نمی‌شوند.

-   Candidateهای جدید فقط به Pool اضافه می‌شوند.

-   مرحله نسخهٔ تاریخی --- استفاده از کل بانک

-   بدون فیلتر Difficulty

-   در این مرحله دیگر Difficulty هیچ محدودیتی

-   برای Candidateها ایجاد نمی‌کند.

-   مرحله ۳ --- تصمیم نهایی

-   حداقل به ۳ Distractor معتبر نیاز است.

-   اگر حداقل ۳ Candidate وجود داشت

-   wrongOptions =

-   انتخاب تصادفی دقیقاً ۳ Candidate

-   از candidates

-   با این شرط:

-   هیچ Candidate دوبار انتخاب نشود.

-   allOptions =

-   wrongOptions\[0\].text,

-   wrongOptions\[1\].text,

-   wrongOptions\[2\].text

-   allOptions.shuffle()

-   و در نهایت:

-   promptText =

-   correctAnswerText =

-   options =

-   allOptions

-   2.  هیچ داده‌ای را تغییر نمی‌دهد.

-   3.  هیچ داده‌ای را Persist نمی‌کند.

-   4.  Difficulty را تغییر نمی‌دهد.

-   5.  Stage را تغییر نمی‌دهد.

-   انجام می‌شود.

-   نمی‌توانند Distractor باشند.

-   12. Distractor نباید از نظر normalize(text)

-   با correctAnswer یکسان باشد.

-   15. correctAnswer دقیقاً یکی از ۴ گزینه است.

-   16. سه گزینه دیگر باید Distractor باشند.

-   17. ترتیب گزینه‌ها باید قبل از خروجی Shuffle شود.

-   18. اگر حتی با استفاده از کل بانک کمتر از

-   ۳ Distractor معتبر وجود داشته باشد:

-   یا اعلام عدم امکان تولید آن را دارد.

-   وابستگی‌های منطقی

-   وابسته به:

-   LanguagePair

-   استفاده‌کننده از:

-   activeLanguagePair

-   Stage Logic

-   و بر اساس آن سؤال تولید می‌کند.

-   هیچ‌گونه تصمیمی درباره:

-   خودشان هستند.

-   جمع‌بندی

-   این نسخه را نهایی می‌دانم. مهم‌ترین اصلاح نسبت به نسخه قبلی این‌ها
    هستند:

-   Unique کردن گزینه‌ها بر اساس normalize(text)

-   مشخص کردن سه مرحله افزایش Pool بدون تغییر اولویت انتخاب

-   خروجی استاندارد TransitionResult باید حداقل شامل موارد زیر باشد:

-   • newStage

-   • hasPathFailure

-   • monthlyWrongCount

-   قواعد تکمیلی:

-   • WEEKLY + Wrong → hasPathFailure = true

-   • MONTHLY + Wrong → hasPathFailure = true و monthlyWrongCount یک
    واحد افزایش می‌یابد

-   • DAILY + Wrong → monthlyWrongCount بدون تغییر باقی می‌ماند

-   • MONTHLY + Correct → monthlyWrongCount بدون تغییر باقی می‌ماند

-   • monthlyWrongCount هرگز Reset نمی‌شود و مقدار تجمعی آن حفظ می‌شود

-   • EASY: Distractorهای معتبر ولی نسبتاً متفاوت‌تر.

-   • MEDIUM: Distractorهای نزدیک‌تر از نظر معنا/زبان؛ Category یا
    Subcategory مشابه در صورت معتبر بودن ترجیح دارد.

-   • HARD: Distractorهای بسیار مشابه و پیچیده‌تر؛ همان
    Category/Subcategory یا نزدیک‌ترین دسته معتبر در اولویت است.

-   • Vocabulary Difficulty معیار انتخاب Distractor نیست.

-   3.  Category-aware Distractor Selection

-   • Stage ∈ {DAILY, WEEKLY, MONTHLY}

-   بنابراین WEEKLY/MONTHLY که موعدشان هنوز نرسیده است نباید نمایش داده
    شوند. مرور LEARNED یک Mode جداگانه است.

-   • کارت بدون Submit پاسخ ایجاد نمی‌کند.

-   • خروج ناقص Session نیازمند Resume دقیق Queue Position در V1 نیست.

-   • Session بعدی یک Session جدید است.

-   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-   اصلاحات قطعی این نسخه:

-   4.  قواعد تجمعی monthlyWrongCount و Historical بودن hasPathFailure
        صریح شدند.

-   5.  قواعد Difficulty برای شکست WEEKLY/MONTHLY صریح و Double Update
        ممنوع شد.

-   6.  hasReachedVeryHard در قرارداد Difficulty/Refresh به‌عنوان
        Historical Flag لحاظ شد.

-   • هر تصمیم محصولی جدید که در قراردادهای FROZEN فعلی تعریف نشده باشد
    باید در نسخه بعدی بررسی شود.

-   Cross-Consistency Correction Log --- نسخهٔ مستندشده

-   اصلاحات قطعی اعمال‌شده:

-   A. قطعات قدیمی/تکراری دیگری که صرفاً با Overrideهای انتهایی بی‌اثر
    شده‌اند، در ممیزی بعدی موردی بررسی شوند.

-   B. هیچ مورد مشکوکی در این مرحله حذف نشده است.

-   موارد قدیمی/مشکوک حذف نشده‌اند؛ قطعات Legacy که با قرارداد نهایی
    Override شده‌اند صریحاً با برچسب LEGACY / SUPERSEDED مشخص شده‌اند.

-   DAILY غلط DAILY فردا (روز بعد) بدون تغییر totalWrong +۱؛ هر کلمه
    حداکثر یک‌بار در روز نمایش داده می‌شود

-   WEEKLY صحیح MONTHLY اکنون + ۳۰ روز بدون تغییر totalCorrect +۱

-   WEEKLY غلط DAILY فردا حداقل MEDIUM + reset totalWrong +۱؛ هر دو
    شمارنده hasPathFailure=true consecutiveCorrect و\
    consecutiveWrong به ۰

-   MONTHLY صحیح LEARNED null بدون تغییر (اگر totalCorrect +۱
    hasPathFailure=false\
    بوده باشد → طبق بخش ۶\
    به EASY می‌رسد)

-   MONTHLY غلط DAILY فردا اولین شکست → HARD؛ totalWrong +۱؛ شکست بعدی →
    monthlyWrongCount +۱؛ VERY_HARD + reset هر hasPathFailure=true دو
    شمارنده\
    consecutiveCorrect و\
    consecutiveWrong به ۰

-   LEARNED اختیاری LEARNED null (بدون بدون تغییر فقط totalCorrect یا
    (صحیح/غلط) (بدون تغییر) totalWrong +۱؛ صرفاً تغییر) History ثبت می‌شود
    ------------------------------------------------------------------------------------------------ -
    -----------------------------------------------------------------------
    سطح فعلی شرط افزایش سطح جدید -----------------------
    ----------------------- ----------------------- EASY ۳ غلط متوالی
    MEDIUM

-   MEDIUM ۳ غلط متوالی HARD

-   HARD ۳ غلط متوالی VERY_HARD

-   VERY_HARD ۳ غلط متوالی VERY_HARD (بدون تغییر)
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    سطح فعلی شرط کاهش سطح جدید -----------------------
    ----------------------- ----------------------- VERY_HARD ۳ صحیح
    متوالی HARD

-   HARD ۳ صحیح متوالی MEDIUM

-   MEDIUM ۳ صحیح متوالی EASY

-   EASY ۳ صحیح متوالی EASY (بدون تغییر)
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    نسخه تغییرات اعمال‌شده -----------------------------------
    ----------------------------------- ۰ مقدار پیش‌فرض داده‌های اولیه،
    بدون هیچ به‌روزرسانی.

-   کلمات تمرین‌نشده کل − تمرین‌شده

-   کلمات یادگرفته‌شده COUNT(\*) از learning_state با stage='LEARNED'
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    وضعیت کلمه امتیاز پیشرفت -----------------------------------
    ----------------------------------- اصلاً تمرین نشده ۰٪

-   اولین تمرین انجام شده ۱۵٪

-   مرور روزانه (DAILY) موفق ۳۵٪

-   مرور هفتگی (WEEKLY) موفق ۶۰٪

-   مرور ماهانه (MONTHLY) در انتظار ۸۰٪

-   یادگرفته‌شده (LEARNED) ۱۰۰٪
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    Achievement شرط -----------------------------------
    ----------------------------------- FIRST_TEN_WORDS یادگیری ۱۰ کلمه
    اول

-   SEVEN_DAY_STREAK ۷ روز پیوسته تمرین

-   THIRTY_DAY_STREAK ۳۰ روز پیوسته تمرین

-   HARD_MODE_MASTER تسلط بر ۲۵ کلمه VERY_HARD

-   10. Legacy / Superseded Code --- NOT EXECUTABLE

-   11. Final Cross-Consistency Gate

-   Appendix A --- Source Basis

-   نسخهٔ مستندشده --- Implementation Corrections

-   FlashLearn

-   فهرست مطالب

-   فهرست مطالب 1

-   ۰. یادداشت این نسخه و روش یکپارچه‌سازی 2

-   ۱. محصول و دامنه (Product & Scope) 3

-   نسخهٔ تاریخی فلسفه طراحی 3

-   نسخهٔ تاریخی فناوری‌های اصلی 3

-   ۲. اصول تغییرناپذیر پروژه 3

-   ۳. معماری و ساختار فنی 4

-   نسخهٔ تاریخی لایه‌ها 4

-   نسخهٔ تاریخی جریان داده 4

-   نسخهٔ تاریخی اصول معماری 4

-   Integrity Rule: 5

-   مالکیت: 5

-   قید یکتایی: 6

-   رابطه V1: 6

-   نسخهٔ تاریخی رنگ‌های نقشی پایه (عمومی رابط کاربری) 6

-   نسخهٔ تاریخی رنگ‌های اختصاصی Difficulty و Stage (کد آماده Kotlin) 6

-   ۱۵. مدیریت حالت‌های خاص (Edge Cases) 7

-   ۱۶. عملکرد، مقیاس‌پذیری و امنیت داده 8

-   نسخهٔ تاریخی عملکرد و مقیاس 8

-   نسخهٔ تاریخی امنیت داده 8

-   ۱۷. تست (Unit & Instrumentation) 8

-   نسخهٔ تاریخی پیش‌نیازها 9

-   نسخهٔ تاریخی ساختار ماژول‌ها 9

-   نسخهٔ تاریخی افزودن یک ویژگی جدید 9

-   ۱۹. توسعه‌های آینده (Future Extension) 9

-   نسخهٔ تاریخی جزئیات: چند جفت‌زبان فعال 10

-   ۲۰. موارد حذف‌شده از v1 (Removed / خارج از Scope) 10

-   ۲۲. تعریف نهایی (Definition of Done) و اصول غیرقابل‌نقض 10

-   ۲۳. پیوست: تاریخچه نسخه‌ها (Changelog) 11

-   نسخهٔ مستندشده → نسخهٔ مستندشده 11

-   نسخهٔ مستندشده → نسخهٔ مستندشده 11

-   نسخهٔ مستندشده → نسخهٔ مستندشده 12

-   ۲۴. جمع‌بندی نهایی و اعلام Source of Truth 12

-   پیوست غیررسمی --- یادداشت‌های باقی‌مانده از گفتگوی طراحی (خارج از
    شماره‌گذاری رسمی سند) 12

-   ضمیمه ارتقا و اصلاحات نهایی -- Post نسخهٔ مستندشده Integration Update
    14

-   C. Session Exit -- تصمیم V1 14

-   D. Difficulty Threshold -- تصمیم V1 14

-   E. Daily New Word Limit -- تصمیم V1 14

-   P0 شامل: 14

-   H. Achievement / Gamification -- تصمیم V1 15

-   J. Development Phase Plan -- نسخه یکپارچه 15

-   K. Cross-Consistency Corrections -- الزامی قبل از Freeze 15

-   L. وضعیت نهایی این Update 15

-   N. Tag Data Model Finalization --- رابطه چند-به-چند (Appendix K) 15

-   PROJECT PROGRESS CHECKPOINT --- 2026-09-07 16

-   9.  Final Data Ownership 17

-   10. Final Cross-Consistency Contracts 17

-   11. Development Phase Plan --- نسخهٔ مستندشده 17

-   12. نسخهٔ مستندشده Audit Checklist 18

-   ۰. یادداشت این نسخه و روش یکپارچه‌سازی

-   دو فایلی که بررسی شد هر دو از یک سند مبنا (App_Spec_v1.9) گرفته شده
    بودند، اما هرکدام مشکل جداگانه داشتند:

-   ۱. محصول و دامنه (Product & Scope)

-   FlashLearn یک اپلیکیشن Android، کاملاً Offline-First و مبتنی بر
    فلش‌کارت و Spaced Repetition برای یادگیری واژگان و جملات است.

-   Android-only؛ حداقل API 26.

-   پشتیبانی چندزبانه در سطح معماری، اما در نسخه فعلی محدود به سه زبان
    فارسی، انگلیسی و اسپانیایی و فقط یک جفت‌زبان فعال (isActive=1) در هر
    لحظه.

-   تمام الگوریتم‌ها داخلی (In-House) و آفلاین هستند؛ هیچ هوش مصنوعی در
    v1 استفاده نمی‌شود.

-   Notifications/Reminders، مرحله NEW، و الگوریتم Anki/SM-2 در v1 خارج
    از دامنه‌اند (بخش ۲۰).

-   نسخهٔ تاریخی فلسفه طراحی

-   برنامه باید این حس را به کاربر منتقل کند: «یک مربی شخصی هوشمند
    واژگان، همراه با یک بازی کوچک آموزش زبان.»

-   نسخهٔ تاریخی فناوری‌های اصلی

-   ۲. اصول تغییرناپذیر پروژه

-   Stage (مرحله یادگیری) کاملاً از Difficulty (درجه سختی) مستقل است؛
    هرکدام قواعد تغییر جدای خود را دارند (بخش ۵ و ۶).

-   ViewModel هیچ منطق تجاری‌ای اجرا نمی‌کند؛ منطق فقط در UseCase/Domain
    است.

-   حذف رکورد همیشه Soft Delete است (active=false)؛ History همیشه حفظ
    می‌شود.

-   ۳. معماری و ساختار فنی

-   نسخهٔ تاریخی لایه‌ها

-   core: ابزارهای مشترک و Dependency Injection

-   نسخهٔ تاریخی جریان داده

-   نسخهٔ تاریخی اصول معماری

-   ViewModelها فقط Orchestrator هستند و منطق مرور را اجرا نمی‌کنند.

-   همه الگوریتم‌ها داخلی (In-House) هستند و هیچ وابستگی به سرویس خارجی
    یا هوش مصنوعی ندارند.

-   Local Data Update Manager مسئول اعمال تغییرات محتوایی نسخه‌های جدید
    روی داده‌های قدیمی است (بخش ۱۰).

-   الگوی مورد استفاده: Model-View-Intent (MVI) به‌صورت سبک
    (Lightweight).

-   هیچ منطق تجاری در ViewModel پیاده‌سازی نمی‌شود؛ همه منطق در UseCaseها
    قرار دارد.

-   ViewModel هیچ ارجاعی به Compose یا Android ندارد (به‌جز
    androidx.lifecycle.ViewModel و viewModelScope).

-   تمام UseCaseها روی Dispatchers.IO یا Dispatchers.Default اجرا
    می‌شوند؛ ViewModel نتیجه را روی Dispatchers.Main دریافت می‌کند.

-   Integrity Rule:

-   Weekly/Monthly Exception در Learning Transition → Forced Difficulty
    Update طبق Transition Contract

-   Difficulty Calculation → current, consecutiveCorrect,
    consecutiveWrong, hasReachedVeryHard در مسیر عادی

-   مالکیت:

-   هر تغییر Difficulty شمارنده‌های consecutiveCorrect و consecutiveWrong
    را صفر می‌کند.

-   قید یکتایی:

-   رابطه V1:

-   نسخهٔ تاریخی رنگ‌های نقشی پایه (عمومی رابط کاربری)

-   نسخهٔ تاریخی رنگ‌های اختصاصی Difficulty و Stage (کد آماده Kotlin)

-   1.  پرچم تکراری نباید در صفحه اصلی وجود داشته باشد.

-   2.  متن خوش‌آمدگویی اضافی حذف شود.

-   4.  علامت «+» اضافی حذف شود.

-   5.  نمایش تکراری «کلمات منتظر» حذف شود.

-   6.  کارت‌های دوخطی غیرضروری جمع‌وجور شوند.

-   7.  متن‌های طولانی خلاصه شوند.

-   8.  در حالت چهارگزینه‌ای، دکمه راهنمایی و نمایش یادداشت وجود داشته
        باشد.

-   10. پرانتزهای داخل کلمات به یادداشت منتقل شوند (نه در متن اصلی
        کلمه).

-   11. دکمه به‌روزرسانی (Refresh) در صفحه کتابخانه واژگان وجود داشته
        باشد.

-   فلش‌کارت با Flip/Swipe و دکمه‌های «❌ بلد نیستم» / «✅ بلدم».

-   بازخورد پاسخ غلط: نمایش قرمز/سبز به‌همراه ۲ ثانیه مکث پیش از سؤال
    بعدی؛ در پاسخ صحیح هیچ مکثی اعمال نمی‌شود و بلافاصله به سؤال بعدی
    می‌رود.

-   مسیرهای هر صفحه جدید باید در Routes و در FlashLearnNavGraph ثبت شوند
    و برای هر صفحه، ViewModel و Compose Screen متناظر ساخته شود. جزئیات
    دقیق مسیرها باید هنگام پیاده‌سازی با ساختار واقعی پروژه تطبیق داده
    شود.

-   به‌روزرسانی خوش‌بینانه (Optimistic Update) با شرط تطبیق وضعیت انتظاری
    انجام می‌شود؛ در صورت عدم تطابق، updatedRows=0 برمی‌گردد و عملیات باید
    دوباره از سر بارگذاری وضعیت فعلی انجام شود.

-   ۱۵. مدیریت حالت‌های خاص (Edge Cases)

-   ۱۶. عملکرد، مقیاس‌پذیری و امنیت داده

-   نسخهٔ تاریخی عملکرد و مقیاس

-   هدف: پاسخ‌گویی بدون افت محسوس تا حدود ۱۰۰,۰۰۰ رکورد.

-   Pagination: تمام کوئری‌های لیست از LIMIT/OFFSET یا Cursor-based
    Pagination استفاده می‌کنند؛ در Compose از LazyColumn با PagingSource.

-   کوئری‌های سنگین فقط فیلدهای لازم را برمی‌گردانند؛ از JOIN بهینه و
    EXISTS به‌جای IN استفاده می‌شود.

-   آستانه عملکرد هدف: نمایش لیست ۵۰ رکورد زیر ۳۰۰ میلی‌ثانیه؛ ثبت یک
    پاسخ زیر ۲۰۰ میلی‌ثانیه.

-   مدیریت حافظه با distinctUntilChanged() روی Flow؛ منابع سنگین با
    remember و derivedStateOf مدیریت می‌شوند.

-   نسخهٔ تاریخی امنیت داده

-   هیچ داده حساسی (رمز عبور، توکن، اطلاعات بانکی) در برنامه ذخیره
    نمی‌شود.

-   رمزنگاری/رمزگشایی ممکن است چند ثانیه طول بکشد؛ باید Progress
    Indicator نمایش داده شود.

-   ۱۷. تست (Unit & Instrumentation)

-   تست Unit و Android Instrumentation برای موارد زیر الزامی است:

-   Transitionهای Stage/Difficulty (طبق جدول بخش ۵ و ۶)

-   Scheduler و ترتیب انتخاب کارت (بخش ۷)

-   تراکنش‌های اتمیک (State+History)

-   سناریوهای Edge Case (بخش ۱۵)

-   نسخهٔ تاریخی پیش‌نیازها

-   نسخهٔ تاریخی ساختار ماژول‌ها

-   نسخهٔ تاریخی افزودن یک ویژگی جدید

-   ۱۹. توسعه‌های آینده (Future Extension)

-   قابل‌تنظیم‌شدن آستانه تغییر سختی (threshold_difficulty) با گزینه‌های
    ۲/۳/۴/۵؛ پیش‌فرض ۳ (بخش نسخهٔ تاریخی).

-   الگوریتم Anki/SM-2 به‌عنوان یک Scheduler اختیاری و جایگزین، در کنار
    Learning Transition / Difficulty Calculation داخلی فعلی؛ این گزینه
    در v1 پیاده‌سازی نمی‌شود.

-   نسخهٔ تاریخی جزئیات: چند جفت‌زبان فعال

-   وضعیت فعلی: فقط یک جفت‌زبان فعال (isActive=1) پشتیبانی می‌شود.

-   امکان فعال‌سازی چند جفت‌زبان هم‌زمان.

-   تعیین یک جفت‌زبان به‌عنوان پیش‌فرض.

-   امکان انتخاب جفت‌زبان در حین مرور بدون نیاز به رفتن به تنظیمات.

-   ۲۰. موارد حذف‌شده از v1 (Removed / خارج از Scope)

-   موارد زیر در این نسخه تعیین‌تکلیف شده و از این پس بخشی از قرارداد
    فعال V1 هستند:

-   موارد دیگری که در فایل کامل در نسخه‌های قبلی آمده بودند (نمایش چند
    معنی در مرور، نحوه ساخت گزینه‌های غلط چهارگزینه‌ای، مکث پاسخ صحیح) در
    واقع پاسخ پیشنهادی مشخصی داشتند و فایل کوتاه هم دقیقاً همان پاسخ‌ها را
    به‌عنوان قانون نهایی در بخش‌های مربوطه (۷ و ۱۲) درج کرده بود؛ بنابراین
    در این نسخه به‌عنوان تصمیم قطعی منتقل شدند، نه موارد باز.

-   ۲۲. تعریف نهایی (Definition of Done) و اصول غیرقابل‌نقض

-   1.  هیچ منطق تجاری متناقض یا تکراری در لایه‌های مختلف وجود ندارد.

-   3.  Scheduler تنها مرجع تشخیص Due-بودن و ترتیب انتخاب کارت است.

-   7.  همه تغییرات State/History اتمیک (Transaction-based) هستند.

-   10. DAILY صحیح → WEEKLY؛ WEEKLY صحیح → MONTHLY؛ MONTHLY صحیح →
        LEARNED.

-   11. DAILY غلط → DAILY (بدون تغییر مرحله)؛ WEEKLY غلط → DAILY (سختی
        حداقل MEDIUM)؛ MONTHLY غلط → DAILY (افزایش monthlyWrongCount).

-   12. اولین غلط MONTHLY → HARD؛ غلط‌های بعدی MONTHLY → VERY_HARD.

-   13. monthlyWrongCount تجمعی است و هرگز Reset نمی‌شود؛ انتقال‌های
        ناموفق شمارنده‌های عمر برنامه را Reset نمی‌کنند.

-   16. محاسبه Transition از state، answer و زمان ورودی کاملاً قطعی
        (Deterministic) است.

-   17. hasPathFailure فقط پس از شکست WEEKLY/MONTHLY به true تبدیل
        می‌شود.

-   18. ViewModelها منطق Transition را پیاده‌سازی نمی‌کنند.

-   19. Progress، Difficulty، Streak و Statistics سیستم‌های مستقل از هم
        هستند.

-   20. Refresh باید فقط از طریق Local Data Update Manager انجام شود.

-   21. همه الگوریتم‌ها کاملاً داخلی هستند و به هوش مصنوعی وابسته نیستند.

-   تمام تصمیم‌های باز این سند پیش از Freeze نهایی تعیین‌تکلیف و بسته
    شده‌اند.

-   ۲۳. پیوست: تاریخچه نسخه‌ها (Changelog)

-   این تاریخچه صرفاً برای مرجع و پیشینه نگه‌داری شده و بخشی از قوانین
    فعال نیست.

-   نسخهٔ مستندشده → نسخهٔ مستندشده

-   نسخهٔ مستندشده → نسخهٔ مستندشده

-   نسخهٔ مستندشده → نسخهٔ مستندشده

-   ۲۴. جمع‌بندی نهایی و اعلام Source of Truth

-   این سند (نسخه نسخهٔ تاریخی) مرجع یکپارچه فعلی پروژه FlashLearn است؛
    از ادغام کامل دو فایل ورودی، رفع مغایرت‌های نام‌گذاری و تکمیل بخش‌های
    ناقص به‌دست آمده. هیچ قانون محصولی تازه و بدون مبنا در منابع اصلی به
    آن اضافه نشده؛ هر جا برای تکمیل یک خلأ لازم بود از استنتاج منطقی
    استفاده شود، با برچسب «اصلاح یکپارچه‌سازی» به‌صراحت مشخص شده است.

-   تصمیم‌های بخش ۲۱ در این نسخه بسته شده‌اند و این سند پس از کنترل نهایی
    با وضعیت FROZEN منتشر می‌شود؛ هر تغییر بعدی فقط با ثبت تصمیم جدید و
    ایجاد نسخه جدید مجاز است.

-   پیوست غیررسمی --- یادداشت‌های باقی‌مانده از گفتگوی طراحی (خارج از
    شماره‌گذاری رسمی سند)

-   خلاصه اجرایی

-   محدودیت تعداد کارت فقط اندازه Session را محدود می‌کند و Eligibility
    را تغییر نمی‌دهد. تنظیمات ظاهری نیز منطق
    Learning/Difficulty/Scheduling را تغییر نمی‌دهند.

-   • توضیح برنامه

-   • Version

-   • History / Changelog

-   • Creator Name / نام سازنده

-   9.  Final Data Ownership

-   • hasReachedVeryHard تاریخی و non-reset است.

-   • WEEKLY wrong → hasPathFailure=true.

-   • MONTHLY wrong → hasPathFailure=true و monthlyWrongCount افزایش
    می‌یابد.

-   • DAILY wrong و MONTHLY correct، monthlyWrongCount را تغییر نمی‌دهند.

-   • monthlyWrongCount تجمعی و بدون Reset است.

-   10. Final Cross-Consistency Contracts

-   • Vocabulary Difficulty = EASY / MEDIUM / HARD / VERY_HARD.

-   11. Development Phase Plan --- نسخهٔ مستندشده

-   12. نسخهٔ مستندشده Audit Checklist

-   □ Version سند نسخهٔ مستندشده است.

-   □ قانون قدیمی Distractor بر اساس همان Vocabulary Difficulty
    حذف/بی‌اثر شده است.

-   □ Category-aware Distractor Selection تعریف شده است.

-   □ مالکیت Stateها سازگار است.

-   □ hasPathFailure و monthlyWrongCount مطابق قواعد نهایی‌اند.

-   • Session Exit: پاسخ‌های ثبت‌شده حفظ می‌شوند؛ Resume دقیق Queue
    Position در V1 الزامی نیست؛ Session بعدی جدید است.

-   • وضعیت: FROZEN --- توضیحات V1 تکمیل و تصمیم‌های باز بسته شده‌اند.

-   هدف این بخش: مشخص‌کردن وضعیت نهایی توضیحات محصول و جلوگیری از
    باقی‌ماندن تصمیم‌های مبهم در سند.

-   • Product Scope & V1 Boundary --- وضعیت: نهایی. محدوده V1 و موارد
    خارج از V1 باید مطابق همین سند تلقی شوند؛ قابلیت‌های Future نباید
    به‌عنوان رفتار الزامی V1 تفسیر شوند.

-   • Session Behavior --- وضعیت: نهایی. خروج از Session باعث ازبین‌رفتن
    پاسخ‌های ثبت‌شده نمی‌شود؛ Resume دقیق موقعیت Queue در V1 الزام نیست و
    Session بعدی جدید است.

-   • Difficulty --- وضعیت: نهایی برای V1. threshold_difficulty برابر 3
    است و تغییر آن توسط کاربر در V1 در Scope نیست.

-   • New Word Policy --- وضعیت: نهایی برای V1. Daily New Word Limit در
    V1 وجود ندارد.

-   در این مرحله موردی که با اطمینان کافی بتوان آن را به‌عنوان خطای قطعیِ
    توضیحات حذف کرد، بدون بررسی دستی حذف نشده است. هر مورد مشکوک بعدی
    باید در همین بخش ثبت شود. -
    -----------------------------------------------------------------------
    بخش فناوری -----------------------------------
    ----------------------------------- رابط کاربری Jetpack Compose +
    Material 3

-   برنامه‌نویسی غیرهمزمان Coroutines + Flow

-   تزریق وابستگی Hilt

-   Intent رویدادهای کاربر، مثلاً LoadData، SubmitAnswer، UpdateSearch.

-   Language / LanguagePair code/name ... ، id، تعریف زبان‌ها؛ فعلاً فقط
    sourceLanguage، یک جفت‌زبان فعال. targetLanguage،\
    isActive

-   
      Ca   tegory / Tag id، name، ... طبقه‌بندی و برچسب‌گذاری.
      ---- ---------------------------------------------------
           نقش Light Dark

    Primary #4F46E5 #818CF8

-   Secondary #14B8A6 #2DD4BF

-   Background #FAFAFA #121212

-   Surface #FFFFFF #1E1E1E

-   Success #22C55E #4ADE80

-   Error #EF4444 #F87171

-   Warning #F59E0B #FBBF24
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    Difficulty رنگ -----------------------------------
    ----------------------------------- EASY #10B981

-   MEDIUM #F59E0B

-   HARD #EF4444

-   VERY_HARD #8B5CF6
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    Stage رنگ -----------------------------------
    ----------------------------------- DAILY #3B82F6

-   WEEKLY #8B5CF6

-   MONTHLY #EC4899

-   LEARNED #10B981
    ----------------------------------------------------------------------- -
    ----------------------------------------------------------------------------
    \# سناریو رفتار مورد انتظار -----------------------
    ----------------------- ---------------------------- ۱ تغییر ساعت
    گوشی همه زمان‌ها بر اساس System.currentTimeMillis() ذخیره می‌شوند؛
    نیازی به اقدام خاص نیست.

-   ۳ خاموش‌شدن ناگهانی برنامه پاسخ‌های ثبت‌شده در دیتابیس حین مرور باقی
    می‌مانند؛ پاسخ ثبت‌نشده از بین می‌رود و کاربر باید دوباره تلاش کند.
    Session ناتمام با endedAt=null در دیتابیس باقی می‌ماند و در آمار
    به‌عنوان جلسه ناقص ثبت می‌شود.

-   ۸ نبود ترجمه هنگام افزودن کاربر باید حداقل یک ترجمه دستی وارد کند؛
    در غیر این صورت خطا نمایش داده می‌شود. -
    -----------------------------------------------------------------------
    ابزار نسخه -----------------------------------
    ----------------------------------- Android Studio Hedgehog (نسخهٔ
    تاریخی) یا بالاتر

-   JDK 17

-   Gradle نسخهٔ تاریخی یا بالاتر

-   Kotlin نسخهٔ تاریخی

-   Android SDK API Level 34
    ----------------------------------------------------------------------- -
    ----------------------------------------------------------------------------------
    مورد وضعیت دلیل ------------------------- -------------------------
    ------------------------------ هوش مصنوعی (AI/ترجمه) حذف‌شده از نسخه
    اول تصمیم بر آفلاین‌بودن کامل برنامه و عدم وابستگی به سرویس خارجی

-   Notifications/Reminders حذف‌شده خارج از دامنه v1

-   مرحله NEW حذف‌شده ساده‌سازی چرخه یادگیری به
    DAILY/WEEKLY/MONTHLY/LEARNED

-   الگوریتم قدیمی امتیازدهی جایگزین با الگوریتم ۳ دقت و پیش‌بینی‌پذیری
    بیشتر Difficulty پاسخ متوالی

-   فرمول ساده درصد پیشرفت جایگزین با مدل مرحله‌ای انگیزشی‌تر بودن؛ نمایش
    پیشرفت (Learned÷Total) امتیازدهی حتی پیش از Learned شدن

-   زبان‌های بیش از سه‌تا محدود به تمرکز روی نسخه اول؛ معماری
    فارسی/انگلیسی/اسپانیایی همچنان چندزبانه باقی می‌ماند در v1\
    ----------------------------------------------------------------------------------

-   ۳ نام کلید تنظیم آستانه نام نهایی Setting Key سختی برابر
    threshold_difficulty است.

-   ۴ محدودیت تعداد کلمات در V1 محدودیت روزانه جدید در روز برای New
    Words وجود ندارد. این قابلیت به نسخه‌های آینده موکول شده است.
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    \# تغییر -----------------------------------
    ----------------------------------- ۱ افزوده شدن مدیریت حالت‌های خاص
    (Edge Cases)

-   ۳ افزوده شدن نسخه‌گذاری داده‌ها (Data Versioning)

-   ۴ افزوده شدن پشتیبانی از چند جفت‌زبان فعال (Future)

-   ۵ افزوده شدن راهنمای توسعه‌دهندگان

-   ۶ افزوده شدن تنظیم تعداد پاسخ‌های متوالی (Difficulty Calibration)

-   ۹ حذف سیستم اعلان و یادآوری (Notifications/Reminders)
    ----------------------------------------------------------------------- -
    -----------------------------------------------------------------------
    \# تغییر -----------------------------------
    ----------------------------------- ۱ اصلاح ترتیب مرور به Daily →
    Weekly → Monthly

-   ۲ برچسب‌گذاری الگوریتم Anki/SM-2 به‌عنوان \[OPTIONAL - Future
    Extension\]

-   ۴ تعریف کلاس VocabularyViewModel (جدید)

-   ۹ حذف فیلد progressScore از مدل دامنه (جایگزین با مدل مرحله‌ای
    محاسباتی)
    -----------------------------------------------------------------------

-   FlashLearn --- End-to-End Integration

-   Version: نسخهٔ مستندشده

-   4.  Integration Gates

-   -   Verify atomic state/history persistence.

-   -   Verify Statistics/Progress reflect accepted activity.

-   -   This phase supplies the E2E integration contract and gate
        checklist.

-   6.  Next Stage

-   FlashLearn --- Final Cross-Consistency Audit + Hardening

-   Version: نسخهٔ مستندشده

-   1.  Three-Way Consistency

-   -   Legacy/conflicting implementation material must not be
        executable.

-   -   No uncertain material is silently deleted.

-   2.  Domain Integrity

-   -   Statistics and Progress remain read-side consumers.

-   -   No unsupported automatic Tag backfill is introduced.

-   -   ViewModels orchestrate; they do not implement domain rules.

-   6.  Hardening

-   -   Empty/null paths are considered.

-   7.  Final Gate

-   FlashLearn --- Final DI / Service Wiring

-   Version: نسخهٔ مستندشده

-   1.  Purpose

-   -   Complete the dependency-construction boundary after Phases 1--9.

-   2.  Dependency Direction

-   -   core provides shared DI infrastructure.

-   -   State and history writes remain atomic transactions.

-   -   Statistics and Progress remain read/aggregation concerns.

-   5.  Presentation Wiring

-   -   ViewModels are orchestrators only.

-   -   AppViewModel owns route selection only.

-   -   Statistics/Progress ViewModels consume their UseCases rather
        than recalculating business rules.

-   -   Static contract files and wiring boundaries are supplied.

-   -   This is not a claim of successful Gradle/Hilt compilation.

-   8.  Next Stage

-   Version: نسخهٔ مستندشده

-   -   Gradle wrapper validation.

-   -   Hilt/annotation processing.

-   -   APK assembly and artifact retention after success.

-   -   ./gradlew clean

-   -   ./gradlew assembleDebug

-   4.  Preconditions

-   -   Do not upload this contract-only package as if it were the
        complete app.

-   -   Do not delete previous source artifacts until final replacement
        mapping is confirmed.

-   5.  Next Step

-   1.  TypeConverters

-   2.  Entities

-   Accents and punctuation are preserved.

-   8.  Phase 3 Validation Gate

-   □ All nine entities are registered.

-   □ Due queries exclude LEARNED and future-due rows.

-   9.  Phase 4 Revalidation Gate

-   10. Next Step

-   FlashLearn --- Phase 4: UseCases

-   1.  Stage Status

-   5.  Persistence / Compatibility

-   9.  Output

-   1.  Scope

-   Added Android string resources for Persian (default) and English
    (values-en).

-   Application label now comes from @string/app_name.

-   Decorative icons remain eligible for null descriptions when an
    adjacent text label already conveys the same action.

-   No Learning State change.

-   PASS --- values/strings.xml exists.

-   PASS --- values-en/strings.xml exists.

-   PASS --- Manifest uses the localized application label.

-   7.  Next Stage

-   2.  Fixed Audio Contract

-   Word Recognition: Spanish audio question + Spanish answer options;
    the user identifies the exact Spanish word heard.

-   Meaning Recognition: Spanish audio question + Persian answer
    options; the user identifies the Persian meaning of the Spanish word
    heard.

-   The answer-option language never changes the audio-question
    language.

-   Replay repeats the current question and does not create a new
    question or alter its correct answer.

-   4.  Persistence

-   PASS --- Word Recognition uses Spanish audio and Spanish options.

-   PASS --- Meaning Recognition uses Spanish audio and Persian options.

-   PASS --- Replay preserves the current question.

-   نسخهٔ مستندشده → Full Integration Audit

-   FlashLearn --- Full Integration Audit --- Stage Output

-   5.  Invariants

-   PASS --- Presentation features do not own learning persistence.

-   PASS --- Spanish audio semantics remain stable.

-   4.  Invariants

-   Audio question language remains Spanish.

-   A configured workflow is not the same as a successful workflow
    execution.

-   The candidate preserves the نسخهٔ مستندشده foundation and subsequent
    stage contracts.

-   3.  Stability Boundary

-   5.  Next Stage

-   1.  Certification Scope

-   2.  Functional Contract

-   Word Recognition = Spanish audio + Spanish answers.

-   Meaning Recognition = Spanish audio + Persian answers.

-   3.  Persistence Contract

-   A workflow file alone is not evidence of a successful run.

-   PASS --- نسخهٔ مستندشده remains the complete foundation and is not
    summarized or replaced.

-   PASS --- نسخهٔ مستندشده integration boundary documented.

-   6.  Final Boundary

# 28. ضمیمه --- قراردادهای پیاده‌سازی و جزئیات تکمیلی

این بخش برای جلوگیری از حذف جزئیات مهمی است که در اسناد منبع پراکنده
بوده‌اند. موارد تکراری در منابع یک‌بار ادغام شده‌اند؛ مواردی که در منابع
به‌عنوان Legacy/Superseded یا نیازمند Verification معرفی شده‌اند، در بخش
مرزهای اثبات نگه داشته شده‌اند.

## 28.1 قراردادهای DAO/Database

DAOهای اصلی باید حداقل قراردادهایی برای:

-   Concept
-   Content
-   LearningState
-   DifficultyState
-   ConceptTag
-   ReviewHistory
-   ReviewSession
-   Tag
-   Settings

داشته باشند.

`getAllActive()` فقط زمانی مجاز است که واقعاً محدود و مناسب Dataset مورد
نظر باشد؛ برای مسیرهای پرتکرار بزرگ، Queryهای هدفمند و Aggregate ترجیح
دارند.

`LearningStateDao` باید Due Queryهای stage/time را پشتیبانی کند.

`ReviewHistoryDao` باید Duplicate Detection و Queryهای scoped به
Concept/Session/Time Window را پشتیبانی کند.

------------------------------------------------------------------------

## 28.2 Mapperها

Mapper فقط تبدیل:

``` text
Entity ↔ Domain
```

را انجام دهد.

Business Logic در Mapper قرار نگیرد.

------------------------------------------------------------------------

## 28.3 Type Converters

برای شناسه‌ها و زمان‌ها، Converterهای پایدار لازم است؛ UUID و Instant باید
Round-trip صحیح داشته باشند.

------------------------------------------------------------------------

## 28.4 Dependency Direction

قرارداد مورد انتظار:

``` text
app/core/presentation
        ↓
domain
        ↑
data
        ↓
database
```

مرزهای دقیق Module باید با Dependencyهای واقعی پروژه یکسان باشند و هیچ
Layer پایین‌تر نباید به UI وابستگی پیدا کند.

------------------------------------------------------------------------

## 28.5 UI State

UI State باید محدود به اطلاعات مورد نیاز UI باشد.

نباید Collectionهای به اندازهٔ کل Database را به‌عنوان UI State حمل کند.

تنظیمات UI موقتی از تنظیمات محصولی Persisted جدا باشند.

------------------------------------------------------------------------

## 28.6 UI/UX Contracts

موارد ثبت‌شده در منابع:

1.  پرچم تکراری در Home نباشد.
2.  متن خوش‌آمدگویی اضافی حذف شود.
3.  اعداد Hardcoded در UI برای تنظیمات محصولی استفاده نشوند؛ از
    UiState/Settings بیایند.
4.  علامت `+` اضافی حذف شود.
5.  متن‌های تکراری مانند «کلمات منتظر» حذف یا ادغام شوند.
6.  کارت‌های دوخطی غیرضروری جمع‌وجور شوند.
7.  متن‌های طولانی در UI خلاصه و قابل خواندن باشند.
8.  در حالت چهارگزینه‌ای Hint و Show Note در صورت وجود Contract مربوطه
    قابل دسترسی باشند.
9.  Notes پشت Flashcard قابل مشاهده باشند.
10. Parentheses معنی‌دار در صورت قرارداد محتوایی می‌توانند به Note منتقل
    شوند، اما Parser نباید کورکورانه متن اصلی را تغییر دهد.
11. Library دارای Refresh باشد.

------------------------------------------------------------------------

## 28.7 Navigation

مقصدهای اصلی مستندشده شامل حوزه‌هایی مانند:

-   Home
-   Review
-   Needs Review
-   Progress
-   Settings
-   About
-   Add Word
-   Bulk Import
-   Backup
-   Library
-   Category Selection
-   Help

هستند.

Navigation نباید با ورود/خروج از Help یا Settings ReviewHistory ایجاد
کند.

------------------------------------------------------------------------

## 28.8 Edge Cases

مواردی که باید صریحاً تست شوند:

-   Concept بدون Content
-   Content بدون Concept
-   LearningState بدون Concept
-   DifficultyState بدون Concept
-   Concept فعال بدون State
-   Duplicate Review Attempt
-   Duplicate Content
-   Duplicate Tag relation
-   Empty Import
-   Malformed Import
-   Orphan Source
-   Orphan Translation
-   Restore با UUID تکراری
-   Restore با Reference نامعتبر
-   Spanish TTS unavailable
-   Rotation وسط Review
-   Process Death وسط Import
-   Cancel وسط Batch
-   Concurrent mutation
-   Very Large Dataset

------------------------------------------------------------------------

## 28.9 Source Audit Boundary

ممیزی‌های منبع در بعضی Snapshotها صراحتاً موارد زیر را به‌عنوان موضوعات
نیازمند Verification یا اصلاح ثبت کرده‌اند:

-   Full Dataset materialization در Library
-   Full ReviewHistory در Progress
-   Full Concept loading برای Category Counts
-   Full candidate materialization در Review Queue
-   Full QuizBank materialization در Dataset بزرگ
-   Hard-coded Theme palettes در Snapshotهای قبل از Consolidation
-   Exhaustive localization coverage
-   Exhaustive accessibility verification
-   Independent Help coverage
-   Direct verification of Room transaction implementation
-   Actual CI run success

این موارد در یک Specification نباید به‌عنوان «حل‌شده» فرض شوند مگر
Evidence اجرای جدید وجود داشته باشد.

------------------------------------------------------------------------

# 29. ضمیمه --- Release Acceptance Matrix

  حوزه             معیار پذیرش
  ---------------- ----------------------------------------------------
  Offline          مسیرهای اصلی بدون Network کار کنند
  Architecture     Dependency Direction و Module Contracts روشن باشند
  Data Integrity   Constraint، Index، FK و Transaction درست باشند
  Learning         Transition دقیقاً طبق قرارداد باشد
  Difficulty       مستقل و deterministic باشد
  Review           Duplicate/Due/Atomicity پوشش داده شود
  Scalability      Full Dataset Load در عملیات عادی ممنوع باشد
  Performance      Benchmark واقعی وجود داشته باشد
  Reliability      Crash/Retry/Process Death دادهٔ ناقص نسازد
  Import           Streaming/Batch و Cancel امن باشد
  Restore          Auto Backup + Transaction + Integrity Check
  Migration        Schema و Data Migration جدا باشند
  Theme            Appearance تحت ThemeSpec/ThemeTokens باشد
  Localization     Resource-based و presentation-only
  Accessibility    semantics/touch/font/non-color cues
  Help             مستقل و presentation-only
  TTS              Spanish Audio Contract ثابت
  Quiz             چهار گزینه، یک Correct، سه Distractor معتبر
  Testing          Unit/Integration/DB/Migration/Recovery/UI/E2E
  CI               Run واقعی برای PASS لازم است
  Runtime TTS      Spanish voice روی دستگاه باید واقعاً بررسی شود

------------------------------------------------------------------------

# 30. ممیزی پوشش نهایی

این ممیزی برای خواننده است تا بداند چه حوزه‌هایی در سند نهایی پوشش داده
شده‌اند:

  -----------------------------------------------------------------------
  حوزه                                وضعیت پوشش
  ----------------------------------- -----------------------------------
  تعریف محصول و محدوده                پوشش کامل

  Concept / Content / LearningState / پوشش کامل
  Difficulty / History / Session      

  Daily / Weekly / Monthly / Learned  پوشش کامل

  تفاوت Learning Stage / Concept      پوشش کامل
  Difficulty / Test Difficulty        

  Invariants و قوانین تغییرناپذیر     پوشش کامل

  معماری و مرز لایه‌ها                 پوشش کامل

  Canonical Key و هویت داده           پوشش کامل

  Transaction و Data Integrity        پوشش کامل

  Library / Search / Filter / Paging  پوشش کامل

  Parser / Language Detection / State پوشش کامل
  Machine                             

  Import / Bulk Import / Cancel /     پوشش کامل
  Retry                               

  Duplicate / Merge / Idempotency     پوشش کامل

  MCQ / Distractor / Translation      پوشش کامل
  Grouping / Fallback                 

  Review Session / Feedback / Auto    پوشش کامل
  Advance                             

  Statistics / Progress / Goal /      پوشش کامل
  Streak / Achievement                

  Theme / Design System / Import /    پوشش کامل
  Export                              

  Localization / RTL-LTR /            پوشش کامل
  Accessibility                       

  Help / Onboarding / Contextual Help پوشش کامل

  TTS و قرارداد Quiz صوتی             پوشش کامل

  Backup / Restore / Integrity Check  پوشش کامل

  Migration / Refresh / Update بدون   پوشش کامل
  از دست رفتن داده                    

  Performance / Scalability / Query   پوشش کامل
  Contracts                           

  Crash / Retry / Process Death /     پوشش کامل
  Concurrency / Recovery              

  Security / Privacy / Offline        پوشش کامل
  Boundary                            

  Unit / Integration / Database /     پوشش کامل
  Migration / UI / E2E / Stress       

  GitHub CI و مرز Runtime Evidence    پوشش کامل

  الگوریتم‌های اصلی                    در Technical Reference انتهای سند

  جزئیات تکمیلی استخراج‌شده از منابع   حفظ شده در بخش تکمیلی

  مرز بین Source Audit و Runtime      صریحاً مشخص شده
  Evidence                            
  -----------------------------------------------------------------------

**قاعدهٔ نهایی:** این سند باید به‌تنهایی برای فهم قرارداد محصول، معماری،
داده، رفتار، الگوریتم‌ها، محدودیت‌ها و معیارهای پذیرش کافی باشد؛ منابع
تاریخی برای فهم متن اصلی الزامی نیستند.

# 31. Definition of Done

این Specification زمانی از نظر محتوایی کامل تلقی می‌شود که:

-   Product Scope روشن باشد.
-   Learning Cycle روشن باشد.
-   Stage/Difficulty/Test Difficulty از هم جدا باشند.
-   Invariantها مشخص باشند.
-   Architecture و Dependency Boundaries مشخص باشند.
-   Data Model و Referential Integrity مشخص باشند.
-   Review Registration اتمیک و Idempotent باشد.
-   Parser/Import/Export قرارداد مشخص داشته باشند.
-   Quiz قرارداد چهارگزینه‌ای مشخص داشته باشد.
-   Library/Review/Progress برای Dataset بزرگ طراحی شده باشند.
-   Backup/Restore/Migration قرارداد Recovery داشته باشند.
-   Theme مستقل و Versioned باشد.
-   Localization/Accessibility مستقل از Learning باشند.
-   Help مستقل از Learning Engine باشد.
-   TTS مستقل از Persistence باشد.
-   Test Pyramid و Invariant Coverage مشخص باشند.
-   CI و Runtime Evidence مرز مشخص داشته باشند.
-   Algorithmهای اصلی در انتهای همین سند مستند باشند.
-   هیچ تصمیم مهمی برای فهم رفتار سیستم وابسته به یک فایل خارجی نباشد.

------------------------------------------------------------------------

# پایان سند

# 27. Technical Reference --- الگوریتم‌های فنی اصلی

> از این بخش به بعد جزئیات دقیق Algorithmها قرار دارد. این بخش عمداً در
> انتهای سند است تا خواننده ابتدا مدل محصول و قراردادها را بفهمد.

## 27.1 Learning Transition Algorithm

### ورودی

-   `currentStage`
-   `isCorrect`
-   `currentTime`
-   `currentMonthlyWrongCount`
-   `currentHasPathFailure`

### خروجی

``` text
TransitionResult {
    newStage
    nextReviewAt
    hasPathFailure
    monthlyWrongCount
}
```

### قواعد

``` text
LEARNED + any
    → LEARNED
    → nextReviewAt = null

DAILY + Correct
    → WEEKLY
    → nextReviewAt = currentTime + 7 days

DAILY + Wrong
    → DAILY
    → nextReviewAt = startOfNextCalendarDay(currentTime)

WEEKLY + Correct
    → MONTHLY
    → nextReviewAt = currentTime + 30 days

WEEKLY + Wrong
    → DAILY
    → nextReviewAt = startOfNextCalendarDay(currentTime)
    → hasPathFailure = true

MONTHLY + Correct
    → LEARNED
    → nextReviewAt = null

MONTHLY + Wrong
    → DAILY
    → nextReviewAt = startOfNextCalendarDay(currentTime)
    → hasPathFailure = true
    → monthlyWrongCount = oldMonthlyWrongCount + 1
```

`monthlyWrongCount` در سایر حالت‌ها بدون تغییر باقی می‌ماند.

`hasPathFailure` اگر قبلاً True بوده، با Review موفق معمولی False نمی‌شود.

### شبه‌کد

``` text
FUNCTION LearningTransition(stage, correct, now, monthlyWrong, pathFailure):

    IF stage == LEARNED:
        RETURN LEARNED, null, pathFailure, monthlyWrong

    IF correct:
        IF stage == DAILY:
            RETURN WEEKLY, now + 7d, pathFailure, monthlyWrong

        IF stage == WEEKLY:
            RETURN MONTHLY, now + 30d, pathFailure, monthlyWrong

        IF stage == MONTHLY:
            RETURN LEARNED, null, pathFailure, monthlyWrong

    ELSE:
        IF stage == DAILY:
            RETURN DAILY, startOfNextCalendarDay(now),
                   pathFailure, monthlyWrong

        IF stage == WEEKLY:
            RETURN DAILY, startOfNextCalendarDay(now),
                   true, monthlyWrong

        IF stage == MONTHLY:
            RETURN DAILY, startOfNextCalendarDay(now),
                   true, monthlyWrong + 1
```

------------------------------------------------------------------------

## 27.2 Due Rule

برای:

-   Daily
-   Weekly
-   Monthly

شرط:

``` text
nextReviewAt IS NOT NULL
AND
nextReviewAt <= now
```

پس:

-   `nextReviewAt < now` → Due
-   `nextReviewAt == now` → Due
-   `nextReviewAt > now` → Not Due
-   `nextReviewAt == null` → Not Eligible

برای Learned، `nextReviewAt` در انتخاب Learned نادیده گرفته می‌شود.

`now` باید در ابتدای عملیات Selection گرفته شود و در طول همان اجرای
Algorithm تغییر نکند.

------------------------------------------------------------------------

## 27.3 Card Selection / SelectReviewQueue

### ورودی

``` text
reviewType:
    DAILY
    WEEKLY
    MONTHLY
    LEARNED

filters:
    difficulty?
    category?
    tag?
    languagePair?

now
```

### Candidate Set

Daily:

``` text
stage = DAILY
AND nextReviewAt IS NOT NULL
AND nextReviewAt <= now
```

Weekly:

``` text
stage = WEEKLY
AND nextReviewAt IS NOT NULL
AND nextReviewAt <= now
```

Monthly:

``` text
stage = MONTHLY
AND nextReviewAt IS NOT NULL
AND nextReviewAt <= now
```

Learned:

``` text
stage = LEARNED
```

### فیلترها

همهٔ فیلترهای فعال با AND ترکیب می‌شوند:

``` text
difficulty == selectedDifficulty
AND category == selectedCategory
AND ConceptTag contains selectedTag
AND languagePair == selectedLanguagePair
```

### Duplicate Concept

``` text
distinctBy(concept.id)
```

هر Concept فقط یک بار در Session قرار می‌گیرد.

### Sort

برای Daily/Weekly/Monthly:

``` text
nextReviewAt ASC
concept.id ASC
```

یعنی کارت قدیمی‌تر ابتدا می‌آید.

برای Learned:

``` text
shuffle()
```

`nextReviewAt` در Learned برای Sort استفاده نمی‌شود.

### خروجی خالی

اگر هیچ Candidate باقی نماند:

``` text
return []
```

این Error نیست.

### Read-only

SelectReviewQueue نباید:

-   Stage
-   nextReviewAt
-   Difficulty
-   counters
-   Concept
-   LearningState
-   DifficultyState
-   Statistics

را تغییر دهد.

------------------------------------------------------------------------

## 27.4 Difficulty Calculation

### Levels

``` text
EASY
MEDIUM
HARD
VERY_HARD
```

### Threshold

مقدار پیش‌فرض:

``` text
threshold_difficulty = 3
```

Threshold باید از Settings خوانده شود و مقدار معتبر آن حداقل 1 است.

در قرارداد پایه، UI نسخهٔ اولیه برای تغییر Threshold الزام نشده است.

### شمارنده‌ها

-   `consecutiveCorrect`
-   `consecutiveWrong`

پاسخ صحیح:

-   `consecutiveCorrect += 1`
-   `consecutiveWrong = 0`

پاسخ غلط:

-   `consecutiveWrong += 1`
-   `consecutiveCorrect = 0`

### رسیدن به Threshold

اگر شمارنده به Threshold برسد:

-   فقط یک Step تغییر Difficulty انجام شود.
-   هر دو Counter صفر شوند.

### مرزها

Easy پایین‌تر نمی‌رود.

Very Hard بالاتر نمی‌رود.

### One Step Easier

``` text
VERY_HARD → HARD
HARD      → MEDIUM
MEDIUM    → EASY
EASY      → EASY
```

### One Step Harder

``` text
EASY      → MEDIUM
MEDIUM    → HARD
HARD      → VERY_HARD
VERY_HARD → VERY_HARD
```

### تغییر Level

هر تغییر Level:

``` text
consecutiveCorrect = 0
consecutiveWrong = 0
```

### Flag تاریخی

اگر Difficulty به Very Hard برسد:

``` text
hasReachedVeryHard = true
```

و این Flag بعداً False نمی‌شود.

------------------------------------------------------------------------

## 27.5 Forced Difficulty Exceptions

این بخش بر مسیر عادی Threshold اولویت دارد.

### Weekly Wrong

``` text
Difficulty >= MEDIUM
```

یعنی:

-   اگر Easy → Medium
-   اگر Medium/Hard/Very Hard → سطح فعلی حفظ می‌شود.

هر دو Counter صفر می‌شوند.

### Monthly Wrong

بعد از افزایش `monthlyWrongCount`:

``` text
newMonthlyWrongCount == 1
    → HARD

newMonthlyWrongCount >= 2
    → VERY_HARD
```

هر دو Counter صفر می‌شوند.

اگر Very Hard شد:

``` text
hasReachedVeryHard = true
```

### اولویت

ترتیب:

1.  Forced Weekly/Monthly
2.  Normal consecutive threshold

این دو مسیر نباید برای یک Review Event هم‌زمان DifficultyState را دوبار
تغییر دهند.

------------------------------------------------------------------------

## 27.6 قرارداد بین Learning Transition و Difficulty

`monthlyWrongCountBefore` باید **مقدار قبل از اجرای Transition** باشد.

ترتیب منطقی:

``` text
load LearningState
load DifficultyState

validate state existence

detect duplicate attempt

validate due

transition = calculateLearningTransition(...)

difficulty = calculateDifficulty(
    oldDifficulty,
    answer,
    reviewType,
    monthlyWrongCountBefore,
    threshold
)

persist:
    LearningState
    DifficultyState
    ReviewHistory
inside one transaction
```

------------------------------------------------------------------------

## 27.7 SubmitReviewAnswer

### پیش‌شرط

اگر Concept فعال باشد ولی LearningState یا DifficultyState وجود نداشته
باشد:

``` text
DATA_INTEGRITY_ERROR
```

هیچ State جدیدی خودکار ساخته نمی‌شود.

### Duplicate

Unique key:

``` text
(sessionId, reviewAttemptId)
```

Duplicate باید قبل از اعمال Transition جدید تشخیص داده شود.

### Atomicity

``` text
BEGIN TRANSACTION

read states
validate duplicate
validate due
calculate transition
calculate difficulty
update learning
update difficulty
insert history

COMMIT
```

هر Exception:

``` text
ROLLBACK
```

------------------------------------------------------------------------

## 27.8 CreateConcept

``` text
BEGIN TRANSACTION

create Concept
create Content(s)
create LearningState(stage = DAILY)
create DifficultyState(current = EASY)

COMMIT
```

هر Failure:

``` text
ROLLBACK
```

Concept ناقص نباید به‌عنوان Concept معتبر باقی بماند.

------------------------------------------------------------------------

## 27.9 Quiz Question Generation

### اصل

Algorithm فقط سؤال را تولید می‌کند.

### مراحل

1.  Concept/Content هدف را مشخص کن.
2.  تمام Translationهای غیرخالی هدف را در یک Target Group جمع کن.
3.  Candidateهای Distractor را پیدا کن.
4.  Concept فعلی را حذف کن.
5.  CanonicalKeyهای تکراری را حذف کن.
6.  Candidateهای همان Session را در صورت قرارداد حذف کن.
7.  Category را در صورت فعال بودن Filter ترجیح بده.
8.  همان Difficulty را ترجیح بده.
9.  Difficultyهای مجاور را در مرحلهٔ بعد بررسی کن.
10. در صورت نیاز کل بانک را بررسی کن.
11. سه Distractor معتبر لازم است.
12. پاسخ صحیح + سه Distractor = چهار گزینه.
13. گزینه‌ها قبل از خروجی Shuffle شوند.

اگر سه Distractor معتبر وجود ندارد:

``` text
Fallback → Flashcard
```

### Non-mutation

این Algorithm نباید هیچ Persistence انجام دهد.

------------------------------------------------------------------------

## 27.10 Parser Pipeline

``` text
RAW IMPORT
    ↓
NORMALIZATION
    ↓
LANGUAGE DETECTOR
    ↓
LINE CLASSIFIER
    ↓
ENTRY STATE MACHINE
    ↓
Translation / Breakdown / Notes
    ↓
ENTRY VALIDATOR
    ↓
DUPLICATE ENGINE
    ↓
RELATIONSHIP ENGINE
    ↓
DATABASE
```

### Normalization

-   Trim
-   whitespace collapse
-   lowercase برای canonical comparison
-   حفظ Accent
-   حفظ punctuation معنی‌دار

### Language Detection

حداقل باید برای:

-   Persian
-   English
-   Spanish
-   Mixed
-   Unknown

قابل استفاده باشد.

### Entry State Machine

Parser نباید یک Regex بزرگ و غیرقابل نگهداری باشد.

Boundaryهای Entry باید بر اساس:

-   numbering
-   line structure
-   language transition
-   translation markers
-   blank/gap
-   known note/grammar patterns

تشخیص داده شوند.

------------------------------------------------------------------------

## 27.11 ResolveConceptForParsedEntry

### خروجی

``` text
ReuseConcept(
    conceptId,
    newContentsToInsert
)

CreateNewConcept(
    allContentsToInsert
)

Conflict(
    matchedConceptIds,
    pieces
)
```

### Match

برای هر Piece:

``` text
SELECT DISTINCT conceptId
FROM Content
WHERE languageCode = piece.languageCode
AND canonicalKey = normalize(piece.text)
```

### چند Match

اگر:

``` text
matchedIds.size > 1
```

→ `Conflict`

و Merge خودکار ممنوع است.

### هیچ Match

``` text
matchedIds.size == 0
→ CreateNewConcept
```

### یک Match

``` text
matchedIds.size == 1
→ ReuseConcept
```

و فقط Contentهای جدید درج شوند.

------------------------------------------------------------------------

## 27.12 Backup Algorithm

### انواع

``` text
VOCABULARY
PROGRESS
FULL
```

Vocabulary شامل:

-   Languages
-   Categories
-   Tags
-   LanguagePairs
-   Concepts
-   Contents

Progress شامل:

-   Settings
-   ReviewSessions
-   ReviewHistory
-   LearningStates
-   DifficultyStates
-   Achievements

Full شامل هر دو مجموعه است.

### Export Metadata

``` text
schemaVersion
exportedAt
backupType
data
```

### UUID

UUID شناسهٔ پایدار بین Backup و Restore است.

Database ID فقط داخلی است.

------------------------------------------------------------------------

## 27.13 Restore Algorithm

### ValidateBackup

قبل از هر Mutation:

1.  Backup موجود باشد.
2.  Schema Version پشتیبانی شود.
3.  ساختار کامل باشد.
4.  UUIDها معتبر باشند.
5.  UUIDهای تکراری تشخیص داده شوند.
6.  Referenceها معتبر باشند.
7.  Required fields معتبر باشند.
8.  Backup Type با جداول موجود سازگار باشد.

Failure:

``` text
Error
```

بدون تغییر Database.

### Auto Backup

قبل از Restore تلاش برای Full Backup فعلی انجام شود.

اگر Auto Backup شکست خورد:

-   به کاربر هشدار داده شود.
-   امکان Cancel وجود داشته باشد.
-   ادامه بدون Auto Backup فقط در صورت Contract صریح مجاز است.

### Transaction

کل Restore در یک Transaction اجرا شود.

Mapهای UUID → Database ID ساخته شوند:

``` text
LanguageUUID → LanguageID
CategoryUUID → CategoryID
TagUUID → TagID
LanguagePairUUID → LanguagePairID
ConceptUUID → ConceptID
ReviewSessionUUID → ReviewSessionID
```

------------------------------------------------------------------------

## 27.14 Restore Dependency Order

``` text
Languages
    ↓
Categories
    ↓
Tags
    ↓
LanguagePairs
    ↓
Concepts
    ↓
Contents
    ↓
ConceptTags
    ↓
ReviewSessions
    ↓
ReviewHistory
    ↓
LearningStates
    ↓
DifficultyStates
    ↓
Settings
    ↓
Achievements
```

اگر Reference ضروری پیدا نشود:

``` text
Exception
→ Rollback
```

پس از Commit:

``` text
CheckIntegrity
```

اجرا شود.

------------------------------------------------------------------------

## 27.15 Statistics Calculation

اصل:

-   Read-only
-   Database/Repository driven
-   بدون Load کل History

محاسبات پایه باید بر اساس Queryهای محدود به Window زمانی انجام شوند.

### Progress

Progress مرحله‌ای می‌تواند بر تعداد Conceptهای هر Stage تکیه کند:

``` text
Daily
Weekly
Monthly
Learned
```

و درصدهای نمایش داده‌شده باید از دادهٔ پایدار استخراج شوند.

### Streak

Streak باید از روزهای دارای Review ثبت‌شده محاسبه شود.

منطق کلی:

1.  روز جاری را بررسی کن.
2.  اگر روز جاری فعالیت معتبر دارد، از آن شروع کن.
3.  در غیر این صورت طبق قرارداد روز قبل بررسی شود.
4.  روزها تا اولین Gap شمارش شوند.

Streak نباید با باز کردن صفحه یا صرفاً شروع Session ایجاد شود.

------------------------------------------------------------------------

## 27.16 Achievement

Achievementها از دادهٔ پایدار مشتق می‌شوند.

نمونهٔ شروط:

``` text
SEVEN_DAY_STREAK
streak >= 7

THIRTY_DAY_STREAK
streak >= 30

MEMORY_BUILDER
learnedCount >= 100

VOCABULARY_BUILDER
conceptCount >= 500

LONG_TERM_MEMORY
monthlyCorrectCount >= 50

VERY_HARD_REACHED
hasReachedVeryHard == true
```

هر Achievement باید فقط یک بار Unlock شود.

------------------------------------------------------------------------

## 27.17 RefreshDataUseCase

### هدف

ارتقای دادهٔ قدیمی بدون حذف و ورود مجدد.

### نسخه‌های مستقل

``` text
CURRENT_CONCEPT_VERSION
CURRENT_CONTENT_VERSION
```

و دو لیست Migration مستقل:

``` text
conceptMigrations
contentMigrations
```

### قانون

Migration نسخه N فقط روی نسخه N-1 اجرا شود.

همهٔ Migrationها:

-   Idempotent
-   محدود به نوع خود
-   مستقل از LearningState
-   مستقل از ReviewHistory

باشند.

### خروجی

``` text
Success(updatedCount)
NoChange
Error(message)
```

### الگوریتم

برای هر Active Concept:

1.  Concept را تا `CURRENT_CONCEPT_VERSION` ارتقا بده.
2.  Contentهای متعلق به Concept را تا `CURRENT_CONTENT_VERSION` ارتقا
    بده.
3.  اگر Concept یا یکی از Contentها تغییر کرد، `updatedCount++`.
4.  اگر هیچ تغییری نبود، NoChange.

### Transaction

کل Refresh باید Transactional باشد.

Failure:

``` text
ROLLBACK
```

### مرز با Room Migration

ترتیب:

``` text
Room Schema Migration
        ↓
Database structure ready
        ↓
RefreshDataUseCase
        ↓
Old data migrated
        ↓
Normal app execution
```

Room Schema Migration ساختار را تغییر می‌دهد.

RefreshDataUseCase دادهٔ داخل رکوردها را تغییر می‌دهد.

------------------------------------------------------------------------

## 27.18 Import Recovery

Import باید وضعیت عملیاتی مشخص داشته باشد:

``` text
NOT_STARTED
RUNNING
SUCCESS
FAILED
CANCELLED
```

### Failure

-   Transaction state قابل پیش‌بینی باشد.
-   Batch boundary صریح باشد.
-   Retry بدون Checkpoint معتبر از ابتدا و ایمن انجام شود.
-   Cancel نباید دادهٔ نیمه‌کاره را به‌صورت تصادفی Commit کند.

### Memory

فایل Import کامل در RAM قرار نگیرد.

------------------------------------------------------------------------

## 27.19 Restore Recovery

-   Auto Backup قبل از Restore
-   Restore هماهنگ همهٔ مجموعه‌های مرتبط
-   Rollback در Failure
-   Snapshot معتبر در صورت نیاز
-   Integrity Check بعد از Commit
-   Restore ناقص = Success نیست

------------------------------------------------------------------------

## 27.20 Migration Recovery

هر Migration باید:

-   From Version مشخص
-   To Version مشخص
-   قابل تست
-   قابل تکرار کنترل‌شده
-   Idempotent در سطح قرارداد خود

باشد.

Failure نباید Database را در وضعیت نامعلوم بگذارد.

بعد از Migration:

-   Integrity Check
-   Regression Test

اجرا شوند.

Migration نباید Learning/Difficulty/History را بدون تصمیم صریح تغییر
دهد.

------------------------------------------------------------------------

## 27.21 Process Death و Lifecycle

State حیاتی نباید فقط داخل Composable یا ViewModel باشد.

Rotation:

-   نباید Review را دوباره ثبت کند.
-   نباید Session state را بی‌دلیل از بین ببرد.

Background:

-   نباید Commit ناقص بسازد.

Process Death:

-   باید از Source of Truth پایدار قابل بازیابی باشد.

File Picker:

-   بازگشت باید State عملیات را درست بازیابی کند.

Low Memory:

-   نباید Data Corruption ایجاد کند.

------------------------------------------------------------------------

## 27.22 Concurrency

Offline بودن به معنی نبود Concurrency نیست.

مسیرهای همزمان ممکن:

-   UI
-   Worker
-   Import
-   Notification
-   Retry

قواعد:

-   دو پاسخ همزمان برای یک Concept نباید State ناسازگار بسازند.
-   دو Import همزمان باید ممنوع یا Serialize شوند.
-   Restore با Mutation همزمان نشود.
-   Migration فقط در حالت امن Database اجرا شود.

------------------------------------------------------------------------

## 27.23 قرارداد Performance برای Queryها

Queryهای حساس باید برای Dataset بزرگ Plan/Index شوند.

موارد مهم:

-   `(stage, nextReviewAt)`
-   `languageCode + canonicalKey`
-   Unique روی `(conceptId, languageCode)`
-   Unique روی `(sessionId, reviewAttemptId)`
-   Queryهای Tag relation
-   Count Queryهای Category
-   Aggregateهای تاریخ‌محور

Index جدید باید با Benchmark قبل/بعد بررسی شود.

------------------------------------------------------------------------

## 27.24 معیار نهایی الگوریتمی

هیچ Algorithm نباید:

-   به UI تصمیم‌گیری بسپارد که چه Stateی باید ذخیره شود.
-   Database را خارج از مرز Use Case تغییر دهد.
-   Presentation را با Business Logic مخلوط کند.
-   اطلاعاتی را که در ورودی نیست اختراع کند.
-   دادهٔ تاریخی را برای راحتی Performance حذف کند.

Algorithm باید تا حد امکان:

-   Pure
-   Deterministic
-   Testable
-   Versionable
-   Replaceable

باشد.

------------------------------------------------------------------------

# 32. Technical Reference --- الگوریتم‌های تکمیلی استخراج‌شده

> این آخرین بخش سند است. موارد زیر جزئیات فنی منبع‌محور هستند که برای
> جلوگیری از حذف هیچ Contract یا قاعدهٔ الگوریتمی نگه داشته شده‌اند.
> متن‌های تکراری قبلاً Deduplicate شده‌اند. مواردی که در منابع با برچسب
> Legacy/Superseded مشخص شده‌اند با همان ماهیت تاریخی حفظ شده‌اند و
> جایگزین قراردادهای اصلی این سند نیستند.

## 32.1 Parser و Vocabulary Import

FlashLearn --- الگوریتم‌های اصلی (Main Algorithms) نسخهٔ مستندشده
استخراج‌شده از فایل ادغام‌شده FlashLearn_ALL_WORD_DOCUMENTS_MERGED_v4.20
--- بدون حذف محتوا. فقط جداسازی الگوریتم‌های اصلی از بقیه توضیحات.
FlashLearn --- Vocabulary Import & Parsing Algorithm نسخهٔ مستندشده
Version: مستندشده Platform: Android Architecture: Offline-First
Processing: کاملاً Deterministic / Local AI / Cloud Dependency: ندارد
Status: FROZEN --- 1. هدف سیستم سیستم Import باید بتواند متن‌های نامنظم و
ترکیبی را دریافت کرده و به‌صورت خودکار: 1. واژه یا عبارت اصلی اسپانیایی
را تشخیص دهد. 2. ترجمه فارسی آن را پیدا کند. 3. شماره‌گذاری، بولت،
علامت‌ها و فرمت‌های غیرضروری را حذف کند. 4. توضیحات، نکات گرامری، مثال‌ها و
تحلیل اجزای عبارت را از واژه اصلی جدا کند. 5. تشخیص دهد چه چیزی Entry
اصلی است و چه چیزی صرفاً توضیح آن است. 6. موارد تکراری را شناسایی کند. 7.
اطلاعات مرتبط با Entry را بدون از بین بردن اطلاعات اصلی ذخیره کند. 8.
تمام عملیات را بدون اینترنت و بدون AI انجام دهد. 2. اصل بنیادی Parser
Parser نباید بر اساس یک Regex بزرگ ساخته شود. ساختار پیشنهادی: RAW TEXT
↓ Normalization Line Segmentation Language Detection Line Classification
Entry Boundary Detection Main Entry Detection Translation Detection
Breakdown Detection Notes / Grammar Detection Entry Type Detection
Duplicate Detection Relationship Detection Validation Vocabulary DB
Parser باید یک State Machine چندمرحله‌ای باشد. 3. مرحله اول ---
Normalization قبل از هرگونه تشخیص، متن باید Normalize شود. 3.1 مواردی که
باید حذف شوند این موارد معنای واژه را تغییر نمی‌دهند: • \* - --- \_ → » «
➜ \# همچنین: 1. 2) 3 - 47: در صورت تشخیص شماره‌گذاری باید شماره از ابتدای
Entry حذف شود. 3.2 مواردی که نباید حذف شوند علائم زیر ممکن است بخشی از
معنی یا ساختار باشند: ¿ ? ¡ ! ... , . : ; ( ) مثلاً: si hubiese... tener
miedo de ... ¿qué quieres? باید حفظ شوند. 4. نرمال‌سازی Unicode متن باید
با Unicode NFC نرمال شود. - فاصله‌های پشت سر هم → یک فاصله - Tab →
Space - Zero Width Space → حذف - Line Endingهای مختلف → "`\n`{=tex}" -
اعداد فارسی → در صورت نیاز به اعداد استاندارد برای تحلیل شماره‌گذاری
تبدیل شوند. ۱. el científico از نظر Parser معادل: 1. el científico است.
اما متن اصلی برای نمایش کاربر باید حفظ شود. 5. تشخیص زبان سیستم حداقل
باید سه حالت داشته باشد: SPANISH PERSIAN MIXED UNKNOWN 5.1 تشخیص فارسی
وجود حروف اصلی فارسی: ا ب پ ت ث ج چ ح خ د ذ ر ز ژ س ش ص ض ط ظ ع غ ف ق ک
گ ل م ن و ه ی امتیاز فارسی را افزایش می‌دهد. 5.2 تشخیص اسپانیایی موارد
زیر امتیاز اسپانیایی را افزایش می‌دهند: á é í ó ú ü ñ ¿ ¡ همچنین کلمات
رایج Function Word: el la los las un una de del que para con por en a y
o pero si como اما این Dictionary نباید به‌تنهایی ملاک باشد. 6. متن MIXED
quiero: می‌خواهم (از querer) این خط: اما این به معنی Entry جدید نیست.
ممکن است: ANALYSIS باشد. بنابراین: «Language Detection به‌تنهایی Line
Classification را تعیین نمی‌کند.» 7. Line Classification هر خط باید ابتدا
به یکی از این انواع تبدیل شود: ENTRY_HEADER TRANSLATION BREAKDOWN NOTE
GRAMMAR_NOTE DERIVATIVE RELATION COMMENT NUMBER SEPARATOR 8.
ENTRY_HEADER خطی که احتمالاً واژه یا عبارت اصلی است. مثال: el científico;
la científica یا: contar con estoy seguro de que todo irá bien no puedes
hacer una tortilla sin romper huevos 9. TRANSLATION ترجمه فارسی Entry
اصلی. دانشمند (مذکر)؛ دانشمند (مؤنث) روی کسی/چیزی حساب کردن مطمئنم که
همه‌چیز خوب پیش می‌رود 10. BREAKDOWN Breakdown توضیح اجزای یک Entry است.
estoy seguro de que: مطمئنم که todo: همه‌چیز irá bien: خوب پیش خواهد رفت
این موارد نباید به‌صورت پیش‌فرض Entry مستقل ایجاد شوند. ساختار: Main Entry
├── Breakdown 1 ├── Breakdown 2 └── Breakdown 3 11. NOTE مواردی مانند:
نکته: توضیحات: احتمال اشتباه: توجه: در شماره ۹ استفاده شد باید به‌عنوان
Note ذخیره شوند. نکته: با esta vez در شماره ۲ هم‌خانواده است. نباید Entry
جدید ایجاد کند. 12. GRAMMAR_NOTE نکته گرامری: این عبارت از زمان گذشته
استفاده می‌کند. باید جدا از Note عمومی ذخیره شود. grammarNotes 13.
DERIVATIVE اگر متن می‌گوید: از ir مشتق شده از ... این به‌صورت پیش‌فرض Entry
جدید نیست. بلکه: DERIVED_FROM رابطه ایجاد می‌کند. irá bien ممکن است دارای
رابطه: irá bien → derived/inflected from → ir اما Parser نباید صرفاً به
دلیل مشاهده‌ی "ir" یک Entry جدید بسازد. 14. COMMENT متن‌هایی مانند: کلمات
این صفحه تنوع بالایی داشتند... در این بخش چند عبارت مهم بررسی می‌شود...
Entry نیستند. باید به: تبدیل شوند. 15. تشخیص Boundary مهم‌ترین بخش Parser
تشخیص شروع Entry جدید است. شماره‌گذاری فقط یکی از Signalها است. 24.
palabra 29. otra palabra 47. tercera palabra نباید فرض شود که: 25 تا 28
وجود دارند. Gap در شماره‌گذاری کاملاً معتبر است. 16. Rule شماره‌گذاری اگر
خط با این الگو شروع شود: \^`\s*`{=tex}`\d+`{=tex}`\s*[\.\):\-]`{=tex}یک
Signal قوی برای شروع Entry است. 24. la esperanza 29. contar con 47. el
enemigo اما: به‌تنهایی Entry نیست. 17. Entry بدون شماره Entry می‌تواند
بدون شماره باشد: روی کسی حساب کردن پس شماره‌گذاری نباید Requirement باشد.
18. State Machine Parser باید State فعلی را نگه دارد. States: IDLE
WAITING_FOR_TRANSLATION READING_ENTRY READING_BREAKDOWN READING_NOTE
READING_COMMENT FINALIZING_ENTRY 19. حالت IDLE Parser در ابتدا: وقتی یک
Entry Candidate پیدا شد: 20. بعد از Entry Parser منتظر ترجمه می‌شود: اگر
خط بعد: باشد: Translation = detected و Entry کامل می‌شود. 21. Entry
چندخطی گاهی Entry اصلی ممکن است چند خط باشد. no puedes hacer una
tortilla sin romper huevos اگر Parser تشخیص دهد که خط دوم ادامه‌ی
اسپانیایی خط اول است، باید: sourceText = "no puedes hacer una tortilla
sin romper huevos" بسازد. 22. تشخیص Translation ترجمه معمولاً یکی از
حالت‌های زیر است: حالت A Spanish Persian حالت B Spanish: Persian حالت C
در حالت C نیز Parser باید بتواند رابطه را تشخیص دهد. 23. Persian Before
Spanish دانشمند el científico Parser نباید صرفاً به دلیل قرارگیری فارسی
در ابتدا آن را Comment تشخیص دهد. باید Candidate Pair ساخته شود:
Candidate A: Persian = دانشمند Candidate B: Spanish = el científico و
سپس رابطه Translation تعیین شود. 24. Translation Score برای هر جفت
Candidate یک امتیاز ساخته شود. +30 Spanish candidate +30 Persian
candidate +20 خطوط مجاور +10 شماره‌گذاری مشترک +10 ساختار مشابه Entryهای
قبلی -30 وجود Marker مربوط به Note -40 وجود توضیح طولانی اگر: score \>=
threshold باشد، Pair به Entry تبدیل شود. Threshold پیشنهادی: 70 25. Main
Entry vs Breakdown این بخش بسیار مهم است. Entry اصلی: و سه خط بعدی:
هستند. 26. Rule مربوط به Colon وجود ":" به‌تنهایی به معنی Entry نیست.
quiero: می‌خواهم می‌تواند Breakdown باشد. اما اگر: 47. quiero می‌خواهم
باشد، احتمال Entry بسیار بیشتر است. «Colon فقط Signal است، نه تصمیم
نهایی.» 27. Breakdown Detection اگر چند خط پشت سر هم ساختار: Spanish :
Persian داشته باشند و قبل از آن یک Main Entry معتبر وجود داشته باشد: →
BREAKDOWN 28. Breakdown Promotion به‌صورت پیش‌فرض: promoteBreakdownToEntry
= false یعنی Breakdown وارد Vocabulary اصلی نمی‌شود. این Option می‌تواند
در آینده قابل فعال‌سازی باشد. 29. Parenthetical Notes la esperanza امید
(در شماره ۹ استفاده شد) sourceText = la esperanza translation = امید
note = در شماره ۹ استفاده شد نباید Parenthetical Note حذف شود. 30.
Parentheses داخل Translation نباید به‌صورت خودکار حذف شود. چون: (مذکر)
(مؤنث) جزء اطلاعات ترجمه هستند. بنابراین Parser فقط زمانی Parentheses را
Note می‌داند که الگوی آن نشان دهد Metadata است. 31. Gender Variants نباید
به‌صورت پیش‌فرض دو Entry کاملاً مستقل شود. ساختار بهتر: sourceVariants: -
el científico - la científica translationVariants: - دانشمند (مذکر) -
دانشمند (مؤنث) و Entry اصلی: entryType = WORD 32. Alternative Separators
برای Variantها این موارد قابل قبول باشند: / یا el enemigo / la enemiga
اما "/" همیشه به معنی Variant نیست. پس Context باید بررسی شود. 33. Entry
Type هر Entry باید یکی از این انواع را داشته باشد: WORD PHRASE SENTENCE
IDIOM COLLOCATION STRUCTURE 34. تشخیص WORD esperanza análisis científico
35. تشخیص PHRASE tener miedo de a no ser que 36. تشخیص SENTENCE اگر
Entry دارای ساختار کامل جمله باشد: به: تبدیل شود. 37. تشخیص IDIOM عبارات
ثابت مانند: estar en las nubes no hay mal que por bien no venga می‌توانند
Candidate برای: باشند. این تشخیص در نسخه اول می‌تواند Rule-Based باشد.
38. تشخیص STRUCTURE a no ser que... که بیشتر یک الگوی زبانی است تا یک
واژه معمولی. 39. جلوگیری از استخراج کلمات داخل توضیحات این فعل از ir
ساخته شده و در اینجا به معنی رفتن است. نباید: ir و: رفتـن به‌عنوان Entry
جدید ساخته شوند. Rule: «وقتی Parser وارد NOTE / GRAMMAR_NOTE / COMMENT
شد، هیچ Candidate داخلی نباید به Entry اصلی Promote شود؛ مگر اینکه
Option مربوطه فعال باشد.» 40. Duplicate Detection Duplicate Detection
باید بعد از Parsing انجام شود. اشتباه است که قبل از Parsing فقط بر اساس
متن خام Duplicate حذف شود. 41. Canonical Key برای هر Source یک:
canonicalKey ساخته شود. " El Científico " تبدیل شود به: اما Accent نباید
حذف شود. 42. Accent مهم است این دو را نباید یکسان فرض کرد: sí él Accent
در اسپانیایی می‌تواند معنی را تغییر دهد. 43. Duplicate Types سیستم حداقل
این حالات را داشته باشد: EXACT_DUPLICATE
SAME_SOURCE_DIFFERENT_TRANSLATION POSSIBLE_DUPLICATE RELATED_FORM 44.
Exact Duplicate در دو Import وجود داشته باشد. نتیجه: 45. Same Source /
Different Translation cura کشیش درمان نباید یکی حذف شود. same source
different meanings و هر دو Translation باید نگهداری شوند. 46. Duplicate
Policy سیستم نباید خودسرانه اطلاعات را حذف کند. Import Mode: ADD_NEW
SKIP_DUPLICATE MERGE UPDATE پیشنهاد Default: 47. Merge اگر Entry جدید
همان Source را داشته باشد: Existing: el cura → کشیش New: → درمان
Translations: - کشیش - درمان 48. Stable ID Duplicate یا Merge نباید ID
قبلی را خراب کند. برای Refresh آینده بسیار مهم است. هر Vocabulary باید:
id داشته باشد. 49. Relationship System ارتباط‌ها: USED_IN INFLECTED_FORM
SYNONYM ANTONYM CONTRAST EXAMPLE_OF RELATED_TO 50. مثال Relationship اگر
متن: اما اگر "ir" هنوز در Database نیست، رابطه می‌تواند به‌صورت Pending
ذخیره شود. 51. Typo Handling Parser نباید متن را اصلاح کند. مثلاً اگر
ورودی: lo melhor باشد، باید همان را ذخیره کند. نه: lo mejor 52. Possible
Correction در صورت نیاز: possibleCorrection = "lo mejor" ذخیره شود.
sourceText = "lo melhor" باید بدون تغییر باقی بماند. 53. Confidence هر
Entry باید Confidence داشته باشد: 0 - 100 95 = بسیار مطمئن 80 = مطمئن 65
= قابل قبول 50 = مشکوک \<50 = نیازمند بررسی 54. Evidence برای Debugging
بهتر است دلیل Confidence ذخیره شود: evidence: - numbered -
spanishDetected - adjacentPersianTranslation - translationMarker -
noteMarkerAbsent این اطلاعات برای توسعه آینده بسیار ارزشمند است. 55.
Validation قبل از Insert: sourceText != empty translationText != empty
sourceLanguage != UNKNOWN باید بررسی شود. 56. Orphan Detection وجود دارد
ولی Translation پیدا نشد: ORPHAN_SOURCE و نباید بدون اجازه حذف شود. 57.
Orphan Persian فارسی وجود دارد ولی Source پیدا نشد: ORPHAN_TRANSLATION و
برای Review/Manual Import نگهداری شود. 58. Import Warning هر مشکل باید
در Import Log ثبت شود: warningType lineNumber rawText message confidence
Line 37: Possible orphan source 59. مدل پیشنهادی Vocabulary data class
VocabularyEntry( val id: Long, val sourceText: String, val canonicalKey:
String, val translationText: String, val sourceLanguage: String, val
targetLanguage: String, val entryType: EntryType, val confidence: Int,
val notes: String?, val grammarNotes: String?, val isDuplicate: Boolean,
val duplicateOf: Long?, val originalImportText: String 60. Variant data
class VocabularyVariant( val vocabularyId: Long, val translationText:
String?, val variantType: VariantType MASCULINE FEMININE ALTERNATIVE 61.
Breakdown data class VocabularyBreakdown( val sourcePart: String, val
translationPart: String, val orderIndex: Int 62. Notes data class
VocabularyNote( val noteType: NoteType, val content: String 63.
Relationship data class VocabularyRelation( val fromVocabularyId: Long,
val toVocabularyId: Long?, val relationType: RelationType, val
unresolvedText: String?, val confidence: Int 64. Import Log data class
ImportIssue( val importId: Long, val lineNumber: Int, val rawText:
String, val issueType: IssueType, val message: String, 65. الگوریتم اصلی
Pseudocode: function parse(text): normalized = normalize(text) lines =
splitIntoLines(normalized) classifiedLines = \[\] for line in lines:
language = detectLanguage(line) type = classifyLine(line, language)
classifiedLines.add(line, language, type) entries = \[\] currentEntry =
null for line in classifiedLines: if isNewEntry(line, currentEntry):
finalize(currentEntry) currentEntry = createEntryCandidate(line) else if
isTranslation(line, currentEntry): attachTranslation(currentEntry, line)
else if isBreakdown(line, currentEntry): attachBreakdown(currentEntry,
line) else if isNote(line): attachNote(currentEntry, line) else if
isGrammarNote(line): attachGrammarNote(currentEntry, line) else:
attachToCurrentContext(line) validate(entries) detectDuplicates(entries)
detectRelations(entries) return entries 66. ترتیب اولویت Rules در صورت
Conflict، اولویت: 1. Explicit Entry Marker 2. Numbered Entry 3. Strong
Entry Pattern 4. Translation Relationship 5. Note Marker 6. Breakdown
Pattern 7. Context 8. Language Score 9. Generic fallback 67. Marker
Dictionary Dictionary باید قابل تغییر باشد. نکته نکته گرامری توضیحات
توجه احتمال اشتباه مقایسه مشتق شده هم‌خانواده ترکیب ترکیب بخش‌ها واژه‌های
جدید کلمات جدید مثال معنی در آینده: nota gramática explicación derivado
sinónimo antónimo ejemplo 68. Marker نباید Hardcoded شود بهتر است:
ParserMarkers داشته باشیم. تا بعداً بدون تغییر Core Parser بتوان
Dictionary را توسعه داد. 69. Configuration ParserConfig( sourceLanguage
= "es", targetLanguage = "fa", promoteBreakdownToEntry = false,
preserveOriginalText = true, detectDuplicates = true, duplicateMode =
MERGE, confidenceThreshold = 70, allowUnnumberedEntries = true 70. اصل
مهم: Preserve Original سیستم باید دو نسخه داشته باشد: originalImportText
normalizedText Original: 24. el científico !!! Normalized: el científico
!!! اطلاعات اصلی هرگز نباید از بین برود. 71. Test Case 1 Input: 1. el
científico; la científica Output: la científica translation: type: 72.
Test Case 2 cuenta conmigo روی من حساب کن اگر هر دو دارای ساختار مستقل
Entry باشند: Entry 1: Entry 2: و در صورت وجود Evidence مناسب: 73. Test
Case 3 Main Entry: Translation: Breakdown: 1. estoy seguro de que →
مطمئنم که 2. todo → همه‌چیز 3. irá bien → خوب پیش خواهد رفت 74. Test Case
4 source = la esperanza 75. Test Case 5 el análisis تحلیل این واژه در
این ساختار استفاده شده است. source = el análisis translation = تحلیل
grammarNote = 76. Test Case 6 اگر قبل از آن Main Entry وجود داشته باشد:
نه Entry مستقل. 77. Test Case 7 3 Entries بدون ایجاد Entry برای: 25 26
27 28 30... 78. Test Case 8 79. Test Case 9 نمی‌توانی برای درست کردن املت
تخم‌مرغ‌ها را نشکنی tortilla قبلاً در شماره ۱۸ ترجمه شد. Note: 80. Test
Case 10 ترجمه... sourceText = lo melhor possibleCorrection = lo mejor و
هرگز: sourceText = lo mejor نشود. 81. Duplicate Test و رکورد دوم بدون
بررسی حذف نشود؛ بلکه Import Policy تصمیم بگیرد. 82. Duplicate با
Translation متفاوت هر دو معنی باید باقی بمانند. 83. مواردی که Parser
نباید انجام دهد Parser نباید: ترجمه را حدس قطعی بزند غلط املایی را
خودکار اصلاح کند واژه داخل Note را Entry کند شماره‌های گمشده را ایجاد کند
Duplicate را بدون Policy حذف کند Accent اسپانیایی را حذف کند Parentheses
را کورکورانه حذف کند Breakdown را Entry مستقل کند 84. Performance چون
برنامه Offline است، Parser باید روی گوشی اجرا شود. هدف: O(n) برای تعداد
خطوط. Duplicate Search باید با: و Index دیتابیس انجام شود. 85. Database
Index روی این فیلد Index ایجاد شود: sourceLanguage targetLanguage ترکیب
پیشنهادی: (sourceLanguage, targetLanguage, canonicalKey) 86. Import
Pipeline در نهایت: Input Normalize Classify Parse Validate Merge / Add /
Skip Save Import Report 87. Import Report بعد از Import باید نتیجه قابل
نمایش باشد: Total Lines: 150 Entries Detected: 42 New Entries: 35
Duplicates: 5 Merged: 3 Warnings: 2 Unresolved: 1 88. Manual Review اگر
Confidence پایین باشد: confidence \< 50 Entry مستقیماً وارد Vocabulary
اصلی نشود و در: Needs Review قرار گیرد. 89. سه سطح Import Auto
confidence \>= 80 → ورود مستقیم Review 50 \<= confidence \< 80 → ورود
همراه با هشدار یا Review Reject/Unresolved → نیازمند بررسی دستی 90. نکته
بسیار مهم درباره Refresh این Parser باید از سیستم Refresh آینده
FlashLearn جدا ولی سازگار باشد. برای هر Vocabulary: نباید با Refresh
تغییر کند. بنابراین اگر نسخه جدید برنامه اطلاعات واژه را اصلاح کرد:
Existing ID Refresh Update metadata / translation / notes / rules نه
اینکه رکورد قدیمی حذف و رکورد جدید ساخته شود. 91. ارتباط Import با
Refresh Import: Text → Vocabulary Refresh: New App Data Compare
canonicalKey Find Existing Vocabulary Apply Versioned Changes
«"canonicalKey" یکی از کلیدی‌ترین فیلدهای کل معماری FlashLearn است.» 92.
اصل نهایی سیستم Parser نباید تلاش کند متن را «زیبا» یا «اصلاح» کند.
وظیفه آن: Detect Separate Relate Preserve یعنی: «اطلاعات را از متن
استخراج کن، نه اینکه اطلاعات جدیدی از خودت بساز.» 93. اولویت پیاده‌سازی
P0 --- ضروری Entry Boundary Note Detection Room DB Output P1 --- مهم
Confidence Evidence Gender Variants Entry Type Relationships Manual
Review P2 --- بعداً Possible Correction Inflection Detection Word Family
Synonym Detection Antonym Detection Advanced Grammar Detection 94. نتیجه
معماری معماری نهایی: ┌─────────────────────┐ │ RAW IMPORT │
└──────────┬──────────┘ │ NORMALIZATION │ │ LANGUAGE DETECTOR │ │ LINE
CLASSIFIER │ │ ENTRY STATE MACHINE│ ┌────────────────┼────────────────┐
↓ ↓ ↓ Translation Breakdown Notes │ │ │
└────────────────┼────────────────┘ │ ENTRY VALIDATOR │ │ DUPLICATE
ENGINE │ │ RELATIONSHIP ENGINE │ │ ROOM DB │ └─────────────────────┘ 95.
اصل Frozen برای نسخه 1.0 برای جلوگیری از تغییرات پراکنده در آینده، این
قواعد باید به‌عنوان Contract نسخه 1.0 در نظر گرفته شوند: 1. Parser کاملاً
Offline است. 2. هیچ AI یا API خارجی در Parsing وجود ندارد. 3. شماره‌گذاری
فقط Signal است. 4. Gap در شماره‌ها معتبر است. 5. Breakdown به‌صورت پیش‌فرض
Entry نیست. 6. Note و Grammar Note Entry نیستند. 7. متن اصلی هرگز خودکار
اصلاح نمی‌شود. 8. Accent اسپانیایی حفظ می‌شود. 9. Parentheses بدون تحلیل
حذف نمی‌شوند. 10. Duplicate بدون Policy حذف نمی‌شود. 11. ID رکوردهای موجود
نباید هنگام Merge/Refresh تغییر کند. 12. "canonicalKey" برای تشخیص رکورد
پایدار استفاده می‌شود. 13. Confidence و Warning برای موارد مبهم ثبت
می‌شوند. 14. اطلاعات استخراج‌شده باید قابل ردیابی به متن اصلی باشند. 15.
Parser نباید اطلاعاتی را که در متن وجود ندارد، به‌عنوان حقیقت تولید کند.

## 32.845 Learning Transition

پایان Specification نسخهٔ مستندشده Learning Transition Algorithm Version:
مستندشده 1. هدف این الگوریتم فقط نتیجه Transition مسیر یادگیری را از روی
Stage فعلی، نتیجه پاسخ و زمان ثبت پاسخ تعیین می‌کند. الگوریتم Pure و
Deterministic است و مستقیماً Database را تغییر نمی‌دهد. 2. مراحل یادگیری
DAILY WEEKLY MONTHLY LEARNED 3. ورودی‌ها currentStage : ReviewStage
isCorrect : Boolean currentTime : Timestamp currentMonthlyWrongCount :
Int currentHasPathFailure : Boolean 4. خروجی TransitionResult { newStage
: ReviewStage, nextReviewAt : Timestamp \| null, hasPathFailure :
Boolean, monthlyWrongCount : Int } 5. قوانین قطعی LEARNED → LEARNED و
nextReviewAt=null؛ پاسخ در LEARNED آن را به چرخه عادی برنمی‌گرداند.
DAILY + Correct → WEEKLY و currentTime + 7 days. WEEKLY + Correct →
MONTHLY و currentTime + 30 days. MONTHLY + Correct → LEARNED و
nextReviewAt=null. DAILY + Wrong → DAILY و
startOfNextCalendarDay(currentTime). WEEKLY + Wrong → DAILY و
startOfNextCalendarDay(currentTime) و hasPathFailure=true. MONTHLY +
Wrong → DAILY و startOfNextCalendarDay(currentTime) و
hasPathFailure=true و monthlyWrongCount=currentMonthlyWrongCount+1.
monthlyWrongCount در همه حالت‌های دیگر بدون تغییر باقی می‌ماند و هرگز
Reset نمی‌شود. hasPathFailure اگر قبلاً true باشد در انتقال موفق دوباره
false نمی‌شود. 6. Pseudocode نهایی FUNCTION
LearningTransition(currentStage, isCorrect, currentTime,
currentMonthlyWrongCount, currentHasPathFailure): IF currentStage ==
LEARNED: RETURN TransitionResult(LEARNED, null, currentHasPathFailure,
currentMonthlyWrongCount) IF isCorrect == true: IF currentStage ==
DAILY: RETURN TransitionResult(WEEKLY, currentTime + 7 days,
currentHasPathFailure, currentMonthlyWrongCount) IF currentStage ==
WEEKLY: RETURN TransitionResult(MONTHLY, currentTime + 30 days,
currentHasPathFailure, currentMonthlyWrongCount) IF currentStage ==
MONTHLY: RETURN TransitionResult(DAILY,
startOfNextCalendarDay(currentTime), currentHasPathFailure,
currentMonthlyWrongCount) RETURN TransitionResult(DAILY,
startOfNextCalendarDay(currentTime), true, currentMonthlyWrongCount)
RETURN TransitionResult(DAILY, startOfNextCalendarDay(currentTime),
true, currentMonthlyWrongCount + 1) 7. مرزبندی با Difficulty Learning
Transition مالک Stage، nextReviewAt، hasPathFailure و monthlyWrongCount
است. DifficultyState توسط Difficulty Calculation Algorithm به‌روزرسانی
می‌شود. قواعد اجباری Difficulty برای شکست WEEKLY/MONTHLY در قرارداد
Difficulty/SubmitReviewAnswer اعمال می‌شوند و نباید دو Update همزمان روی
DifficultyState انجام شود. End of Algorithm

## 32.896 Review Scheduling و Card Selection

# الگوریتم کامل Review Scheduling / Card Selection

REVIEW SCHEDULING / CARD SELECTION ALGORITHM نام الگوریتم:
SelectReviewQueue نسخه: Final انتخاب تمام Conceptهایی که در زمان شروع
Session واجد شرایط مرور هستند، اعمال فیلترهای انتخابی کاربر، و تولید یک
لیست مرتب‌شده برای Review Session. این الگوریتم فقط کارت‌ها را انتخاب
می‌کند و هیچ تغییری در وضعیت یادگیری، Difficulty یا زمان Review ایجاد
نمی‌کند. 1. ورودی‌ها reviewType : Enum مقادیر مجاز: reviewType مشخص می‌کند
کاربر کدام نوع کارت را می‌خواهد مرور کند. این مقدار از انتخاب کاربر در
Home Screen دریافت می‌شود.
------------------------------------------------------------ filters :
Object تمام فیلترها اختیاری هستند. filters.difficulty : Difficulty?
filters.category : CategoryId? filters.tag : TagId? filters.languagePair
: LanguagePairId? مقادیر Difficulty: EASY MEDIUM HARD VERY_HARD اگر یک
فیلتر NULL باشد، آن فیلتر اعمال نمی‌شود. now : Timestamp زمان دقیق شروع
درخواست Review Session. تمام مقایسه‌های زمانی این الگوریتم بر اساس همین
مقدار انجام می‌شوند. الگوریتم نباید در وسط اجرای خود چند بار زمان سیستم
را بخواند. now در ابتدای اجرای الگوریتم تعیین شده و تا پایان همان اجرا
ثابت می‌ماند. 2. خروجی orderedConceptList : List`<Concept>`{=html} لیست
Conceptهای واجد شرایط برای Review. ویژگی‌های خروجی: - می‌تواند خالی
باشد. - فقط Conceptهای واجد شرایط را شامل می‌شود. - هیچ Concept تکراری
نباید در خروجی وجود داشته باشد. - ترتیب خروجی بر اساس reviewType تعیین
می‌شود. اگر هیچ کارت واجد شرایطی وجود نداشته باشد: return \[\] نمایش
پیام‌هایی مانند: "چیزی برای مرور نیست" جزء این الگوریتم نیست و توسط UI
انجام می‌شود. 3. پیش‌شرط‌های داده برای هر LearningState باید یک Concept
مرتبط وجود داشته باشد. هر Concept باید اطلاعات موردنیاز فیلترها را در
اختیار داشته باشد: concept.id concept.categoryId ConceptTag relation
(Concept ↔ Tag) concept.languagePairId LearningState باید حداقل شامل
اطلاعات زیر باشد: stage nextReviewAt و وضعیت Difficulty مربوط به
Concept/یادگیری باید قابل دسترسی باشد. اگر یک LearningState فاقد Concept
مرتبط معتبر باشد، نباید باعث Crash شدن الگوریتم شود. آن رکورد باید از
Candidate Set کنار گذاشته شود. 4. مرحله اول --- ایجاد Candidate Set 4.1.
DAILY reviewType == DAILY آنگاه تمام LearningStateهایی انتخاب می‌شوند که:
stage == DAILY nextReviewAt IS NOT NULL nextReviewAt \<= now candidates
= LearningState WHERE stage == DAILY AND nextReviewAt IS NOT NULL AND
nextReviewAt \<= now 4.2. WEEKLY reviewType == WEEKLY آنگاه: WHERE stage
== WEEKLY 4.3. MONTHLY reviewType == MONTHLY WHERE stage == MONTHLY 4.4.
LEARNED reviewType == LEARNED WHERE stage == LEARNED در حالت LEARNED:
کاملاً نادیده گرفته می‌شود. یعنی حتی اگر: nextReviewAt == NULL باشد، کارت
LEARNED همچنان واجد شرایط است. همچنین اگر: دارای هر مقدار دیگری باشد، در
انتخاب LEARNED هیچ تأثیری ندارد. 5. مرحله دوم --- اتصال LearningState به
Concept برای هر LearningState موجود در candidates: Concept مرتبط با آن
پیدا می‌شود. ساختار منطقی هر Candidate: Candidate { learningState concept
اگر Concept مرتبط پیدا نشود: آن Candidate حذف می‌شود. در این مرحله هیچ
داده‌ای تغییر نمی‌کند. 6. مرحله سوم --- اعمال فیلترها تمام فیلترهای فعال
با منطق AND اعمال می‌شوند. یعنی یک کارت فقط زمانی باقی می‌ماند که تمام
فیلترهای انتخاب‌شده را پاس کند. 6.1. فیلتر Difficulty filters.difficulty
!= NULL candidate.difficultyState.current == filters.difficulty
filters.difficulty == NULL هیچ محدودیتی بابت Difficulty اعمال نمی‌شود.
6.2. فیلتر Category filters.category != NULL
candidate.concept.categoryId == filters.category اگر NULL باشد: تمام
Categoryها مجاز هستند. 6.3. فیلتر Tag filters.tag != NULL CONTAINS
filters.tag یعنی اگر Concept دارای Tag انتخاب‌شده باشد، Candidate واجد
شرایط است. اگر Concept چند Tag داشته باشد، وجود حداقل یک Tag مطابق کافی
است. filters.tag == NULL هیچ محدودیتی بابت Tag اعمال نمی‌شود. 6.4. فیلتر
Language Pair filters.languagePair != NULL
candidate.concept.languagePairId == filters.languagePair تمام Language
Pairها مجاز هستند. 7. شرط نهایی واجد شرایط بودن برای DAILY / WEEKLY /
MONTHLY: یک Candidate فقط در صورتی وارد خروجی می‌شود که: stage ==
reviewType AND difficulty filter را پاس کند category filter را پاس کند
tag filter را پاس کند languagePair filter را پاس کند برای LEARNED: stage
== LEARNED در LEARNED: جزء شروط انتخاب نیست. 8. مرحله چهارم --- حذف
موارد تکراری هر Concept فقط یک بار باید در خروجی وجود داشته باشد. اگر به
هر دلیل بیش از یک LearningState به یک Concept اشاره کند: Concept باید
فقط یک بار در orderedConceptList قرار گیرد. در حالت عادی Data Model باید
رابطه مشخصی بین Concept و LearningState داشته باشد تا چنین تکراری ایجاد
نشود. این قانون برای جلوگیری از نمایش یک کارت به‌صورت تکراری در یک
Session است. 9. مرحله پنجم --- مرتب‌سازی 9.1. DAILY برای: مرتب‌سازی:
nextReviewAt ASC سپس در صورت برابر بودن: concept.id ASC قدیمی‌ترین کارت
ابتدا. 9.2. WEEKLY سپس: 9.3. MONTHLY بنابراین در هر سه حالت: PRIMARY
KEY: SECONDARY KEY: اگر یک کارت مدت بیشتری از زمان تعیین‌شده Review آن
گذشته باشد، قبل از کارت‌هایی قرار می‌گیرد که دیرکرد کمتری دارند. 10.
مرتب‌سازی LEARNED candidates.shuffle() ترتیب کارت‌های LEARNED تصادفی است.
برای مرتب‌سازی نیز استفاده نمی‌شود. برای تعیین ترتیب LEARNED استفاده
نمی‌شود. هدف این است که ترتیب نمایش کارت‌های Learned قابل پیش‌بینی و ثابت
نباشد. 11. مرحله ششم --- تبدیل Candidate به Concept List پس از پایان
فیلتر و مرتب‌سازی: orderedConceptList = candidates.map { candidate -\>
candidate.concept خروجی فقط شامل: Concept LearningState و
DifficultyState در خروجی این الگوریتم بازگردانده نمی‌شوند، مگر اینکه
معماری داخلی برنامه برای اجرای Session به آن‌ها نیاز داشته باشد. 12.
خروجی خالی اگر پس از Candidate Selection و Filtering هیچ کارت باقی
نماند: این حالت خطا محسوب نمی‌شود. DAILY انتخاب شده است. هیچ کارت DAILY
وجود ندارد که: \[\] UI می‌تواند بر اساس خروجی خالی پیام مناسب نمایش دهد.
13. رفتار در برابر nextReviewAt کارت واجد شرایط نیست. nextReviewAt \<
now کارت واجد شرایط است. nextReviewAt == now nextReviewAt \> now کارت
هنوز واجد شرایط نیست. تمام موارد بالا نادیده گرفته می‌شوند. 14. رفتار
Read-Only SelectReviewQueue نباید هیچ‌یک از موارد زیر را تغییر دهد:
difficulty consecutiveCorrect consecutiveWrong DifficultyState
statistics این الگوریتم فقط عملیات Read / Query / Filter / Sort انجام
می‌دهد. 15. عدم تغییر وضعیت کارت انتخاب یک کارت برای Review به معنی شروع
Review واقعی یا ثبت پاسخ کاربر نیست. بنابراین صرفاً قرار گرفتن کارت در:
orderedConceptList هیچ تغییری در Stage یا Difficulty ایجاد نمی‌کند. تغییر
وضعیت فقط پس از پاسخ کاربر و از طریق الگوریتم‌های مربوطه انجام می‌شود. 16.
استقلال از Learning Transition SelectReviewQueue مسئول تعیین Stage نیست.
این الگوریتم فقط Stage فعلی را می‌خواند. تعیین می‌کند که کارت در چه Stage
قرار دارد و nextReviewAt چه زمانی باشد. بر اساس همان وضعیت موجود کارت را
انتخاب می‌کند. هیچ‌کدام جایگزین دیگری نیستند. 17. استقلال از Difficulty
Calculation SelectReviewQueue Difficulty را محاسبه نمی‌کند. Difficulty
قبلاً توسط: Difficulty Calculation Algorithm محاسبه و ذخیره شده است.
SelectReviewQueue فقط در صورت فعال بودن: filters.difficulty از
Difficulty فعلی به‌عنوان فیلتر استفاده می‌کند. 18. استقلال از UI این
الگوریتم نباید درباره موارد زیر تصمیم بگیرد: نمایش پیام نمایش Empty
State نمایش Loading نمایش Error تعداد کارت قابل نمایش در UI طراحی صفحه
دکمه‌ها انیمیشن Progress Bar وظیفه الگوریتم فقط: SELECT FILTER SORT
RETURN 19. Pseudocode نهایی FUNCTION SelectReviewQueue( reviewType,
filters, ): IF reviewType == DAILY OR reviewType == WEEKLY OR reviewType
== MONTHLY: SELECT LearningState WHERE ELSE IF reviewType == LEARNED:
ELSE: RETURN \[\]
-------------------------------------------------------- -- اتصال به
Concept candidates JOIN Concept حذف Candidateهایی که Concept معتبر
ندارند. -- اعمال فیلترها candidates.filter { LEGACY / SUPERSEDED:
Difficulty must be read from candidate.difficultyState.current per final
Data Model contract. filters.category == NULL OR
candidate.concept.categoryId LEGACY / SUPERSEDED: Tag filtering must use
the ConceptTag relation per final Data Model contract.
filters.languagePair == NULL OR candidate.concept.languagePairId -- حذف
Duplicate Concept .distinctBy { candidate.concept.id } -- مرتب‌سازی
candidates.sortedWith( nextReviewAt ASC, -- تبدیل به خروجی
candidates.map { RETURN orderedConceptList END FUNCTION 20. مثال ---
DAILY فرض کنیم now برابر است با: 10 September 2026 - 12:00 سه کارت
داریم: Card A stage = DAILY nextReviewAt = 08:00 Card B nextReviewAt =
11:00 Card C nextReviewAt = 15:00 Card C هنوز زمان Review آن نرسیده است.
21. مثال --- فیلترها فرض: reviewType = WEEKLY filters.difficulty = HARD
filters.category = Animals filters.tag = Important filters.languagePair
= Spanish-Persian فقط کارت‌هایی انتخاب می‌شوند که: stage == WEEKLY
difficulty == HARD category == Animals tag شامل Important languagePair
== Spanish-Persian اگر حتی یکی از این شروط برقرار نباشد، کارت حذف می‌شود.
22. مثال --- LEARNED reviewType = LEARNED کارت‌های زیر: Card A: stage =
LEARNED nextReviewAt = NULL Card B: nextReviewAt = 2027-01-01 Card C:
nextReviewAt = 2026-01-01 هر سه کارت واجد شرایط هستند. زیرا در LEARNED
فقط: بررسی می‌شود. سپس ترتیب آن‌ها: shuffle() می‌شود. 23. مثال --- نتیجه
خالی اگر کاربر انتخاب کند: ولی هیچ کارت MONTHLY با: وجود نداشته باشد:
orderedConceptList = \[\] الگوریتم بدون خطا: و UI تصمیم می‌گیرد پیام
مناسب نمایش دهد. 24. ویژگی‌های نهایی الگوریتم این الگوریتم: READ-ONLY
مستقل از Stage Transition مستقل از Difficulty Calculation مستقل از UI
مستقل از Quiz Generation مستقل از Statistics مستقل از Backup/Restore
مستقل از Import/Parsing مسئولیت آن فقط این است: دریافت Review Type پیدا
کردن Candidateها بررسی زمان Review اتصال به Concept اعمال Filterها حذف
Duplicateها Sort / Shuffle بازگرداندن Concept List 25. اصل کلیدی
SelectReviewQueue هیچ تصمیمی درباره اینکه: «کارت بعد از پاسخ چه وضعیتی
پیدا کند» نمی‌گیرد. فقط تصمیم می‌گیرد: «در این لحظه، با این Review Type و
این Filterها، کدام کارت‌ها باید برای Review در اختیار Session قرار
بگیرند؟» وضعیت نهایی

## 32.1260 Concept Resolution و Duplicate Handling

این نسخه، نسخه کامل الگوریتم Review Scheduling / Card Selection است و
می‌تواند به‌عنوان بخش ۷.۱ در Master Specification قرار بگیرد. نام:
ResolveConceptForParsedEntry ورودی: parsedPieces : List`<Piece>`{=html}
هر Piece شامل: { languageCode, text parsedPieces خروجی Parser برای یک خط
/ یک ورودی است و می‌تواند شامل ۲ یا چند Piece باشد؛ source + translation
یا source + چند translation خروجی یکی از سه حالت: ReuseConcept(
conceptId, newContentsToInsert: List`<Piece>`{=html} CreateNewConcept(
allContentsToInsert: List`<Piece>`{=html} Conflict( matchedConceptIds:
Set`<Long>`{=html}, pieces: List`<Piece>`{=html} تابع کمکی:
normalize(text): return text.trim().lowercase()
──────────────────────────────────── مرحله ۱ --- پیدا کردن Match برای هر
Piece برای هر piece در parsedPieces: normalizedText =
normalize(piece.text) matchedConceptIdsForPiece = SELECT DISTINCT
conceptId FROM Content languageCode == piece.languageCode AND
normalize(text) == normalizedText piece.matchedConceptIds =
matchedConceptIdsForPiece مرحله ۲ --- تعیین وضعیت کلی ورودی matchedIds =
مجموعه یکتای تمام conceptIdهایی که در matchedConceptIdsForPieceهای همه
Pieceها وجود دارند اگر matchedIds.size \> 1: return Conflict(
matchedConceptIds = matchedIds, pieces = parsedPieces // یعنی Pieceهای
این ورودی به بیش از یک Concept // موجود متصل شده‌اند. // // سیستم نباید
به‌صورت خودکار Conceptها را Merge کند. // تصمیم نهایی باید توسط کاربر
انجام شود. اگر matchedIds.size == 0: return CreateNewConcept(
allContentsToInsert = parsedPieces // هیچ‌یک از Pieceها قبلاً در دیتابیس
وجود ندارد. // بنابراین یک Concept کاملاً جدید ساخته می‌شود. اگر
matchedIds.size == 1: conceptId = matchedIds.single() مرحله ۲.۱ ---
تشخیص Contentهای جدید newPieces = parsedPieces.filter { piece -\> NOT
EXISTS Content conceptId == conceptId AND languageCode ==
piece.languageCode AND normalize(text) == normalize(piece.text) return
ReuseConcept( conceptId = conceptId, newContentsToInsert = newPieces
مرحله ۳ --- اجرای نتیجه توسط لایه بالاتر اگر نتیجه CreateNewConcept
باشد: 1. یک Concept جدید ایجاد شود. 2. تمام allContentsToInsert به‌عنوان
Content زیر Concept جدید درج شوند. اگر نتیجه ReuseConcept باشد: 1.
Concept موجود حفظ شود. 2. فقط newContentsToInsert به‌عنوان Content جدید
زیر همان conceptId درج شوند. 3. اگر newContentsToInsert خالی باشد: هیچ
کاری انجام نشود. // یعنی کل ورودی دقیقاً از قبل وجود داشته است. اگر نتیجه
Conflict باشد: 1. هیچ Concept یا Content جدیدی ایجاد نشود. 2. هیچ
Content موجودی تغییر نکند. 3. Conflict به کاربر نمایش داده شود. 4.
matchedConceptIds به کاربر ارائه شود. 5. کاربر یکی از گزینه‌های زیر را
انتخاب کند: - انتخاب یکی از Conceptهای موجود - لغو ورود 6. در صورت
انتخاب Concept توسط کاربر، لایه بالاتر می‌تواند Pieceهای جدید را زیر
Concept انتخاب‌شده درج کند.

## 32.1348 Backup و Restore

7.  این الگوریتم خودش Conceptها را Merge نمی‌کند. الگوریتم نهایی: Backup
    & Restore --- بخش ۹ بخش الف: Backup نام: "CreateBackup" backupType :
    Enum { VOCABULARY, PROGRESS, FULL خروجی: ExportData شامل:
    schemaVersion exportedAt داده‌های جداول مربوطه جداول Vocabulary:
    Languages Categories Tags LanguagePairs Concepts Contents جداول
    Progress: Settings ReviewSessions ReviewHistory LearningStates
    DifficultyStates Achievements الگوریتم: اگر backupType ==
    VOCABULARY: فقط جداول Vocabulary را Export کن اگر backupType ==
    PROGRESS: جداول Progress را Export کن علاوه بر آن: ConceptUUIDهای
    مورد استفاده توسط را به عنوان ConceptReferences ذخیره کن // خود
    Conceptها و Contentها در این نوع Backup // ذخیره نمی‌شوند. اگر
    backupType == FULL: تمام جداول Vocabulary

-   تمام جداول Progress را Export کن return ExportData( داده‌های
    انتخاب‌شده، schemaVersion فعلی، exportedAt = زمان فعلی اصل شناسه‌ها:
    در Backup، تمام موجودیت‌هایی که بین دیتابیس‌ها منتقل می‌شوند باید دارای
    UUID پایدار باشند. UUID = شناسه پایدار بین Backup و Restore Database
    ID = شناسه داخلی همان دیتابیس Database ID نباید مبنای ارتباط بین
    Backup و دیتابیس مقصد قرار گیرد. بخش ب: Restore "RestoreBackup" data
    : ExportData Success(newCount, mergedCount) \| AbortedByUser
    Error(message) مرحله ۱ --- اعتبارسنجی Backup قبل از هرگونه تغییر در
    دیتابیس: ValidateBackup(data) موارد زیر بررسی شوند:

1.  data وجود داشته باشد.
2.  schemaVersion معتبر و قابل پشتیبانی باشد.
3.  ساختار ExportData کامل و قابل خواندن باشد.
4.  UUID موجودیت‌های هر جدول معتبر باشند.
5.  UUIDهای تکراری داخل یک Backup شناسایی شوند.
6.  ارجاع‌های ضروری بین موجودیت‌ها معتبر باشند.
7.  داده‌های اجباری فاقد مقدار نامعتبر باشند.
8.  backupType و مجموعه جداول موجود با ساختار Backup سازگار باشند. اگر
    Validation شکست خورد: return Error(message) و هیچ تغییری در دیتابیس
    انجام نشود. مرحله ۲ --- Backup ایمنی خودکار از وضعیت فعلی
    autoBackupResult = تلاش برای CreateBackup(FULL) و ذخیره آن در محل
    امن اگر Backup ایمنی موفق شد: ادامه بده اگر Backup ایمنی شکست خورد:
    به کاربر هشدار بده: "Backup ایمنی گرفته نشد، مطمئنی می‌خواهی ادامه
    بدهی؟" اگر کاربر لغو کرد: return AbortedByUser اگر کاربر ادامه داد:
    Restore را بدون Backup ایمنی ادامه بده مرحله ۳ --- اجرای کل Restore
    داخل یک Transaction واحد db.withTransaction { newCount = 0
    mergedCount = 0 conceptIdMap = Map\<ConceptUUID,
    DatabaseConceptID\>() languageIdMap = Map\<LanguageUUID,
    DatabaseLanguageID\>() categoryIdMap = Map\<CategoryUUID,
    DatabaseCategoryID\>() tagIdMap = Map\<TagUUID, DatabaseTagID\>()
    languagePairIdMap = Map\<LanguagePairUUID,
    DatabaseLanguagePairID\>() reviewSessionIdMap =
    Map\<ReviewSessionUUID, DatabaseReviewSessionID\>() مرحله ۳.۱ ---
    Restore Languages برای هر Language در Backup: existing =
    languageRepo.findByUuid(language.uuid) اگر موجود بود:
    languageRepo.update( language با id = existing.id
    languageIdMap\[language.uuid\] = existing.id mergedCount++ اگر موجود
    نبود: newId = languageRepo.insert(language)
    languageIdMap\[language.uuid\] = newId newCount++ مرحله ۳.۲ ---
    Restore Categories برای هر Category:
    categoryRepo.findByUuid(category.uuid) categoryRepo.update( category
    با id = existing.id categoryIdMap\[category.uuid\] = existing.id در
    غیر این صورت: categoryRepo.insert(category)
    categoryIdMap\[category.uuid\] = newId مرحله ۳.۳ --- Restore Tags
    برای هر Tag: tagRepo.findByUuid(tag.uuid) tagRepo.update( tag با id
    = existing.id tagIdMap\[tag.uuid\] = existing.id tagRepo.insert(tag)
    tagIdMap\[tag.uuid\] = newId مرحله ۳.۴ --- Restore LanguagePairs
    برای هر LanguagePair: ابتدا UUID زبان‌های مبدأ و مقصد از طریق:
    languageIdMap به ID واقعی دیتابیس مقصد تبدیل شوند.
    languagePairRepo.findByUuid(languagePair.uuid)
    languagePairRepo.update( languagePair با id = existing.id و language
    IDهای واقعی مقصد languagePairIdMap\[languagePair.uuid\] =
    existing.id languagePairRepo.insert( با language IDهای واقعی مقصد
    languagePairIdMap\[languagePair.uuid\] = newId اگر Language موردنیاز
    پیدا نشد: Restore را متوقف کن Exception پرتاب کن مرحله ۳.۵ ---
    Restore Concepts برای هر Concept:
    conceptRepo.findByUuid(concept.uuid) conceptRepo.update(
    conceptIdMap\[concept.uuid\] = existing.id
    conceptRepo.insert(concept) conceptIdMap\[concept.uuid\] = newId در
    صورت وجود وابستگی به جداول مرجع، شناسه‌های آن‌ها باید از Mapهای مربوطه
    به ID واقعی دیتابیس مقصد تبدیل شوند. مرحله ۳.۶ --- Restore Contents
    برای هر Content: conceptId = conceptIdMap\[content.conceptUuid\] اگر
    Concept متناظر پیدا نشد: skip existingByUuid =
    contentRepo.findByUuid(content.uuid) اگر Content با همان UUID وجود
    داشت: contentRepo.update(content, با id = existingByUuid.id و
    conceptId = conceptId) // canonicalKey هم‌زمان از Content.text نهایی
    بازمحاسبه می‌شود اگر UUID موجود نبود: existing =
    contentRepo.find(conceptId, content.languageCode) اگر existing وجود
    داشت: اگر existing.text == content.text: وگرنه:
    contentRepo.update(content, با id = existing.id و conceptId =
    conceptId) اگر existing وجود نداشت: contentRepo.insert(content, با
    conceptId = conceptId) اصل نهایی Duplicate در Restore Content:
9.  ابتدا UUID بررسی می‌شود.
10. اگر UUID یافت نشد، رکورد داخل همان Concept فقط با (conceptId +
    languageCode) جست‌وجو می‌شود.
11. اگر رکورد موجود و متن یکسان باشد، Skip.
12. اگر رکورد موجود و متن متفاوت باشد، Update همان رکورد.
13. اگر رکورد موجود نباشد، Insert.
14. canonicalKey فقط از Content.text نهایی محاسبه می‌شود و معیار Restore
    Merge نیست. بنابراین Restore یک Backup تکراری نباید Contentهای یکسان
    را دوباره ایجاد کند. مرحله ۳.۷ --- Restore ReviewSessions برای هر
    ReviewSession: reviewSessionRepo.findByUuid(session.uuid)
    reviewSessionRepo.update( session reviewSessionIdMap\[session.uuid\]
    = existing.id reviewSessionRepo.insert(session)
    reviewSessionIdMap\[session.uuid\] = newId مرحله ۳.۸ --- Restore
    ReviewHistory برای هر History: ابتدا: conceptId =
    conceptIdMap\[history.conceptUuid\] sessionId =
    reviewSessionIdMap\[history.sessionUuid\] اگر هرکدام پیدا نشد:
    reviewHistoryRepo.findByUuid(history.uuid) reviewHistoryRepo.update(
    history و conceptId = conceptId و sessionId = sessionId
    reviewHistoryRepo.insert( با conceptId = conceptId اصل:
    "ReviewHistory" هرگز نباید صرفاً با "insert" بدون بررسی UUID Restore
    شود. این کار از ایجاد Historyهای تکراری در Restoreهای چندباره
    جلوگیری می‌کند. مرحله ۳.۹ --- Restore LearningStates برای هر
    LearningState: conceptIdMap\[state.conceptUuid\] اگر Concept پیدا
    نشد: learningStateRepo.findByConcept(conceptId)
    learningStateRepo.update( state learningStateRepo.insert( هر Concept
    فقط باید یک LearningState فعال داشته باشد. اگر رکورد (conceptId,
    tagId) وجود نداشت، درج می‌شود؛ اگر وجود داشت، بدون ایجاد Duplicate
    نادیده گرفته می‌شود (Idempotent Insert). برای هر ConceptTag در
    Backup، conceptId و tagId متناظر از conceptIdMap و tagIdMap گرفته
    می‌شود. مرحله ۳.۱۱ --- Restore ConceptTags اصل: هر Concept فقط باید
    یک DifficultyState فعال داشته باشد.
    difficultyStateRepo.insert(state, conceptId = conceptId)
    difficultyStateRepo.update(state, id = existing.id, conceptId =
    conceptId) existing = difficultyStateRepo.findByConcept(conceptId)
    conceptId = conceptIdMap\[state.conceptUuid\] برای هر
    DifficultyState: مرحله ۳.۱۰ --- Restore DifficultyStates مرحله ۳.۱۲
    --- Restore Settings برای هر Setting:
    settingsRepo.findByKey(setting.key) settingsRepo.update(setting)
    settingsRepo.insert(setting) Settings بر اساس "key" شناخته می‌شوند و
    نباید با Database ID فایل Backup Merge شوند. مرحله ۳.۱۳ --- Restore
    Achievements برای هر Achievement:
    achievementRepo.findByType(achievement.type) achievementRepo.update(
    achievement achievementRepo.insert(achievement) مرحله ۴ --- Commit
    اگر تمام مراحل Transaction بدون Exception اجرا شدند: Transaction
    Commit return Success( newCount, mergedCount مرحله ۵ --- Rollback در
    صورت خطا اگر در هر مرحله داخل Transaction Exception رخ دهد:
    withTransaction کل تغییرات را Rollback می‌کند دیتابیس مقصد = وضعیت
    دقیقاً قبل از شروع Restore اگر مرحله Backup ایمنی موفق بوده باشد:
    Backup ایمنی معتبر از وضعیت قبل از Restore در اختیار کاربر باقی
    می‌ماند. قوانین قطعی الگوریتم قانون ۱ --- UUID ID = شناسه داخلی
    دیتابیس هیچ رابطه‌ای نباید صرفاً بر اساس Database ID موجود در Backup
    Restore شود. قانون ۲ --- Mapping برای موجودیت‌هایی که ID داخلی دارند،
    Restore باید Map زیر را ایجاد و استفاده کند: UUID → Database ID
    واقعی مقصد به‌خصوص برای: قانون ۳ --- Transaction تمام تغییرات Restore
    در یک Transaction واحد انجام می‌شوند. هیچ بخشی از Restore نباید خارج
    از Transaction تغییر دائمی در دیتابیس ایجاد کند. قانون ۴ ---
    Duplicate Restore چندباره یک Backup نباید باعث ایجاد رکوردهای تکراری
    شود. برای Content، معیار Merge نهایی همان قرارداد بخش M است: ابتدا
    UUID؛ در نبود UUID، (conceptId + languageCode). canonicalKey برای
    Matching/Import استفاده می‌شود و معیار Restore Merge نیست. تغییر
    Content.text در رکورد موجود باید با Update همان رکورد انجام شود.
    قانون ۵ --- Progress Backup "PROGRESS" شامل خود Vocabulary نیست.
    برای حفظ ارتباط Progress با Vocabulary، UUID مربوط به Conceptهای
    مورد استفاده به‌عنوان "ConceptReferences" ذخیره می‌شوند. PROGRESS
    Backup برای Restore نیازمند وجود Conceptهای متناظر در دیتابیس مقصد
    است. اگر Concept موردنیاز وجود نداشته باشد، رکورد وابسته‌ای که بدون
    آن قابل Restore نیست: قانون ۶ --- Safety Backup قبل از Restore همیشه
    تلاش می‌شود: FULL Backup از وضعیت فعلی گرفته و ذخیره شود. اگر این
    Backup ناموفق باشد، ادامه Restore فقط با تأیید صریح کاربر مجاز است.
    قانون ۷ --- Validation Backup نامعتبر قبل از شروع Transaction رد
    می‌شود و هیچ تغییری در دیتابیس ایجاد نمی‌کند. قانون ۸ --- Atomicity
    نتیجه Restore فقط یکی از این دو حالت است: همه تغییرات موفق → Commit
    حداقل یک خطا → Rollback کامل

## 32.1640 Statistics و Streak و Achievement

هیچ حالت نیمه‌Restore مجاز نیست. === الگوریتم نهایی: Statistics
Calculation --- بخش ۱۱ === ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ۱۱.۱ ---
آمار پایه نام: GetBasicStatistics totalActiveWords practicedWords
unpracticedWords learnedWords تعریف: totalActiveWords = COUNT(\*) FROM
concept WHERE active = true practicedWords = COUNT(DISTINCT
review_history.conceptId) FROM review_history JOIN concept ON concept.id
= review_history.conceptId WHERE concept.active = true unpracticedWords
= MAX(0, totalActiveWords - practicedWords) learnedWords = FROM
learning_state ON concept.id = learning_state.conceptId WHERE
learning_state.stage = 'LEARNED' AND concept.active = true تمام آمار
اصلی این بخش بر اساس Conceptهای Active محاسبه می‌شوند. داده‌های مربوط به
Conceptهای غیرفعال در آمار جاری کاربر محاسبه نمی‌شوند. ۱۱.۲ --- درصد
پیشرفت نام: CalculateProgressPercentage لیست همه Conceptهای Active اگر
لیست خالی باشد: return 0 totalScore = 0 برای هر concept: state =
learningState متناظر با concept.id score = بر اساس state?.stage: → 100 →
80 → 60 → 35 اگر state وجود ندارد یا Stage دیگری دارد: اگر تعداد
ReviewHistoryهای این Concept \> 0: → 15 → 0 totalScore += score return:
totalScore / تعداد کل Conceptهای Active نوع مقدار: می‌تواند اعشاری
نگهداری شود. نحوه گرد کردن فقط در لایه نمایش تعیین می‌شود. ۱۱.۳ ---
Streak نام: CalculateStreak reviewDays = مجموعه یکتای روزهای تقویمی محلی
که در آن‌ها حداقل یک ReviewHistory ثبت شده است. Timestamp هر
ReviewHistory باید با Device Local Timezone به LocalDate تبدیل شود. سپس
reviewDays به ترتیب نزولی (جدیدترین روز اول) مرتب می‌شود. اگر reviewDays
خالی باشد: today = روز تقویمی فعلی در Device Local Timezone
mostRecentDay = reviewDays\[0\] mostRecentDay != today mostRecentDay !=
(today - 1 روز) // قانون ملایم: // اگر امروز هنوز Review ثبت نشده ولی
دیروز Review داشته، // Streak دیروز همچنان معتبر است. streak = 1 برای i
از 1 تا آخر reviewDays: reviewDays\[i\] == reviewDays\[i-1\] - 1 روز
streak += 1 توقف حلقه return streak ۱۱.۴ --- Achievements Dependency
جدید در DifficultyState: hasReachedVeryHard : Boolean = false این فیلد
یک Historical Flag است. هرگاه Difficulty یک Concept به VERY_HARD برسد:
hasReachedVeryHard = true اگر Difficulty بعداً از VERY_HARD پایین‌تر آمد:
hasReachedVeryHard همچنان true باقی می‌ماند. این مقدار هرگز به false
برنمی‌گردد، مگر در حذف/Reset صریح داده توسط سیستم یا کاربر طبق قوانین
مربوط به Reset. CheckAndUnlockAchievements این تابع می‌تواند: - بعد از هر
پاسخ Review - یا هنگام باز شدن صفحه Statistics اجرا شود. newlyUnlocked :
List`<Achievement>`{=html} فقط Achievementهایی که در همین اجرای تابع
برای اولین بار Unlock شده‌اند. مقداردهی: newlyUnlocked = \[\]
Achievementها: 1. FIRST_TEN_WORDS شرط: practicedWords \>= 10 2.
SEVEN_DAY_STREAK streak \>= 7 3. THIRTY_DAY_STREAK streak \>= 30 4.
MEMORY_BUILDER learnedWords \>= 100 5. VOCABULARY_BUILDER
totalActiveWords \>= 500 6. HARD_MODE_MASTER COUNT(
difficultyState.hasReachedVeryHard == true learningState.stage ==
'LEARNED' concept.active == true ) \>= 25 7. LONG_TERM_MEMORY
COUNT(DISTINCT conceptId) reviewStage == 'MONTHLY' isCorrect == true AND
concept.active == true \>= 50 منطق Unlock: alreadyUnlocked =
achievementRepository .findByType(type) ?.isUnlocked ?: false اگر
alreadyUnlocked == true: رد شو در غیر این صورت اگر شرط Achievement
برقرار است: achievement = Achievement( type = type, ..., isUnlocked =
true, unlockedAt = now achievementRepository.upsert(achievement)
newlyUnlocked.add(achievement) هیچ کاری انجام نده return newlyUnlocked
قانون UI: UI فقط newlyUnlocked را برای: - Animation - Notification -
Achievement Popup استفاده می‌کند. Achievementهایی که قبلاً Unlock شده‌اند

## 32.1793 Difficulty Calculation

نباید مجدداً به‌عنوان Achievement جدید نمایش داده شوند. Scope: محاسبه و
به‌روزرسانی Difficulty بر اساس نتیجه مرور Dependency: "AppSetting →
threshold_difficulty" AI Dependency: None Execution Mode: Offline /
Deterministic این الگوریتم مسئول محاسبه و به‌روزرسانی سطح سختی
("Difficulty") یک Concept بر اساس نتایج متوالی مرورهای کاربر است.
Difficulty از Learning Stage مستقل است. سطوح Difficulty: این الگوریتم
فقط Difficulty را محاسبه می‌کند و مسئول تعیین موارد زیر نیست: Learning
Stage Review Interval Review Scheduling 2. ورودی‌ها
currentDifficultyState : DifficultyState( current, consecutiveCorrect,
hasReachedVeryHard isCorrect : Boolean threshold : Integer 2.1 current
سطح فعلی Difficulty: 2.2 consecutiveCorrect تعداد پاسخ‌های صحیح متوالی که
از آخرین تغییر Difficulty یا آخرین پاسخ غلط ثبت شده‌اند. 2.3
consecutiveWrong تعداد پاسخ‌های غلط متوالی که از آخرین تغییر Difficulty
یا آخرین پاسخ صحیح ثبت شده‌اند. 2.4 isCorrect نتیجه پاسخ فعلی: true =
پاسخ صحیح false = پاسخ غلط 2.5 threshold تعداد پاسخ‌های متوالی لازم برای
تغییر یک‌پله‌ای Difficulty. مقدار از: AppSetting → threshold_difficulty
دریافت می‌شود. مقدار پیش‌فرض: 3 شرط اعتبار: threshold \>= 1 اعتبارسنجی
"threshold_difficulty" باید در لایه Settings انجام شود. این الگوریتم
باید فقط مقدار معتبر دریافت کند و مسئول Validation تنظیمات نیست. 3.
خروجی newDifficultyState : 4. قوانین پایه قانون 1 --- پاسخ صحیح با
دریافت پاسخ صحیح: consecutiveCorrect += 1 consecutiveWrong = 0 قانون 2
--- پاسخ غلط با دریافت پاسخ غلط: consecutiveWrong += 1
consecutiveCorrect = 0 قانون 3 --- رسیدن به Threshold وقتی یکی از
شمارنده‌ها به "threshold" برسد: Difficulty فقط یک پله تغییر می‌کند. قانون
4 --- Reset پس از اعمال تغییر Difficulty: قانون 5 --- عدم رسیدن به
Threshold اگر شمارنده هنوز به "threshold" نرسیده باشد: Difficulty بدون
تغییر باقی می‌ماند. 5. منطق پاسخ صحیح newConsecutiveCorrect =
currentDifficultyState.consecutiveCorrect + 1 newConsecutiveWrong = 0 IF
newConsecutiveCorrect \>= threshold: newLevel = OneStepEasier(current)
newConsecutiveCorrect = 0 newLevel = current 6. منطق پاسخ غلط isCorrect
== false newConsecutiveWrong = currentDifficultyState.consecutiveWrong +
1 IF newConsecutiveWrong \>= threshold: OneStepHarder(current) 7. تابع
OneStepEasier FUNCTION OneStepEasier(current): SWITCH current:
VERY_HARD: RETURN HARD HARD: RETURN MEDIUM MEDIUM: RETURN EASY EASY:
VERY_HARD → HARD HARD → MEDIUM MEDIUM → EASY EASY → EASY Difficulty هرگز
پایین‌تر از "EASY" نمی‌رود. 8. تابع OneStepHarder FUNCTION
OneStepHarder(current): RETURN VERY_HARD EASY → MEDIUM MEDIUM → HARD
HARD → VERY_HARD VERY_HARD → VERY_HARD Difficulty هرگز بالاتر از
"VERY_HARD" نمی‌رود. 9. شبه‌کد کامل FUNCTION CalculateDifficulty(
currentDifficultyState, isCorrect, currentMonthlyWrongCount, threshold
current = currentDifficultyState.current consecutiveCorrect =
currentDifficultyState.consecutiveCorrect consecutiveWrong =
currentDifficultyState.consecutiveWrong hasReachedVeryHard =
currentDifficultyState.hasReachedVeryHard // --- Forced Updates (اولویت
بالاتر از شمارنده متوالی) --- IF reviewType == WEEKLY AND isCorrect ==
false: newLevel = max(current, MEDIUM) ELSE IF reviewType == MONTHLY AND
isCorrect == false: IF currentMonthlyWrongCount == 0: newLevel = HARD
newLevel = VERY_HARD newConsecutiveCorrect = consecutiveCorrect + 1
newLevel = OneStepEasier(current) consecutiveCorrect =
newConsecutiveCorrect newConsecutiveWrong = consecutiveWrong + 1
newLevel = OneStepHarder(current) consecutiveWrong = newConsecutiveWrong
// --- hasReachedVeryHard (monotonic) --- IF newLevel == VERY_HARD:
RETURN DifficultyState( current = newLevel, consecutiveCorrect =
consecutiveCorrect, consecutiveWrong = consecutiveWrong,
hasReachedVeryHard = hasReachedVeryHard 10. مثال‌های رفتاری مثال 1 --- سه
پاسخ صحیح Initial: Difficulty = MEDIUM Correct = 0 Wrong = 0 threshold =
3 Review 1 → Correct Correct = 1 Review 2 → Correct Correct = 2 Review 3
→ Correct Difficulty = EASY مثال 2 --- سه پاسخ غلط Review 1 → Wrong
Wrong = 1 Review 2 → Wrong Wrong = 2 Review 3 → Wrong Difficulty = HARD
مثال 3 --- شکستن زنجیره صحیح Correct Wrong Difficulty = unchanged دو
پاسخ صحیح قبلی دیگر برای رسیدن به Threshold محسوب نمی‌شوند. مثال 4 ---
شکستن زنجیره غلط مثال 5 --- رسیدن به سقف Difficulty = VERY_HARD سطح
بالاتر از "VERY_HARD" وجود ندارد. مثال 6 --- رسیدن به کف سطح پایین‌تر از
"EASY" وجود ندارد. 11. استقلال از Learning Stage Difficulty Calculation
مستقل از: این الگوریتم Stage را تغییر نمی‌دهد. همچنین مستقیماً
"nextReviewAt" یا فاصله مرور بعدی را محاسبه نمی‌کند. 12. قرارداد با
Learning Transition این بخش قرارداد بین دو الگوریتم است و جزو منطق داخلی
Difficulty Calculation محسوب نمی‌شود. 12.1 مرور عادی اگر مرور از نوع:
باشد و هیچ Exception از طرف Transition وجود نداشته باشد: Learning
Transition Difficulty Calculation می‌تواند نتیجه پاسخ را پردازش کند. 12.2
LEARNED اگر Concept در Stage زیر باشد: Difficulty Calculation اجرا
نمی‌شود. Concept در این وضعیت دیگر در چرخه معمول Review قرار ندارد. 12.3
Weekly/Monthly Exception این Exception بخشی از قرارداد Review Response
است و فقط یک مسیر باید DifficultyState را برای همان پاسخ تغییر دهد.
WEEKLY + Wrong → Difficulty حداقل MEDIUM می‌شود؛ اگر current=EASY باشد به
MEDIUM می‌رود و اگر current از MEDIUM بالاتر باشد حفظ می‌شود. هر دو
consecutive counter صفر می‌شوند. MONTHLY + Wrong → پس از افزایش
monthlyWrongCount، اگر مقدار جدید 1 باشد Difficulty=HARD و اگر مقدار
جدید \>=2 باشد Difficulty=VERY_HARD. هر دو consecutive counter صفر
می‌شوند. اگر Difficulty به VERY_HARD برسد، hasReachedVeryHard=true می‌شود
و در کاهش‌های بعدی false نمی‌شود. این Exception جایگزین اجرای عادی
threshold برای همان Review Event است و هر دو مسیر نباید همزمان اجرا
شوند. 13. قانون جلوگیری از Double Update در یک Review Event، Difficulty
نباید توسط دو مسیر مختلف تغییر کند. Normal Review Weekly/Monthly
Exception Learning Transition Direct Update اما هر دو مسیر نباید همزمان
اجرا شوند. 14. Initialization Contract مقدار اولیه "DifficultyState"
هنگام ایجاد یک Concept جدید، جزء این الگوریتم نیست. این مقدار باید در
Data Model / Concept Creation Specification تعریف شود. قرارداد پیشنهادی:
current = EASY, consecutiveCorrect = 0, Initialization ≠ Difficulty
Calculation الگوریتم Difficulty فقط یک "DifficultyState" موجود را دریافت
کرده و State جدید را محاسبه می‌کند. 15. Deterministic بودن این الگوریتم
باید کاملاً Deterministic باشد. برای ورودی‌های یکسان:
currentDifficultyState isCorrect خروجی همیشه باید یکسان باشد. نباید به
موارد زیر وابسته باشد: Randomness AI Internet Server Current Time
External API 16. Offline بودن تمام محاسبات روی دستگاه قابل انجام است.
نیازی به: Cloud وجود ندارد. 17. Invariants پس از اجرای الگوریتم، شرایط
زیر باید همیشه برقرار باشند: 1. current ∈ {EASY, MEDIUM, HARD,
VERY_HARD} 2. consecutiveCorrect \>= 0 3. consecutiveWrong \>= 0 4.
consecutiveCorrect و consecutiveWrong نمی‌توانند همزمان بزرگ‌تر از صفر
باشند. 5. اگر Difficulty تغییر کند: 6. Difficulty در هر اجرای الگوریتم
حداکثر یک پله تغییر می‌کند. 7. Difficulty هرگز از EASY پایین‌تر یا از
VERY_HARD بالاتر نمی‌رود. 18. خلاصه نهایی رفتار CORRECT │ ▼
consecutiveCorrect + 1 آیا به threshold رسید؟ │ │ خیر بله ▼ ▼ بدون تغییر
یک پله آسان‌تر هر دو Counter = 0 WRONG consecutiveWrong + 1 بدون تغییر یک
پله سخت‌تر 19. وضعیت نهایی Algorithm: Version: 1.1 Status: FROZEN
Responsibility: Calculate and update Difficulty only Stages handled:
None Default threshold: Difficulty levels: AI: Network: Deterministic:
Yes

## 32.2055 Refresh و Data Migration

Offline: ۷.X --- Refresh / Data Migration Algorithm RefreshDataUseCase
اعمال تغییرات نسخه‌های جدید برنامه روی داده‌های قدیمی موجود در دیتابیس،
بدون نیاز به حذف یا ورود مجدد داده‌ها.
================================================== 1. ثابت‌های سراسری
CURRENT_CONCEPT_VERSION : Int آخرین نسخه معتبر Concept در نسخه فعلی
برنامه. CURRENT_CONTENT_VERSION : Int آخرین نسخه معتبر Content در نسخه
فعلی برنامه. conceptMigrations : Ordered List لیست ترتیبی Migrationهای
مربوط به Concept: \[ migrateConceptToVersion1, migrateConceptToVersion2,
migrateConceptToVersion3,\] contentMigrations : Ordered List لیست ترتیبی
Migrationهای مربوط به Content: migrateContentToVersion1,
migrateContentToVersion2, migrateContentToVersion3, 2. قرارداد
Migrationها هر Concept Migration فقط یک نسخه را ارتقا می‌دهد:
migrateConceptToVersionN( conceptVersionNMinus1 -\> conceptVersionN هر
Content Migration فقط یک نسخه را ارتقا می‌دهد: migrateContentToVersionN(
contentVersionNMinus1 contentVersionN Concept version مستندشده
migrateConceptToVersion2 Concept version مستندشده
migrateConceptToVersion3 Concept version مستندشده قانون: Migration نسخه
N فقط باید روی رکورد نسخه N-1 قابل اعمال باشد. تمام Migrationها باید: -
Idempotent باشند. - فقط داده مربوط به نوع خود را تغییر دهند. - Migration
مربوط به Concept، Content را تغییر ندهد. - Migration مربوط به Content،
Concept را تغییر ندهد. - LearningState را تغییر ندهند. - ReviewHistory
را تغییر ندهند. 3. ورودی ندارد. UseCase روی تمام Conceptهای Active موجود
در دیتابیس اجرا می‌شود. Success(updatedCount) حداقل یک Concept یا Content
به‌روزرسانی شده است. updatedCount = تعداد Conceptهایی که خود Concept یا
حداقل یکی از Contentهای متعلق به آن تغییر کرده است. NoChange هیچ Concept
یا Contentای نیاز به Migration نداشته است. اجرای Refresh با خطا مواجه
شده است. 5. الگوریتم اصلی updatedCount = 0 برای هر concept در تمام
Active Concepts: -------------------------------------------------- 5.1
--- Migration مربوط به Concept conceptChanged = false v =
concept.dataVersion تا زمانی که: v \< CURRENT_CONCEPT_VERSION مراحل زیر
انجام شود: nextVersion = v + 1 اگر Migration مربوط به nextVersion در
conceptMigrations وجود نداشت: return Error( "Missing Concept Migration
for version" + nextVersion concept =
conceptMigrations[nextVersion](concept) v = nextVersion conceptChanged =
true پس از پایان حلقه: اگر conceptChanged == true: concept.dataVersion =
v ذخیره Concept در دیتابیس 5.2 --- Migration مربوط به Content
contentChangedForConcept = false برای هر content متعلق به همین concept:
contentChanged = false cv = content.dataVersion cv \<
CURRENT_CONTENT_VERSION nextVersion = cv + 1 در contentMigrations وجود
نداشت: "Missing Content Migration for version" content =
contentMigrations[nextVersion](content) cv = nextVersion contentChanged
= true اگر contentChanged == true: content.dataVersion = cv ذخیره
Content در دیتابیس contentChangedForConcept = true 5.3 --- شمارش
conceptChanged == true OR contentChangedForConcept == true updatedCount
= updatedCount + 1 6. پایان الگوریتم updatedCount \> 0 return
Success(updatedCount) return NoChange 7. مدیریت خطا اگر هر Migration در
هنگام اجرا با خطا مواجه شود: اگر Migration موردنیاز برای نسخه بعدی وجود
نداشته باشد: اگر ذخیره Concept یا Content در دیتابیس با خطا مواجه شود:
Refresh نباید در صورت وجود خطا، نتیجه Success یا NoChange برگرداند. 8.
قوانین Transaction اجرای Refresh باید به‌صورت Transaction انجام شود. شروع
Transaction اجرای تمام Migrationهای موردنیاز و ذخیره تغییرات اگر همه
عملیات موفق بودند: Commit اگر هر مرحله‌ای با خطا مواجه شد: Rollback
جلوگیری از باقی ماندن دیتابیس در وضعیت نیمه‌به‌روزشده. 9. محدوده داده‌های
قابل تغییر این الگوریتم فقط موارد زیر را تغییر می‌دهد: Content این
الگوریتم به‌صورت مستقیم یا غیرمستقیم نباید تغییر دهد: مگر اینکه در نسخه
آینده یک Migration مستقل و صریح برای آن‌ها به Master Specification اضافه
شود. 10. Idempotency هر Migration باید به‌صورت مستقل Idempotent باشد.
همچنین شرط: dataVersion \< CURRENT_VERSION باعث می‌شود رکوردی که قبلاً به
آخرین نسخه رسیده است، در اجرای بعدی دوباره Migration نشود.
CURRENT_CONCEPT_VERSION = 4 Concept Version = 1 اجرای Refresh: 1 → 2 2 →
3 3 → 4 اجرای مجدد Refresh: 4 \< 4 → false هیچ Migrationای اجرا نمی‌شود.
11. استقلال Concept و Content نسخه Concept تعیین‌کننده نسخه Content نیست.
Concept.dataVersion = 4 Content.dataVersion = 2 CURRENT_CONTENT_VERSION
= 5 در این حالت: Concept: هیچ Migrationای ندارد. Content: 4 → 5 این دو
فرآیند مستقل از یکدیگر هستند. ۷.X --- Refresh / Data Migration Algorithm
این دو فرآیند مستقل از یکدیگر هستند. 12. جداسازی از Room Schema
Migration Room Schema Migration و RefreshDataUseCase دو فرآیند کاملاً
مستقل هستند. ترتیب اجرای آن‌ها: 1. اجرای Room Schema Migration 2. آماده
شدن ساختار دیتابیس 3. اجرای RefreshDataUseCase 4. آماده شدن داده‌های
قدیمی برای نسخه فعلی برنامه 5. اجرای عادی برنامه Room Schema Migration:
مسئول تغییر ساختار دیتابیس است. - اضافه کردن ستون - حذف ستون - تغییر
ساختار جدول - تغییر Index - تغییر Constraint RefreshDataUseCase: مسئول
تغییر داده‌های موجود داخل رکوردهاست. RefreshDataUseCase نباید ساختار جدول
را تغییر دهد. 13. رفتار دکمه Refresh هر زمان کاربر Refresh را اجرا کند:
Room Schema Migration قبلاً توسط Room انجام شده است. روی تمام Active
Concepts اجرا می‌شود. اگر داده‌ای نیاز به Migration داشته باشد: اگر هیچ
داده‌ای نیاز به Migration نداشته باشد: اگر مشکلی رخ دهد: 14. اصل نهایی
RefreshDataUseCase برای این طراحی شده است که: نسخه جدید برنامه بتواند
داده‌های قدیمی موجود در دیتابیس را بدون حذف اطلاعات کاربر، مرحله‌به‌مرحله
به ساختار و محتوای مورد انتظار نسخه فعلی ارتقا دهد.

## 32.2247 Implementation-ready Algorithm Contracts

هر نسخه جدید برنامه فقط Migrationهای جدید خود را اضافه می‌کند. مثال:
Status: IMPLEMENTATION-READY (v4 --- Canonical, referenced by Algorithms
v4) Source: Descriptions v4 + Algorithms v4 + Code نسخهٔ مستندشده +
Corrections 2026-09-10 Date: 2026-09-10 (v4 revision) 1. Enums &
Supporting Types package com.flashlearn.domain.model enum class Stage {
DAILY, WEEKLY, MONTHLY, LEARNED } enum class VocabularyDifficulty {
EASY, MEDIUM, HARD, VERY_HARD } enum class ReviewType { DAILY, WEEKLY,
MONTHLY, LEARNED, // voluntary only RANDOM // Due DAILY+WEEKLY+MONTHLY
only (shuffle) } data class TransitionResult( val newStage: Stage, val
nextReviewAt: java.time.Instant?, val hasPathFailure: Boolean, val
monthlyWrongCount: Int ) 2. Learning Transition Algorithm (Pure &
Deterministic) Rules (from Algorithms §5 + Corrections): - DAILY +
Correct → WEEKLY, nextReviewAt = now + 7 days - DAILY + Wrong → DAILY,
nextReviewAt = start of next calendar day - WEEKLY + Correct → MONTHLY,
nextReviewAt = now + 30 days - WEEKLY + Wrong → DAILY, nextReviewAt =
start of next calendar day, hasPathFailure = true - MONTHLY + Correct →
LEARNED, nextReviewAt = null - MONTHLY + Wrong → DAILY, nextReviewAt =
start of next calendar day, hasPathFailure = true, monthlyWrongCount +
1 - LEARNED → stays LEARNED, nextReviewAt = null (no change to
flags/counters) - monthlyWrongCount is cumulative and never reset by a
successful transition - hasPathFailure is never reset by normal review
package com.flashlearn.domain.algorithm import
com.flashlearn.domain.model.\* import java.time.Instant import
java.time.ZoneId import java.time.temporal.ChronoUnit /\*\* \* Pure
function. Does NOT touch the database. \* All state changes are applied
later inside SubmitReviewAnswer @Transaction. */ fun
calculateLearningTransition( learningState: LearningState, isCorrect:
Boolean, reviewedAt: Instant, zoneId: ZoneId = ZoneId.systemDefault() ):
TransitionResult { val currentStage = learningState.stage val
currentMonthlyWrong = learningState.monthlyWrongCount val
currentHasPathFailure = learningState.hasPathFailure // LEARNED is
terminal for normal flow if (currentStage == Stage.LEARNED) { return
TransitionResult( newStage = Stage.LEARNED, nextReviewAt = null,
hasPathFailure = currentHasPathFailure, monthlyWrongCount =
currentMonthlyWrong ) } return if (isCorrect) { when (currentStage) {
Stage.DAILY -\> TransitionResult( newStage = Stage.WEEKLY, nextReviewAt
= reviewedAt.plus(7, ChronoUnit.DAYS), hasPathFailure =
currentHasPathFailure, monthlyWrongCount = currentMonthlyWrong )
Stage.WEEKLY -\> TransitionResult( newStage = Stage.MONTHLY,
nextReviewAt = reviewedAt.plus(30, ChronoUnit.DAYS), hasPathFailure =
currentHasPathFailure, monthlyWrongCount = currentMonthlyWrong )
Stage.MONTHLY -\> TransitionResult( newStage = Stage.LEARNED,
nextReviewAt = null, hasPathFailure = currentHasPathFailure,
monthlyWrongCount = currentMonthlyWrong ) else -\> error("Unexpected
stage: \$currentStage") } } else { when (currentStage) { Stage.DAILY -\>
TransitionResult( newStage = Stage.DAILY, nextReviewAt =
startOfNextCalendarDay(reviewedAt, zoneId), hasPathFailure =
currentHasPathFailure, // DAILY wrong does NOT set path failure
monthlyWrongCount = currentMonthlyWrong ) Stage.WEEKLY -\>
TransitionResult( newStage = Stage.DAILY, nextReviewAt =
startOfNextCalendarDay(reviewedAt, zoneId), hasPathFailure = true,
monthlyWrongCount = currentMonthlyWrong ) Stage.MONTHLY -\>
TransitionResult( newStage = Stage.DAILY, nextReviewAt =
startOfNextCalendarDay(reviewedAt, zoneId), hasPathFailure = true,
monthlyWrongCount = currentMonthlyWrong + 1 ) else -\> error("Unexpected
stage: \$currentStage") } } } /** \* Returns the Instant of 00:00:00 of
the next calendar day \* in the given ZoneId (device local timezone). */
fun startOfNextCalendarDay(instant: Instant, zoneId: ZoneId): Instant {
val localDate = instant.atZone(zoneId).toLocalDate() return
localDate.plusDays(1).atStartOfDay(zoneId).toInstant() } 3. Difficulty
Calculation Algorithm (Final Corrected) Priority order (from Algorithms
§5 + §6 + Corrections 2026-09-10): 11. 1. Forced Updates (WEEKLY wrong /
MONTHLY wrong) have higher priority than consecutive counters. 12. 2.
WEEKLY + Wrong → at least MEDIUM + reset both counters. 13. 3. MONTHLY +
Wrong → first failure (monthlyWrongCountBefore == 0) → HARD; subsequent
→ VERY_HARD + reset counters. 14. 4. Normal path: consecutive
correct/wrong with threshold (default 3). Opposite answer zeros the
other counter. 15. 5. Any level change (forced or normal) resets both
consecutiveCorrect and consecutiveWrong to 0. 16. 6. hasReachedVeryHard
is monotonic: once true under normal review, stays true. 17. 7. Forced
updates only apply to real WEEKLY and MONTHLY reviews (not RANDOM or
LEARNED). package com.flashlearn.domain.algorithm import
com.flashlearn.domain.model.* /** * Pure function. Does NOT touch the
database. \* \* Important contract: \* - monthlyWrongCountBefore must be
the value FROM LearningState BEFORE Transition is applied. \* - This
allows correct detection of "first MONTHLY failure" vs "subsequent". */
fun calculateDifficulty( state: DifficultyState, isCorrect: Boolean,
reviewType: ReviewType, monthlyWrongCountBefore: Int, // pre-increment
value from LearningState threshold: Int = 3 ): DifficultyState { var
newLevel = state.current var cc = state.consecutiveCorrect var cw =
state.consecutiveWrong var reachedVeryHard = state.hasReachedVeryHard //
------------------------------------------------- // 1. Forced Updates
(highest priority) // -------------------------------------------------
when { reviewType == ReviewType.WEEKLY && !isCorrect -\> { // At least
MEDIUM newLevel = if (state.current.ordinal \<
VocabularyDifficulty.MEDIUM.ordinal) { VocabularyDifficulty.MEDIUM }
else { state.current } cc = 0 cw = 0 } reviewType == ReviewType.MONTHLY
&& !isCorrect -\> { // First failure → HARD, subsequent → VERY_HARD
newLevel = if (monthlyWrongCountBefore == 0) { VocabularyDifficulty.HARD
} else { VocabularyDifficulty.VERY_HARD } cc = 0 cw = 0 } else -\> { //
------------------------------------------------- // 2. Normal
consecutive-counter logic //
------------------------------------------------- if (isCorrect) { cw =
0 val newCC = cc + 1 if (newCC \>= threshold) { newLevel =
oneStepEasier(state.current) cc = 0 cw = 0 } else { newLevel =
state.current cc = newCC } } else { cc = 0 val newCW = cw + 1 if (newCW
\>= threshold) { newLevel = oneStepHarder(state.current) cc = 0 cw = 0 }
else { newLevel = state.current cw = newCW } } } } //
------------------------------------------------- // 3. Monotonic
hasReachedVeryHard // -------------------------------------------------
if (newLevel == VocabularyDifficulty.VERY_HARD) { reachedVeryHard = true
} return state.copy( current = newLevel, consecutiveCorrect = cc,
consecutiveWrong = cw, hasReachedVeryHard = reachedVeryHard ) } private
fun oneStepEasier(d: VocabularyDifficulty): VocabularyDifficulty = when
(d) { VocabularyDifficulty.VERY_HARD -\> VocabularyDifficulty.HARD
VocabularyDifficulty.HARD -\> VocabularyDifficulty.MEDIUM
VocabularyDifficulty.MEDIUM -\> VocabularyDifficulty.EASY
VocabularyDifficulty.EASY -\> VocabularyDifficulty.EASY } private fun
oneStepHarder(d: VocabularyDifficulty): VocabularyDifficulty = when (d)
{ VocabularyDifficulty.EASY -\> VocabularyDifficulty.MEDIUM
VocabularyDifficulty.MEDIUM -\> VocabularyDifficulty.HARD
VocabularyDifficulty.HARD -\> VocabularyDifficulty.VERY_HARD
VocabularyDifficulty.VERY_HARD -\> VocabularyDifficulty.VERY_HARD } 4.
Correct Call Order inside SubmitReviewAnswer This order is mandatory so
that monthlyWrongCountBefore is the pre-increment value. // Inside
@Transaction val learning =
learningStateRepository.get(request.conceptId) ?: error("LearningState
not found") val difficulty =
difficultyStateRepository.get(request.conceptId) ?:
error("DifficultyState not found") validateDue(learning,
request.reviewedAt) validateAttemptUniqueness(request.sessionId,
request.reviewAttemptId) // 1. Transition first (calculates new
monthlyWrongCount) val transition = calculateLearningTransition(
learningState = learning, isCorrect = request.isCorrect, reviewedAt =
request.reviewedAt ) // 2. Difficulty second -- MUST pass the OLD
monthlyWrongCount val newDifficulty = calculateDifficulty( state =
difficulty, isCorrect = request.isCorrect, reviewType =
request.reviewType, monthlyWrongCountBefore =
learning.monthlyWrongCount, // BEFORE transition threshold = 3 ) // 3.
Persist both + ReviewHistory atomically learningStateRepository.upsert(
learning.copy( stage = transition.newStage, nextReviewAt =
transition.nextReviewAt, hasPathFailure = transition.hasPathFailure,
monthlyWrongCount = transition.monthlyWrongCount, totalCorrect = if
(request.isCorrect) learning.totalCorrect + 1 else
learning.totalCorrect, totalWrong = if (!request.isCorrect)
learning.totalWrong + 1 else learning.totalWrong, lastReviewedAt =
request.reviewedAt ) ) difficultyStateRepository.upsert(newDifficulty)
reviewHistoryRepository.insert( ReviewHistory( sessionId =
request.sessionId, reviewAttemptId = request.reviewAttemptId, conceptId
= request.conceptId, reviewedAt = request.reviewedAt, isCorrect =
request.isCorrect, reviewType = request.reviewType ) ) 5. Unit Tests
(JUnit 5) 5.1 Learning Transition Tests package
com.flashlearn.domain.algorithm import com.flashlearn.domain.model.*
import org.junit.jupiter.api.Assertions.\* import
org.junit.jupiter.api.Test import java.time.Instant import
java.time.ZoneOffset import java.util.UUID class LearningTransitionTest
{ private val now = Instant.parse("2026-09-10T10:00:00Z") private val
zone = ZoneOffset.UTC private fun baseState( stage: Stage, monthlyWrong:
Int = 0, pathFailure: Boolean = false ) = LearningState( id =
UUID.randomUUID(), conceptId = UUID.randomUUID(), stage = stage,
nextReviewAt = now, monthlyWrongCount = monthlyWrong, hasPathFailure =
pathFailure, totalCorrect = 0, totalWrong = 0, lastReviewedAt = null )
@Test fun `DAILY correct goes to WEEKLY with +7 days`() { val result =
calculateLearningTransition(baseState(Stage.DAILY), true, now, zone)
assertEquals(Stage.WEEKLY, result.newStage)
assertEquals(now.plusSeconds(7 \* 86400), result.nextReviewAt)
assertFalse(result.hasPathFailure) assertEquals(0,
result.monthlyWrongCount) } @Test fun
`DAILY wrong stays DAILY, next calendar day, pathFailure unchanged`() {
val result = calculateLearningTransition(baseState(Stage.DAILY), false,
now, zone) assertEquals(Stage.DAILY, result.newStage)
assertEquals(startOfNextCalendarDay(now, zone), result.nextReviewAt)
assertFalse(result.hasPathFailure) assertEquals(0,
result.monthlyWrongCount) } @Test fun
`WEEKLY wrong goes to DAILY + pathFailure true`() { val result =
calculateLearningTransition(baseState(Stage.WEEKLY), false, now, zone)
assertEquals(Stage.DAILY, result.newStage)
assertTrue(result.hasPathFailure) assertEquals(0,
result.monthlyWrongCount) } @Test fun
`MONTHLY wrong increases monthlyWrongCount and sets pathFailure`() { val
result = calculateLearningTransition(baseState(Stage.MONTHLY,
monthlyWrong = 2), false, now, zone) assertEquals(Stage.DAILY,
result.newStage) assertTrue(result.hasPathFailure) assertEquals(3,
result.monthlyWrongCount) } @Test fun
`MONTHLY correct goes to LEARNED, counters unchanged`() { val result =
calculateLearningTransition( baseState(Stage.MONTHLY, monthlyWrong = 1,
pathFailure = true), true, now, zone ) assertEquals(Stage.LEARNED,
result.newStage) assertNull(result.nextReviewAt)
assertTrue(result.hasPathFailure) assertEquals(1,
result.monthlyWrongCount) } @Test fun
`LEARNED stays LEARNED regardless of answer`() { val correct =
calculateLearningTransition(baseState(Stage.LEARNED), true, now, zone)
val wrong = calculateLearningTransition(baseState(Stage.LEARNED), false,
now, zone) assertEquals(Stage.LEARNED, correct.newStage)
assertEquals(Stage.LEARNED, wrong.newStage)
assertNull(correct.nextReviewAt) assertNull(wrong.nextReviewAt) } } 5.2
Difficulty Calculation Tests package com.flashlearn.domain.algorithm
import com.flashlearn.domain.model.\* import
org.junit.jupiter.api.Assertions.\* import org.junit.jupiter.api.Test
import java.util.UUID class DifficultyCalculationTest { private fun
state( current: VocabularyDifficulty = VocabularyDifficulty.EASY, cc:
Int = 0, cw: Int = 0, reached: Boolean = false ) = DifficultyState( id =
UUID.randomUUID(), conceptId = UUID.randomUUID(), current = current,
consecutiveCorrect = cc, consecutiveWrong = cw, hasReachedVeryHard =
reached ) // ---------- Forced Updates ---------- @Test fun
`WEEKLY wrong forces at least MEDIUM and resets counters`() { val result
= calculateDifficulty(state(VocabularyDifficulty.EASY), false,
ReviewType.WEEKLY, 0) assertEquals(VocabularyDifficulty.MEDIUM,
result.current) assertEquals(0, result.consecutiveCorrect)
assertEquals(0, result.consecutiveWrong) } @Test fun
`WEEKLY wrong keeps existing HARD`() { val result =
calculateDifficulty(state(VocabularyDifficulty.HARD), false,
ReviewType.WEEKLY, 0) assertEquals(VocabularyDifficulty.HARD,
result.current) } @Test fun
`MONTHLY first wrong (countBefore=0) sets HARD`() { val result =
calculateDifficulty(state(), false, ReviewType.MONTHLY,
monthlyWrongCountBefore = 0) assertEquals(VocabularyDifficulty.HARD,
result.current) assertEquals(0, result.consecutiveCorrect)
assertEquals(0, result.consecutiveWrong) } @Test fun
`MONTHLY subsequent wrong (countBefore greater than 0) sets VERY_HARD`()
{ val result = calculateDifficulty(state(), false, ReviewType.MONTHLY,
monthlyWrongCountBefore = 1)
assertEquals(VocabularyDifficulty.VERY_HARD, result.current)
assertTrue(result.hasReachedVeryHard) } // ---------- Normal consecutive
logic ---------- @Test fun
`three consecutive wrongs from EASY go to MEDIUM`() { var s = state()
repeat(2) { s = calculateDifficulty(s, false, ReviewType.DAILY, 0)
assertEquals(VocabularyDifficulty.EASY, s.current) } s =
calculateDifficulty(s, false, ReviewType.DAILY, 0)
assertEquals(VocabularyDifficulty.MEDIUM, s.current) assertEquals(0,
s.consecutiveWrong) } @Test fun
`opposite answer zeros the previous counter`() { var s = state(cc = 2) s
= calculateDifficulty(s, false, ReviewType.DAILY, 0) assertEquals(0,
s.consecutiveCorrect) assertEquals(1, s.consecutiveWrong)
assertEquals(VocabularyDifficulty.EASY, s.current) } @Test fun
`hasReachedVeryHard is monotonic`() { var s = state(current =
VocabularyDifficulty.HARD, cw = 2) s = calculateDifficulty(s, false,
ReviewType.DAILY, 0) // → VERY_HARD
assertEquals(VocabularyDifficulty.VERY_HARD, s.current)
assertTrue(s.hasReachedVeryHard) // later become easier s =
calculateDifficulty(s.copy(consecutiveCorrect = 2), true,
ReviewType.DAILY, 0) assertEquals(VocabularyDifficulty.HARD, s.current)
assertTrue(s.hasReachedVeryHard) // still true } @Test fun
`threshold not reached keeps level and increments counter`() { val s =
calculateDifficulty(state(cw = 1), false, ReviewType.DAILY, 0)
assertEquals(VocabularyDifficulty.EASY, s.current) assertEquals(2,
s.consecutiveWrong) } } 6. Recommended Next Steps 18. Copy the two pure
functions + enums into the domain module. 19. Run the Unit Tests above
(they should all pass). 20. Implement Repository interfaces
(LearningStateRepository, DifficultyStateRepository, ...). 21. Implement
SubmitReviewAnswerUseCase exactly with the call order shown in section
4. 22. Add Integration Test that proves the @Transaction rolls back on
any failure. This document is the authoritative implementation guide for
Phase 2 algorithms. All rules are taken from the frozen نسخهٔ مستندشده
triad + 2026-09-10 corrections. نسخهٔ مستندشده --- Phase 2 Algorithm
Corrections این بخش به‌عنوان الحاقیهٔ اصلاحی نسخهٔ مستندشده اضافه شده و در
تعارض با بخش‌های قدیمی‌تر همین سند، همین الحاقیه مرجع اجرایی است. 1)
Threshold correction در نسخهٔ اجرایی نسخهٔ مستندشده، threshold_difficulty
پیش‌فرض 3 است. منطق شمارندهٔ عادی بر پاسخ‌های متوالی هم‌نوع اعمال می‌شود. سه
پاسخ صحیح متوالی در EASY باعث MEDIUM شدن نمی‌شود؛ EASY کف سختی است. سه
پاسخ غلط متوالی در DAILY از EASY به MEDIUM می‌رود و هر دو شمارنده پس از
تغییر Difficulty صفر می‌شوند. 2) Duplicate-attempt ordering برای
SubmitReviewAnswer، ابتدا وجود LearningState و DifficultyState بررسی
می‌شود؛ سپس Duplicate Detection بر اساس (sessionId, reviewAttemptId)
انجام می‌شود؛ بعد Due Validation اجرا می‌شود. این ترتیب باعث می‌شود replay
یک attempt قبلاً ثبت‌شده همیشه به‌صورت Duplicate شناخته شود، حتی اگر
nextReviewAt در تلاش اول جلو رفته باشد. 3) ReviewSession schema
ReviewSessionEntity شامل id، startedAt، endedAt و reviewType است. این
تغییر نسبت به bootstrap نسخهٔ مستندشده باعث ارتقای Room schema از 1 به 2
شده است. 4) Migration برای مرز schema 1→2، ستون reviewType در
review_sessions با مقدار پیش‌فرض DAILY اضافه می‌شود و migration در
database builder ثبت شده است. 5) Transaction CreateConcept و
SubmitReviewAnswer همچنان باید تمام نوشتن‌های وابسته را در یک
FlashLearnDatabase.withTransaction انجام دهند. 6) Hilt/build graph
Dependency graph نهایی نسخهٔ مستندشده شامل Hilt plugin/dependencies و
application bootstrap است. core به domain/data/database وابسته است؛ data
به domain/database وابسته است؛ database به domain وابسته است. 7) Test
corrections Instrumentation gateها از nested runBlocking حذف شده‌اند.
Gate rollback مستقیماً DAO را برای بررسی ReviewHistory می‌خواند و Gate
threshold سه wrong متوالی را با زمان‌های متناسب با due schedule آزمایش
می‌کند. 8) Verification status این نسخه static-audit corrected است. در
این محیط Gradle/Android build اجرا نشده است؛ بنابراین PASS نهایی build و
instrumentation فقط پس از اجرای CI در GitHub قابل اعلام است. FlashLearn
--- Phase 3: Room Database IMPLEMENTATION-READY --- pending integration
validation Source of Truth: Descriptions v4 + Algorithms v4 + Phase 1
v4 + Phase 2 v4. The نسخهٔ مستندشده Room document is implementation
source material only; v4 contracts take precedence. 0. v4 Alignment -
ConceptEntity uses entryType, not the legacy contentType name. - Domain
repository signatures must match Phase 1 v4. - LearningState and
DifficultyState are independent tables/models. - Active Concept without
LearningState or DifficultyState is DATA_INTEGRITY_ERROR; never invent
state. - Content is unique on (conceptId, languageCode).

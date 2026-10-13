# برنامه اجرایی اصلاح باگ‌های دیتابیس، کلمات تکراری و جستجوی بدون اعراب (نسخه ۱.۳۴.۰)

این برنامه گام‌های فنی و معماری لازم جهت رفع باگ‌های با اولویت بالای بخش دیتابیس (موارد ۱.۱، ۱.۲ و ۱.۳ سند `PROPOSED_ISSUES_LIST.md`) و اجرای دقیق چک‌لیست انتشاراتی برای نسخه ۱.۳۴.۰ را تبیین می‌کند.

---

## تصمیمات و تاییدات کاربر (User Review & Critical Decisions)

- **اولویت انتخابی کاربر**: تمرکز بر باگ‌های دیتابیس، بررسی کلمات تکراری و جستجوی بدون اعراب (موارد ۱.۱، ۱.۲ و ۱.۳).
- **روال انتشاراتی**: اعمال کامل چک‌لیست استاندارد انتشار (`RELEASE_ROUTINE_CHECKLIST.md`) و ارتقای نسخه به `1.34.0` (کد نسخه `43`).
- **حفظ یکپارچگی پایگاه داده بدون ریسک Migration**: بدون تغییر ساختار جداول Room جهت پیشگیری مطلق از خطاهای نصب یا تخریب داده‌های کاربر در آپدیت‌ها.

---

## ۱. مرور کلی و راهکارها (Overview & Core Concept)

1. **بررسی عدم تکرار در افزودن دستی واژه (`addWord` در `VocabularyRepository.kt`) [مورد ۱.۱]:**
   - پیش از تولید `UUID` و درج رکورد جدید، بررسی وجود رکورد فعال با همان `canonicalKey` و `languageCode`.
   - در صورت وجود واژه فعال تکراری، بازگرداندن پیام خطای مشخص و کاربرپسند (`Result.failure`).
   - در صورت وجود واژه اما غیرفعال بودن آن، فعال‌سازی مجدد واژه (`active = true`) و به‌روزرسانی ترجمه‌ها و برچسب زمان.

2. **پشتیبانی از جستجوی بدون اعراب اسپانیایی در دیتابیس [مورد ۱.۲]:**
   - در کوئری‌های `searchConceptsFiltered` و `searchConcepts` در `ConceptDao.kt`، کاراکترهای با اعراب اسپانیایی (`á`, `é`, `í`, `ó`, `ú`, `ü`, `ñ`) در فیلد `canonicalKey` با توابع توکار `REPLACE` در SQLite به حروف پایه تبدیل می‌شوند تا جستجوی کلماتی مثل `adios` واژه `adiós` را با دقت کامل بازیابی کند.

3. **نرمال‌سازی کامل متون فارسی در `TextUtilities.kt` [مورد ۱.۳]:**
   - تبدیل حروف عربی «ي» و «ك» به فارسی «ی» و «ک».
   - تبدیل انواع الف‌های همزه‌دار و مددار («آ»، «أ»، «إ») به «ا» در کلیدهای جستجو.
   - پاک‌سازی و یکنواخت‌سازی نیم‌فاصله (`\u200C` / ZWNJ) در تولید کلید جستجو جهت برابری نتایج متصل و منفصل.

4. **اجرای گام‌به‌گام چک‌لیست انتشار (`docs/RELEASE_ROUTINE_CHECKLIST.md`):**
   - کامپایل نهایی با `compile_applet`.
   - افزایش `versionCode = 43` و `versionName = "1.34.0"` در `app/build.gradle.kts`.
   - همگام‌سازی `README.md` و `docs/Yadin-Specification1-1.md`.
   - ثبت کامل تغییرات در `docs/CHANGELOG.md`.
   - به‌روزرسانی وضعیت موارد در `docs/PROPOSED_ISSUES_LIST.md`.

---

## ۲. دیاگرام جریان معماری و داده (Technical Architecture Diagram)

```
┌────────────────────────────────────────────────────────┐
│                      UI / Screens                      │
│   (AddEditWordScreen / LibraryScreen Search Field)     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                  VocabularyRepository                  │
├────────────────────────────────────────────────────────┤
│ 1. addWord():                                          │
│    - Compute canonicalKey                              │
│    - Query findActiveContentByCanonicalKey()           │
│    - If exists -> Return Friendly Error (No Duplicate) │
│    - If inactive exists -> Reactivate & Update         │
│ 2. searchFilteredPaged():                              │
│    - TextUtilities.searchKey() with Persian &          │
│      Spanish Diacritics Normalization                  │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                   ConceptDao (SQLite)                  │
├────────────────────────────────────────────────────────┤
│ searchConceptsFiltered / searchConcepts:               │
│ - SQLite REPLACE chain for Spanish diacritics          │
│   (á->a, é->e, í->i, ó->o, ú->u, ü->u, ñ->n)           │
│ - SQLite REPLACE chain for Persian Arabic yeh/kaf      │
│   (ي->ی, ك->ک, آ->ا)                                   │
└────────────────────────────────────────────────────────┘
```

---

## ۳. مراحل اجرایی گام‌به‌گام (Implementation Steps)

1. **اصلاح `TextUtilities.kt`:**
   - افزودن تبدیل ی/ك عربی به فارسی و نرمال‌سازی الف و نیم‌فاصله در `cleanText` و `searchKey`.
2. **اصلاح `VocabularyRepository.kt` (`addWord`):**
   - افزودن استعلام `findContentByCanonicalKey` و جلوگیری از ایجاد کانسپت تکراری.
3. **اصلاح کوئری‌های جستجو در `ConceptDao.kt`:**
   - به‌روزرسانی عبارات شرطی `LIKE` برای پشتیبانی بدون اعراب و نرمال‌سازی در سطح پایگاه داده.
4. **تأیید ساخت و کامپایل:**
   - فراخوانی ابزار `compile_applet`.
5. **اجرای چک‌لیست انتشاراتی نسخه ۱.۳۴.۰:**
   - افزایش نسخه در `build.gradle.kts`، ثبت لاگ در `CHANGELOG.md`، و همگام‌سازی `README.md`، `Yadin-Specification1-1.md` و `PROPOSED_ISSUES_LIST.md`.

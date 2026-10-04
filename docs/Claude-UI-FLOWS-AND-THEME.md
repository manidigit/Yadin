# UI Flows و Theme System - FlashLearn v6.84

این سند شامل جریان دقیق صفحات، نقاط تصمیم، و تعریف کامل Theme System است.

---

## بخش اول: UI Flows

### Flow ۱: صفحهٔ اصلی (Home)

#### مسیر ورود:
```
برنامه باز می‌شود
    ↓
Splash Screen (۲ ثانیه)
    ↓
Database Migration (اگر لازم)
    ↓
صفحهٔ Home
```

#### Layout صفحهٔ Home:
```
┌──────────────────────────────┐
│      FlashLearn              │ ← Header
├──────────────────────────────┤
│  🔥 Streak: 15 روز           │ ← Streak Banner
│     بهترین: 30 روز           │
├──────────────────────────────┤
│                              │
│  ┌────────────────────────┐  │
│  │ 📚 روزانه              │  │ ← Review Card 1
│  │ آماده: 12              │  │
│  │ [شروع]                 │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 📖 هفتگی              │  │ ← Review Card 2
│  │ آماده: 5               │  │
│  │ [شروع]                 │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 📕 ماهانه              │  │ ← Review Card 3
│  │ آماده: 2               │  │
│  │ [شروع]                 │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 🎲 تصادفی              │  │ ← Review Card 4
│  │ همه کلمات              │  │
│  │ [شروع]                 │  │
│  └────────────────────────┘  │
│                              │
├──────────────────────────────┤
│ خلاصه:                       │
│ کل: 120 | یادگرفته: 45 (37%) │ ← Summary
│ دقت: 82% | آماده امروز: 12   │
├──────────────────────────────┤
│ [🏠] [📚] [📊] [⚙️]          │ ← Bottom Navigation
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
کاربر کلیک می‌کند بر [شروع] روزانه
    ↓
صفحهٔ تنظیمات مرور باز می‌شود (Flow ۲)

کاربر کلیک می‌کند بر [⚙️] (Settings Tab)
    ↓
صفحهٔ Settings باز می‌شود (Flow ۶)

کاربر کلیک می‌کند بر [📊] (Progress Tab)
    ↓
صفحهٔ Progress باز می‌شود (Flow ۵)
```

---

### Flow ۲: صفحهٔ تنظیمات مرور (Review Setup)

#### Layout:
```
┌──────────────────────────────┐
│ ← نوع مرور               X  │ ← Header
├──────────────────────────────┤
│                              │
│ نوع مرور:                    │
│ ○ فلش‌کارت                    │ ← انتخاب گزینه
│ ● کوئیز (۳ گزینه)             │
│                              │
├──────────────────────────────┤
│ سطح کوئیز: (اگر کوئیز)        │
│ ● مبتدی     ○ متوسط  ○ حرفه   │ ← سطح انتخاب شود
│                              │
├──────────────────────────────┤
│ جهت آزمون:                   │
│ ● اسپانیایی → فارسی           │ ← جهت معمولی
│ ○ فارسی → اسپانیایی           │ ← جهت برعکس
│                              │
├──────────────────────────────┤
│ فیلتر دسته‌بندی (اختیاری):    │
│ [همه] ↓                      │ ← Dropdown
│ (انتخاب‌شده: Verbs)            │
│                              │
├──────────────────────────────┤
│ فیلتر سطح سختی (اختیاری):     │
│ [همه] ↓                      │ ← Dropdown
│ (انتخاب‌شده: MEDIUM, HARD)     │
│                              │
├──────────────────────────────┤
│         [شروع مرور]          │ ← دکمه فعالی
│                              │
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
کاربر تنظیمات را انجام می‌دهد
    ↓
کلیک بر [شروع مرور]
    ↓
IF نوع = FLASHCARD:
  صفحهٔ فلش‌کارت باز می‌شود (Flow ۳.۱)
ELSE IF نوع = QUIZ:
  صفحهٔ کوئیز باز می‌شود (Flow ۳.۲)
```

---

### Flow ۳.۱: صفحهٔ فلش‌کارت (Flashcard Review)

#### Layout (وضعیت اول - پنهان):
```
┌──────────────────────────────┐
│ ← Progress: 5/20             │ ← هدر (سؤال فعلی)
├──────────────────────────────┤
│                              │
│                              │
│                              │
│     کلمهٔ مبدأ                │ ← متن بزرگ
│     "MANZANA"                │
│     (سیب)                     │
│                              │
│                              │
│          [🔊 صدا]            │ ← دکمهٔ صدا
│                              │
│                              │
│      [نمایش ترجمه]            │ ← دکمهٔ اصلی
│                              │
└──────────────────────────────┘
```

#### Layout (وضعیت دوم - نمایش ترجمه):
```
┌──────────────────────────────┐
│ ← Progress: 5/20             │
├──────────────────────────────┤
│                              │
│     کلمهٔ مبدأ                │
│     "MANZANA"                │
│                              │
│     ─────────────────────    │ ← جداکننده
│                              │
│     ترجمه‌ها:                 │
│     • سیب                     │ ← اولی
│     • میوهٔ قرمز               │ ← دوم (اگر موجود)
│                              │
│     یادداشت: میوه‌ای شیرین      │ ← یادداشت (اختیاری)
│                              │
├──────────────────────────────┤
│                              │
│      [✓ درست]  [✗ غلط]      │ ← دکمه‌های پاسخ
│                              │
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
کاربر [درست] یا [غلط] را کلیک می‌کند
    ↓
updateStageAndSchedule() اجرا می‌شود
updateDifficulty() اجرا می‌شود
ReviewHistory ثبت می‌شود
    ↓
اگر سؤالات پایان یافته:
  صفحهٔ نتایج (Flow ۴)
ELSE:
  سؤالِ بعدی نمایش داده می‌شود
```

---

### Flow ۳.۲: صفحهٔ کوئیز (Quiz Review)

#### Layout:
```
┌──────────────────────────────┐
│ ← Progress: 5/20             │ ← هدر
├──────────────────────────────┤
│                              │
│     سؤال:                     │
│     MANZANA = ?              │ ← سؤال
│                              │
├──────────────────────────────┤
│                              │
│  ┌────────────────────────┐  │
│  │ ○ سیب                  │  │ ← گزینه ۱
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ ○ ماشین                │  │ ← گزینه ۲
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ ○ خانه                 │  │ ← گزینه ۳
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ ○ شاخ و برگ             │  │ ← گزینه ۴
│  └────────────────────────┘  │
│                              │
└──────────────────────────────┘
```

#### وضعیت پس از انتخاب (سبز = درست):
```
┌──────────────────────────────┐
│ Progress: 5/20               │
├──────────────────────────────┤
│  ┌────────────────────────┐  │
│  │ ✓ سیب         [سبز]   │  │ ← پاسخ صحیح
│  └────────────────────────┘  │
│                              │
│  [درست! ✓]                  │ ← بازخورد
│                              │
│  (۳ ثانیه صبر...)            │ ← خودکار انتظار
│                              │
└──────────────────────────────┘
```

#### وضعیت پس از انتخاب (قرمز = غلط):
```
┌──────────────────────────────┐
│ Progress: 5/20               │
├──────────────────────────────┤
│  ┌────────────────────────┐  │
│  │ ✓ سیب          [سبز]   │  │ ← پاسخ صحیح
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ ✗ ماشین        [قرمز]  │  │ ← پاسخ غلط
│  └────────────────────────┘  │
│                              │
│  [غلط! ✗]                   │ ← بازخورد
│                              │
│  (۳ ثانیه صبر...)            │ ← خودکار انتظار
│                              │
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
کاربر یک گزینه را تپ می‌کند
    ↓
انتخاب disable می‌شود (نمی‌تواند تغییر بدهد)
    ↓
بازخورد فوری:
  - اگر درست: سبز ✓
  - اگر غلط: قرمز ✗ + پاسخ صحیح سبز
    ↓
updateStageAndSchedule() اجرا می‌شود
updateDifficulty() اجرا می‌شود
ReviewHistory ثبت می‌شود
    ↓
۳ ثانیه صبر (خودکار)
    ↓
اگر سؤالات پایان یافته:
  صفحهٔ نتایج (Flow ۴)
ELSE:
  سؤالِ بعدی خودکار نمایش داده می‌شود
```

---

### Flow ۴: صفحهٔ نتایج (Results)

#### Layout:
```
┌──────────────────────────────┐
│ نتایج جلسهٔ مرور             │ ← عنوان
├──────────────────────────────┤
│                              │
│   دقت:  ██████░░  75%        │ ← Progress Bar
│                              │
│   درست: 15/20                │ ← جزئیات
│   غلط: 5/20                  │
│   مدت: 3 دقیقه               │
│                              │
│   Streak: 15 روز  🔥         │ ← رگبار (اپدیت شده)
│                              │
├──────────────────────────────┤
│                              │
│    [دوبارہ شروع]  [بازگشت]   │ ← دکمه‌های انتخاب
│                              │
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
[دوبارہ شروع]:
  → صفحهٔ تنظیمات مرور بر می‌گردد

[بازگشت]:
  → صفحهٔ Home
```

---

### Flow ۵: صفحهٔ پیشرفت (Progress)

#### Layout:
```
┌──────────────────────────────┐
│ 📊 پیشرفت                   │ ← عنوان
├──────────────────────────────┤
│                              │
│ خلاصه:                       │
│ • کل: 120 کلمه               │ ← خلاصه
│ • یادگرفته: 45 (37%)        │
│ • دقت کلی: 82%               │
│ • Streak: 15 روز             │
│                              │
├──────────────────────────────┤
│ توزیع مرحله:                │
│ DAILY    ████░░░░░░  40      │ ← نمودار
│ WEEKLY   ███░░░░░░░░ 30      │
│ MONTHLY  ██░░░░░░░░░ 20      │
│ LEARNED  █░░░░░░░░░░ 10      │
│                              │
├──────────────────────────────┤
│ توزیع سطح سختی:              │
│ EASY     ██████░░░░░░ 30     │
│ MEDIUM   ████████░░░░ 40     │
│ HARD     ████░░░░░░░░ 20     │
│ VERY_HARD██░░░░░░░░░░ 10     │
│                              │
├──────────────────────────────┤
│ روند (۳۰ روز آخر):          │
│   Week1  Week2  Week3  Week4 │
│      █      ██    ███   ████ │ ← نمودار روند
│                              │
└──────────────────────────────┘
```

---

### Flow ۶: صفحهٔ تنظیمات (Settings)

#### Layout:
```
┌──────────────────────────────┐
│ ⚙️ تنظیمات                  │ ← عنوان
├──────────────────────────────┤
│                              │
│ 🎨 ظاهر                      │
│ ┌──────────────────────────┐ │
│ │ تم: [Grok Gold ▼]        │ │ ← Dropdown
│ │ حالت: [تاریک] [روشن] ◉  │ │ ← Toggle
│ └──────────────────────────┘ │
│                              │
├──────────────────────────────┤
│ 🌐 زبان                      │
│ ┌──────────────────────────┐ │
│ │ رابط: [فارسی ▼]          │ │ ← Dropdown
│ │ جفت یادگیری: [es-fa ▼]   │ │ ← Dropdown
│ └──────────────────────────┘ │
│                              │
├──────────────────────────────┤
│ 🎓 یادگیری                   │
│ ┌──────────────────────────┐ │
│ │ آستانهٔ سختی: ◉──3──────│ │ ← Slider
│ │ سطح کوئیز پیش‌فرض: [متوسط ▼]│
│ │ maxPoolSize: [50 ▼]      │ │
│ └──────────────────────────┘ │
│                              │
├──────────────────────────────┤
│ 💾 داده                      │
│ ┌──────────────────────────┐ │
│ │ [📥 بکاپ کنید]          │ │ ← دکمه
│ │ [📤 بازیابی کنید]         │ │ ← دکمه
│ └──────────────────────────┘ │
│                              │
├──────────────────────────────┤
│ ℹ️ درباره                    │
│ نسخه: 6.84                   │
│                              │
└──────────────────────────────┘
```

---

### Flow ۷: صفحهٔ کتابخانه (Library)

#### Layout:
```
┌──────────────────────────────┐
│ 📚 کتابخانه                  │ ← عنوان
├──────────────────────────────┤
│ [🔍 جستجو...____________]   │ ← جستجو
│ [فیلتر ▼]                    │ ← Dropdown
├──────────────────────────────┤
│                              │
│ ┌────────────────────────┐  │
│ │ apple (سیب)            │  │ ← کلمه
│ │ نوع: WORD | دسته: Food │  │
│ │ سطح: MEDIUM            │  │
│ │ مرحله: WEEKLY          │  │
│ │ [ویرایش] [حذف]         │  │ ← اقدامات
│ └────────────────────────┘  │
│                              │
│ ┌────────────────────────┐  │
│ │ run (دویدن)            │  │ ← کلمه
│ │ نوع: WORD | دسته: Verbs│  │
│ │ سطح: HARD              │  │
│ │ مرحله: DAILY           │  │
│ │ [ویرایش] [حذف]         │  │
│ └────────────────────────┘  │
│                              │
│ ┌────────────────────────┐  │
│ │ ...                    │  │
│ └────────────────────────┘  │
│                              │
└──────────────────────────────┘
```

#### تعامل‌های کلیدی:
```
کاربر بر کلمه کلیک می‌کند
    ↓
صفحهٔ جزئیات (Library Detail) باز می‌شود

[ویرایش]:
  → صفحهٔ ویرایش کلمه باز می‌شود

[حذف]:
  → کلمه soft-delete می‌شود
  → تایید پاپ‌آپ نمایش داده می‌شود
```

---

### Flow ۸: صفحهٔ افزودن کلمه (Add Word)

#### روش ۱: تکی (Single Entry)

```
┌──────────────────────────────┐
│ ➕ افزودن کلمه              │ ← عنوان
├──────────────────────────────┤
│                              │
│ کلمهٔ مبدأ (اجباری):          │
│ [________________]           │ ← TextField
│                              │
│ ترجمه (اجباری):              │
│ [________________]           │ ← TextField
│ [+ ترجمهٔ دیگری]             │ ← اضافی
│                              │
│ نوع (اختیاری):               │
│ [WORD ▼]                     │ ← Dropdown
│                              │
│ دسته (اختیاری):              │
│ [Verbs ▼]                    │ ← Dropdown
│                              │
│ یادداشت (اختیاری):            │
│ [________________]           │ ← TextField
│                              │
├──────────────────────────────┤
│                              │
│      [➕ اضافه کن]           │ ← دکمه فعالی
│                              │
└──────────────────────────────┘
```

#### روش ۲: دسته‌ای (Bulk Import)

```
┌──────────────────────────────┐
│ 📄 وارد کردن دسته‌ای          │ ← عنوان
├──────────────────────────────┤
│                              │
│ [چسباندن متن یا فایل]        │ ← Paste Area
│                              │
│ (CSV یا متن معمولی)         │
│ manzana, سیب                │
│ run, دویدن                   │
│ ...                          │
│                              │
├──────────────────────────────┤
│                              │
│ نحوهٔ رفتار با تکراری:        │
│ ○ رد کردن                    │ ← گزینه
│ ○ ادغام (اضافی)              │ ← گزینه
│ ● بروزرسانی                  │ ← انتخاب
│                              │
├──────────────────────────────┤
│      [📥 وارد کن]           │ ← دکمه
│                              │
└──────────────────────────────┘
```

---

## بخش دوم: Theme System

### Theme Architecture

```
FlashLearnTheme
  ├── ColorScheme
  │   ├── primary
  │   ├── secondary
  │   ├── background
  │   ├── surface
  │   ├── error
  │   ├── success
  │   ├── textPrimary
  │   └── textSecondary
  ├── Typography
  │   ├── fontFamily
  │   ├── headlineLarge
  │   ├── headlineMedium
  │   ├── headlineSmall
  │   ├── bodyLarge
  │   ├── bodyMedium
  │   ├── bodySmall
  │   └── labelMedium
  ├── Spacing
  │   ├── xs: 4dp
  │   ├── sm: 8dp
  │   ├── md: 16dp
  │   ├── lg: 24dp
  │   ├── xl: 32dp
  │   └── pageMargin: 16dp
  ├── Shapes
  │   ├── cornerSmall: 4dp
  │   ├── cornerMedium: 8dp
  │   ├── cornerLarge: 12dp
  │   └── cornerFull: 50%
  └── Components
      ├── buttons
      ├── cards
      ├── inputs
      └── iconSize
```

---

### تم ۱: Grok Gold (Light)

#### رنگ‌ها:
```kotlin
primary = Color(0xFFD4AF37)          // طلایی
secondary = Color(0xFF8B6914)        // طلای تیره
background = Color(0xFFFEFEFE)       // سفید تقریباً
surface = Color(0xFFF5F5F5)          // خاکستری خفیف
error = Color(0xFFD32F2F)            // قرمز
success = Color(0xFF388E3C)          // سبز
textPrimary = Color(0xFF1A1A1A)      // سیاه
textSecondary = Color(0xFF757575)    // خاکستری

// shadows
shadowColor = Color(0xFF000000).copy(alpha = 0.2f)
```

#### Typography:
```kotlin
fontFamily = "Vazir"                 // فارسی
headlineLarge = 28.sp, bold
headlineMedium = 24.sp, semibold
bodyLarge = 16.sp, regular
bodyMedium = 14.sp, regular
bodySmall = 12.sp, regular
labelMedium = 12.sp, semibold
```

#### Shapes:
```kotlin
cornerSmall = 4.dp
cornerMedium = 8.dp
cornerLarge = 16.dp   // بیشتر
```

#### Components:
```kotlin
ButtonHeight = 48.dp
CardElevation = 8.dp   // سایه واضح
```

---

### تم ۲: Claude Olive (Light)

#### رنگ‌ها:
```kotlin
primary = Color(0xFF6B8E23)          // olive green
secondary = Color(0xFF556B2F)        // dark olive
background = Color(0xFFFCFBF7)       // کرم روشن
surface = Color(0xFFF1EFE8)          // کرم
error = Color(0xFFB71C1C)            // قرمز
success = Color(0xFF2E7D32)          // سبز
textPrimary = Color(0xFF1B1B1B)      // سیاه
textSecondary = Color(0xFF6F6F6F)    // خاکستری

shadowColor = Color(0xFF2D5016).copy(alpha = 0.15f)
```

#### Typography:
```kotlin
fontFamily = "Vazir"
headlineLarge = 28.sp, semibold
headlineMedium = 24.sp, medium
bodyLarge = 16.sp, regular
bodyMedium = 14.sp, regular
```

#### Shapes:
```kotlin
cornerSmall = 4.dp
cornerMedium = 6.dp
cornerLarge = 12.dp   // گرد تر
```

---

### تم ۳: Modern Minimal (Dark Preferred)

#### رنگ‌ها (Dark Mode):
```kotlin
primary = Color(0xFF4A9EFF)          // آبی روشن
secondary = Color(0xFF1E88E5)        // آبی
background = Color(0xFF121212)       // سیاه تقریباً
surface = Color(0xFF1E1E1E)          // خاکستری تیره
error = Color(0xFFFF5252)            // قرمز روشن
success = Color(0xFF4CAF50)          // سبز
textPrimary = Color(0xFFFFFFFF)      // سفید
textSecondary = Color(0xFFBDBDBD)    // خاکستری روشن

shadowColor = Color(0xFF000000).copy(alpha = 0.4f)
```

#### Typography:
```kotlin
fontFamily = "Vazir"
headlineLarge = 28.sp, bold
headlineMedium = 24.sp, bold
bodyLarge = 16.sp, regular
bodyMedium = 14.sp, regular

// سبک: مینیمال
// بدون decoration
```

#### Shapes:
```kotlin
cornerSmall = 2.dp    // تقریباً مربع
cornerMedium = 4.dp
cornerLarge = 8.dp    // minimal
```

---

### Spacing Tokens (مشترک):

```kotlin
spacingXs = 4.dp      // gap
spacingSm = 8.dp      // padding small
spacingMd = 16.dp     // padding standard
spacingLg = 24.dp     // padding large
spacingXl = 32.dp     // section gap
pageMargin = 16.dp    // حاشیهٔ صفحه
```

---

### Component Design Guide

#### Button:
```
Height: 48.dp
Padding: 16.dp (horizontal), 12.dp (vertical)
Corner: theme.cornerMedium
Text: labelMedium
State:
  - Enabled: primary color
  - Pressed: darken by 10%
  - Disabled: grey + opacity 50%
```

#### Card:
```
Elevation: theme specific
  - Grok Gold: 8.dp
  - Claude Olive: 4.dp
  - Modern Minimal: 2.dp
Corner: theme.cornerLarge
Padding: spacingMd
Background: surface color
Border: none (or thin line in dark mode)
```

#### TextField:
```
Height: 56.dp
Padding: spacingMd
Corner: theme.cornerMedium
Border: 1.dp, textSecondary
Focus: 2.dp, primary
Cursor: primary color
```

#### Icon:
```
Size: 
  - Large: 32.dp
  - Medium: 24.dp
  - Small: 18.dp
Color: textPrimary or primary
```

---

### Dark Mode Variants

#### Grok Gold (Dark):
```kotlin
primary = Color(0xFFFFD700)          // طلایی روشن‌تر
background = Color(0xFF1A1A1A)       // سیاه تقریباً
surface = Color(0xFF2A2A2A)          // خاکستری تیره
textPrimary = Color(0xFFFFFAF0)      // کرم روشن
textSecondary = Color(0xFFBDBDBD)    // خاکستری

// بقیه رنگ‌ها adjust
```

#### Claude Olive (Dark):
```kotlin
primary = Color(0xFF9CCC65)          // سبز روشن
background = Color(0xFF1B1B1B)       // سیاه
surface = Color(0xFF2A2A2A)          // خاکستری تیره
textPrimary = Color(0xFFFFF9E6)      // کرم روشن
```

#### Modern Minimal (Light):
```kotlin
primary = Color(0xFF1E88E5)          // آبی تیره
background = Color(0xFFFAFAFA)       // سفید
surface = Color(0xFFF5F5F5)          // خاکستری روشن
textPrimary = Color(0xFF212121)      // سیاه
textSecondary = Color(0xFF757575)    // خاکستری
```

---

### Theme Selection Logic

```kotlin
FUNCTION applyTheme(themeId: String, isDark: Boolean) {
  theme = WHEN themeId:
    "grok" → GrokGoldTheme
    "claude" → ClaudeOliveTheme
    "minimal" → ModernMinimalTheme
    else → GrokGoldTheme
  
  IF isDark:
    theme = theme.toDarkMode()
  
  applyToApp(theme)
}
```

---

### Hard-Coded Rule

❌ این‌ها پیدا نشده باید باشند:
```kotlin
// غلط:
Text(color = Color(0xFFD4AF37))  // رنگ مستقیم

// صحیح:
Text(color = theme.colorScheme.primary)
```

---

**پایان UI Flows و Theme System**

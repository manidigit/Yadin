# لیست مشکلات و وظایف آتی پروژه یادین (Yadin Pending Issues & Tasks)

این لیست صرفاً شامل مشکلات و ارتقاهای فنی تاییدشده و فعال در سورس‌کد است که باید در مراحل بعد برطرف گردند. (کدهای پروژه فعلاً تغییری نیافته‌اند.)

---

## ۱. اولویت بالا (P1) - باگ‌های رفتاری و همزمانی

### ۱.۱. عدم غیرفعال‌سازی دکمه هنگام ذخیره و بازیابی (Debouncing & Disabled State)
* **موقعیت:**
  - `app/src/main/java/com/manidigit/yadin/ui/screens/AddEditWordScreen.kt`
  - `app/src/main/java/com/manidigit/yadin/ui/screens/BackupScreen.kt`
* **شرح مشکل:**
  - در صفحه افزودن/ویرایش واژه، دکمه ذخیره فاقد استیت محلی `isSaving` بوده و حین عملیات درج در پایگاه داده غیرفعال (`enabled = false`) نمی‌شود. در صورت فشردن سریع و چندباره دکمه توسط کاربر، درخواست‌های درج تکراری ارسال می‌شود.
  - در صفحه پشتیبان‌گیری، دکمه آغاز بازیابی بکاپ نیز در حین اجرای فرآیند بازگردانی دیتابیس قفل و غیرفعال نمی‌شود.
* **اقدام لازم:**
  - تعریف استیت `isSaving` و اعمال `enabled = !isSaving` روی دکمه ذخیره.
  - اعمال `enabled = !isRestoring` روی دکمه اجرای بازیابی در دیالوگ تایید.

---

## ۲. اولویت متوسط (P2) - بهینه‌سازی حافظه و معماری دیتابیس

### ۲.۱. بافر کردن کل فایل پشتیبان در حافظه RAM و ریسک خطای OutOfMemory
* **موقعیت:**
  - `app/src/main/java/com/manidigit/yadin/data/repository/BackupRepository.kt`
* **شرح مشکل:**
  - متدهای `createBackupJsonWithOptions` و خروجی CSV کل رکوردهای چند هزار لغت، ترجمه‌ها و دسته‌ها را در یک شیء `JSONObject` و یک `StringBuilder` در حافظه RAM بافر می‌کنند و سپس کل رشته را با `root.toString(2)` تولید می‌کنند. در دیتابیس‌های حجیم، این امر بار حافظه بسیار بالایی ایجاد کرده و ریسک `OutOfMemoryError` دارد.
* **اقدام لازم:**
  - تغییر رویکرد ساخت رشته در حافظه به نوشتن مستقیم جریانی بر بستر `JsonWriter` و `OutputStream` فایل مقصد.

### ۲.۲. فقدان قید `@ForeignKey(onDelete = CASCADE)` در انتیتی‌های دیتابیس
* **موقعیت:**
  - `app/src/main/java/com/manidigit/yadin/data/local/entity/Entities.kt`
* **شرح مشکل:**
  - جدول‌های وابسته (مانند `learning_states`, `difficulty_states`, `review_history`, `concept_categories`) قید رسمی کلید خارجی با حذف آبشاری (`CASCADE`) در حاشیه‌نویسی Room ندارند. پاکسازی فعلی متکی بر کدهای ریپازیتوری است و در سطح موتور SQLite قیدی تعریف نشده است.
* **اقدام لازم:**
  - افزودن `@ForeignKey` با `onDelete = ForeignKey.CASCADE` به انتیتی‌های مربوطه در Room.

### ۲.۳. انباشتگی چند کلاسه در فایل‌های Entity و DAO (اصلاح ساختار Clean Architecture)
* **موقعیت:**
  - `app/src/main/java/com/manidigit/yadin/data/local/entity/Entities.kt` (۱۱ انتیتی در یک فایل)
  - `app/src/main/java/com/manidigit/yadin/data/local/dao/OtherDaos.kt` (تجمیع `SettingsDao` و `AchievementDao`)
* **شرح مشکل:**
  - تجمیع تمامی انتیتی‌ها در یک فایل طولانی و ادغام چند DAO در یک فایل غیراختصاصی خوانایی، تفکیک وظایف و نگهداشت‌پذیری پروژه را کاهش می‌دهد.
* **اقدام لازم:**
  - تفکیک انتیتی‌ها به فایل‌های مجزا.
  - تفکیک `OtherDaos.kt` به دو فایل اختصاصی `SettingsDao.kt` و `AchievementDao.kt`.

---

## ۳. اولویت پایین (P3) - بهینه‌سازی جزئی رابط کاربری

### ۳.۱. بهینه‌سازی Sub-composition انیمیشن چرخش کارت
* **موقعیت:**
  - `app/src/main/java/com/manidigit/yadin/ui/screens/FlashcardScreen.kt`
* **شرح مشکل:**
  - در چرخش ۳ بعدی فلش‌کارت، شرط نمای رو و پشت کارت در لحظه تغییر زاویه ارزیابی می‌شود؛ تفکیک بیشتر کامپوزبل جلو و پشت کارت می‌تواند از هرگونه ری‌کامپوزیشن غیرضروری در فریم‌های میانی چرخش جلوگیری کند.
* **اقدام لازم:**
  - ایزوله‌سازی محتوای دو طرف کارت در دو Composable مجزا درون انیمیشن.

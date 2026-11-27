# Yadin Multi-User Backend — Gemini Implementation Handoff

**Status:** Approved implementation contract  
**Date:** 2026-11-27  
**Android repository:** https://github.com/manidigit/Yadin  
**Backend repository:** https://github.com/manidigit/Yadin-Server  
**Production API base:** `https://yadin.manico.es/yadin/api/v1`

> این سند مکمل مشخصات محصول است و فقط تصمیم‌های جدید مربوط به چندکاربره‌شدن، سرور، حساب‌ها و همگام‌سازی را تعیین می‌کند. مشخصات آموزشی، الگوریتم‌ها، UX و رفتار فعلی برنامه در `docs/Yadin-Specification1-1.md` همچنان معتبر است؛ بند قدیمی «هیچ حساب کاربری یا بک‌اند اختصاصی وجود ندارد» برای این قابلیت صراحتاً لغو شده است.

## 1) مأموریت و قواعد غیرقابل مذاکره

پیاده‌سازی یک سامانه چندکاربره برای Yadin با معماری **Offline-First**:

- Android: Kotlin, Jetpack Compose, Room؛ تمام قابلیت‌های اصلی باید بدون اینترنت کار کنند.
- Backend: PHP 8.3 روی هاست اشتراکی، MariaDB 10.6، REST API زیر مسیر `/yadin/api/v1`.
- حساب‌ها فقط توسط مدیر ساخته می‌شوند؛ ثبت‌نام عمومی، بازیابی رمز از طریق ایمیل و ورود ناشناس در فاز اول وجود ندارد.
- هر کاربر داده‌های شخصی مستقل دارد؛ هیچ query یا endpoint نباید داده کاربر دیگر را افشا یا تغییر دهد.
- بانک واژگان عمومی، مشترک و تحت مدیریت مرکزی است؛ برنامه‌های کاربران فقط نسخه منتشرشده را دریافت می‌کنند.
- واژگان شخصی کاربر هرگز خودکار به بانک عمومی منتشر نمی‌شود.
- برنامه بدون اتصال به سرور همچنان قابل استفاده است؛ خطای شبکه نباید عملیات محلی را متوقف کند.
- هیچ داده‌ای حذف یا reset نشود. Migration باید non-destructive و قابل آزمون باشد.
- رمز، access token و refresh token خام هرگز در DB یا log ذخیره نشود.
- هیچ secret واقعی در Git، APK، مستندات عمومی یا پاسخ API قرار نگیرد.

## 2) وضعیت موجود که باید حفظ شود

### Backend موجود
مخزن `Yadin-Server` روی branch اصلی دارای این موارد است:
- Front controller: `index.php`
- Router ساده و exact-match: `src/Http/Router.php`
- Request/Response: `src/Http/`
- اتصال PDO: `src/Database/Connection.php`
- Env loader: `src/Support/Env.php`
- Migration اولیه: `database/migrations/001_initial_schema.sql`
- Deploy با GitHub Actions و FTPS.

در Production این دو endpoint بررسی شده‌اند و سالم‌اند:
- `GET /api/v1/health` → `status=ok`
- `GET /api/v1/ready` → `status=ready`, database=ok

آدرس کامل:
- `https://yadin.manico.es/yadin/api/v1/health`
- `https://yadin.manico.es/yadin/api/v1/ready`

مسیر `/yadin/` فعلاً route ندارد و 404 آن به‌تنهایی نشانه خرابی API نیست.

Migration اولیه هم‌اکنون جدول‌های `users`, `devices`, `refresh_tokens`, `user_settings`, `concepts`, `contents`, `categories`, `concept_categories`, `vocabulary_revisions`, `vocabulary_changes` را ایجاد می‌کند. قبل از افزودن migration جدید، schema واقعی را بررسی کن و migration قبلی را بازنویسی یا destructive اجرا نکن.

### Android موجود
- Room database فعلی version **11** است.
- `SettingEntity` فعلی فقط `key` را Primary Key دارد؛ این مدل برای چندکاربره مناسب نیست.
- جداول یادگیری، سختی، جلسات مرور، تاریخچه، تنظیمات و دستاوردها باید به کاربر جاری scope شوند.
- `RemoteSeedUpdateRepository` فعلی برای دریافت seed استاتیک از GitHub وجود دارد. آن را بی‌دلیل حذف یا با API جدید جایگزین نکن؛ دریافت بانک عمومی از سرور باید با نسخه‌بندی و قرارداد جدید طراحی شود.
- قبل از تغییر هر Entity/DAO/Repository، تمام ارجاعات، backup/import/export، seed importer و تست‌های مرتبط را بررسی کن.

## 3) مدل مالکیت و تفکیک داده

سه نوع داده را از هم جدا نگه دار:

### A. بانک واژگان عمومی (Global)
- مفهوم، محتوا، ترجمه، مثال، تلفظ و دسته‌بندی منتشرشده.
- برای تمام کاربران یکسان و فقط خواندنی در کلاینت.
- سرور مرجع نهایی است.
- هر مفهوم دارای شناسه پایدار UUID به نام `public_id` است؛ شناسه عددی داخلی DB هرگز به Android تحمیل نشود.
- انتشار تغییرات فقط توسط مدیر مجاز است و هر انتشار revision افزایشی دارد.

### B. واژگان شخصی (User-owned)
- هر مفهوم شخصی باید `owner_user_id` داشته باشد و فقط مالک آن را ببیند/ویرایش کند.
- در کلاینت، مفهوم شخصی با مفهوم عمومی قاطی یا به‌جای آن overwrite نشود.
- هیچ مسیر خودکاری برای public کردن داده شخصی وجود نداشته باشد.
- حذف حساب/واژه نباید واژه عمومی را حذف کند.

### C. داده شخصی یادگیری (User-owned)
موارد زیر برای هر کاربر مستقل هستند:
- وضعیت یادگیری هر مفهوم و جهت کارت (normal/reverse)
- سختی و شمارنده‌های سختی
- جلسات مرور و اقلام جلسه
- تاریخچه مرور
- تنظیمات
- دستاوردها و آمار مشتق‌شده (در صورت ذخیره‌سازی)

در Android تمام این داده‌ها باید با `userId` فعال scope شوند. شناسه کاربر از session معتبر به دست می‌آید، نه از پارامتر ارسالی دلخواه کلاینت. در API نیز هر query با user ID استخراج‌شده از token محدود شود.

**نکته مهم:** برای جداول محلی، داده‌های مشترک واژگان عمومی را به زور user-scoped نکن؛ داده‌های شخصی را حتماً user-scoped کن. اگر یک جدول ترکیبی است، مالکیت را صریح با نوع منبع و owner مشخص کن.

## 4) حساب و احراز هویت

### سیاست حساب
- فقط نقش `admin` اجازه ساخت، غیرفعال‌کردن و مدیریت حساب‌های `member` را دارد.
- هیچ endpoint ثبت‌نام عمومی وجود ندارد.
- ایمیل در زمان ذخیره trim و lowercase شود.
- رمز عبور حداقل 12 کاراکتر؛ در صورت امکان حداقل یک سیاست قوی‌تر بدون محدودیت غیرمنطقی اعمال شود.
- ذخیره رمز فقط با `password_hash()` و بررسی با `password_verify()`. Argon2id در صورت پشتیبانی PHP؛ در غیر این صورت PASSWORD_DEFAULT.
- خطای login نباید مشخص کند ایمیل وجود ندارد یا رمز اشتباه است.
- کاربر disabled و device revoked نباید بتواند refresh یا API call انجام دهد.

### Token design
برای فاز اول از opaque random bearer token استفاده کن؛ JWT لازم نیست:
- Access token: حداقل 32 بایت تصادفی امن، عمر 15 دقیقه.
- Refresh token: حداقل 64 بایت تصادفی امن، عمر 30 روز، فقط hash آن در DB.
- token خام فقط یک بار در پاسخ HTTPS برگردد؛ hash با SHA-256 و مقایسه امن ذخیره/بررسی شود.
- refresh rotation اجباری: هر refresh token فقط یک بار قابل استفاده؛ توکن قبلی revoke و توکن جدید صادر شود.
- reuse توکن refresh قبلی باید کل زنجیره/دستگاه مربوط را revoke کند و رویداد امنیتی ثبت شود.
- logout، logout-all-devices و revoke-device باید server-side اثر واقعی داشته باشند.
- Access tokenها نیز باید قابل revoke باشند؛ جدول `access_tokens` یا طراحی معادل لازم است.
- tokenها در Android در EncryptedSharedPreferences یا Android Keystore-backed secure storage نگهداری شوند؛ نه plain SharedPreferences، Room، log یا backup عمومی.
- تمام ارتباطات فقط HTTPS؛ هیچ راهکار غیرفعال‌کردن certificate validation مجاز نیست.

### ساخت اولین مدیر
هیچ credential پیش‌فرضی در repo نگذار. یک روش bootstrap یک‌باره و امن طراحی کن:
- ترجیحاً اسکریپت CLI/دستور مدیریتی که روی محیط امن اجرا شود.
- چون هاست shell access ندارد، جایگزین موقت فقط با secret تصادفی قوی در ENV، endpoint محدودشده و one-time قابل استفاده است؛ پس از ساخت اولین admin فوراً endpoint و secret را حذف/غیرفعال کن.
- هرگز bootstrap endpoint عمومی دائمی باقی نماند.
- مستندات دقیق ساخت اولین مدیر را بنویس؛ رمز مدیر را در GitHub Actions log چاپ نکن.

## 5) API Contract

تمام پاسخ‌ها JSON و دارای HTTP status درست باشند. خطاها ساختار ثابت داشته باشند:
```json
{"error":{"code":"VALIDATION_ERROR","message":"...","details":{}}}
```
برای هر درخواست authenticated، هویت فقط از Bearer token معتبر استخراج شود.

### Public / system
- `GET /health` — liveness؛ بدون وابستگی DB.
- `GET /ready` — اتصال DB و وضعیت schema.
- `GET /vocabulary/manifest` — revision فعلی، schemaVersion، counts، generatedAt.
- `GET /vocabulary/changes?since={revision}&limit={n}` — تغییرات incremental؛ cursor/revision واضح، حداکثر limit کنترل‌شده؛ حذف‌ها tombstone باشند.
- `GET /vocabulary/snapshot` — snapshot صفحه‌بندی‌شده برای نصب اولیه یا بازیابی از revision نامعتبر؛ هرگز کل بانک حجیم را یکجا بدون limit برنگردان.

### Authentication
- `POST /auth/login` — email, password, deviceUuid, deviceName, platform؛ صدور access/refresh.
- `POST /auth/refresh` — refresh token و device؛ rotation اتمیک.
- `POST /auth/logout` — revoke نشست/توکن جاری.
- `POST /auth/logout-all` — revoke تمام device/tokenهای کاربر.
- `GET /auth/me` — اطلاعات حساب جاری و نقش.
- `GET /auth/devices` — دستگاه‌های کاربر.
- `DELETE /auth/devices/{deviceId}` — revoke دستگاه متعلق به همان کاربر.

### Admin
- `POST /admin/users` — ایجاد member.
- `GET /admin/users` — فهرست صفحه‌بندی‌شده.
- `PATCH /admin/users/{id}` — displayName/status؛ فقط admin.
- `POST /admin/users/{id}/reset-password` — تنظیم رمز جدید توسط admin؛ رمز در پاسخ/log ثبت نشود.
- `POST /admin/vocabulary/publish` — انتشار revision پس از validation کامل.
- هر endpoint مدیریتی باید role را server-side بررسی کند.

### User sync (فاز بعد از Auth و Global Vocabulary)
- `GET /sync/pull?cursor={opaqueCursor}` — تغییرات شخصی سرور به کلاینت.
- `POST /sync/push` — ارسال batch تغییرات محلی.
- هر mutation دارای `operationId` UUID یکتا، `entityId` پایدار، `updatedAt` و `baseVersion` باشد.
- پاسخ push برای هر operation نتیجه مستقل بدهد: applied / duplicate / conflict / rejected.
- عملیات push idempotent باشد؛ ارسال مجدد operationId نباید دوباره اعمال شود.
- cursor فقط پس از commit موفق تغییر کند.
- batch هرگز نیمه‌کاره ثبت نشود؛ تراکنش DB یا semantics روشن per-operation.
- conflict policy برای داده یادگیری: از دست دادن هیچ review history ممنوع؛ history append-only و deduplicate با operationId؛ state با version/updatedAt و قاعده قطعی reconcile شود.
- سرور نباید داده عمومی واژگان را از push کاربر بپذیرد.

### HTTP conventions
- 200 موفق، 201 ساخته شد، 204 حذف/خروج موفق، 400 malformed، 401 token نامعتبر/منقضی، 403 نقش/مالکیت ناکافی، 404 موجود نیست، 409 conflict، 422 validation، 429 rate limit، 500 خطای داخلی، 503 DB unavailable.
- CORS فقط در صورت نیاز و allowlist؛ API موبایل به CORS نیاز ندارد.
- Content-Type درخواست‌های JSON بررسی شود؛ اندازه body محدود شود.
- pagination limit پیش‌فرض 50 و حداکثر 200.
- تاریخ‌ها UTC ISO-8601 در API؛ تاریخ روز محلی یادگیری همان semantics فعلی برنامه را حفظ کند.

## 6) Sync architecture

این دو جریان را جدا نگه دار:

1. **Global vocabulary sync:** pull-only از سرور به Android با revision افزایشی و snapshot recovery.
2. **Personal learning sync:** دوطرفه، فقط برای داده‌های شخصی حساب احراز‌شده.

قواعد:
- Android همیشه ابتدا local write را انجام دهد و عملیات sync را در outbox پایدار ثبت کند.
- WorkManager برای retry پس از اتصال، backoff نمایی و constraint شبکه.
- پاسخ‌های موقت 5xx/timeout retry؛ خطاهای 4xx غیرقابل retry تا رفع علت.
- هیچ عملیات sync نباید روی Main thread باشد.
- تغییرات دریافتی در تراکنش Room اعمال شوند.
- cursor و داده‌های batch در یک تراکنش هماهنگ commit شوند.
- از last-write-wins ساده برای تاریخچه استفاده نکن.
- حذف‌ها با tombstone و retention مشخص انجام شوند؛ حذف فیزیکی فوری سبب resurrection نشود.
- sync چند دستگاهی باید قابل پیش‌بینی باشد؛ شناسه operation مستقل از دستگاه و پایدار باشد.

## 7) Android Room migration

نسخه فعلی Room = 11؛ نسخه بعدی باید حداقل 12 باشد. schema export را فعال کن و schema JSON را در repo نگه دار.

### الزام‌ها
- `SettingEntity`: کلید اصلی از `key` به composite key شامل `userId + key` تغییر کند. برای تنظیمات قبل از ورود، namespace مستقل مثل `LOCAL_GUEST` یا جدول جدا داشته باش.
- جداول یادگیری/سختی/مرور/تاریخچه/دستاوردهای شخصی باید `userId` داشته باشند و index مناسب `userId` بسازند.
- تمام DAOهای این جداول باید userId را در تمام read/update/delete و Flowها اعمال کنند؛ query بدون userId ممنوع، مگر متد مدیریتی صریح و تست‌شده.
- کلیدهای یکتا و indexهای فعلی باید با userId بازطراحی شوند؛ مثلاً unique(conceptId,direction) تبدیل به unique(userId,conceptId,direction) برای داده‌های شخصی.
- داده‌های بانک عمومی با userId تهی/ثابت یا جدول مجزا باقی بمانند؛ راهکار نهایی را با schema فعلی هماهنگ کن و در سند migration توضیح بده.
- جدول outbox و sync metadata اضافه شوند؛ حداقل cursor عمومی، cursor شخصی، آخرین sync، operationId، payload، entityType، entityId، retryCount، status و timestamps.
- اطلاعات token در جدول sync/outbox یا Room ذخیره نشود؛ secure storage جدا.
- migration باید روی نسخه واقعی 11 با داده پرشده آزمایش شود؛ فقط migration خالی کافی نیست.
- export/import/backup باید با مدل چندکاربره هماهنگ شود و token/secret را هرگز export نکند.

### مهم‌ترین قاعده حفظ داده قدیمی
داده‌های نصب فعلی که قبل از ورود حساب ساخته شده‌اند، خودکار به اولین حسابی که login می‌کند منتقل نشوند. هنگام اولین ورود:
- اگر داده محلی legacy وجود دارد، کاربر یک انتخاب شفاف ببیند: «انتقال داده‌های این دستگاه به این حساب» یا «فعلاً انتقال نده».
- انتقال باید یک‌باره، اتمیک، قابل لغو تا قبل از commit و idempotent باشد.
- تا پیش از انتخاب، داده legacy دست‌نخورده بماند.
- هرگز داده یک حساب به حساب دیگری نسبت داده نشود.
- logout نباید داده حساب قبلی را به حساب بعدی نمایش دهد. دیتای کاربر قبلی در فضای محلی جدا یا با فیلتر کامل نگهداری شود.

## 8) Security / privacy / reliability checklist

- PDO prepared statements برای تمام ورودی‌ها.
- password_hash/password_verify؛ هیچ رمز خام.
- random_bytes برای tokenها؛ hash token در DB.
- rate limit login بر اساس IP و identifier؛ lockout دائمی و قابل سوءاستفاده نساز.
- عدم افشای وجود email در login.
- authorization در هر endpoint؛ IDOR ممنوع.
- role admin در سمت سرور.
- validate و normalize همه ورودی‌ها؛ طول/نوع/enum/UUID.
- عدم نمایش exception، SQL یا path سرور به کلاینت.
- log بدون password/token/Authorization header؛ ثبت رویدادهای login failure، refresh reuse، revoke، admin actions.
- CSRF برای endpointهای bearer-token لازم نیست، اما هیچ auth مبتنی بر cookie ناخواسته اضافه نکن.
- secrets فقط در GitHub Actions Secrets و ENV هاست؛ `.env` از public webroot خارج شود اگر ساختار هاست اجازه می‌دهد. اگر فعلاً ممکن نیست، deny rule و تست مستقیم دسترسی الزامی است.
- DB user با حداقل مجوز لازم.
- backup پیش از migration و تست restore.
- production errors خاموش؛ error log سرور فعال.
- HTTPS اجباری؛ درخواست HTTP به HTTPS redirect شود.

## 9) Backend structure and quality

ساختار فعلی را به‌تدریج تمیز کن؛ از rewrite بی‌دلیل پرهیز:
- Controllers/Handlers
- Services (AuthService, TokenService, UserService, VocabularyService, SyncService)
- Repositories/Queries
- Middleware (Authentication, Authorization, RateLimit)
- Validation
- Database migrations
- Tests

Router باید GET/POST/PATCH/DELETE را پشتیبانی کند، route params امن داشته باشد و middleware قابل ترکیب باشد. Request باید JSON body را validate کند. Response باید status/header/JSON را یکپارچه کند. خطاهای uncaught به پاسخ امن تبدیل و با correlation id log شوند.

برای MariaDB 10.6 و PHP 8.3 کدنویسی کن؛ dependency سنگین یا نیازمند shell/daemon نصب نکن. روی هاست اشتراکی cron موجود است ولی اجرای دائمی worker فرض نشود. Sync در درخواست HTTP و cron سبک قابل انجام باشد.

## 10) Testing / acceptance gates

### Backend tests
- login صحیح/غلط/disabled؛ خطای یکسان برای ایمیل ناموجود و رمز غلط.
- refresh rotation؛ reuse token قبلی؛ logout و revoke.
- member نمی‌تواند admin endpoint را اجرا کند.
- user A هیچ داده user B را نمی‌خواند، تغییر نمی‌دهد یا حذف نمی‌کند (تست IDOR).
- prepared statement و validation.
- sync duplicate operation، retry، conflict، batch failure و cursor.
- vocabulary changes/tombstone/revision/snapshot.
- health بدون DB و ready با DB.
- تمام migrationها روی DB خالی و DB دارای داده اجرا شوند.

### Android tests
- migration 11→12 با fixture واقعی و تعداد رکورد قبل/بعد یکسان.
- جداسازی داده دو user در تمام DAOها و صفحه‌ها.
- logout/login user B هیچ state متعلق به user A نشان ندهد.
- legacy data فقط با انتخاب کاربر منتقل شود.
- offline create/review و بعداً sync.
- duplicate push و pull idempotent.
- token در log/backup/export نیست.
- backup/restore قدیمی و جدید بدون از دست رفتن داده.

### Definition of Done
هیچ مرحله‌ای Done نیست مگر:
1. کد compile شود.
2. تست‌های مربوطه سبز باشند.
3. migration non-destructive تأیید شود.
4. API contract و مستندات به‌روز باشند.
5. هیچ secret در diff/log نباشد.
6. GitHub Actions CI موفق باشد.
7. deploy به production فقط پس از تست staging/backup و تأیید migration انجام شود.

## 11) ترتیب اجرای اجباری برای Gemini

کار را در یک PR یا commitهای کوچک و قابل بازبینی انجام بده؛ از کاربر سؤال‌های تکراری نپرس. اگر چیزی از کد واقعی قابل کشف است، خودت repository را بررسی کن.

**Phase 0 — Audit (اول انجام بده):**
- تمام Entity/DAO/Repository/Room migrations، backup/import/export، login/settings/navigation، Gradle dependencies و Server schema/router/env را بررسی کن.
- ماتریس مالکیت جداول و فهرست queryهای فاقد user scope تهیه کن.
- اختلاف کد واقعی با این سند را گزارش کن؛ حدس نزن.

**Phase 1 — Backend foundation + Auth:**
- Router/Request/Response، migrationهای جدید، access/refresh token، login/refresh/logout/me، admin user CRUD، bootstrap امن اولین مدیر، تست‌ها.
- ابتدا روی محیط توسعه/staging؛ سپس deploy.
- در پایان endpointهای واقعی را با curl و تست DB تأیید کن.

**Phase 2 — Android account foundation:**
- secure token storage، API client، auth repository/use cases، login/logout UI و state handling.
- هیچ صفحه یا داده قدیمی را حذف نکن.
- migration Room و user-scoping را همراه تست اضافه کن.
- logout و account switching را تست کن.

**Phase 3 — Global vocabulary:**
- انتشار مدیریتی revision، manifest، incremental changes، tombstones، snapshot recovery.
- Android pull-only و اعمال transactional.
- Seed فعلی و remote seed update تا وقتی مسیر جدید کامل و تست نشده، خراب یا حذف نشود.

**Phase 4 — Personal sync:**
- outbox/inbox، cursor، operationId، idempotency، conflict handling، WorkManager retry.
- ابتدا فقط انواع داده‌ای را sync کن که مالکیت و مدلشان مشخص است.

**Phase 5 — Hardening:**
- تست امنیتی IDOR، token lifecycle، rate limit، backup restore، multi-device و offline.
- مستندات API، ERD، schema، deployment و عملیات admin را نهایی کن.

در پایان، یک گزارش کوتاه شامل فایل‌های تغییرکرده، migrationها، تست‌ها، موارد باقی‌مانده و لینک CI بده. هیچ ادعای تست یا deploy موفق بدون اجرای واقعی نکن.

## 12) نکات عملیاتی محیط فعلی

- دامنه: `yadin.manico.es`
- مسیر پروژه روی دامنه: `/yadin/`
- API base: `https://yadin.manico.es/yadin/api/v1`
- PHP: 8.3
- MariaDB: 10.6
- cPanel shared hosting؛ SSH در دسترس نیست؛ cron موجود است.
- FTPS Explicit روی port 21.
- FTP host دارای certificate معتبر: `autumn.ourhosted.com`
- FTP account به پوشه پروژه محدود شده؛ در GitHub Secret مقدار `FTP_REMOTE_DIR=/` است. این مقدار را به مسیر مطلق سرور تبدیل نکن.
- DB name/user: `manicoir_yadin`; DB host باید از ENV موجود خوانده شود.
- IP هاست: `85.9.111.123`.
- هیچ password/token/secret واقعی را در کد یا مستندات ننویس و از صاحب پروژه درخواست ارسال آن‌ها نکن.
- deploy workflow موجود را حفظ کن و آن را برای migrations به‌صورت خودکار و بدون backup اجرا نکن.

---
**دستور نهایی:** این سند را همراه سورس فعلی هر دو repository بخوان. مشخصات محصول فعلی را حفظ کن و فقط قابلیت multi-user/backend را مطابق این قرارداد اضافه کن. اول Audit واقعی، بعد پیاده‌سازی مرحله‌ای. بازنویسی کل برنامه یا تغییر الگوریتم‌های یادگیری خارج از محدوده است.

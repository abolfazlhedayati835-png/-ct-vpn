# Ninja Config — راهنمای راه‌اندازی

## 🔵 ساخت APK فقط با گوشی (بدون کامپیوتر) — Termux + GitHub Actions

اگه کامپیوتر ندارید، این روش رو دنبال کنید. کل کار روی خود گوشی
اندرویدتون انجام میشه:

### قدم ۱: نصب Termux
از [F-Droid](https://f-droid.org/packages/com.termux/) اپ **Termux**
رو نصب کنید (نسخه‌ی Google Play قدیمیه، از F-Droid نصب کنید).

### قدم ۲: آماده‌سازی Termux
داخل Termux این دستورات رو یکی‌یکی بزنید:
```
pkg update -y
pkg install git zip unzip -y
termux-setup-storage
```
(وقتی اجازه‌ی دسترسی به فایل‌ها رو خواست، Allow بزنید)

### قدم ۳: انتقال فایل پروژه
فایل `NinjaConfigApp.zip` که از این چت گرفتید رو (معمولاً توی پوشه‌ی
Download گوشیه) داخل Termux کپی و باز کنید:
```
cp /sdcard/Download/NinjaConfigApp.zip ~/
cd ~
unzip NinjaConfigApp.zip
cd NinjaConfigApp
```

### قدم ۴: ساخت ریپازیتوری روی گیت‌هاب
1. اگه اکانت گیت‌هاب ندارید، از مرورگر گوشی به [github.com](https://github.com/join) برید و بسازید.
2. روی گیت‌هاب یک ریپازیتوری جدید و **خالی** بسازید (بدون README)، مثلاً به نام `ninja-config-app`.
3. یک Personal Access Token بسازید: Settings → Developer settings →
   Personal access tokens → Generate new token (دسترسی `repo` رو تیک بزنید) و کدش رو یه جا کپی کنید (فقط یه بار نشون داده میشه).

### قدم ۵: پوش کردن پروژه از Termux
```
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/USERNAME/ninja-config-app.git
git push -u origin main
```
موقع push، بجای پسورد، همون **Personal Access Token** که ساختید رو
وارد کنید.

### قدم ۶: دیدن ساخته‌شدن خودکار APK
1. توی مرورگر گوشی، به ریپازیتوری‌تون روی گیت‌هاب برید → تب **Actions**.
2. یه ورک‌فلو به اسم "Build APK" می‌بینید که به‌صورت خودکار شروع به کار
   کرده (چند دقیقه طول می‌کشه).
3. وقتی تیک سبز خورد، روش کلیک کنید و پایین صفحه بخش **Artifacts**
   رو ببینید — یه فایل به اسم `ninja-config-debug-apk` هست، دانلودش
   کنید (یه zip کوچیکه که خودِ APK داخلشه).
4. اون zip رو با یه فایل‌منیجر باز/اکسترکت کنید و روی فایل `.apk`
   بزنید تا نصب بشه (ممکنه لازم باشه "نصب از منابع ناشناس" رو توی
   تنظیمات گوشی فعال کنید).

⚠️ توجه: این APK فعلاً با یه Firebase غیرواقعی (placeholder) ساخته
میشه تا اولین build خطا نده. برای اینکه کانفیگ‌ها واقعاً ذخیره و
نمایش داده بشن، باید طبق بخش «وصل کردن به Firebase» زیر، فایل واقعی
`google-services.json` رو جایگزین کنید و دوباره push کنید (Actions
خودش دوباره APK جدید می‌سازه).

---


پروژه‌ی اندروید (Kotlin + Jetpack Compose) که یک لیست کانفیگ VPN/پروکسی
(V2Ray, Shadowsocks, ...) رو به کاربر نشون میده. شما به‌عنوان ادمین از داخل
خود اپ (آیکون چرخ‌دنده بالای صفحه) کانفیگ‌ها رو اضافه/ویرایش/حذف می‌کنید
و همه چیز روی Firebase Firestore ذخیره و به‌صورت زنده (real-time) روی
گوشی همه‌ی کاربرا نمایش داده میشه.

## ۱) باز کردن پروژه
1. Android Studio (نسخه‌ی جدید، Hedgehog به بعد) رو باز کنید.
2. Open → پوشه‌ی `NinjaConfigApp` رو انتخاب کنید.
3. صبر کنید Gradle Sync تموم بشه (به اینترنت نیاز داره تا وابستگی‌ها رو دانلود کنه).

## ۲) وصل کردن به Firebase (ضروری)
1. به [console.firebase.google.com](https://console.firebase.google.com) برید و یک پروژه‌ی جدید بسازید.
2. یک اپ Android به پروژه اضافه کنید با همین Package name:
   `com.ninjaconfig.app`
3. فایل `google-services.json` که دانلود می‌کنید رو داخل پوشه‌ی `app/`
   قرار بدید (کنار `build.gradle.kts`).
4. توی کنسول Firebase، از منوی سمت چپ برید به **Firestore Database** و
   یک دیتابیس جدید بسازید (Start in production mode کافیه).

### تنظیم قوانین امنیتی Firestore (خیلی مهم)
چون اپ بدون لاگین کار می‌کنه، باید با Security Rules جلوی نوشتن توسط
کاربرای عادی رو بگیرید (وگرنه هرکسی که اپ رو دیکامپایل کنه می‌تونه
کانفیگ‌های شما رو پاک/دستکاری کنه). یه نمونه‌ی ساده:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /configs/{configId} {
      allow read: if true;   // همه می‌تونن بخونن
      allow write: if false; // فقط از کنسول Firebase یا یه بک‌اند امن بنویسید
    }
  }
}
```

با این تنظیمات، دکمه‌های ادمین داخل اپ دیگه کار نمی‌کنن (چون نوشتن از
کلاینت بسته‌س). دو راه دارید:
- **ساده:** فعلاً `allow write: if true;` بذارید تا با پین ادمین داخل
  اپ تست کنید، ولی قبل از انتشار عمومی حتماً محدودش کنید.
- **درست و امن:** یه Cloud Function یا بک‌اند کوچیک بسازید که با یک
  کلید مخفی، نوشتن رو انجام بده (نه مستقیم از اپ کاربر).

### تغییر پین ادمین
پیش‌فرض پین ورود به بخش ادمین `1234` هست. حتماً قبلِ استفاده‌ی واقعی
عوضش کنید — توی فایل:
`app/src/main/java/com/ninjaconfig/app/ui/screens/AdminScreen.kt`
مقدار `ADMIN_PIN` رو تغییر بدید.

## ۳) درباره‌ی «اتصال واقعی VPN»
این پروژه رابط کاربری، مدیریت کانفیگ‌ها، و نمایش QR/کپی لینک رو کامل
پیاده‌سازی کرده. اما **تانل کردن واقعی ترافیک** (یعنی وقتی کاربر
کانکت میزنه، ترافیک گوشیش واقعاً از پروکسی رد بشه) نیاز به یک هسته‌ی
native داره (Xray-core / V2Ray core کامپایل‌شده برای اندروید) که یک
پروژه‌ی جدا و بزرگه. دو گزینه دارید:

1. **ساده‌ترین راه (پیاده‌سازی فعلی):** دکمه‌ی «اتصال با اپ VPN» لینک
   کانفیگ رو کپی می‌کنه و سعی می‌کنه اپی مثل v2rayNG که از قبل نصبه رو
   باز کنه تا کاربر Import کنه.
2. **راه کامل:** کتابخونه‌ی متن‌باز هسته‌ی V2Ray/Xray برای اندروید
   (مثلاً پروژه‌ی [AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite)
   که پایه‌ی همون v2rayNG هست) رو به‌صورت AAR به `app/build.gradle.kts`
   اضافه کنید و یک `VpnService` بسازید که این کور رو اجرا کنه. این کار
   قابل انجامه ولی حجمش زیاده — اگه خواستید توی همین چت قدم‌به‌قدم
   جلو می‌ریم و اون بخش رو هم اضافه می‌کنیم.

## ۴) ساختار پروژه
```
app/src/main/java/com/ninjaconfig/app/
  MainActivity.kt              نقطه‌ی ورود، مسیریابی بین صفحات
  data/
    ConfigModels.kt            مدل VpnConfig و گروه‌بندی کشورها
    ConfigRepository.kt        خواندن/نوشتن Firestore
    ConfigViewModel.kt         اتصال ریپازیتوری به UI
  ui/screens/
    HomeScreen.kt               صفحه‌ی اصلی، لیست کشورها
    AdminScreen.kt               صفحه‌ی مدیریت (پین + فرم افزودن/ویرایش)
    ConfigDetailSheet.kt         شیت پایین صفحه: QR، کپی، اتصال
  ui/theme/                     رنگ‌بندی و تایپوگرافی تیره
```

## ۵) اجرا
دکمه‌ی Run توی Android Studio رو بزنید (روی امولاتور یا گوشی واقعی).
اولین بار که کانفیگی اضافه نشده، صفحه خالی نشون داده میشه — از آیکون
چرخ‌دنده بالا وارد بخش ادمین بشید و اولین کانفیگ رو اضافه کنید.

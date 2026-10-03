# دفتري (Daftari)

تطبيق أندرويد أصلي لتنظيم الحياة الدراسية للطالب — مبني بـ Kotlin + Jetpack Compose + Room + WorkManager + DataStore.

## طريقة الحصول على ملف APK بدون تثبيت أي برنامج

1. أنشئ مستودع (Repository) جديد على GitHub (خاص أو عام).
2. ارفع كل ملفات هذا المشروع كما هي إلى المستودع (تأكد إن مجلد `.github/workflows` انرفع أيضًا).
3. تأكد أن اسم الفرع الرئيسي هو `main` (الملف يعتمد على ذلك — لو اسم الفرع عندك `master` غيّر السطر في `.github/workflows/build-apk.yml`).
4. بعد الرفع، افتح تبويب **Actions** في المستودع — بيبدأ البناء تلقائيًا (يأخذ 3-6 دقائق تقريبًا).
5. لما تخلص العملية (علامة ✅ خضراء)، افتح النتيجة واضغط على **daftari-debug-apk** تحت قسم Artifacts — بينزل لك ملف مضغوط فيه `app-debug.apk`.
6. انقل الملف لجوالك (عن طريق الجوجل درايف أو الواتساب لنفسك) وثبّته (لازم تفعّل "السماح بالتثبيت من مصادر غير معروفة" أول مرة).

## ملاحظات مهمة
- هذه نسخة **Debug** (غير موقّعة رسميًا) — كافية للتجربة والاستخدام الشخصي، لكن لو حبيت تنشرها على متجر Google Play لاحقًا نحتاج نسوي توقيع (Release signing) بخطوة إضافية.
- كل البيانات تُحفظ محليًا على الجهاز فقط، ولا حاجة لإنترنت في الاستخدام اليومي.
- لو حبيت تشغّل البناء يدويًا بدون رفع كود جديد، تقدر تدخل تبويب Actions وتضغط "Run workflow" (بسبب `workflow_dispatch`).

## هيكلة المشروع
- `app/src/main/java/com/daftari/app/data` — قاعدة بيانات Room (الكيانات وDAOs)
- `app/src/main/java/com/daftari/app/repository` — طبقة الوصول الموحدة للبيانات
- `app/src/main/java/com/daftari/app/viewmodel` — منطق العمل (ViewModel)
- `app/src/main/java/com/daftari/app/ui` — الشاشات (Jetpack Compose)
- `app/src/main/java/com/daftari/app/notification` — التذكيرات (WorkManager)
- `app/src/main/java/com/daftari/app/settings` — الإعدادات (DataStore)
- `app/src/main/java/com/daftari/app/backup` — تصدير واستيراد النسخة الاحتياطية

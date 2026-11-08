# חתימה על BOXLY (גרסת Release ל-Google Play)

המפתח והסיסמאות נשארים אצלך. **לעולם אל תעלה אותם ל-git ואל תשלח אותם בצ'אט.**

## 1. יצירת מפתח (פעם אחת)
```bash
keytool -genkeypair -v -keystore boxly-upload.jks -alias boxly -keyalg RSA -keysize 2048 -validity 10000
```
הפקודה תשאל סיסמה ופרטים. **שמור גיבוי של הקובץ והסיסמה** (למשל במנהל סיסמאות).

## 2. הגדרה מקומית
צור בתיקיית הפרויקט קובץ `keystore.properties` (כבר ב-.gitignore):
```
storeFile=boxly-upload.jks
storePassword=הסיסמה-שלך
keyAlias=boxly
keyPassword=הסיסמה-שלך
```

## 3. בנייה
ב-Android Studio: **Build → Generate Signed App Bundle / APK → Android App Bundle**, או משורת הפקודה:
```bash
gradle :app:bundleRelease
```
הקובץ: `app/build/outputs/bundle/release/app-release.aab` — את זה מעלים ל-Play Console.

## Play App Signing
בהעלאה הראשונה מומלץ להפעיל **Play App Signing**: Google מחזיקה את מפתח החתימה הסופי, והמפתח שיצרת הוא מפתח העלאה בלבד, ואפשר לאפס אותו אם יאבד.

# BOXLY 📦

אפליקציית Android לניהול ארגזים במעבר דירה.

## Free
- עד 20 ארגזים
- חיפוש חפצים
- שמות חדרים ומיקומים

## Pro — ₪20
- ארגזים ללא הגבלה
- מיועד להוסיף מפה ומיקום GPS
- QR לכל ארגז

## פתיחה
1. פתח את התיקייה ב-Android Studio.
2. המתן ל-Gradle Sync.
3. הפעל על מכשיר Android.

## לפני פרסום
- הוסף Google Maps API Key אמיתי ב-`AndroidManifest.xml`.
- החלף את `upgradeToProForTesting()` ב-Google Play Billing מלא.
- הוסף שמירה קבועה (Room/DataStore או שרת).
- הוסף מצלמת QR וסורק.

זהו MVP התחלתי. רכישות אמיתיות חייבות לעבור אימות של Google Play Billing ולא רק כפתור מקומי.

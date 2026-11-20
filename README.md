# יאפו YAPO

אתר/אפליקציית PWA בפייטון (Flask), מותאם לטלפון – ניתן להתקין למסך הבית.

## הרצה
```bash
pip install -r requirements.txt
python app.py            # http://localhost:8000
# פרודקשן:
gunicorn -b 0.0.0.0:8000 app:app
```

## התקנה בטלפון
פתחו את הכתובת בטלפון (HTTPS נדרש מחוץ ל-localhost) ← "הוסף למסך הבית".

## רשימת ההמתנה
נשמרת ב-SQLite (`yapo.db`). לצפייה:
```bash
sqlite3 yapo.db "select email, role, created from waitlist"
```

"""YAPO – Flask app (mobile-first PWA landing + waitlist API)."""
import os
import re
import sqlite3
from pathlib import Path

from flask import Flask, jsonify, render_template, request, send_from_directory

BASE = Path(__file__).parent
DB = os.environ.get("YAPO_DB", str(BASE / "yapo.db"))
EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
ROLES = {"עסק", "יוצר/ת תוכן", "צופה"}

app = Flask(__name__)


def db():
    con = sqlite3.connect(DB)
    con.execute(
        "CREATE TABLE IF NOT EXISTS waitlist ("
        "id INTEGER PRIMARY KEY, email TEXT UNIQUE NOT NULL,"
        "role TEXT NOT NULL, created TEXT DEFAULT CURRENT_TIMESTAMP)"
    )
    return con


@app.get("/")
def index():
    return render_template("index.html")


@app.post("/api/join")
def join():
    data = request.get_json(silent=True) or {}
    email = str(data.get("email", "")).strip().lower()
    role = str(data.get("role", ""))
    if len(email) > 254 or not EMAIL_RE.match(email):
        return jsonify(error="כתובת אימייל לא תקינה"), 400
    if role not in ROLES:
        role = "צופה"
    with db() as con:
        con.execute("INSERT OR IGNORE INTO waitlist(email, role) VALUES (?, ?)", (email, role))
    return jsonify(ok=True)


@app.get("/manifest.webmanifest")
def manifest():
    return jsonify(
        name="יאפו YAPO",
        short_name="יאפו",
        lang="he",
        dir="rtl",
        start_url="/",
        scope="/",
        display="standalone",
        background_color="#0b0b12",
        theme_color="#7c4dff",
        icons=[
            {"src": "/static/icon-192.png", "sizes": "192x192", "type": "image/png", "purpose": "any maskable"},
            {"src": "/static/icon-512.png", "sizes": "512x512", "type": "image/png", "purpose": "any maskable"},
        ],
    ), 200, {"Content-Type": "application/manifest+json"}


@app.get("/sw.js")
def sw():
    resp = send_from_directory(BASE / "static", "sw.js", mimetype="text/javascript")
    resp.headers["Service-Worker-Allowed"] = "/"
    resp.headers["Cache-Control"] = "no-cache"
    return resp


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 8000)))

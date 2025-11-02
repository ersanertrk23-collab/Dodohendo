import os
from typing import List, Dict

OPENAI_KEY = os.getenv('OPENAI_API_KEY')

# OpenAI istemcisini yalnızca anahtar varsa yükle (hata korumalı)
openai = None
if OPENAI_KEY:
    try:
        import openai as _openai
        _openai.api_key = OPENAI_KEY
        openai = _openai
    except Exception:
        openai = None

class AIClient:
    def __init__(self, model: str = "gpt-3.5-turbo"):
        self.model = model
        self.use_openai = bool(OPENAI_KEY and openai)

    def send_message(self, message: str, history: List[Dict] = None) -> str:
        history = history or []
        if self.use_openai:
            system_prompt = {"role": "system", "content": "Türkçe yanıt ver. Kısa ve nazik ol."}
            messages = [system_prompt]
            for turn in history:
                if 'user' in turn:
                    messages.append({"role": "user", "content": turn['user']})
                if 'assistant' in turn:
                    messages.append({"role": "assistant", "content": turn['assistant']})
            messages.append({"role": "user", "content": message})
            try:
                resp = openai.ChatCompletion.create(model=self.model, messages=messages, max_tokens=400)
                return resp.choices[0].message.content.strip()
            except Exception as e:
                return f"OpenAI hatası: {e}"
        else:
            # Basit fallback: kural tabanlı + echo
            low = message.strip().lower()
            if any(g in low for g in ("merhaba","selam","selamlar","hi")):
                return "Merhaba! Size nasıl yardım edebilirim?"
            if "nasıl" in low and "yap" in low:
                return "Bunu adım adım açıklamamı ister misiniz?"
            return f"Echo: {message}"

app.py:
import os
import argparse
from dotenv import load_dotenv
from flask import Flask, request, jsonify
from ai import AIClient

load_dotenv()

ai = AIClient()

app = Flask(__name__)

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json() or {}
    message = data.get('message')
    if not message:
        return jsonify({'error': 'message alanı gereklidir.'}), 400
    history = data.get('history', [])
    try:
        reply = ai.send_message(message, history=history)
        return jsonify({'reply': reply})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

def cli_loop():
    print("Dodohendo CLI - çıkmak için CTRL+C")
    history = []
    while True:
        try:
            msg = input("Siz: ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nÇıkılıyor.")
            break
        if not msg:
            continue
        reply = ai.send_message(msg, history=history)
        print("AI:", reply)
        history.append({'user': msg, 'assistant': reply})

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--cli', action='store_true', help='CLI modunda çalıştır')
    args = parser.parse_args()
    if args.cli:
        cli_loop()
    else:
        port = int(os.getenv('PORT', 5000))
        app.run(host='0.0.0.0', port=port)

Dockerfile:
# Basit Dockerfile
FROM python:3.10-slim
WORKDIR /app

# Sisteme bağımlılık gerekirse ekle
RUN apt-get update && apt-get install -y --no-install-recommends build-essential && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000
ENV FLASK_APP=app.py
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "app:app", "--workers", "1"]

.env.example:
# Açıkça paylaşmayın. Geliştirme için .env kullanın.
OPENAI_API_KEY=your_openai_api_key_here

.gitignore:
__pycache__/
*.pyc
.env
.env.local
venv/
.DS_Store

import os
import argparse
from dotenv import load_dotenv
from flask import Flask, request, jsonify, send_from_directory
from ai import AIClient

load_dotenv()

# Server-side API token (if set, /chat requires Authorization: Bearer <token>)
API_TOKEN = os.getenv('API_TOKEN')

ai = AIClient()

app = Flask(__name__, static_folder='static', static_url_path='/static')

@app.route('/')
def index():
    return send_from_directory('static', 'index.html')

@app.route('/chat', methods=['POST'])
def chat():
    # If API_TOKEN is set in environment, require Authorization header
    if API_TOKEN:
        auth = request.headers.get('Authorization', '')
        if not auth.startswith('Bearer ') or auth.split('Bearer ')[1].strip() != API_TOKEN:
            return jsonify({'error': 'Yetkisiz (invalid token)'}), 403

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
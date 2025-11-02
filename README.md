# Dodohendo - Basit Yapay Zeka Asistanı (Starter)

Bu repo, Türkçe başlangıç seviyesi bir yapay zeka (AI) programı içerir. Proje hem komut satırı etkileşimi hem de basit bir REST API (Flask) sağlar. Varsayılanta OpenAI API'si kullanılır; API anahtarınız yoksa basit bir echo/moderatör modu çalışır.

Özellikler
- Komut satırı üzerinden sohbet
- Flask ile /chat endpoint'i (JSON POST)
- OpenAI'ye bağlanma (opsiyonel)
- Dockerfile ile konteynerleme

Hızlı başlangıç
1. Python 3.10+ yükleyin.
2. Ortam değişkenleri: `OPENAI_API_KEY` (isteğe bağlı). Bir .env dosyası kullanın.
3. Bağımlılıkları yükleyin:

   pip install -r requirements.txt

4a. Komut satırı modu:

   python app.py --cli

4b. REST API modu:

   export FLASK_APP=app.py
   flask run --host=0.0.0.0 --port=5000

   POST /chat JSON örneği:
   {
     "message": "Merhaba"
   }

Geliştirme ve katkı
- Bu başlangıç kodu özelleştirilebilir: farklı model bağlayıcıları, daha iyi hata yönetimi ve web arayüzü eklenebilir.

Lisans
MIT

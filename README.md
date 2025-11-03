# Dodohendo (örnek)

Bu proje, Android cihazlarda çalışacak basit bir sesle komut uygulaması iskeletidir.
Özellikler:
- Konuşmayı dinleme (SpeechRecognizer)
- Konuşma ile yanıt (TextToSpeech)
- Basit komutlar: arama, uygulama açma, SMS gönderme, harita sorgusu

Kurulum:
1. Android Studio'da yeni bir proje yaratıp bu dosyaları uygun yerlere koyun.
2. build.gradle dosyasını proje seviyesine göre uyarlayın.
3. Cihazda RECORD_AUDIO, CALL_PHONE, SEND_SMS izinlerini verin.
4. Uygulamayı gerçek cihazda test edin (emulatorda mikrofon/arama/sms sınırlamaları olabilir).

Gizlilik:
- Uygulama mikrofonu kullanır ve konuşma verisini yerel olarak işler. Gerçek bir uygulamada konuşma verilerini kaydetmemeli veya harici sunuculara göndermeden önce kullanıcıdan izin alıp bilgilendirme yapılmalıdır.
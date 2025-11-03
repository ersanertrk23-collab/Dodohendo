package com.ersanertrk23collab.dodohendo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // handle if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // request permissions
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.RECORD_AUDIO)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.CALL_PHONE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.SEND_SMS)

        if (needed.isNotEmpty()) requestPerms.launch(needed.toTypedArray())

        tts = TextToSpeech(this, this)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                VoiceUI()
            }
        }
    }

    @Composable
    fun VoiceUI() {
        var listening by remember { mutableStateOf(false) }
        var lastText by remember { mutableStateOf("Henüz komut yok") }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Dodohendo - Sesli Komut Uygulaması")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Son komut: $lastText")
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Button(onClick = {
                    startListening { text ->
                        lastText = text
                        handleCommand(text)
                    }
                    listening = true
                }) {
                    Text("Dinlemeye Başla")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    stopListening()
                    listening = false
                }) {
                    Text("Durdur")
                }
            }
        }
    }

    private fun startListening(onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("tr", "TR"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Komut söyleyin...")
        }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                speakText("Ses anlaşılamadı, lütfen tekrar edin.")
            }

            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = list?.firstOrNull() ?: ""
                onResult(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(listener)
        try {
            startActivityForResult(intent, 1234)
        } catch (e: ActivityNotFoundException) {
            // Fallback: use SpeechRecognizer API directly
            speechRecognizer?.startListening(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotEmpty()) {
                handleCommand(text)
            }
        }
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
    }

    private fun handleCommand(text: String) {
        // Basit komut ayıklama (Türkçe'ye göre örnek)
        val lower = text.lowercase(Locale.getDefault())
        when {
            lower.contains("ara") && lower.matches(Regex(".*\d.*")) -> {
                // eğer numara içeriyorsa doğrudan ara
                val tel = Regex("\\+?\\d[\\d\\s-]{6,}\\d").find(lower)?.value ?: ""
                if (tel.isNotBlank()) {
                    callNumber(tel)
                    speakText("$tel aranıyor")
                } else {
                    speakText("Kimi aramak istersiniz?")
                }
            }
            lower.contains("ara") && lower.contains("araşıyor") -> {
                speakText("Anlamadım, lütfen tekrar edin.")
            }
            lower.contains("araş") || lower.contains("arama") -> {
                // örnek: "ahmet'i ara" -> burada isim var, gösterme için dialer aç
                val name = lower.replace("beni", "").replace("ara", "").trim()
                speakText("$name için arama ekranını açıyorum")
                openDialer("") // açar, numara boş
            }
            lower.contains("uygulama aç") || lower.contains("uygulamayı aç") -> {
                // örnek: "WhatsApp uygulamasını aç"
                val pkg = findPackageForCommonApp(lower)
                if (pkg != null) {
                    openApp(pkg)
                    speakText("Uygulama açılıyor")
                } else {
                    speakText("Hangi uygulamayı açmak istiyorsunuz? Paket adını söyler misiniz?")
                }
            }
            lower.contains("mesaj") || lower.contains("sms") -> {
                // basit: "mesaj at 0505... içeriği ..."
                val tel = Regex("\\+?\\d[\\d\\s-]{6,}\\d").find(lower)?.value ?: ""
                val body = lower.replace(Regex("\\+?\\d[\\d\\s-]{6,}\\d"), "").replace("mesaj", "").replace("at", "").trim()
                if (tel.isNotBlank()) {
                    sendSms(tel, body.ifEmpty { "Merhaba" })
                    speakText("Mesaj gönderiliyor")
                } else {
                    speakText("Hangi numaraya mesaj atacağımı söyler misiniz?")
                }
            }
            lower.contains("nerede") || lower.contains("harita") || lower.contains("yol") -> {
                // haritada arama: "Eiffel Kulesi nerede"
                val query = lower.replace("nerede", "").replace("haritada", "").trim()
                openMaps(query.ifEmpty { "yakınım" })
                speakText("$query için harita açılıyor")
            }
            lower.contains("merhaba") || lower.contains("selam") -> {
                speakText("Merhaba! Size nasıl yardımcı olabilirim?")
            }
            else -> {
                speakText("Komut anlaşılamadı: $text")
            }
        }
    }

    private fun callNumber(tel: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${tel.replace("\\s".toRegex(), "")}"))
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            // izin yoksa dialer aç
            openDialer(tel)
        }
    }

    private fun openDialer(number: String) {
        val uri = if (number.isBlank()) Uri.parse("tel:") else Uri.parse("tel:${number.replace("\\s".toRegex(), "")}")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)
    }

    private fun sendSms(number: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${number.replace("\\s".toRegex(), "")}")
            putExtra("sms_body", body)
        }
        startActivity(intent)
    }

    private fun openMaps(query: String) {
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun openApp(packageName: String) {
        val pm = packageManager
        val launch = pm.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            startActivity(launch)
        } else {
            // Play Store aç
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        }
    }

    private fun findPackageForCommonApp(text: String): String? {
        // Basit eşleme örneği; isterseniz uzatırım
        return when {
            text.contains("whatsapp") -> "com.whatsapp"
            text.contains("telegram") -> "org.telegram.messenger"
            text.contains("instagram") -> "com.instagram.android"
            text.contains("facebook") -> "com.facebook.katana"
            text.contains("youtube") -> "com.google.android.youtube"
            else -> null
        }
    }

    private fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("tr", "TR")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
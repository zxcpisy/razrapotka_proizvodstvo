package com.example.golosspomoch;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.*;
import android.speech.*;
import android.view.Gravity;
import android.view.WindowManager;
import android.speech.tts.TextToSpeech;
import androidx.core.app.NotificationCompat;
import kotlinx.coroutines.*;
import java.util.*;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VoiceService : Service(), RecognitionListener {

    private lateinit var tts: TextToSpeech
    private lateinit var overlay: OverlayView
    private lateinit var wm: WindowManager
    private lateinit var recognizer: SpeechRecognizer

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())

        tts = TextToSpeech(this) {}

        setupOverlay()
        setupSpeech()
    }

    private fun setupOverlay() {
        overlay = OverlayView(this)

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END

                wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(overlay, params)
    }

    private fun setupSpeech() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(this)
        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        recognizer.startListening(intent)
    }

    override fun onResults(results: Bundle?) {
        val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.get(0) ?: return

                overlay.update("Ты: $text")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = CommandProcessor.process(text, this@VoiceService)

                overlay.update(response)
                speak(response)

            } catch (e: Exception) {
                overlay.update("Ошибка: ${e.message}")
            }

            delay(500)
            startListening()
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onError(error: Int) {
        startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.destroy()
        wm.removeView(overlay)
        tts.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun createNotification(): Notification {
        val channelId = "voice_channel"

        val channel = NotificationChannel(
                channelId,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
                .setContentTitle("Ассистент работает")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build()
    }
}

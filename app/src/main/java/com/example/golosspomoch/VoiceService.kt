package com.example.golosspomoch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceService : Service(), RecognitionListener {

    private lateinit var tts: TextToSpeech
    private lateinit var overlay: OverlayView
    private lateinit var wm: WindowManager
    private lateinit var recognizer: SpeechRecognizer

    @RequiresApi(Build.VERSION_CODES.O)
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
            val response = CommandProcessor.process(text, this@VoiceService)

            overlay.update(response)
            tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)

            delay(500)
            startListening()
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val id = "voice"

        val channel = NotificationChannel(
            id, "Assistant", NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return NotificationCompat.Builder(this, id)
            .setContentTitle("Ассистент работает")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }
}
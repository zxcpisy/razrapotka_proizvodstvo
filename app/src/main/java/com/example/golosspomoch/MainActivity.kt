package com.example.golosspomoch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Тестовая функция ассистента
        testAssistant()
    }

    private fun testAssistant() {
        // Корутин scope на Main для UI
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Всё сетевое делаем в Dispatchers.IO
                val response = withContext(Dispatchers.IO) {
                    QwenClient.ask("Привет ассистент")
                }
                Log.d("AssistantTest", "Ответ ассистента: $response")
            } catch (e: Exception) {
                Log.e("AssistantTest", "Ошибка запроса к Qwen", e)
            }
        }
    }
}



package com.example.golosspomoch


import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object QwenClient {

    private const val API_KEY = "sk-df43a1078dc247be9ac0acbfe9d875e1"

    // URL для Qwen модели через интерфейс совместимости
    // Обрати внимание: именно этот endpoint подходит для чат‑completion
    private const val QWEN_URL =
        "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions"

    private val client = OkHttpClient()

    suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {

        // Собираем JSON запрос
        val jsonReq = """
        {
          "model": "qwen‑1.2‑chat",
          "messages": [
            {"role":"user","content":"$prompt"}
          ]
        }
        """.trimIndent()

        val body = jsonReq.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(QWEN_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext "Ошибка: пустой ответ"

        // Разбираем JSON
        val gson = Gson()
        val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)

        // Получаем текст ответа
        val choices = jsonObj.getAsJsonArray("choices") ?: return@withContext "Ошибка разбора"
        val first = choices.get(0).asJsonObject
        val message = first.getAsJsonObject("message")
        val content = message.get("content").asString

        return@withContext content
    }
}
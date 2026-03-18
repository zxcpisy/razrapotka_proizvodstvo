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

    private const val API_KEY = "sk-56cdb9afe3cb49a08c2088d3f07d44af"
    private const val QWEN_URL = "https://cn-hongkong.dashscope.aliyuncs.com/compatible-mode/v1"

    private val client = OkHttpClient()

    suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val jsonReq = """
                {
                  "model": "qwen-plus",
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

            // Логируем ответ для отладки
            println("Qwen response: $responseBody")

            val jsonObj = Gson().fromJson(responseBody, JsonObject::class.java)
                ?: return@withContext "Ошибка разбора: пустой JSON"

            val choices = jsonObj.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val first = choices[0].asJsonObject
                val messageElement = first.get("message")
                return@withContext when {
                    messageElement?.isJsonObject == true -> messageElement.asJsonObject.get("content")?.asString
                    messageElement?.isJsonPrimitive == true -> messageElement.asString
                    else -> "Ошибка разбора: message пустой"
                } as String
            }

            val result = jsonObj.get("result")?.asString
            result ?: "Ошибка разбора: неизвестный формат JSON"

        } catch (e: Exception) {
            "Ошибка запроса к Qwen: ${e.localizedMessage}"
        }
    }
}

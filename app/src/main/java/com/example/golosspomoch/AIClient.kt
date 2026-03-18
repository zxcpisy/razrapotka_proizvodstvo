package com.example.golosspomoch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.jvm.java
import com.google.gson.Gson
import com.google.gson.JsonObject

object AIClient {

    private const val API_KEY = "YOUR_API_KEY"

    private val client = OkHttpClient()

    suspend fun ask(text: String): String = withContext(Dispatchers.IO) {

        val json = """
        {
          "model": "gpt-4o-mini",
          "messages": [{"role":"user","content":"$text"}]
        }
        """

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext "Ошибка"

        val jsonObj = Gson().fromJson(body, JsonObject::class.java)

        return@withContext jsonObj["choices"]
            .asJsonArray[0]
            .asJsonObject["message"]
            .asJsonObject["content"]
            .asString
    }
}
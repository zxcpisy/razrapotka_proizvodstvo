package com.example.golosspomoch

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommandProcessor {

    suspend fun process(text: String, context: Context): String {

        val lower = text.lowercase()

        if (lower.contains("ютуб")) {
            val intent = context.packageManager
                .getLaunchIntentForPackage("com.google.android.youtube")

            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            return "Открываю YouTube"
        }

        if (lower.contains("время")) {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }

        return AIClient.ask(text)
    }
}
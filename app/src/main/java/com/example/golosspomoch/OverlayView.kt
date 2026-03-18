package com.example.golosspomoch

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.TextView

class OverlayView(context: Context) : FrameLayout(context) {

    private val textView = TextView(context)

    init {
        textView.text = "🎤"
        textView.setTextColor(Color.WHITE)
        textView.textSize = 16f

        setBackgroundColor(Color.BLACK)
        setPadding(40, 40, 40, 40)

        addView(textView)
    }

    fun update(text: String) {
        post { textView.text = text }
    }
}
package com.tunifyx.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val log = intent.getStringExtra("crash_log") ?: "No crash info"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }

        layout.addView(TextView(this).apply {
            text = "❌ TunifyX Crash"
            textSize = 20f
            setTextColor(0xFFFF5555.toInt())
            setPadding(0, 0, 0, 24)
        })

        layout.addView(ScrollView(this).apply {
            addView(TextView(this@CrashActivity).apply {
                text = log
                textSize = 11f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF1A1A1A.toInt())
                setPadding(16, 16, 16, 16)
                setTextIsSelectable(true)
            })
        })

        setContentView(layout)
        window.decorView.setBackgroundColor(0xFF121212.toInt())
    }
}

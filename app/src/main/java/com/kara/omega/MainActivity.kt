package com.kara.omega

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(9, 12, 15))
        }

        val title = TextView(this).apply {
            text = "KARA OMEGA"
            textSize = 30f
            setTextColor(Color.rgb(0, 230, 130))
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 30, 8, 30)
        }

        root.addView(title)
        root.addView(status)

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }

            val python = Python.getInstance()
            val module = python.getModule("main")
            val dashboard = module.callAttr("get_dashboard")

            status.text = """
                ANDROID ENGINE: READY

                PYTHON ENGINE: CONNECTED

                VERSION: 1.0

                M3U ANALYZER: READY

                TOTAL: 0

                SUCCESS: 0

                FAILED: 0

                SYSTEM STATUS: ONLINE
            """.trimIndent()

        } catch (e: Exception) {
            status.text = "PYTHON ENGINE ERROR:\n${e.message}"
        }

        setContentView(root)
    }
}

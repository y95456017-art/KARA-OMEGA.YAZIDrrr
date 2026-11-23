package com.kara.omega

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import org.json.JSONArray

class MainActivity : Activity() {

    private val green = Color.rgb(0, 230, 130)
    private val dark = Color.rgb(9, 12, 15)
    private val card = Color.rgb(20, 26, 28)
    private val white = Color.WHITE

    private var arabic = true
    private var loadedContent = ""
    private var channelCount = 0
    private var channelList = JSONArray()

    private lateinit var root: LinearLayout
    private lateinit var stats: TextView
    private lateinit var results: TextView
    private lateinit var status: TextView

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(8, 12, 8, 12)
        }
    }

    private fun makeButton(
        value: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = value
            textSize = 14f
            setTextColor(white)
            background = GradientDrawable().apply {
                setColor(Color.rgb(25, 65, 47))
                cornerRadius = 18f
                setStroke(1, green)
            }
            setOnClickListener { action() }
        }
    }

    private fun section(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            background = GradientDrawable().apply {
                setColor(card)
                cornerRadius = 22f
                setStroke(1, Color.rgb(35, 75, 55))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
            drawUI()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Python Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun drawUI() {
        val rtl = if (arabic) View.LAYOUT_DIRECTION_RTL
                  else View.LAYOUT_DIRECTION_LTR

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(dark)
            layoutDirection = rtl
        }

        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        body.addView(text("KARA OMEGA", 30f, green, true))
        body.addView(text(
            "M3U ANALYZER  •  VERSION 2.0",
            12f, white
        ))

        val langButton = makeButton(
            if (arabic) "English" else "العربية"
        ) {
            arabic = !arabic
            drawUI()
        }

        body.addView(langButton)

        val dashboard = section()
        dashboard.addView(text(
            if (arabic) "لوحة التحكم" else "DASHBOARD",
            21f, green, true
        ))

        dashboard.addView(text(
            "◉  RADAR SYSTEM  ◉",
            20f, green, true
        ))

        status = text(
            if (arabic) "النظام جاهز للعمل" else "SYSTEM READY",
            15f, white
        )
        dashboard.addView(status)

        stats = text("", 15f, white)
        dashboard.addView(stats)

        body.addView(dashboard)

        val actions = section()
        actions.addView(text(
            if (arabic) "إدارة قوائم M3U" else "M3U MANAGER",
            20f, green, true
        ))

        actions.addView(makeButton(
            if (arabic) "＋ استيراد ملف M3U" else "＋ IMPORT M3U"
        ) {
            openM3U()
        })

        actions.addView(makeButton(
            if (arabic) "مسح القائمة" else "CLEAR LIST"
        ) {
            loadedContent = ""
            channelList = JSONArray()
            channelCount = 0
            updateStats()
            results.text = ""
            status.text = if (arabic) "تم مسح القائمة"
                           else "LIST CLEARED"
        })

        body.addView(actions)

        val resultCard = section()
        resultCard.addView(text(
            if (arabic) "نتائج التحليل" else "ANALYSIS RESULTS",
            20f, green, true
        ))

        results = text(
            if (arabic) "لم يتم استيراد أي ملف بعد"
            else "NO FILE IMPORTED",
            13f, white
        )

        resultCard.addView(results)
        body.addView(resultCard)

        scroll.addView(body)
        root.addView(scroll)
        setContentView(root)

        updateStats()
    }

    private fun openM3U() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "audio/x-mpegurl",
                    "application/vnd.apple.mpegurl",
                    "text/plain"
                )
            )
        }

        startActivityForResult(intent, 100)
    }

    @Deprecated("Using activity result for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            try {
                val input = contentResolver.openInputStream(data.data!!)
                    ?: throw Exception("Cannot open file")

                loadedContent = input.bufferedReader().use {
                    it.readText()
                }

                val py = Python.getInstance()
                val module = py.getModule("main")

                val output = module.callAttr(
                    "parse_m3u",
                    loadedContent
                ).toJava(String::class.java)

                val parsed = JSONObject(output)
                channelCount = parsed.getInt("total")
                channelList = parsed.getJSONArray("channels")

                updateStats()
                showChannels()

                status.text = if (arabic)
                    "تم تحليل الملف بنجاح"
                else
                    "FILE ANALYZED SUCCESSFULLY"

            } catch (e: Exception) {
                status.text = "ERROR: ${e.message}"
                Toast.makeText(
                    this,
                    e.message ?: "Import failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateStats() {
        if (!::stats.isInitialized) return

        stats.text = if (arabic) {
            """
            إجمالي القنوات: $channelCount
            القنوات المحللة: $channelCount
            الاتصالات الناجحة: غير مفحوصة
            الاتصالات الفاشلة: غير مفحوصة
            """.trimIndent()
        } else {
            """
            TOTAL CHANNELS: $channelCount
            PARSED: $channelCount
            WORKING: NOT TESTED
            FAILED: NOT TESTED
            """.trimIndent()
        }
    }

    private fun showChannels() {
        val output = StringBuilder()

        val limit = minOf(channelList.length(), 40)

        for (i in 0 until limit) {
            val item = channelList.getJSONObject(i)

            val name = item.optString("name", "Unknown")
            val group = item.optString("group", "Uncategorized")

            output.append("${i + 1}. $name\n")
            output.append("   $group\n\n")
        }

        if (channelList.length() > limit) {
            output.append(
                if (arabic)
                    "تم عرض أول 40 قناة فقط"
                else
                    "Showing first 40 channels"
            )
        }

        results.text = output.toString()
    }
}

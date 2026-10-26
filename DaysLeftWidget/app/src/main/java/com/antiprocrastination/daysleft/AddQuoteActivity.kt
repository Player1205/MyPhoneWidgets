package com.antiprocrastination.daysleft

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddQuoteActivity : AppCompatActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_quote)

        // Capture Widget ID if opened via Samsung configure action
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        // Default result for widget configure
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
        }

        setupDialogControls()
        refreshQuotesList()
    }

    private fun setupDialogControls() {
        val etQuote = findViewById<EditText>(R.id.et_dialog_quote)
        val btnAdd = findViewById<Button>(R.id.btn_dialog_add)
        val btnClose = findViewById<TextView>(R.id.btn_dialog_close)
        val btnDone = findViewById<Button>(R.id.btn_dialog_done)

        btnAdd?.setOnClickListener {
            val text = etQuote?.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                QuoteManager.addQuote(this, text)
                etQuote?.text?.clear()
                refreshQuotesList()
                refreshWidgets()
                Toast.makeText(this, "Quote added to your widget!", Toast.LENGTH_SHORT).show()
            }
        }

        val dismissAction = {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)
            }
            finish()
        }

        btnClose?.setOnClickListener { dismissAction() }
        btnDone?.setOnClickListener { dismissAction() }
    }

    private fun refreshQuotesList() {
        val container = findViewById<LinearLayout>(R.id.container_dialog_quotes) ?: return
        val tvSubtitle = findViewById<TextView>(R.id.tv_dialog_subtitle)
        container.removeAllViews()

        val quotes = QuoteManager.getQuotes(this)
        val activeIndex = QuoteManager.getActiveIndex(this)

        if (quotes.size <= 1) {
            tvSubtitle?.text = "1 quote active. Add more to enable 3-hour auto-shuffle!"
        } else {
            tvSubtitle?.text = "${quotes.size} quotes saved. Auto-shuffling every 3 hours on your widget!"
        }

        for ((index, quote) in quotes.withIndex()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 12, 16, 12)
                val isCurrent = index == activeIndex
                setBackgroundColor(if (isCurrent) Color.parseColor("#1F2436") else Color.parseColor("#151824"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 6, 0, 0)
                }
                layoutParams = params
            }

            val tvQuote = TextView(this).apply {
                text = "“$quote”"
                setTextColor(Color.WHITE)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(tvQuote)

            if (index == activeIndex) {
                val tvBadge = TextView(this).apply {
                    text = "ACTIVE"
                    setTextColor(Color.parseColor("#FFB703"))
                    textSize = 10f
                    setPadding(10, 4, 10, 4)
                }
                row.addView(tvBadge)
            } else {
                val btnShow = TextView(this).apply {
                    text = "SHOW"
                    setTextColor(Color.parseColor("#8B919E"))
                    textSize = 11f
                    setPadding(10, 4, 10, 4)
                    setOnClickListener {
                        QuoteManager.setQuoteActive(this@AddQuoteActivity, index)
                        refreshQuotesList()
                        refreshWidgets()
                    }
                }
                row.addView(btnShow)
            }

            if (quotes.size > 1) {
                val btnDelete = TextView(this).apply {
                    text = "✕"
                    setTextColor(Color.parseColor("#FF5252"))
                    textSize = 13f
                    setPadding(14, 4, 6, 4)
                    setOnClickListener {
                        QuoteManager.deleteQuote(this@AddQuoteActivity, index)
                        refreshQuotesList()
                        refreshWidgets()
                    }
                }
                row.addView(btnDelete)
            }

            container.addView(row)
        }
    }

    private fun refreshWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, DaysLeftWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        val quote = QuoteManager.getCurrentQuote(this)
        for (id in appWidgetIds) {
            DaysLeftWidgetProvider.updateAppWidget(this, appWidgetManager, id, quote)
        }
    }
}
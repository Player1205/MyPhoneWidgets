package com.antiprocrastination.daysleft

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPreview()
        setupQuotesVault()
        setupPinWidgetButton()
    }

    private fun setupPreview() {
        val today = LocalDate.now()
        val now = LocalTime.now()
        val currentQuote = QuoteManager.getCurrentQuote(this)

        findViewById<TextView>(R.id.tv_year_label)?.text = ""
        findViewById<TextView>(R.id.btn_quick_add)?.text = ""
        findViewById<TextView>(R.id.tv_days_hero)?.text = ""
        findViewById<TextView>(R.id.tv_quote_label)?.text = ""
        findViewById<TextView>(R.id.tv_quote_text)?.text = ""

        // Render full pixel-perfect widget preview bitmap
        val widgetBitmap = YearDotsGenerator.generateWidgetBitmap(
            context = this,
            quote = currentQuote,
            today = today,
            now = now
        )
        findViewById<ImageView>(R.id.iv_year_dots)?.setImageBitmap(widgetBitmap)
    }

    private fun setupQuotesVault() {
        val etNewQuote = findViewById<EditText>(R.id.et_new_quote) ?: return
        val btnAddQuote = findViewById<Button>(R.id.btn_add_quote) ?: return

        btnAddQuote.setOnClickListener {
            val text = etNewQuote.text.toString().trim()
            if (text.isNotEmpty()) {
                QuoteManager.addQuote(this, text)
                etNewQuote.text.clear()
                setupPreview()
                refreshQuotesListUI()
                refreshWidgets()
                Toast.makeText(this, "Quote added to your vault!", Toast.LENGTH_SHORT).show()
            }
        }

        refreshQuotesListUI()
    }

    private fun refreshQuotesListUI() {
        val container = findViewById<LinearLayout>(R.id.container_quotes_list) ?: return
        val tvStatus = findViewById<TextView>(R.id.tv_vault_status)
        container.removeAllViews()

        val quotes = QuoteManager.getQuotes(this)
        val activeIndex = QuoteManager.getActiveIndex(this)

        if (quotes.size <= 1) {
            tvStatus?.text = "1 quote active. Add more to enable 3-hour auto-shuffle!"
        } else {
            tvStatus?.text = "${quotes.size} quotes saved. Auto-shuffling every 3 hours on your widget!"
        }

        for ((index, quote) in quotes.withIndex()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 12, 16, 12)
                val isCurrent = index == activeIndex
                setBackgroundColor(if (isCurrent) Color.parseColor("#1C2030") else Color.parseColor("#141722"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
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
                    setPadding(12, 4, 12, 4)
                }
                row.addView(tvBadge)
            } else {
                val btnShow = TextView(this).apply {
                    text = "SHOW"
                    setTextColor(Color.parseColor("#8B919E"))
                    textSize = 11f
                    setPadding(12, 4, 12, 4)
                    setOnClickListener {
                        QuoteManager.setQuoteActive(this@MainActivity, index)
                        setupPreview()
                        refreshQuotesListUI()
                        refreshWidgets()
                    }
                }
                row.addView(btnShow)
            }

            if (quotes.size > 1) {
                val btnDelete = TextView(this).apply {
                    text = "✕"
                    setTextColor(Color.parseColor("#FF5252"))
                    textSize = 14f
                    setPadding(16, 4, 8, 4)
                    setOnClickListener {
                        QuoteManager.deleteQuote(this@MainActivity, index)
                        setupPreview()
                        refreshQuotesListUI()
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

    private fun setupPinWidgetButton() {
        val btnPin = findViewById<Button>(R.id.btn_pin_widget) ?: return
        btnPin.setOnClickListener {
            val appWidgetManager = getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(this, DaysLeftWidgetProvider::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                appWidgetManager.isRequestPinAppWidgetSupported
            ) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                Toast.makeText(
                    this,
                    "Follow Samsung prompt to place widget on Home Screen!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Long-press your Home Screen and select 'Days Left Widget'",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

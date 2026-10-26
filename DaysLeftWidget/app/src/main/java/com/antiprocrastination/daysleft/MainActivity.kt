package com.antiprocrastination.daysleft

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPreview()
        setupCustomQuoteEditor()
        setupPinWidgetButton()
    }

    private fun setupPreview() {
        val today = LocalDate.now()
        val year = today.year
        val totalDays = if (today.isLeapYear) 366 else 365
        val dayOfYear = today.dayOfYear
        val daysLeft = totalDays - dayOfYear
        val percentGone = (dayOfYear.toDouble() / totalDays.toDouble()) * 100.0

        val quoteItem = QuoteManager.getCurrentQuote(this)

        findViewById<TextView>(R.id.tv_year_label)?.text = "in $year"
        findViewById<TextView>(R.id.tv_pill_days)?.text = "$daysLeft days left"
        findViewById<TextView>(R.id.tv_percent_badge)?.text =
            String.format(Locale.US, "%.1f%% gone", percentGone)
        findViewById<TextView>(R.id.tv_quote_label)?.text = quoteItem.label
        findViewById<TextView>(R.id.tv_quote_text)?.text = quoteItem.text
    }

    private fun setupCustomQuoteEditor() {
        val etQuote = findViewById<EditText>(R.id.et_custom_quote) ?: return
        val btnSave = findViewById<Button>(R.id.btn_save_quote) ?: return

        etQuote.setText(QuoteManager.getCustomQuote(this))

        btnSave.setOnClickListener {
            val text = etQuote.text.toString().trim()
            if (text.isNotEmpty()) {
                QuoteManager.setCustomQuote(this, text)
                setupPreview()

                // Trigger widget update
                val appWidgetManager = AppWidgetManager.getInstance(this)
                val componentName = ComponentName(this, DaysLeftWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                val quoteItem = QuoteManager.getCurrentQuote(this)
                for (id in appWidgetIds) {
                    DaysLeftWidgetProvider.updateAppWidget(this, appWidgetManager, id, quoteItem)
                }

                Toast.makeText(this, "Quote updated on widget!", Toast.LENGTH_SHORT).show()
            }
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
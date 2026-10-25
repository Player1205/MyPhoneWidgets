package com.antiprocrastination.daysleft

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
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
        setupPinWidgetButton()
    }

    private fun setupPreview() {
        val today = LocalDate.now()
        val year = today.year
        val totalDays = if (today.isLeapYear) 366 else 365
        val dayOfYear = today.dayOfYear
        val daysLeft = totalDays - dayOfYear
        val percentGone = (dayOfYear.toDouble() / totalDays.toDouble()) * 100.0

        findViewById<TextView>(R.id.tv_year_label)?.text = "⏳ YEAR $year"
        findViewById<TextView>(R.id.tv_days_number)?.text = daysLeft.toString()
        findViewById<TextView>(R.id.tv_percent_badge)?.text =
            String.format(Locale.US, "%.1f%% GONE", percentGone)
        findViewById<ProgressBar>(R.id.progress_year)?.apply {
            max = 100
            progress = percentGone.toInt()
        }
        findViewById<TextView>(R.id.tv_quote_text)?.text =
            QuoteManager.getCurrentQuote(this)
    }

    private fun setupPinWidgetButton() {
        val btnPin = findViewById<Button>(R.id.btn_pin_widget)
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

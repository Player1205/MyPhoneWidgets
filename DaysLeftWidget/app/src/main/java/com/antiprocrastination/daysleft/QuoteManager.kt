package com.antiprocrastination.daysleft

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException

object QuoteManager {

    private const val PREFS_NAME = "days_left_widget_prefs"
    private const val KEY_QUOTES_JSON = "user_quotes_json"
    private const val KEY_CURRENT_INDEX = "active_quote_index"
    private const val KEY_LAST_ROTATION_TIME = "last_quote_rotation_time"

    private const val THREE_HOURS_MS = 3L * 60 * 60 * 1000 // 3 hours in milliseconds

    /**
     * Get all quotes written by the user. Defaults to ["i am one of the few"].
     */
    fun getQuotes(context: Context): List<String> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_QUOTES_JSON, null) ?: return listOf("i am one of the few")
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                val q = jsonArray.optString(i)?.trim()
                if (!q.isNullOrEmpty()) {
                    list.add(q)
                }
            }
            if (list.isEmpty()) listOf("i am one of the few") else list
        } catch (e: JSONException) {
            listOf("i am one of the few")
        }
    }

    /**
     * Add a new quote written by the user.
     */
    fun addQuote(context: Context, quoteText: String) {
        val cleaned = quoteText.trim()
            .removeSurrounding("“", "”")
            .removeSurrounding("\"", "\"")
            .removeSurrounding("'", "'")
        if (cleaned.isEmpty()) return

        val currentList = getQuotes(context).toMutableList()
        currentList.add(cleaned)
        saveQuotes(context, currentList)
    }

    /**
     * Delete a quote at the specified index.
     */
    fun deleteQuote(context: Context, index: Int) {
        val currentList = getQuotes(context).toMutableList()
        if (currentList.size <= 1) return // Keep at least one quote
        if (index in 0 until currentList.size) {
            currentList.removeAt(index)
            saveQuotes(context, currentList)

            val prefs = getPrefs(context)
            var currentIdx = prefs.getInt(KEY_CURRENT_INDEX, 0)
            if (currentIdx >= currentList.size) {
                currentIdx = 0
                prefs.edit().putInt(KEY_CURRENT_INDEX, 0).apply()
            }
        }
    }

    /**
     * Get the currently active quote. Automatically rotates every 3 hours if 2 or more quotes exist.
     */
    fun getCurrentQuote(context: Context): String {
        val quotes = getQuotes(context)
        if (quotes.size <= 1) {
            return quotes.firstOrNull() ?: "i am one of the few"
        }

        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val lastRotation = prefs.getLong(KEY_LAST_ROTATION_TIME, 0L)
        var currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)

        // Rotate if 3 hours have elapsed
        if (now - lastRotation >= THREE_HOURS_MS) {
            currentIndex = (currentIndex + 1) % quotes.size
            prefs.edit()
                .putInt(KEY_CURRENT_INDEX, currentIndex)
                .putLong(KEY_LAST_ROTATION_TIME, now)
                .apply()
        }

        return quotes[currentIndex % quotes.size]
    }

    /**
     * Manually advances to the next quote (e.g. when widget or preview is tapped).
     */
    fun cycleToNextQuote(context: Context): String {
        val quotes = getQuotes(context)
        if (quotes.size <= 1) {
            return quotes.firstOrNull() ?: "i am one of the few"
        }

        val prefs = getPrefs(context)
        val currentIndex = (prefs.getInt(KEY_CURRENT_INDEX, 0) + 1) % quotes.size
        val now = System.currentTimeMillis()

        prefs.edit()
            .putInt(KEY_CURRENT_INDEX, currentIndex)
            .putLong(KEY_LAST_ROTATION_TIME, now)
            .apply()

        return quotes[currentIndex]
    }

    /**
     * Set a specific quote active immediately.
     */
    fun setQuoteActive(context: Context, index: Int): String {
        val quotes = getQuotes(context)
        if (index in 0 until quotes.size) {
            val now = System.currentTimeMillis()
            getPrefs(context).edit()
                .putInt(KEY_CURRENT_INDEX, index)
                .putLong(KEY_LAST_ROTATION_TIME, now)
                .apply()
            return quotes[index]
        }
        return getCurrentQuote(context)
    }

    fun getActiveIndex(context: Context): Int {
        val quotes = getQuotes(context)
        if (quotes.size <= 1) return 0
        return getPrefs(context).getInt(KEY_CURRENT_INDEX, 0) % quotes.size
    }

    private fun saveQuotes(context: Context, quotes: List<String>) {
        val jsonArray = JSONArray()
        for (q in quotes) {
            jsonArray.put(q)
        }
        getPrefs(context).edit()
            .putString(KEY_QUOTES_JSON, jsonArray.toString())
            .apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
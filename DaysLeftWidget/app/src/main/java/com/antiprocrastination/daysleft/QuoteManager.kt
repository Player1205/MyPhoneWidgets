package com.antiprocrastination.daysleft

import android.content.Context
import android.content.SharedPreferences

data class QuoteItem(val label: String, val text: String)

object QuoteManager {

    private const val PREFS_NAME = "days_left_widget_prefs"
    private const val KEY_QUOTE_INDEX = "current_quote_idx"
    private const val KEY_CUSTOM_QUOTE = "custom_user_quote"

    private val DEFAULT_QUOTES = listOf(
        QuoteItem("quote by me:", "“i am one of the few”"),
        QuoteItem("reality check:", "“procrastination is arrogance assuming you're guaranteed tomorrow.”"),
        QuoteItem("mindset:", "“stop planning. start executing today.”"),
        QuoteItem("discipline:", "“the clock doesn't negotiate. neither should you.”"),
        QuoteItem("identity:", "“one day or day one. you decide.”"),
        QuoteItem("reality check:", "“every hour scrolled is a piece of ambition discarded.”"),
        QuoteItem("mindset:", "“a year from now, you will wish you had started right now.”"),
        QuoteItem("discipline:", "“regret weighs tons. discipline weighs ounces.”"),
        QuoteItem("identity:", "“your future self is either thanking you or blaming you.”"),
        QuoteItem("discipline:", "“action cures fear and destroys procrastination.”")
    )

    fun getCurrentQuote(context: Context): QuoteItem {
        val prefs = getPrefs(context)
        val customQuote = prefs.getString(KEY_CUSTOM_QUOTE, null)?.trim()
        val idx = prefs.getInt(KEY_QUOTE_INDEX, 0) % DEFAULT_QUOTES.size

        return if (idx == 0 && !customQuote.isNullOrEmpty()) {
            QuoteItem("quote by me:", "“$customQuote”")
        } else {
            DEFAULT_QUOTES[idx]
        }
    }

    fun cycleToNextQuote(context: Context): QuoteItem {
        val prefs = getPrefs(context)
        val currentIdx = prefs.getInt(KEY_QUOTE_INDEX, 0)
        val nextIdx = (currentIdx + 1) % DEFAULT_QUOTES.size
        prefs.edit().putInt(KEY_QUOTE_INDEX, nextIdx).apply()
        return getCurrentQuote(context)
    }

    fun setCustomQuote(context: Context, quoteText: String) {
        val cleaned = quoteText.trim().removeSurrounding("“", "”").removeSurrounding("\"", "\"")
        getPrefs(context).edit()
            .putString(KEY_CUSTOM_QUOTE, cleaned)
            .putInt(KEY_QUOTE_INDEX, 0)
            .apply()
    }

    fun getCustomQuote(context: Context): String {
        return getPrefs(context).getString(KEY_CUSTOM_QUOTE, "i am one of the few") ?: "i am one of the few"
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
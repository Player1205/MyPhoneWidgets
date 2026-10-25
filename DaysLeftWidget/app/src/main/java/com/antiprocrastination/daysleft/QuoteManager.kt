package com.antiprocrastination.daysleft

import android.content.Context
import android.content.SharedPreferences

object QuoteManager {

    private const val PREFS_NAME = "days_left_widget_prefs"
    private const val KEY_QUOTE_INDEX = "current_quote_idx"

    private val REALITY_CHECKS = listOf(
        "\"Stop planning your life. Start executing today.\"",
        "\"Procrastination is arrogance assuming you're guaranteed tomorrow.\"",
        "\"Every hour scrolled is a piece of your ambition discarded.\"",
        "\"A year from now, you will wish you had started right now.\"",
        "\"You don't lack time. You lack ruthless discipline.\"",
        "\"One day or Day One. You decide.\"",
        "\"Comfort is the quiet grave of your potential.\"",
        "\"The clock doesn't negotiate. Neither should your work ethic.\"",
        "\"Regret weighs tons. Discipline weighs ounces.\"",
        "\"You are trading hours of your life. Make sure the trade is worth it.\"",
        "\"Your future self is either thanking you or blaming you.\"",
        "\"Action cures fear and destroys procrastination.\""
    )

    fun getCurrentQuote(context: Context): String {
        val prefs = getPrefs(context)
        val idx = prefs.getInt(KEY_QUOTE_INDEX, 0) % REALITY_CHECKS.size
        return REALITY_CHECKS[idx]
    }

    fun cycleToNextQuote(context: Context): String {
        val prefs = getPrefs(context)
        val currentIdx = prefs.getInt(KEY_QUOTE_INDEX, 0)
        val nextIdx = (currentIdx + 1) % REALITY_CHECKS.size
        prefs.edit().putInt(KEY_QUOTE_INDEX, nextIdx).apply()
        return REALITY_CHECKS[nextIdx]
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}

package com.example.nfcdailycheckin.ui.theme

import android.content.Context

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

object ThemePref {
    private const val PREFS = "settings_prefs"
    private const val KEY_THEME = "theme_mode"

    fun load(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun save(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }
}
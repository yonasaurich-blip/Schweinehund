package com.example.nfcdailycheckin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* ---------------------------------------------------
   Farbkonzept:
   - Lila/Lavender = App-Identität
   - Grün = erledigt ✅
   - Rot = nicht erledigt ❌
   - Dark Mode gedämpft
--------------------------------------------------- */

/* ---------- Light Theme ---------- */

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B5BD2),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFE6E0FF),
    onPrimaryContainer = Color(0xFF1B1457),

    secondary = Color(0xFF4CAF50), // erledigt
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF0D3B12),

    tertiary = Color(0xFFD32F2F), // nicht erledigt
    onTertiary = Color.White,

    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFF410002),

    surface = Color(0xFFF9F8FF),
    surfaceVariant = Color(0xFFEDE9FF),
    onSurface = Color(0xFF171717),
)

/* ---------- Dark Theme ---------- */

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFC6FF),
    onPrimary = Color(0xFF1B1457),

    primaryContainer = Color(0xFF3C2F8A),
    onPrimaryContainer = Color(0xFFE6E0FF),

    secondary = Color(0xFF66BB6A), // erledigt
    onSecondary = Color(0xFF0D3B12),

    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),

    tertiary = Color(0xFFEF5350), // nicht erledigt
    onTertiary = Color(0xFF410002),

    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),

    surface = Color(0xFF12121A),
    surfaceVariant = Color(0xFF232238),
    onSurface = Color(0xFFF2F2F2),
)

/* ---------- Custom App Colors ---------- */

// Karten-Hintergrund (Stats, erledigte Tasks etc.)
private val LightLavenderCard = Color(0xFFEAE6FF)
private val DarkLavenderCard = Color(0xFF2A2744)

// Statusfarben für Übersicht (❌ / ✅)
private val LightDoneGreen = Color(0xFF2E7D32)
private val DarkDoneGreen = Color(0xFF66BB6A)

private val LightFailRed = Color(0xFFC62828)
private val DarkFailRed = Color(0xFFEF5350)

/* ---------- Helpers ---------- */

@Composable
fun lavenderCardColor(): Color =
    if (isSystemInDarkTheme()) DarkLavenderCard else LightLavenderCard

@Composable
fun doneColor(): Color =
    if (isSystemInDarkTheme()) DarkDoneGreen else LightDoneGreen

@Composable
fun failColor(): Color =
    if (isSystemInDarkTheme()) DarkFailRed else LightFailRed

/* ---------- Theme Wrapper ---------- */

@Composable
fun DailyCheckinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}

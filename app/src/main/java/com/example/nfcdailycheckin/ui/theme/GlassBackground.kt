package com.example.nfcdailycheckin.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Verlauf-Hintergrund, auf dem die Glas-Karten wirken.
 * Dunkelmodus: tiefes Violett → fast Schwarz, mit leichtem Lila-Schimmer.
 * Hellmodus: zartes Lavendel.
 */
@Composable
fun GlassBackground(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val brush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1A1530),
                Color(0xFF0E0B1A),
                Color(0xFF160F2B)
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 1600f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF3F0FF),
                Color(0xFFE9E4FF),
                Color(0xFFF6F2FF)
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 1600f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}
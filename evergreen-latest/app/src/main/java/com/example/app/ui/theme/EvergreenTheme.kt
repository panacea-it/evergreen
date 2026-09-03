package com.example.app.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Composable
fun EvergreenTheme(content: @Composable () -> Unit) {
    MaterialTheme(typography = AppTypography) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Default)
        ) {
            content()
        }
    }
}

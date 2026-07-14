package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimary,
    secondary = BentoSecondary,
    tertiary = BentoTertiary,
    background = BentoBgDark,
    surface = BentoSurfaceDark,
    onPrimary = BentoBgDark,
    onSecondary = BentoBgDark,
    onBackground = BentoBgLight,
    onSurface = BentoBgLight,
    outlineVariant = BentoBorderDark,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    secondary = BentoSecondary,
    tertiary = BentoTertiary,
    background = BentoBgLight,
    surface = BentoSurfaceLight,
    onPrimary = BentoSurfaceLight,
    onSecondary = BentoSurfaceLight,
    onBackground = BentoBgDark,
    onSurface = BentoBgDark,
    outlineVariant = BentoBorderLight,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve our beautiful premium theme branding colors!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

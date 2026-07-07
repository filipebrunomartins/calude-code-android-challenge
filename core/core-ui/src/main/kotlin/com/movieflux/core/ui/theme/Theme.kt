package com.movieflux.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = TealGreenPrimary,
        onPrimary = TealGreenOnPrimary,
        primaryContainer = TealGreenContainer,
    )

private val DarkColors =
    darkColorScheme(
        primary = TealGreenPrimaryLightScheme,
        onPrimary = TealGreenOnPrimary,
        primaryContainer = TealGreenContainerDark,
    )

@Composable
fun MovieFluxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MovieFluxTypography,
        content = content,
    )
}

package com.pucetec.mrcanchas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SportBluePrimary,
    secondary = SportOrangeSecondary,
    tertiary = SportYellowTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SportBluePrimary,
    secondary = SportOrangeSecondary,
    tertiary = SportYellowTertiary,
    background = TechnicalLightBackground,
    surface = PureWhiteSurface,
    onBackground = DarkBlueOnSurface,
    onSurface = DarkBlueOnSurface
)

@Composable
fun MrCanchasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

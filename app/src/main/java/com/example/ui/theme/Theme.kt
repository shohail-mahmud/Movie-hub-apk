package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MovieHubColorScheme = darkColorScheme(
    primary = AmberGold,
    secondary = AmberDark,
    tertiary = AmberGold,
    background = NearBlack,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = NearBlack,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MovieHubColorScheme,
        typography = Typography,
        content = content
    )
}

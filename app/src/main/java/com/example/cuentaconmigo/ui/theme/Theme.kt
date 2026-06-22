package com.example.cuentaconmigo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green90,
    onPrimaryContainer = Green10,
    secondary = GreenGray40,
    onSecondary = Color.White,
    secondaryContainer = GreenGray90,
    onSecondaryContainer = GreenGray10,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Gray99,
    onBackground = Gray10,
    surface = Gray99,
    onSurface = Gray10,
    surfaceVariant = GrayVariant90,
    onSurfaceVariant = GrayVariant30,
    outline = GrayVariant50,
    outlineVariant = GrayVariant80,
    inverseSurface = Gray20,
    inverseOnSurface = Gray95,
    inversePrimary = Green80,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green20,
    primaryContainer = Green30,
    onPrimaryContainer = Green90,
    secondary = GreenGray80,
    onSecondary = GreenGray20,
    secondaryContainer = GreenGray30,
    onSecondaryContainer = GreenGray90,
    tertiary = Teal80,
    onTertiary = Teal20,
    tertiaryContainer = Teal30,
    onTertiaryContainer = Teal90,
    error = Red80,
    onError = Red20,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Red90,
    background = Gray10,
    onBackground = Gray90,
    surface = Gray10,
    onSurface = Gray90,
    surfaceVariant = GrayVariant30,
    onSurfaceVariant = GrayVariant80,
    outline = GrayVariant60,
    outlineVariant = GrayVariant30,
    inverseSurface = Gray90,
    inverseOnSurface = Gray20,
    inversePrimary = Green40,
)

@Composable
fun CuentaConMigoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val brand = if (darkTheme) DarkBrand else LightBrand

    CompositionLocalProvider(LocalBrand provides brand) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
